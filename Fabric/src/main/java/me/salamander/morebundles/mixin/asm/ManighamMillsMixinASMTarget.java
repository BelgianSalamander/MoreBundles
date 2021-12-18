package me.salamander.morebundles.mixin.asm;

import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {
        EnchantmentCategory.class
},
priority = Integer.MAX_VALUE //Make sure this runs last
)
public class ManighamMillsMixinASMTarget {
}
