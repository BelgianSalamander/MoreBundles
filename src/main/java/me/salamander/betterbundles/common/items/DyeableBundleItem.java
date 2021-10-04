package me.salamander.betterbundles.common.items;

import net.minecraft.item.BundleItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

//Replaces normal bundle
public class DyeableBundleItem extends BundleItem implements DyeableItem {
    private static final int DEFAULT_BUNDLE_COLOR = 0xCD7B46;

    public DyeableBundleItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getColor(ItemStack stack) {
        NbtCompound nbt = stack.getSubNbt("display");
        if(nbt != null && nbt.contains("color", NbtElement.NUMBER_TYPE)){
            return nbt.getInt("color");
        }
        return DEFAULT_BUNDLE_COLOR;
    }
}
