package cz.wolverstone.agonia.packbranding.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class MenuConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("PackBranding");
    private static final String CONFIG_FOLDER = "packbranding";
    private static final String CONFIG_FILE = "menu.properties";

    private static MenuConfig instance;

    // Config values
    private String packVersion = "1.0";
    private boolean enableCustomTitle = true;
    private boolean enableMainMenuText = true;
    private boolean enablePauseMenuText = true;
    private String customTextMainMenu = "";
    private String customTextPauseMenu = "";
    private boolean hideRealmsButton = true;
    private boolean enableCustomIcon = false;

    private MenuConfig() {}

    public static MenuConfig load() {
        if (instance != null) {
            return instance;
        }

        instance = new MenuConfig();
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FOLDER);
        Path configFile = configDir.resolve(CONFIG_FILE);

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            if (!Files.exists(configFile)) {
                String defaultConfig = """
                        # ========================================
                        # PackBranding Configuration
                        # ========================================

                        # --- Pack Info ---
                        # Pack version string, used with {packversion} token
                        packVersion=1.0

                        # --- Custom Window Title ---
                        # Enable custom window title (true/false)
                        # When disabled, the default Minecraft title is used
                        enableCustomTitle=true
                        # Title is configured in: config/packbranding/windowtitle.txt
                        # Supported tokens: {mcversion}, {packversion}, {username}, {modcount}, {modversion:modid}

                        # --- Main Menu Text ---
                        # Enable custom text on the main menu screen (true/false)
                        enableMainMenuText=true
                        # Text displayed in the bottom-right corner of the main menu
                        # Supports color codes: #RRGGBB (hex), &0-&f (Minecraft colors)
                        # Supports formatting: &l (bold), &o (italic), &n (underline), &m (strikethrough), &r (reset)
                        # Supports tokens: {mcversion}, {packversion}, {username}, {modcount}
                        # Example: #FF5555MyPack #FFFFFF{packversion} #AAAAAA| MC {mcversion}
                        customTextMainMenu=

                        # --- Pause Menu Text ---
                        # Enable custom text on the pause menu screen (true/false)
                        enablePauseMenuText=true
                        # Text displayed in the bottom-right corner of the pause menu
                        # Same color codes, formatting, and tokens as main menu text
                        customTextPauseMenu=

                        # --- Hide Realms Button ---
                        # Hide the Realms button and notifications on the main menu (true/false)
                        hideRealmsButton=true

                        # --- Custom Window Icon ---
                        # Enable custom window icon (true/false)
                        # Place your icon at: config/packbranding/icon.png
                        enableCustomIcon=false
                        """;
                Files.writeString(configFile, defaultConfig);
                LOGGER.info("Created default menu config: {}", configFile);
            } else {
                Properties props = new Properties();
                props.load(Files.newBufferedReader(configFile));

                instance.packVersion = props.getProperty("packVersion", "1.0");
                instance.enableCustomTitle = Boolean.parseBoolean(props.getProperty("enableCustomTitle", "true"));
                instance.enableMainMenuText = Boolean.parseBoolean(props.getProperty("enableMainMenuText", "true"));
                instance.enablePauseMenuText = Boolean.parseBoolean(props.getProperty("enablePauseMenuText", "true"));
                instance.customTextMainMenu = props.getProperty("customTextMainMenu", "");
                instance.customTextPauseMenu = props.getProperty("customTextPauseMenu", "");
                instance.hideRealmsButton = Boolean.parseBoolean(props.getProperty("hideRealmsButton", "true"));
                instance.enableCustomIcon = Boolean.parseBoolean(props.getProperty("enableCustomIcon", "false"));

                LOGGER.info("Loaded menu config");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load menu config", e);
        }

        return instance;
    }

    public static MenuConfig getInstance() {
        return instance != null ? instance : load();
    }

    public String getPackVersion() {
        return packVersion;
    }

    public boolean isEnableCustomTitle() {
        return enableCustomTitle;
    }

    public boolean isEnableMainMenuText() {
        return enableMainMenuText;
    }

    public boolean isEnablePauseMenuText() {
        return enablePauseMenuText;
    }

    public String getCustomTextMainMenu() {
        return customTextMainMenu;
    }

    public String getCustomTextPauseMenu() {
        return customTextPauseMenu;
    }

    public boolean isHideRealmsButton() {
        return hideRealmsButton;
    }

    public boolean isEnableCustomIcon() {
        return enableCustomIcon;
    }

    public static Path getIconPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FOLDER).resolve("icon.png");
    }

    private static int parseColor(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }
}
