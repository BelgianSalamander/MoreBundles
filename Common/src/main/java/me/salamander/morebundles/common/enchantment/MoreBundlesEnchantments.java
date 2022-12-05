package me.salamander.morebundles.common.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Supplier;

public class MoreBundlesEnchantments {
    public static Supplier<? extends Enchantment> ABSORB = AbsorbEnchantment.INSTANCE;
    public static Supplier<? extends Enchantment> EXTRACT = ExtractEnchantment.INSTANCE;
}
