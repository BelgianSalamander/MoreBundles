package me.salamander.morebundles.common.enchantment;

import me.salamander.morebundles.MoreBundles;
import net.minecraft.util.registry.Registry;

public class Enchantments {
    public static void registerAll(){
        Registry.register(Registry.ENCHANTMENT, MoreBundles.ID("extract"), ExtractEnchantment.INSTANCE);
        Registry.register(Registry.ENCHANTMENT, MoreBundles.ID("absorb"), AbsorbEnchantment.INSTANCE);
    }
}
