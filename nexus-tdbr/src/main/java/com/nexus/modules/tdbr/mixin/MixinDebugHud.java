package com.nexus.modules.tdbr.mixin;

import com.nexus.modules.tdbr.util.Profiler;
import com.nexus.modules.tdbr.PLSManager;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class MixinDebugHud {
    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void addNexusInfo(CallbackInfoReturnable<List<String>> cir) {
        List<String> lines = cir.getReturnValue();
        lines.add("");
        lines.add("§6[§eNexus TDBR§6]");
        lines.add("  Path: " + (PLSManager.INSTANCE.getEnabled() ? "§aPLS (on-chip)" : "§eMRT Fallback"));
        lines.add("  GPU time: §b" + Profiler.INSTANCE.getLastGPU() + " ms§r");
        lines.add("  FPS: §b" + String.format("%.1f", Profiler.INSTANCE.getFPS()) + "§r");
        cir.setReturnValue(lines);
    }
}
