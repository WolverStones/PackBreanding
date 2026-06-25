package cz.wolverstone.agonia.packbranding.client.window;

import cz.wolverstone.agonia.packbranding.PackBranding;
import cz.wolverstone.agonia.packbranding.client.config.MenuConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies the configured custom window title, resolving {token} placeholders.
 */
public final class WindowTitleManager {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([a-z]+)(?::([^}]+))?}");

    private WindowTitleManager() {
    }

    public static void applyTitle(MenuConfig config) {
        String parsedTitle = parseTitle(config.getWindowTitle());
        Minecraft.getInstance().getWindow().setTitle(parsedTitle);
        PackBranding.LOGGER.info("Window title set to: {}", parsedTitle);
    }

    private static String parseTitle(String input) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = TOKEN_PATTERN.matcher(input);

        while (matcher.find()) {
            String token = matcher.group(1);
            String arg = matcher.group(2);

            String replacement = processToken(token, arg);
            if (replacement == null) {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String processToken(String token, String arg) {
        return switch (token) {
            case "mcversion" -> getMinecraftVersion();
            case "packversion" -> MenuConfig.getInstance().getPackVersion();
            case "username" -> Minecraft.getInstance().getUser().getName();
            case "modversion" -> {
                if (arg == null || arg.isEmpty()) {
                    yield null;
                }
                Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(arg);
                yield mod.map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("?");
            }
            case "modcount" -> String.valueOf(FabricLoader.getInstance().getAllMods().size());
            default -> null;
        };
    }

    private static String getMinecraftVersion() {
        return FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("?");
    }
}
