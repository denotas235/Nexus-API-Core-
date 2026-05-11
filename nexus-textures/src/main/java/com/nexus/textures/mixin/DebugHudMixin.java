package com.nexus.textures.mixin;

import com.nexus.textures.ASTCDebugHud;
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
        ASTCDebugHud.addLeftLines(cir.getReturnValue());
    }
}