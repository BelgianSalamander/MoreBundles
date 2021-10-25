package me.salamander.betterbundles.common.enchantment;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.enchantment.EnchantmentTarget;

public class CustomEnchantmentTarget{
    public static EnchantmentTarget BUNDLE;

    public static void loadValues(){
        BUNDLE = ClassTinkerers.getEnum(EnchantmentTarget.class, "BUNDLE");
    }
}
