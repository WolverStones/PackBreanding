package cz.wolverstone.agonia.packbranding.client;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowTitleChanger {
    private static final Logger LOGGER = LoggerFactory.getLogger("PackBranding");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([a-z]+)(?::([^}]+))?}");

    public static void applyTitle(WindowTitleConfig config) {
        String parsedTitle = parseTitle(config.getTitle());
        Minecraft.getInstance().getWindow().setTitle(parsedTitle);
        LOGGER.info("Window title set to: {}", parsedTitle);
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
