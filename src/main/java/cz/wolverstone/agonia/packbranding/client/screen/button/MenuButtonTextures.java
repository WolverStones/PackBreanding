package cz.wolverstone.agonia.packbranding.client.screen.button;

import cz.wolverstone.agonia.packbranding.PackBranding;
import cz.wolverstone.agonia.packbranding.client.config.MenuConfig;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loads menu-button icon PNGs from {@code config/packbranding/buttons/} into
 * dynamic textures registered with the texture manager, so they can be drawn by
 * {@link IconLinkButton}. Mirrors {@link cz.wolverstone.agonia.packbranding.client.window.WindowIconManager}'s
 * NativeImage handling.
 */
public final class MenuButtonTextures {
    /** file name (lower-case) -> registered texture id */
    private static final Map<String, Identifier> REGISTERED = new HashMap<>();

    private MenuButtonTextures() {
    }

    /**
     * Loads the icons for the given button configs, registering a texture for
     * each. Safe to call again on resource reload; previously registered ids are
     * reused.
     */
    public static void load(Iterable<MenuButtonConfig> buttons) {
        Path dir = MenuConfig.getButtonsDir();
        for (MenuButtonConfig button : buttons) {
            String iconFile = button.iconFile();
            // sprite: icons are atlas sprites, not PNG files - nothing to load.
            if (iconFile.startsWith("sprite:")) {
                continue;
            }
            register(dir, iconFile);
        }
    }

    /**
     * Returns the registered texture id for an icon file, or {@code null} if it
     * could not be loaded.
     */
    public static Identifier get(String iconFile) {
        return REGISTERED.get(key(iconFile));
    }

    private static void register(Path dir, String iconFile) {
        String key = key(iconFile);
        if (REGISTERED.containsKey(key)) {
            return;
        }

        Path file = dir.resolve(iconFile);
        if (!Files.exists(file)) {
            PackBranding.LOGGER.warn("Menu button icon not found: {}", file);
            return;
        }

        try (InputStream stream = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture texture = new DynamicTexture(() -> "packbranding-button-" + key, image);
            Identifier id = Identifier.fromNamespaceAndPath(PackBranding.MOD_ID, "button/" + sanitize(key));
            Minecraft.getInstance().getTextureManager().register(id, texture);
            REGISTERED.put(key, id);
            PackBranding.LOGGER.info("Loaded menu button icon: {}", file);
        } catch (IOException e) {
            PackBranding.LOGGER.error("Failed to load menu button icon: {}", file, e);
        }
    }

    private static String key(String iconFile) {
        return iconFile.toLowerCase(Locale.ROOT);
    }

    /** Identifier paths allow only [a-z0-9/._-]; replace anything else. */
    private static String sanitize(String name) {
        return name.replaceAll("[^a-z0-9/._-]", "_");
    }
}
