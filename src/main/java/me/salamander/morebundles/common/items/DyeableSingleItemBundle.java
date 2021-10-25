package me.salamander.morebundles.common.items;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class DyeableSingleItemBundle extends SingleItemBundle implements DyeableItem {
    private static final int DEFAULT_BUNDLE_COLOR = 0xCD7B46;

    public DyeableSingleItemBundle(Settings settings, int capacity) {
        super(settings, capacity);
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
