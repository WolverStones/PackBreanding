package cz.wolverstone.agonia.packbranding.mixin.client;

import cz.wolverstone.agonia.packbranding.client.MenuConfig;
import cz.wolverstone.agonia.packbranding.client.ComponentParser;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        MenuConfig config = MenuConfig.getInstance();
        if (config == null) return;

        if (!config.isEnablePauseMenuText()) return;

        String customText = config.getCustomTextPauseMenu();
        if (customText == null || customText.isEmpty()) return;

        Component parsedText = ComponentParser.parse(customText);
        int textWidth = this.font.width(parsedText);

        int x = this.width - textWidth - 2;
        int y = this.height - 10;

        StringWidget widget = new StringWidget(x, y, textWidth, 10, parsedText, this.font);
        this.addRenderableWidget(widget);
    }
}
