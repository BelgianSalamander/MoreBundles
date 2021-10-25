package me.salamander.morebundles.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;

public class ExtractEnchantment extends Enchantment {
    public static final ExtractEnchantment INSTANCE = new ExtractEnchantment();

    private ExtractEnchantment() {
        //The enchantment target is not actually used
        super(Rarity.UNCOMMON, CustomEnchantmentTarget.BUNDLE, new EquipmentSlot[]{});
    }

    @Override
    public int getMinPower(int level) {
        return 20;
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
