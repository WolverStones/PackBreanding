package cz.wolverstone.agonia.packbranding.client.text;

import cz.wolverstone.agonia.packbranding.client.config.MenuConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses brand text strings into a {@link Component}, resolving color codes,
 * formatting codes and {token} placeholders.
 */
public final class ComponentParser {

    private static final Map<Character, ChatFormatting> COLOR_CODES = new HashMap<>();

    static {
        COLOR_CODES.put('0', ChatFormatting.BLACK);
        COLOR_CODES.put('1', ChatFormatting.DARK_BLUE);
        COLOR_CODES.put('2', ChatFormatting.DARK_GREEN);
        COLOR_CODES.put('3', ChatFormatting.DARK_AQUA);
        COLOR_CODES.put('4', ChatFormatting.DARK_RED);
        COLOR_CODES.put('5', ChatFormatting.DARK_PURPLE);
        COLOR_CODES.put('6', ChatFormatting.GOLD);
        COLOR_CODES.put('7', ChatFormatting.GRAY);
        COLOR_CODES.put('8', ChatFormatting.DARK_GRAY);
        COLOR_CODES.put('9', ChatFormatting.BLUE);
        COLOR_CODES.put('a', ChatFormatting.GREEN);
        COLOR_CODES.put('b', ChatFormatting.AQUA);
        COLOR_CODES.put('c', ChatFormatting.RED);
        COLOR_CODES.put('d', ChatFormatting.LIGHT_PURPLE);
        COLOR_CODES.put('e', ChatFormatting.YELLOW);
        COLOR_CODES.put('f', ChatFormatting.WHITE);
        COLOR_CODES.put('l', ChatFormatting.BOLD);
        COLOR_CODES.put('m', ChatFormatting.STRIKETHROUGH);
        COLOR_CODES.put('n', ChatFormatting.UNDERLINE);
        COLOR_CODES.put('o', ChatFormatting.ITALIC);
        COLOR_CODES.put('r', ChatFormatting.RESET);
    }

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([a-z]+)}");
    /** Matches a clickable link in the form {@code [label](https://url)}. */
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)]\\(([^)]+)\\)");
    /** Default link color (light blue) when the label has no explicit color. */
    private static final int LINK_COLOR = 0x55AAFF;

    private ComponentParser() {
    }

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        input = replaceTokens(input);
        return parseLinks(input);
    }

    /**
     * Returns whether the raw text contains at least one valid {@code [label](url)}
     * link. Used to decide if the text widget needs to be clickable.
     */
    public static boolean hasLink(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        Matcher matcher = LINK_PATTERN.matcher(input);
        while (matcher.find()) {
            if (Urls.parse(matcher.group(2).trim()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Splits the input into plain segments and {@code [label](url)} link
     * segments. Plain text is color-parsed normally; link labels are
     * color-parsed and additionally made clickable (underlined, openable URL).
     */
    private static Component parseLinks(String input) {
        Matcher matcher = LINK_PATTERN.matcher(input);
        MutableComponent result = Component.empty();
        int last = 0;

        while (matcher.find()) {
            if (matcher.start() > last) {
                result.append(parseColors(input.substring(last, matcher.start())));
            }

            String label = matcher.group(1);
            String url = matcher.group(2).trim();
            result.append(buildLink(label, url));

            last = matcher.end();
        }

        if (last < input.length()) {
            result.append(parseColors(input.substring(last)));
        }

        return result;
    }

    private static Component buildLink(String label, String url) {
        URI uri = Urls.parse(url);
        MutableComponent link = (MutableComponent) parseColors(label);

        if (uri == null) {
            // Not a valid URL: render the label, but not clickable.
            return link;
        }

        ClickEvent click = new ClickEvent.OpenUrl(uri);
        return link.withStyle(style -> {
            style = style.withClickEvent(click).withUnderlined(true);
            // Only apply the default link color if the label set no color itself.
            if (style.getColor() == null) {
                style = style.withColor(LINK_COLOR);
            }
            return style;
        });
    }

    private static String replaceTokens(String input) {
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String token = matcher.group(1);
            String replacement = getTokenValue(token);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static String getTokenValue(String token) {
        return switch (token) {
            case "mcversion" -> FabricLoader.getInstance()
                    .getModContainer("minecraft")
                    .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                    .orElse("?");
            case "packversion" -> MenuConfig.getInstance().getPackVersion();
            case "username" -> {
                Minecraft client = Minecraft.getInstance();
                yield client.getUser() != null ? client.getUser().getName() : "?";
            }
            case "modcount" -> String.valueOf(FabricLoader.getInstance().getAllMods().size());
            default -> "{" + token + "}";
        };
    }

    private static Component parseColors(String input) {
        MutableComponent result = Component.empty();
        StringBuilder currentComponent = new StringBuilder();
        Integer currentHexColor = null;
        ChatFormatting currentColor = ChatFormatting.WHITE;
        boolean bold = false, italic = false, underline = false, strikethrough = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '#' && i + 6 < input.length()) {
                String potentialHex = input.substring(i + 1, i + 7);
                if (potentialHex.matches("[A-Fa-f0-9]{6}")) {
                    if (currentComponent.length() > 0) {
                        result.append(createStyledComponent(currentComponent.toString(), currentColor, currentHexColor, bold, italic, underline, strikethrough));
                        currentComponent = new StringBuilder();
                    }
                    currentHexColor = Integer.parseInt(potentialHex, 16);
                    currentColor = null;
                    i += 6;
                    continue;
                }
            }

            if ((c == '&' || c == '§') && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));

                if (COLOR_CODES.containsKey(code)) {
                    if (currentComponent.length() > 0) {
                        result.append(createStyledComponent(currentComponent.toString(), currentColor, currentHexColor, bold, italic, underline, strikethrough));
                        currentComponent = new StringBuilder();
                    }

                    ChatFormatting format = COLOR_CODES.get(code);

                    if (format == ChatFormatting.RESET) {
                        currentColor = ChatFormatting.WHITE;
                        currentHexColor = null;
                        bold = italic = underline = strikethrough = false;
                    } else if (format == ChatFormatting.BOLD) {
                        bold = true;
                    } else if (format == ChatFormatting.ITALIC) {
                        italic = true;
                    } else if (format == ChatFormatting.UNDERLINE) {
                        underline = true;
                    } else if (format == ChatFormatting.STRIKETHROUGH) {
                        strikethrough = true;
                    } else {
                        currentColor = format;
                        currentHexColor = null;
                    }

                    i++;
                    continue;
                }
            }

            currentComponent.append(c);
        }

        if (currentComponent.length() > 0) {
            result.append(createStyledComponent(currentComponent.toString(), currentColor, currentHexColor, bold, italic, underline, strikethrough));
        }

        return result;
    }

    private static MutableComponent createStyledComponent(String text, ChatFormatting color, Integer hexColor, boolean bold, boolean italic, boolean underline, boolean strikethrough) {
        MutableComponent styledComponent = Component.literal(text);

        if (hexColor != null) {
            final int hex = hexColor;
            styledComponent = styledComponent.withStyle(style -> style.withColor(hex));
        } else if (color != null) {
            styledComponent = styledComponent.withStyle(color);
        }

        if (bold) styledComponent = styledComponent.withStyle(ChatFormatting.BOLD);
        if (italic) styledComponent = styledComponent.withStyle(ChatFormatting.ITALIC);
        if (underline) styledComponent = styledComponent.withStyle(ChatFormatting.UNDERLINE);
        if (strikethrough) styledComponent = styledComponent.withStyle(ChatFormatting.STRIKETHROUGH);

        return styledComponent;
    }
}
