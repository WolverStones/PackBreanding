package cz.wolverstone.agonia.packbranding.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IconChanger {
    private static final Logger LOGGER = LoggerFactory.getLogger("PackBranding");
    private static final int ICON_16 = 16;
    private static final int ICON_32 = 32;
    private static boolean applied = false;

    public static void applyConfiguredIcon() {
        if (applied) return;
        applied = true;

        Path iconDir = MenuConfig.getIconDir();
        try {
            Files.createDirectories(iconDir);
        } catch (IOException e) {
            LOGGER.warn("Failed to create icon directory: {}", iconDir, e);
        }

        Path icon16 = resolveExisting(iconDir.resolve("icon_16x16.png"), MenuConfig.getIcon16Path());
        Path icon32 = resolveExisting(iconDir.resolve("icon_32x32.png"), MenuConfig.getIcon32Path());
        Path iconSingle = resolveExisting(iconDir.resolve("icon.png"), MenuConfig.getIconSinglePath());

        if (Files.exists(icon16) && Files.exists(icon32)) {
            setIcon(List.of(new IconSource(icon16, ICON_16), new IconSource(icon32, ICON_32)));
            return;
        }

        if (Files.exists(iconSingle)) {
            setIcon(List.of(new IconSource(iconSingle, null)));
            return;
        }

        if (Files.exists(icon16) || Files.exists(icon32)) {
            LOGGER.warn("Both icon files are required when using split icons: {} and {}", icon16, icon32);
        } else {
            LOGGER.warn("Custom icon enabled but no icon files found in {}", MenuConfig.getConfigDir());
        }
    }

    private static Path resolveExisting(Path preferred, Path fallback) {
        return Files.exists(preferred) ? preferred : fallback;
    }

    private static void setIcon(List<IconSource> sources) {
        try {
            setWindowIcon(sources);
            if (sources.size() == 1) {
                LOGGER.info("Window icon set from: {}", sources.get(0).path());
            } else {
                LOGGER.info("Window icon set from: {} and {}", sources.get(0).path(), sources.get(1).path());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to set window icon", e);
        }
    }

    private static void setWindowIcon(List<IconSource> sources) throws IOException {
        List<ByteBuffer> loadedImages = new ArrayList<>(sources.size());

        try (MemoryStack stack = MemoryStack.stackPush()) {
            GLFWImage.Buffer glfwImages = GLFWImage.malloc(sources.size(), stack);

            for (int i = 0; i < sources.size(); i++) {
                IconSource source = sources.get(i);
                try (InputStream stream = Files.newInputStream(source.path());
                     NativeImage nativeImage = NativeImage.read(stream)) {
                    if (source.expectedSize() != null) {
                        int expected = source.expectedSize();
                        if (nativeImage.getWidth() != expected || nativeImage.getHeight() != expected) {
                            LOGGER.warn("Custom window icon enabled, but {} is not {}x{}.", source.path(), expected, expected);
                            return;
                        }
                    }

                    ByteBuffer buffer = MemoryUtil.memAlloc(nativeImage.getWidth() * nativeImage.getHeight() * 4);
                    loadedImages.add(buffer);
                    buffer.asIntBuffer().put(nativeImage.copyPixelsAbgr());
                    glfwImages.position(i);
                    glfwImages.width(nativeImage.getWidth());
                    glfwImages.height(nativeImage.getHeight());
                    glfwImages.pixels(buffer);
                }
            }

            GLFW.glfwSetWindowIcon(MinecraftClient.getInstance().getWindow().getHandle(), glfwImages);
        } finally {
            loadedImages.forEach(MemoryUtil::memFree);
        }
    }

    private record IconSource(Path path, Integer expectedSize) {}
}
