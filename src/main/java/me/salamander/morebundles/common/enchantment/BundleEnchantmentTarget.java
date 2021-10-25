package me.salamander.morebundles.common.enchantment;

import me.salamander.morebundles.common.ExtraBundleInfo;
import me.salamander.morebundles.mixin.MixinEnchantmentTarget;
import net.minecraft.item.Item;

public class BundleEnchantmentTarget extends MixinEnchantmentTarget {
    @Override
    public boolean isAcceptableItem(Item item) {
        return item instanceof ExtraBundleInfo.Access;
    }
}
