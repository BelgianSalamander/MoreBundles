package me.salamander.morebundles.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;

public class AbsorbEnchantment extends Enchantment {
    public static final AbsorbEnchantment INSTANCE = new AbsorbEnchantment();

    protected AbsorbEnchantment() {
        super(Rarity.UNCOMMON, CustomEnchantmentTarget.BUNDLE, new EquipmentSlot[]{});
    }

    @Override
    public int getMinPower(int level) {
        return 10;
    }

    @Override
    public int getMaxPower(int level) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return true;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return true;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof BundleItem;
    }
}
