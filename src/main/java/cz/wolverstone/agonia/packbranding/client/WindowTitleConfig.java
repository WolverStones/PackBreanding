package cz.wolverstone.agonia.packbranding.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WindowTitleConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("PackBranding");
    private static final String CONFIG_FOLDER = "packbranding";
    private static final String CONFIG_FILE = "windowtitle.txt";
    private static final String DEFAULT_TITLE = "Minecraft {mcversion}";

    private static WindowTitleConfig instance;
    private String title;

    private WindowTitleConfig(String title) {
        this.title = title;
    }

    public static WindowTitleConfig load() {
        if (instance != null) {
            return instance;
        }

        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FOLDER);
        Path configFile = configDir.resolve(CONFIG_FILE);

        String title = DEFAULT_TITLE;

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                LOGGER.info("Created config directory: {}", configDir);
            }

            if (!Files.exists(configFile)) {
                Files.writeString(configFile, DEFAULT_TITLE);
                LOGGER.info("Created default window title config: {}", configFile);
            } else {
                title = Files.readString(configFile).trim();
                if (title.isEmpty()) {
                    title = DEFAULT_TITLE;
                }
                LOGGER.info("Loaded window title: {}", title);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load window title config", e);
        }

        instance = new WindowTitleConfig(title);
        return instance;
    }

    public String getTitle() {
        return title;
    }
}
