package cz.wolverstone.agonia.packbranding.client.window;

import cz.wolverstone.agonia.packbranding.PackBranding;
import cz.wolverstone.agonia.packbranding.client.config.MenuConfig;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Surface;

import static org.lwjgl.sdl.SDLPixels.SDL_PIXELFORMAT_ABGR8888;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies a custom window icon from the config directory.
 */
public final class WindowIconManager {
    private static final int ICON_16 = 16;
    private static final int ICON_32 = 32;
    private static boolean applied = false;

    private WindowIconManager() {
    }

    public static void resetAndApply() {
        applied = false;
        applyConfiguredIcon();
    }

    public static void applyConfiguredIcon() {
        if (applied) return;
        applied = true;

        Path iconDir = MenuConfig.getIconDir();
        try {
            Files.createDirectories(iconDir);
        } catch (IOException e) {
            PackBranding.LOGGER.warn("Failed to create icon directory: {}", iconDir, e);
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
            PackBranding.LOGGER.warn("Both icon files are required when using split icons: {} and {}", icon16, icon32);
        } else {
            PackBranding.LOGGER.warn("Custom icon enabled but no icon files found in {}", MenuConfig.getConfigDir());
        }
    }

    private static Path resolveExisting(Path preferred, Path fallback) {
        return Files.exists(preferred) ? preferred : fallback;
    }

    private static void setIcon(List<IconSource> sources) {
        try {
            setWindowIcon(sources);
            if (sources.size() == 1) {
                PackBranding.LOGGER.info("Window icon set from: {}", sources.get(0).path());
            } else {
                PackBranding.LOGGER.info("Window icon set from: {} and {}", sources.get(0).path(), sources.get(1).path());
            }
        } catch (Exception e) {
            PackBranding.LOGGER.error("Failed to set window icon", e);
        }
    }

    private static void setWindowIcon(List<IconSource> sources) throws IOException {
        List<NativeImage> loadedImages = new ArrayList<>(sources.size());
        List<SDL_Surface> surfaces = new ArrayList<>(sources.size());

        try {
            for (IconSource source : sources) {
                NativeImage nativeImage = readImage(source);
                if (nativeImage == null) {
                    return;
                }
                loadedImages.add(nativeImage);

                SDL_Surface surface = SDLSurface.SDL_CreateSurfaceFrom(
                        nativeImage.getWidth(), nativeImage.getHeight(), SDL_PIXELFORMAT_ABGR8888,
                        nativeImage.getPixelBytes(), nativeImage.getWidth() * 4);
                if (surface == null) {
                    PackBranding.LOGGER.warn("Failed to create SDL surface for icon: {}", source.path());
                    return;
                }
                surfaces.add(surface);
            }

            SDL_Surface primary = surfaces.get(0);
            for (int i = 1; i < surfaces.size(); i++) {
                SDLSurface.SDL_AddSurfaceAlternateImage(primary, surfaces.get(i));
            }

            if (!SDLVideo.SDL_SetWindowIcon(Minecraft.getInstance().getWindow().handle(), primary)) {
                PackBranding.LOGGER.warn("Failed to set window icon");
            }
        } finally {
            surfaces.forEach(SDLSurface::SDL_DestroySurface);
            loadedImages.forEach(NativeImage::close);
        }
    }

    private static NativeImage readImage(IconSource source) throws IOException {
        try (InputStream stream = Files.newInputStream(source.path())) {
            NativeImage nativeImage = NativeImage.read(stream);
            if (source.expectedSize() != null) {
                int expected = source.expectedSize();
                if (nativeImage.getWidth() != expected || nativeImage.getHeight() != expected) {
                    PackBranding.LOGGER.warn("Custom window icon enabled, but {} is not {}x{}.", source.path(), expected, expected);
                    nativeImage.close();
                    return null;
                }
            }
            return nativeImage;
        }
    }

    private record IconSource(Path path, Integer expectedSize) {}
}
