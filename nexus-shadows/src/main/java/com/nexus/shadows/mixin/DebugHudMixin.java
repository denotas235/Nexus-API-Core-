package com.nexus.shadows.mixin;

import com.nexus.shadows.ShadowDebugHud;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(DebugHud.class)
public class DebugHudMixin {
    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void onGetLeftText(CallbackInfoReturnable<List<String>> cir) {
        ShadowDebugHud.addLeftLines(cir.getReturnValue());
    }
}