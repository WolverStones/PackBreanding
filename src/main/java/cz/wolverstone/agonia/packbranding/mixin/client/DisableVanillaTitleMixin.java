package cz.wolverstone.agonia.packbranding.mixin.client;

import cz.wolverstone.agonia.packbranding.client.MenuConfig;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class DisableVanillaTitleMixin {

    @Inject(method = "updateWindowTitle", at = @At("HEAD"), cancellable = true)
    private void onUpdateWindowTitle(CallbackInfo ci) {
        if (MenuConfig.getInstance().isEnableCustomTitle()) {
            ci.cancel();
        }
    }
}
