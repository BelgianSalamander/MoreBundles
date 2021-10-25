package me.salamander.betterbundles.common.enchantment;

import me.salamander.betterbundles.common.ExtraBundleInfo;
import me.salamander.betterbundles.mixin.MixinEnchantmentTarget;
import net.minecraft.item.Item;

public class BundleEnchantmentTarget extends MixinEnchantmentTarget {
    @Override
    public boolean isAcceptableItem(Item item) {
        return item instanceof ExtraBundleInfo.Access;
    }
}
