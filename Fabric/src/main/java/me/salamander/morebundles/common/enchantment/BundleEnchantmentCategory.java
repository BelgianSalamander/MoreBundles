package me.salamander.morebundles.common.enchantment;

import me.salamander.morebundles.mixin.MixinEnchantmentCategory;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;

public class BundleEnchantmentCategory extends MixinEnchantmentCategory {
    @Override
    public boolean canEnchant(Item item) {
        return item instanceof BundleItem;
    }
}
