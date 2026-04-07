package cz.wolverstone.agonia.packbranding.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("PackBranding");

    @Override
    public void onInitializeClient() {
        LOGGER.info("PackBranding initializing...");

        WindowTitleConfig titleConfig = WindowTitleConfig.load();
        MenuConfig menuConfig = MenuConfig.load();

        Minecraft.getInstance().execute(() -> {
            if (menuConfig.isEnableCustomTitle()) {
                WindowTitleChanger.applyTitle(titleConfig);
            }
            if (menuConfig.isEnableCustomIcon()) {
                IconChanger.applyConfiguredIcon();
            }
        });

        if (menuConfig.isEnableCustomIcon()) {
            ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                Identifier.fromNamespaceAndPath("packbranding", "icon_reload"),
                new SimpleReloadListener<Void>() {
                    @Override
                    protected Void prepare(PreparableReloadListener.SharedState state) {
                        return null;
                    }

                    @Override
                    protected void apply(Void result, PreparableReloadListener.SharedState state) {
                        IconChanger.resetAndApply();
                    }
                }
            );
        }

        LOGGER.info("PackBranding initialized!");
    }
}
