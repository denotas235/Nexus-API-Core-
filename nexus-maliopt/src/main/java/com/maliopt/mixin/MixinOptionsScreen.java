package com.maliopt.mixin;

import com.maliopt.config.MaliOptConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class MixinOptionsScreen extends Screen {

    protected MixinOptionsScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addMaliOptButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("⚡ MaliOpt Settings"),
                button -> {
                    if (this.client != null) {
                        this.client.setScreen(
                            new com.maliopt.config.MaliOptConfigScreen().createConfigScreen(this).get()
                        );
                    }
                }
            ).dimensions(this.width / 2 - 100, this.height / 6 + 150, 200, 20)
            .build());
    }
}
