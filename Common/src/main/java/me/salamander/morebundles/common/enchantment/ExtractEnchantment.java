package me.salamander.morebundles.common.enchantment;

import me.salamander.morebundles.common.Common;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class ExtractEnchantment extends Enchantment {
    public static final ExtractEnchantment INSTANCE = new ExtractEnchantment();
    
    public ExtractEnchantment() {
        super(Rarity.UNCOMMON, Common.BUNDLE_ENCHANTMENT_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }
    
    @Override
    public int getMaxLevel() {
        return 1;
    }
    
    @Override
    public int getMinCost(int $$0) {
        return 20;
    }
    
    @Override
    public int getMaxCost(int $$0) {
        return 100;
    }
    
    @Override
    public boolean isTradeable() {
        return true;
    }
    
    @Override
    public boolean isDiscoverable() {
        return true;
    }
}
