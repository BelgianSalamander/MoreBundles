package me.salamander.betterbundles.common.enchantment;

import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class CustomEnchantmentTarget{
    private static final Map<EnchantmentTarget, Predicate<Item>> isAcceptableMap = new HashMap<>();

    public static void addTarget(EnchantmentTarget target, Predicate<Item> predicate){
        isAcceptableMap.put(target, predicate);
    }

    public static Predicate<Item> getPredicate(EnchantmentTarget target){
        return isAcceptableMap.get(target);
    }

    static {
        EnchantmentTarget.values();
    }

    public static EnchantmentTarget BUNDLE;
}
