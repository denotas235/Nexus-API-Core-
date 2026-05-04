package com.maliopt.mixin;

import net.minecraft.class_315;
import net.minecraft.class_7172;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_315.class)
public interface GameOptionsAccessor {
    @Accessor("viewDistance")
    class_7172<Integer> maliopt_getViewDistance();

    @Accessor("simulationDistance")
    class_7172<Integer> maliopt_getSimulationDistance();
}
