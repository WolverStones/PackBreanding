package cz.wolverstone.agonia.packbranding.client.screen.button;

import cz.wolverstone.agonia.packbranding.PackBranding;
import cz.wolverstone.agonia.packbranding.client.config.ConfigModel;
import cz.wolverstone.agonia.packbranding.client.text.Urls;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A single resolved menu icon button: an icon file (under
 * {@code config/packbranding/buttons/}), a validated link to open, and an
 * optional tooltip.
 *
 * @param iconFile icon: a PNG file name, or {@code sprite:<id>} for an atlas sprite
 * @param url      validated http(s) link opened on click
 * @param tooltip  raw tooltip text (may be empty), color codes supported
 * @param index    target position in the icon row (0 = first), or -1 to append
 */
public record MenuButtonConfig(String iconFile, URI url, String tooltip, int index) {

    /**
     * Resolves the configured button entries, skipping any with a missing icon
     * or an invalid url (logging a warning for each).
     */
    public static List<MenuButtonConfig> resolve(List<ConfigModel.Button> entries) {
        List<MenuButtonConfig> buttons = new ArrayList<>();
        if (entries == null) {
            return buttons;
        }

        int n = 0;
        for (ConfigModel.Button entry : entries) {
            n++;
            if (entry == null) {
                continue;
            }
            if (isBlank(entry.icon)) {
                PackBranding.LOGGER.warn("Menu button {} has no icon, skipping", n);
                continue;
            }
            URI uri = Urls.parse(entry.url);
            if (uri == null) {
                PackBranding.LOGGER.warn("Menu button {} has no valid url, skipping", n);
                continue;
            }
            String tooltip = entry.tooltip != null ? entry.tooltip : "";
            buttons.add(new MenuButtonConfig(entry.icon.trim(), uri, tooltip, entry.index));
        }

        return buttons;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
