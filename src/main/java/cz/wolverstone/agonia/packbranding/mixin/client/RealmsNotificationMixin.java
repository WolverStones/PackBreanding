package cz.wolverstone.agonia.packbranding.mixin.client;

import cz.wolverstone.agonia.packbranding.client.MenuConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RealmsNotificationsScreen.class)
public class RealmsNotificationMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (MenuConfig.getInstance().isHideRealmsButton()) {
            ci.cancel();
        }
    }
}
