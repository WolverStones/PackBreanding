package cz.wolverstone.agonia.packbranding.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("PackBranding");

    @Override
    public void onInitializeClient() {
        LOGGER.info("PackBranding initializing...");

        WindowTitleConfig titleConfig = WindowTitleConfig.load();
        MenuConfig menuConfig = MenuConfig.load();

        MinecraftClient.getInstance().execute(() -> {
            if (menuConfig.isEnableCustomTitle()) {
                WindowTitleChanger.applyTitle(titleConfig);
            }
        });

        LOGGER.info("PackBranding initialized!");
    }
}
