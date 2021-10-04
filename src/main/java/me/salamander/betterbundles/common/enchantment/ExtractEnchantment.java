package me.salamander.betterbundles.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ExtractEnchantment extends Enchantment {
    public static final ExtractEnchantment INSTANCE = new ExtractEnchantment();

    private ExtractEnchantment() {
        super(Rarity.UNCOMMON, CustomEnchantmentTarget.BUNDLE, new EquipmentSlot[]{});
    }

    @Override
    public int getMinPower(int level) {
        return 25;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
