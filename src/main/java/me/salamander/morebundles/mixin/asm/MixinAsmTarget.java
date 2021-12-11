package me.salamander.morebundles.mixin.asm;

import net.minecraft.enchantment.EnchantmentTarget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
        value = EnchantmentTarget.class,
        priority = Integer.MAX_VALUE
)
public class MixinAsmTarget {
    //Intentionally left blank
}
