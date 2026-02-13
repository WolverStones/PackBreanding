package cz.wolverstone.agonia.packbranding.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParser {

    private static final Map<Character, Formatting> COLOR_CODES = new HashMap<>();

    static {
        COLOR_CODES.put('0', Formatting.BLACK);
        COLOR_CODES.put('1', Formatting.DARK_BLUE);
        COLOR_CODES.put('2', Formatting.DARK_GREEN);
        COLOR_CODES.put('3', Formatting.DARK_AQUA);
        COLOR_CODES.put('4', Formatting.DARK_RED);
        COLOR_CODES.put('5', Formatting.DARK_PURPLE);
        COLOR_CODES.put('6', Formatting.GOLD);
        COLOR_CODES.put('7', Formatting.GRAY);
        COLOR_CODES.put('8', Formatting.DARK_GRAY);
        COLOR_CODES.put('9', Formatting.BLUE);
        COLOR_CODES.put('a', Formatting.GREEN);
        COLOR_CODES.put('b', Formatting.AQUA);
        COLOR_CODES.put('c', Formatting.RED);
        COLOR_CODES.put('d', Formatting.LIGHT_PURPLE);
        COLOR_CODES.put('e', Formatting.YELLOW);
        COLOR_CODES.put('f', Formatting.WHITE);
        COLOR_CODES.put('l', Formatting.BOLD);
        COLOR_CODES.put('m', Formatting.STRIKETHROUGH);
        COLOR_CODES.put('n', Formatting.UNDERLINE);
        COLOR_CODES.put('o', Formatting.ITALIC);
        COLOR_CODES.put('r', Formatting.RESET);
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([a-z]+)}");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)]\\(([^)]+)\\)");

    public static Text parse(String input) {
        if (input == null || input.isEmpty()) {
            return Text.empty();
        }

        input = replaceTokens(input);
        return parseWithLinks(input);
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
                MinecraftClient client = MinecraftClient.getInstance();
                yield client.getSession() != null ? client.getSession().getUsername() : "?";
            }
            case "modcount" -> String.valueOf(FabricLoader.getInstance().getAllMods().size());
            default -> "{" + token + "}";
        };
    }

    private static Text parseWithLinks(String input) {
        return parseColors(input);
    }

    private static Text parseColors(String input) {
        MutableText result = Text.empty();
        StringBuilder currentText = new StringBuilder();
        Integer currentHexColor = null;
        Formatting currentColor = Formatting.WHITE;
        boolean bold = false, italic = false, underline = false, strikethrough = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '#' && i + 6 < input.length()) {
                String potentialHex = input.substring(i + 1, i + 7);
                if (potentialHex.matches("[A-Fa-f0-9]{6}")) {
                    if (currentText.length() > 0) {
                        result.append(createStyledText(currentText.toString(), currentColor, currentHexColor, bold, italic, underline, strikethrough));
                        currentText = new StringBuilder();
                    }
                    currentHexColor = Integer.parseInt(potentialHex, 16);
                    currentColor = null;
                    i += 6;
                    continue;
                }
            }

            if ((c == '&' || c == '\u00A7') && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));

                if (COLOR_CODES.containsKey(code)) {
                    if (currentText.length() > 0) {
                        result.append(createStyledText(currentText.toString(), currentColor, currentHexColor, bold, italic, underline, strikethrough));
                        currentText = new StringBuilder();
                    }

                    Formatting format = COLOR_CODES.get(code);

                    if (format == Formatting.RESET) {
                        currentColor = Formatting.WHITE;
                        currentHexColor = null;
                        bold = italic = underline = strikethrough = false;
                    } else if (format == Formatting.BOLD) {
                        bold = true;
                    } else if (format == Formatting.ITALIC) {
                        italic = true;
                    } else if (format == Formatting.UNDERLINE) {
                        underline = true;
                    } else if (format == Formatting.STRIKETHROUGH) {
                        strikethrough = true;
                    } else {
                        currentColor = format;
                        currentHexColor = null;
                    }

                    i++;
                    continue;
                }
            }

            currentText.append(c);
        }

        if (currentText.length() > 0) {
            result.append(createStyledText(currentText.toString(), currentColor, currentHexColor, bold, italic, underline, strikethrough));
        }

        return result;
    }

    private static MutableText createStyledText(String text, Formatting color, Integer hexColor, boolean bold, boolean italic, boolean underline, boolean strikethrough) {
        MutableText styledText = Text.literal(text);

        if (hexColor != null) {
            final int hex = hexColor;
            styledText = styledText.styled(style -> style.withColor(hex));
        } else if (color != null) {
            styledText = styledText.formatted(color);
        }

        if (bold) styledText = styledText.formatted(Formatting.BOLD);
        if (italic) styledText = styledText.formatted(Formatting.ITALIC);
        if (underline) styledText = styledText.formatted(Formatting.UNDERLINE);
        if (strikethrough) styledText = styledText.formatted(Formatting.STRIKETHROUGH);

        return styledText;
    }
}
