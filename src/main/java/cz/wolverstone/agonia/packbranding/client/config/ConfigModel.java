package cz.wolverstone.agonia.packbranding.client.config;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * GSON-mapped model of {@code config/packbranding/config.json}. Field names map
 * directly to JSON keys. Defaults here are used when a key is absent.
 *
 * <p>The {@code _comment} fields are documentation-only: the mod never reads
 * them, they exist purely so the generated JSON is self-explanatory (JSON has no
 * native comment syntax).
 */
public class ConfigModel {

    @SerializedName("_comment")
    public String comment = "PackBranding config. Text fields support color codes (&a, #RRGGBB), styles (&l &o &n &m &r), tokens ({mcversion} {packversion} {username} {modcount}) and links [label](https://...). See README for details.";

    public String packVersion = "1.0";
    public WindowTitle windowTitle = new WindowTitle();
    public Icon icon = new Icon();
    public boolean hideRealms = true;
    public MenuText mainMenu = new MenuText();
    public MenuText pauseMenu = new MenuText();
    public MenuButtons menuButtons = new MenuButtons();

    public static class WindowTitle {
        @SerializedName("_comment")
        public String comment = "Replaces the window title bar text. Supports tokens incl. {modversion:modid}.";
        public boolean enabled = true;
        public String title = "Minecraft {mcversion}";
    }

    public static class Icon {
        @SerializedName("_comment")
        public String comment = "Custom window/taskbar icon. Put PNGs in config/packbranding/icon/ (icon_16x16.png + icon_32x32.png) or a single icon.png.";
        public boolean enabled = false;
    }

    public static class MenuText {
        @SerializedName("_comment")
        public String comment = "One text per corner; leave empty for none. 'aboveCopyright' (main menu only) keeps bottom corners above the vanilla copyright line.";
        public boolean enabled = true;
        public boolean aboveCopyright = true;
        public String topLeft = "";
        public String topRight = "";
        public String bottomLeft = "";
        public String bottomRight = "";
    }

    public static class MenuButtons {
        @SerializedName("_comment")
        public String comment = "Clickable icon buttons. Put icon PNGs in config/packbranding/buttons/. 'title'/'pause' are separate lists. 'hidePauseIcons' hides vanilla ESC icons by key (see _hidePauseIcons_options).";
        public boolean enabled = true;
        /** Buttons shown on the title screen. */
        public List<Button> title = new ArrayList<>();
        /** Buttons shown in the pause (ESC) menu icon row. */
        public List<Button> pause = new ArrayList<>();
        /** Translation keys of vanilla pause icon-row buttons to hide, e.g. "menu.reportBugs". */
        public List<String> hidePauseIcons = new ArrayList<>();
        /** Documentation-only list of keys accepted by {@link #hidePauseIcons}. */
        @SerializedName("_hidePauseIcons_options")
        public List<String> hidePauseIconsOptions = new ArrayList<>();
    }

    public static class Button {
        public String icon = "";
        public String url = "";
        public String tooltip = "";
        /** Position in the icon row (0 = first). Omit or -1 to append at the end. */
        public int index = -1;
    }
}
