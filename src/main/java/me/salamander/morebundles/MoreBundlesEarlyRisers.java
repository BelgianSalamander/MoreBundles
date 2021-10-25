package me.salamander.morebundles;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class MoreBundlesEarlyRisers implements Runnable{
    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.INSTANCE.getMappingResolver();

        String enchantmentTarget = remapper.mapClassName("intermediary", "net.minecraft.class_1886");

        ClassTinkerers.enumBuilder(enchantmentTarget).addEnumSubclass("BUNDLE", "me.salamander.morebundles.common.enchantment.BundleEnchantmentTarget").build();
    }
}
