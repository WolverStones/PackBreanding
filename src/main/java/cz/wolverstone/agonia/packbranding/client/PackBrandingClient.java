package cz.wolverstone.agonia.packbranding.client;

import cz.wolverstone.agonia.packbranding.PackBranding;
import cz.wolverstone.agonia.packbranding.client.config.MenuConfig;
import cz.wolverstone.agonia.packbranding.client.screen.ScreenBrandingHandler;
import cz.wolverstone.agonia.packbranding.client.screen.button.MenuButtonTextures;
import cz.wolverstone.agonia.packbranding.client.window.WindowIconManager;
import cz.wolverstone.agonia.packbranding.client.window.WindowTitleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public class PackBrandingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PackBranding.LOGGER.info("PackBranding initializing...");

        MenuConfig menuConfig = MenuConfig.load();

        Minecraft.getInstance().execute(() -> {
            if (menuConfig.isEnableCustomTitle()) {
                WindowTitleManager.applyTitle(menuConfig);
            }
            if (menuConfig.isEnableCustomIcon()) {
                WindowIconManager.applyConfiguredIcon();
            }
            // Register button icon textures on the render thread before any menu
            // is shown (both the title and pause button lists).
            MenuButtonTextures.load(menuConfig.getTitleButtons());
            MenuButtonTextures.load(menuConfig.getPauseButtons());
        });

        if (menuConfig.isEnableCustomIcon()) {
            registerIconReloadListener();
        }

        ScreenBrandingHandler.register();

        PackBranding.LOGGER.info("PackBranding initialized!");
    }

    private static void registerIconReloadListener() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                Identifier.fromNamespaceAndPath(PackBranding.MOD_ID, "icon_reload"),
                new SimpleReloadListener<Void>() {
                    @Override
                    protected Void prepare(PreparableReloadListener.SharedState state) {
                        return null;
                    }

                    @Override
                    protected void apply(Void result, PreparableReloadListener.SharedState state) {
                        WindowIconManager.resetAndApply();
                    }
                }
        );
    }
}
