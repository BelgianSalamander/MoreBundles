package me.salamander.betterbundles.common.enchantment;

import me.salamander.betterbundles.BetterBundles;
import net.minecraft.util.registry.Registry;

public class Enchantments {
    public static void registerAll(){
        Registry.register(Registry.ENCHANTMENT, BetterBundles.ID("extract"), ExtractEnchantment.INSTANCE);
        Registry.register(Registry.ENCHANTMENT, BetterBundles.ID("absorb"), AbsorbEnchantment.INSTANCE);
    }
}
