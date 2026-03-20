package cz.wolverstone.agonia.packbranding.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
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
            if (menuConfig.isEnableCustomIcon()) {
                IconChanger.applyConfiguredIcon();
            }
        });

        if (menuConfig.isEnableCustomIcon()) {
            ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of("packbranding", "icon_reload");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        IconChanger.resetAndApply();
                    }
                }
            );
        }

        LOGGER.info("PackBranding initialized!");
    }
}
