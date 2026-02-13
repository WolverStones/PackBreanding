package cz.wolverstone.agonia.packbranding.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

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

            if (menuConfig.isEnableCustomIcon()) {
                Path iconPath = MenuConfig.getIconPath();
                if (Files.exists(iconPath)) {
                    IconChanger.setIcon(iconPath);
                } else {
                    LOGGER.warn("Custom icon enabled but file not found: {}", iconPath);
                }
            }
        });

        LOGGER.info("PackBranding initialized!");
    }
}
