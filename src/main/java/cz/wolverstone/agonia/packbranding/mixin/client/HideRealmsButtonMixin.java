package cz.wolverstone.agonia.packbranding.mixin.client;

import cz.wolverstone.agonia.packbranding.client.MenuConfig;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class HideRealmsButtonMixin {

    private static final String REALMS_KEY = "menu.online";

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!MenuConfig.getInstance().isHideRealmsButton()) {
            return;
        }

        TitleScreen screen = (TitleScreen) (Object) this;

        screen.children().forEach(child -> {
            if (child instanceof ButtonWidget button) {
                Text message = button.getMessage();
                if (message != null) {
                    String key = message.getString();
                    if (key.equals(Text.translatable(REALMS_KEY).getString())) {
                        button.visible = false;
                        button.active = false;
                    }
                }
            }
        });
    }
}
