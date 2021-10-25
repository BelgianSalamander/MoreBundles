package me.salamander.betterbundles;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class BetterBundlesEarlyRisers implements Runnable{
    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.INSTANCE.getMappingResolver();

        String enchantmentTarget = remapper.mapClassName("intermediary", "net.minecraft.class_1886");

        ClassTinkerers.enumBuilder(enchantmentTarget).addEnumSubclass("BUNDLE", "me.salamander.betterbundles.common.enchantment.BundleEnchantmentTarget").build();
    }
}
