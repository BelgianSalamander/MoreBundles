package me.salamander.morebundles.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class DyeableBundleItem extends BundleItem implements DyeableLeatherItem {
    private static final int DEFAULT_BUNDLE_COLOR = 0xCD7B46;
    
    public DyeableBundleItem(Properties settings) {
        super(settings);
    }
    
    @Override
    public int getColor(ItemStack stack) {
        CompoundTag nbt = stack.getTagElement("display");
        if(nbt != null && nbt.contains("color", Tag.TAG_ANY_NUMERIC)){
            return nbt.getInt("color");
        }
        return DEFAULT_BUNDLE_COLOR;
    }
    
}
