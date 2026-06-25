package cz.wolverstone.agonia.packbranding.client.screen;

import cz.wolverstone.agonia.packbranding.client.config.MenuConfig;
import cz.wolverstone.agonia.packbranding.client.screen.button.IconLinkButton;
import cz.wolverstone.agonia.packbranding.client.screen.button.MenuButtonConfig;
import cz.wolverstone.agonia.packbranding.client.text.ComponentParser;
import cz.wolverstone.agonia.packbranding.client.window.WindowIconManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Applies all screen-level branding (custom corner text, hiding the Realms
 * button, re-applying the window icon) using the Fabric Screen API instead of
 * mixins.
 *
 * <p>Registered once from the client entrypoint; the {@code AFTER_INIT} event
 * fires every time a screen is (re)initialised, so this also survives screen
 * resizes.
 */
public final class ScreenBrandingHandler {
    private static final String REALMS_BUTTON_KEY = "menu.online";
    private static final int LINE_HEIGHT = 10;
    private static final int MARGIN = 2;

    private ScreenBrandingHandler() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                onTitleScreenInit(screen);
            } else if (screen instanceof PauseScreen) {
                onPauseScreenInit(screen);
            }
        });
    }

    private static void onTitleScreenInit(Screen screen) {
        MenuConfig config = MenuConfig.getInstance();

        if (config.isEnableCustomIcon()) {
            WindowIconManager.applyConfiguredIcon();
        }

        if (config.isHideRealmsButton()) {
            hideRealmsButton(screen);
        }

        if (config.isEnableMainMenuText()) {
            // The title screen draws the copyright (bottom-left) and version
            // (bottom-right) on the last line. By default we shift our bottom
            // corners up one line to sit above them; if the player prefers, they
            // can place the text all the way down (over the copyright) instead.
            int bottomReserve = config.isMainMenuTextAboveCopyright() ? LINE_HEIGHT : 0;
            addCornerTexts(screen, config.getMainMenuText(), bottomReserve);
        }

        addMenuButtons(screen, config);
    }

    /**
     * Adds the configured icon buttons to the title screen's icon row (the small
     * 20x20 vanilla buttons such as language / accessibility). Buttons honor their
     * {@code index} (0 = first, before the vanilla icons), and the whole row is
     * re-laid out and re-centered so there are no gaps. (The pause menu does the
     * same via a mixin into its layout.)
     */
    private static void addMenuButtons(Screen screen, MenuConfig config) {
        List<MenuButtonConfig> configs = config.getTitleButtons();
        if (configs.isEmpty()) {
            return;
        }

        // Find the vanilla icon row. Anchor on the vanilla SpriteIconButtons
        // (language / accessibility), then pull in *every* 20x20 widget sitting on
        // that same row -- including icons added by other mods (e.g. ukulib's
        // config button). If we re-centered only the vanilla icons, a foreign icon
        // would be left at its old position and stick out of the row.
        List<AbstractWidget> row = new ArrayList<>();
        Integer rowY = null;
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (widget instanceof SpriteIconButton icon
                    && icon.getWidth() == IconLinkButton.SIZE
                    && icon.getHeight() == IconLinkButton.SIZE) {
                row.add(icon);
                rowY = icon.getY();
            }
        }
        if (rowY != null) {
            final int y0 = rowY;
            for (AbstractWidget widget : Screens.getWidgets(screen)) {
                if (!row.contains(widget)
                        && widget.getWidth() == IconLinkButton.SIZE
                        && widget.getHeight() == IconLinkButton.SIZE
                        && widget.getY() == y0) {
                    row.add(widget);
                }
            }
        }
        row.sort(java.util.Comparator.comparingInt(AbstractWidget::getX));

        // Remember the original row center/Y so we can re-center after inserting.
        int y = row.isEmpty() ? screen.height - IconLinkButton.SIZE - MARGIN : row.get(0).getY();
        int centerX = row.isEmpty() ? screen.width / 2 : rowCenterX(row);

        // Insert our buttons at their requested index.
        for (MenuButtonConfig button : configs) {
            IconLinkButton widget = IconLinkButton.create(button, screen);
            if (widget == null) {
                continue;
            }
            int index = button.index();
            if (index < 0 || index > row.size()) {
                row.add(widget);
            } else {
                row.add(index, widget);
            }
            Screens.getWidgets(screen).add(widget);
        }

        layoutRow(row, centerX, y);
    }

    private static int rowCenterX(List<AbstractWidget> row) {
        int left = row.get(0).getX();
        AbstractWidget last = row.get(row.size() - 1);
        int right = last.getX() + last.getWidth();
        return (left + right) / 2;
    }

    /** Re-positions a row of equally-sized icons centered on {@code centerX}. */
    private static void layoutRow(List<AbstractWidget> row, int centerX, int y) {
        int pitch = IconLinkButton.SIZE + MARGIN;
        int totalWidth = row.size() * pitch - MARGIN;
        int x = centerX - totalWidth / 2;
        for (AbstractWidget widget : row) {
            widget.setPosition(x, y);
            x += pitch;
        }
    }

    private static void onPauseScreenInit(Screen screen) {
        MenuConfig config = MenuConfig.getInstance();

        if (config.isEnablePauseMenuText()) {
            // The pause menu has nothing in the bottom corners, so use them fully.
            addCornerTexts(screen, config.getPauseMenuText(), 0);
        }
    }

    private static void addCornerTexts(Screen screen, Map<Corner, String> texts, int bottomReserve) {
        for (Map.Entry<Corner, String> entry : texts.entrySet()) {
            addCornerText(screen, entry.getKey(), entry.getValue(), bottomReserve);
        }
    }

    /**
     * Adds a brand text widget anchored to the given corner of the screen.
     * Empty text is ignored, so empty corners render nothing.
     *
     * @param bottomReserve extra space kept free at the very bottom (used on the
     *                      title screen to avoid overlapping the vanilla
     *                      copyright/version line)
     */
    private static void addCornerText(Screen screen, Corner corner, String rawText, int bottomReserve) {
        if (rawText == null || rawText.isEmpty()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        Component text = ComponentParser.parse(rawText);
        int textWidth = font.width(text);

        int x = corner.isRight() ? screen.width - textWidth - MARGIN : MARGIN;
        int y = corner.isBottom()
                ? screen.height - LINE_HEIGHT - MARGIN - bottomReserve
                : MARGIN;

        StringWidget widget = new StringWidget(x, y, textWidth, LINE_HEIGHT, text, font);
        // Only text that actually contains a link needs to receive clicks.
        // StringWidget is inactive by default, and AbstractWidget.mouseClicked
        // ignores clicks unless the widget is active, so enable it here and route
        // the clicked style to the vanilla link confirmation screen.
        if (ComponentParser.hasLink(rawText)) {
            widget.active = true;
            widget.setComponentClickHandler(style -> handleStyleClick(screen, style));
        }
        Screens.getWidgets(screen).add(widget);
    }

    private static void handleStyleClick(Screen screen, Style style) {
        ClickEvent event = style.getClickEvent();
        if (event instanceof ClickEvent.OpenUrl openUrl) {
            ConfirmLinkScreen.confirmLinkNow(screen, openUrl.uri());
        }
    }

    /**
     * Hides the Realms ("menu.online") button on the title screen.
     */
    private static void hideRealmsButton(Screen screen) {
        String realmsLabel = Component.translatable(REALMS_BUTTON_KEY).getString();
        List<AbstractWidget> widgets = Screens.getWidgets(screen);

        for (AbstractWidget widget : widgets) {
            if (widget instanceof Button button && realmsLabel.equals(button.getMessage().getString())) {
                button.visible = false;
                button.active = false;
            }
        }
    }
}
