package cz.wolverstone.agonia.packbranding.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import cz.wolverstone.agonia.packbranding.client.config.MenuConfig;
import cz.wolverstone.agonia.packbranding.client.screen.button.IconLinkButton;
import cz.wolverstone.agonia.packbranding.client.screen.button.MenuButtonConfig;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Customizes the pause menu's icon row, the same way ModMenu does in 26.2:
 * capture the local {@code LinearLayout} that vanilla fills with its icon
 * buttons, optionally remove vanilla icons (by translation key, so the row
 * reflows with no gap), and append the configured custom buttons.
 *
 * <p>A higher-than-default {@code priority} makes our injector run <em>after</em>
 * other mods that add icons to the same row (e.g. ukulib's config button, which
 * uses the default priority of 1000). That way those foreign icons are already
 * present when we rebuild the row, so our buttons land at consistent positions
 * and the row re-centers around the final set without overlap.
 */
@Mixin(value = PauseScreen.class, priority = 1500)
public abstract class PauseScreenIconButtonsMixin {

    @Inject(
            method = "createPauseMenu",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void packbranding$customizeIconRow(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconRow) {
        MenuConfig config = MenuConfig.getInstance();
        Set<String> hidden = config.getHiddenPauseIconLabels();

        // Collect the existing children, dropping the ones we want to hide. The
        // layout has no per-element removal, so we rebuild it: this makes the row
        // reflow with no gap where a hidden icon used to be.
        List<LayoutElement> keep = new ArrayList<>();
        iconRow.visitChildren(child -> {
            if (child instanceof AbstractWidget aw && hidden.contains(aw.getMessage().getString())) {
                return; // skip -> effectively removed
            }
            keep.add(child);
        });

        boolean removedAny = !hidden.isEmpty();
        boolean addedAny = insertCustomButtons(config, keep);

        // Nothing to change? leave the layout untouched.
        if (!removedAny && !addedAny) {
            return;
        }

        iconRow.removeChildren();
        for (LayoutElement element : keep) {
            iconRow.addChild(element);
        }
    }

    /**
     * Inserts the configured custom buttons into {@code row} at their requested
     * index (clamped to the current size); index &lt; 0 appends at the end.
     * Returns whether any button was inserted.
     */
    private boolean insertCustomButtons(MenuConfig config, List<LayoutElement> row) {
        if (!config.isEnableMenuButtons()) {
            return false;
        }
        Screen screen = (Screen) (Object) this;
        boolean added = false;
        for (MenuButtonConfig button : config.getPauseButtons()) {
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
            added = true;
        }
        return added;
    }
}
