package me.salamander.morebundles.common.items.handlers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class InextractibleBundleHandler extends DefaultBundleHandler{
    public InextractibleBundleHandler(int capacity, boolean concealed) {
        super(capacity, concealed);
    }
    
    @Override
    public ItemStack removeFirstItem(CompoundTag bundleNBT) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack removeSingleItem(CompoundTag bundleNBT) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack removeFirst(CompoundTag bundleNBT, Predicate<ItemStack> condition) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack removeStack(CompoundTag tag, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack removeStack(CompoundTag tag, int index, int amount) {
        return ItemStack.EMPTY;
    }
    
}
