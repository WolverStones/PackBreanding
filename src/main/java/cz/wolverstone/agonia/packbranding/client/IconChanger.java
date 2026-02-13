package cz.wolverstone.agonia.packbranding.client;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class IconChanger {
    private static final Logger LOGGER = LoggerFactory.getLogger("PackBranding");

    public static void setIcon(Path iconPath) {
        if (!Files.exists(iconPath)) {
            LOGGER.warn("Icon file not found: {}", iconPath);
            return;
        }

        try {
            long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
            setWindowIcon(windowHandle, iconPath);
            LOGGER.info("Window icon set from: {}", iconPath);
        } catch (Exception e) {
            LOGGER.error("Failed to set window icon", e);
        }
    }

    private static void setWindowIcon(long windowHandle, Path iconPath) throws IOException {
        byte[] iconBytes = Files.readAllBytes(iconPath);
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(iconBytes.length);
        imageBuffer.put(iconBytes);
        imageBuffer.flip();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer icon = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
            if (icon == null) {
                LOGGER.error("Failed to load icon image: {}", STBImage.stbi_failure_reason());
                return;
            }

            try (GLFWImage.Buffer icons = GLFWImage.malloc(1)) {
                icons.get(0).set(width.get(0), height.get(0), icon);
                GLFW.glfwSetWindowIcon(windowHandle, icons);
            } finally {
                STBImage.stbi_image_free(icon);
            }
        }
    }
}
