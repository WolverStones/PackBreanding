package cz.wolverstone.agonia.packbranding.client.text;

import cz.wolverstone.agonia.packbranding.PackBranding;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Shared helpers for parsing and validating http(s) URLs used by clickable
 * links and menu icon buttons.
 */
public final class Urls {

    private Urls() {
    }

    /**
     * Parses an http/https URL, returning {@code null} (and logging a warning)
     * for invalid or unsupported URLs.
     */
    public static URI parse(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                PackBranding.LOGGER.warn("Ignoring URL with unsupported scheme: {}", url);
                return null;
            }
            return uri;
        } catch (URISyntaxException e) {
            PackBranding.LOGGER.warn("Ignoring invalid URL: {}", url);
            return null;
        }
    }
}
