package cz.wolverstone.agonia.packbranding.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import cz.wolverstone.agonia.packbranding.PackBranding;
import cz.wolverstone.agonia.packbranding.client.screen.Corner;
import cz.wolverstone.agonia.packbranding.client.screen.button.MenuButtonConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The mod configuration, loaded from {@code config/packbranding/config.json}.
 *
 * <p>This is a thin facade over {@link ConfigModel} (the GSON-mapped JSON), so
 * the rest of the mod keeps using simple typed getters.
 */
public class MenuConfig {
    private static final String CONFIG_FILE = "config.json";
    // disableHtmlEscaping keeps '&', '§', '<', etc. readable instead of writing
    // them as & / § unicode escapes.
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final String EXAMPLE_BUTTON_RESOURCE = "/assets/packbranding/example_button.png";
    private static final String EXAMPLE_BUTTON_FILE = "example.png";

    private static MenuConfig instance;

    private final ConfigModel model;
    private final Map<Corner, String> mainMenuText = new EnumMap<>(Corner.class);
    private final Map<Corner, String> pauseMenuText = new EnumMap<>(Corner.class);
    private final List<MenuButtonConfig> titleButtons;
    private final List<MenuButtonConfig> pauseButtons;

    private MenuConfig(ConfigModel model) {
        this.model = model;
        fillCorners(mainMenuText, model.mainMenu);
        fillCorners(pauseMenuText, model.pauseMenu);
        this.titleButtons = MenuButtonConfig.resolve(model.menuButtons.title);
        this.pauseButtons = MenuButtonConfig.resolve(model.menuButtons.pause);
    }

    public static MenuConfig load() {
        if (instance != null) {
            return instance;
        }

        Path configFile = ConfigFile.resolve(CONFIG_FILE);
        ConfigModel model = new ConfigModel();

        if (ConfigFile.ensureDir()) {
            if (!ConfigFile.exists(configFile)) {
                // First run: write defaults and drop in an example button icon so a
                // fresh install shows working examples out of the box.
                model = defaultModel();
                write(configFile, model);
                installExampleButtonIcon();
            } else {
                model = read(configFile);
            }
        }

        instance = new MenuConfig(model);
        return instance;
    }

    public static MenuConfig getInstance() {
        return instance != null ? instance : load();
    }

    private static ConfigModel read(Path configFile) {
        try {
            String json = ConfigFile.readString(configFile);
            ConfigModel model = GSON.fromJson(json, ConfigModel.class);
            if (model == null) {
                PackBranding.LOGGER.warn("Empty config, using defaults");
                return new ConfigModel();
            }
            sanitize(model);
            PackBranding.LOGGER.info("Loaded config");
            return model;
        } catch (IOException | JsonSyntaxException e) {
            PackBranding.LOGGER.error("Failed to load config, using defaults", e);
            return new ConfigModel();
        }
    }

    private static void write(Path configFile, ConfigModel model) {
        try {
            Files.writeString(configFile, GSON.toJson(model));
            PackBranding.LOGGER.info("Created default config: {}", configFile);
        } catch (IOException e) {
            PackBranding.LOGGER.error("Failed to write default config: {}", configFile, e);
        }
    }

    /** Replaces any null sub-objects (omitted in the JSON) with defaults. */
    private static void sanitize(ConfigModel model) {
        if (model.windowTitle == null) model.windowTitle = new ConfigModel.WindowTitle();
        if (model.icon == null) model.icon = new ConfigModel.Icon();
        if (model.mainMenu == null) model.mainMenu = new ConfigModel.MenuText();
        if (model.pauseMenu == null) model.pauseMenu = new ConfigModel.MenuText();
        if (model.menuButtons == null) model.menuButtons = new ConfigModel.MenuButtons();
        if (model.packVersion == null) model.packVersion = "1.0";
    }

    private static void fillCorners(Map<Corner, String> target, ConfigModel.MenuText text) {
        putCorner(target, Corner.TOP_LEFT, text.topLeft);
        putCorner(target, Corner.TOP_RIGHT, text.topRight);
        putCorner(target, Corner.BOTTOM_LEFT, text.bottomLeft);
        putCorner(target, Corner.BOTTOM_RIGHT, text.bottomRight);
    }

    private static void putCorner(Map<Corner, String> target, Corner corner, String value) {
        if (value != null && !value.isEmpty()) {
            target.put(corner, value);
        }
    }

    private static void installExampleButtonIcon() {
        try {
            Path buttonsDir = getButtonsDir();
            Files.createDirectories(buttonsDir);
            Path target = buttonsDir.resolve(EXAMPLE_BUTTON_FILE);
            if (Files.exists(target)) {
                return;
            }
            try (InputStream in = MenuConfig.class.getResourceAsStream(EXAMPLE_BUTTON_RESOURCE)) {
                if (in == null) {
                    PackBranding.LOGGER.warn("Bundled example button icon not found: {}", EXAMPLE_BUTTON_RESOURCE);
                    return;
                }
                Files.copy(in, target);
                PackBranding.LOGGER.info("Installed example button icon: {}", target);
            }
        } catch (IOException e) {
            PackBranding.LOGGER.error("Failed to install example button icon", e);
        }
    }

    // --- Typed getters (facade over the model) ---

    public String getPackVersion() {
        return model.packVersion;
    }

    public boolean isEnableCustomTitle() {
        return model.windowTitle.enabled;
    }

    public String getWindowTitle() {
        String title = model.windowTitle.title;
        return (title == null || title.isEmpty()) ? "Minecraft {mcversion}" : title;
    }

    public boolean isEnableMainMenuText() {
        return model.mainMenu.enabled;
    }

    public boolean isEnablePauseMenuText() {
        return model.pauseMenu.enabled;
    }

    public boolean isMainMenuTextAboveCopyright() {
        return model.mainMenu.aboveCopyright;
    }

    /** Corner -> raw text for the main (title) menu. Only non-empty corners present. */
    public Map<Corner, String> getMainMenuText() {
        return mainMenuText;
    }

    /** Corner -> raw text for the pause menu. Only non-empty corners present. */
    public Map<Corner, String> getPauseMenuText() {
        return pauseMenuText;
    }

    public boolean isHideRealmsButton() {
        return model.hideRealms;
    }

    public boolean isEnableCustomIcon() {
        return model.icon.enabled;
    }

    public boolean isEnableMenuButtons() {
        return model.menuButtons.enabled;
    }

    /** Icon buttons for the title screen; empty if none/disabled. */
    public List<MenuButtonConfig> getTitleButtons() {
        return model.menuButtons.enabled ? titleButtons : List.of();
    }

    /** Icon buttons for the pause (ESC) menu; empty if none/disabled. */
    public List<MenuButtonConfig> getPauseButtons() {
        return model.menuButtons.enabled ? pauseButtons : List.of();
    }

    /**
     * Translation keys of vanilla pause icon-row buttons to hide, already
     * resolved to their displayed text so they can be matched against
     * {@code button.getMessage().getString()}.
     */
    public java.util.Set<String> getHiddenPauseIconLabels() {
        java.util.Set<String> labels = new java.util.HashSet<>();
        for (String key : model.menuButtons.hidePauseIcons) {
            if (key != null && !key.isBlank()) {
                labels.add(net.minecraft.network.chat.Component.translatable(key.trim()).getString());
            }
        }
        return labels;
    }

    // --- Path helpers ---

    public static Path getConfigDir() {
        return ConfigFile.getConfigDir();
    }

    /** Directory holding menu button icons: {@code config/packbranding/buttons/}. */
    public static Path getButtonsDir() {
        return ConfigFile.resolve("buttons");
    }

    public static Path getIconDir() {
        return ConfigFile.resolve("icon");
    }

    public static Path getIcon16Path() {
        return ConfigFile.resolve("icon_16x16.png");
    }

    public static Path getIcon32Path() {
        return ConfigFile.resolve("icon_32x32.png");
    }

    public static Path getIconSinglePath() {
        return ConfigFile.resolve("icon.png");
    }

    /** Builds the default config shipped on first run, with example content. */
    private static ConfigModel defaultModel() {
        ConfigModel model = new ConfigModel();

        model.packVersion = "1.0";
        model.windowTitle.enabled = true;
        model.windowTitle.title = "Minecraft {mcversion}";
        model.icon.enabled = false;
        model.hideRealms = true;

        model.mainMenu.enabled = true;
        model.mainMenu.aboveCopyright = true;
        model.mainMenu.topLeft = "&6&lMyPack";
        model.mainMenu.topRight = "&7v{packversion}";
        model.mainMenu.bottomLeft = "[Website](https://example.com)";
        model.mainMenu.bottomRight = "&8| &fMC {mcversion}";

        model.pauseMenu.enabled = true;
        model.pauseMenu.topLeft = "&6&lMyPack";
        model.pauseMenu.topRight = "&7v{packversion}";
        model.pauseMenu.bottomLeft = "&7Playing as &f{username}";
        model.pauseMenu.bottomRight = "&8| &fMC {mcversion}";

        model.menuButtons.enabled = true;
        // Two examples: a PNG icon and a built-in (vanilla) sprite icon.
        model.menuButtons.title.add(exampleButton());
        model.menuButtons.title.add(spriteExampleButton());
        model.menuButtons.pause.add(exampleButton());
        model.menuButtons.pause.add(spriteExampleButton());
        // hidePauseIcons left empty so nothing vanilla is removed by default.
        // The options list below is documentation-only (the mod ignores it).
        model.menuButtons.hidePauseIconsOptions = List.of(
                "menu.reportBugs",
                "menu.sendFeedback",
                "menu.playerReporting",
                "menu.online"
        );

        return model;
    }

    private static ConfigModel.Button exampleButton() {
        ConfigModel.Button example = new ConfigModel.Button();
        example.icon = EXAMPLE_BUTTON_FILE;
        example.url = "https://example.com";
        example.tooltip = "&9Visit our website";
        return example;
    }

    private static ConfigModel.Button spriteExampleButton() {
        ConfigModel.Button example = new ConfigModel.Button();
        // Built-in sprite instead of a PNG file (note the "sprite:" prefix).
        example.icon = "sprite:icon/language";
        example.url = "https://example.com";
        example.tooltip = "&aBuilt-in sprite icon";
        return example;
    }
}
