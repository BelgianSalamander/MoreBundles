package me.salamander.morebundles.common.enchantment;

import me.salamander.morebundles.common.Common;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class AbsorbEnchantment extends Enchantment {
    public static final AbsorbEnchantment INSTANCE = new AbsorbEnchantment();
    
    protected AbsorbEnchantment() {
        super(Rarity.COMMON, Common.BUNDLE_ENCHANTMENT_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }
    
    @Override
    public int getMinCost(int $$0) {
        return 10;
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
