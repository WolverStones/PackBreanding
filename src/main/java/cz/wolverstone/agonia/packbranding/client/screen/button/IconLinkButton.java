package cz.wolverstone.agonia.packbranding.client.screen.button;

import cz.wolverstone.agonia.packbranding.client.text.ComponentParser;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.net.URI;

/**
 * A 20x20 icon button that opens a URL (via the vanilla confirmation screen)
 * when clicked. The icon is a dynamic texture loaded from the config folder by
 * {@link MenuButtonTextures}.
 *
 * <p>Extends {@link AbstractButton} so it gets the standard vanilla button
 * background for free; only the icon (drawn in {@link #extractContents}) is
 * custom.
 */
public class IconLinkButton extends AbstractButton {
    public static final int SIZE = 20;
    private static final int ICON_SIZE = 16;

    private final Identifier icon;
    private final boolean sprite;
    private final URI url;
    private final Screen screen;

    private IconLinkButton(int x, int y, Identifier icon, boolean sprite, URI url, Component narration, Screen screen) {
        super(x, y, SIZE, SIZE, narration);
        this.icon = icon;
        this.sprite = sprite;
        this.url = url;
        this.screen = screen;
    }

    /**
     * Builds a button for the given config on the given screen, or {@code null}
     * if its icon is not available.
     *
     * <p>The icon is either a vanilla/atlas sprite (when the config value starts
     * with {@code sprite:}) or a PNG file loaded from {@code buttons/}.
     */
    public static IconLinkButton create(MenuButtonConfig config, Screen screen) {
        String iconValue = config.iconFile();
        Identifier icon;
        boolean sprite;

        if (iconValue.startsWith("sprite:")) {
            icon = Identifier.tryParse(iconValue.substring("sprite:".length()).trim());
            sprite = true;
        } else {
            icon = MenuButtonTextures.get(iconValue);
            sprite = false;
        }
        if (icon == null) {
            return null;
        }

        Component tooltip = ComponentParser.parse(config.tooltip());
        IconLinkButton button = new IconLinkButton(0, 0, icon, sprite, config.url(), tooltip, screen);
        if (config.tooltip() != null && !config.tooltip().isEmpty()) {
            button.setTooltip(Tooltip.create(tooltip));
        }
        return button;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        ConfirmLinkScreen.confirmLinkNow(screen, url);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        // Draw the vanilla button background first (AbstractButton does NOT do this
        // automatically in 26.2), then the icon centered on top.
        extractDefaultSprite(extractor);

        // Respect the widget's current alpha so the icon fades together with the
        // background. The title screen sets this via Screen.fadeWidgets() during
        // its fade-in animation; elsewhere it stays at 1.0.
        int iconX = getX() + (getWidth() - ICON_SIZE) / 2;
        int iconY = getY() + (getHeight() - ICON_SIZE) / 2;
        int color = (Mth.ceil(getAlpha() * 255.0F) << 24) | 0xFFFFFF;
        if (sprite) {
            // Atlas sprite (vanilla or another mod's GUI sprite).
            extractor.blitSprite(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY,
                    ICON_SIZE, ICON_SIZE, color);
        } else {
            // Standalone dynamic texture loaded from a PNG file.
            extractor.blit(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, 0.0F, 0.0F,
                    ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, color);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, createNarrationMessage());
        if (isActive()) {
            output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
        }
    }
}
