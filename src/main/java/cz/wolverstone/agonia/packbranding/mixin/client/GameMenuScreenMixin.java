package cz.wolverstone.agonia.packbranding.mixin.client;

import cz.wolverstone.agonia.packbranding.client.MenuConfig;
import cz.wolverstone.agonia.packbranding.client.TextParser;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        MenuConfig config = MenuConfig.getInstance();
        if (config == null) return;

        if (!config.isEnablePauseMenuText()) return;

        String customText = config.getCustomTextPauseMenu();
        if (customText == null || customText.isEmpty()) return;

        Text parsedText = TextParser.parse(customText);
        int textWidth = this.textRenderer.getWidth(parsedText);

        int x = this.width - textWidth - 2;
        int y = this.height - 10;

        TextWidget widget = new TextWidget(x, y, textWidth, 10, parsedText, this.textRenderer);
        this.addDrawableChild(widget);
    }
}
