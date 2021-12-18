package me.salamander.morebundles.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

/**
 * Provides a way to manage a bundle's NBT data
 */
public abstract class BundleHandler {
    public static String CAPACITY_KEY = "MaxBundleStorage";
    public static String CONCEAL_KEY = "BundleContentsHidden";
    public static String ITEMS_LIST_KEY = "Items";
    
    public final int getMaxCapacity(CompoundTag bundleNBT){
        if(!bundleNBT.contains(CAPACITY_KEY)){
            bundleNBT.putInt(CAPACITY_KEY, getDefaultCapacity());
            return getDefaultCapacity();
        }else{
            return bundleNBT.getInt(CAPACITY_KEY);
        }
    }
    
    public final boolean isConcealed(CompoundTag bundleNBT){
        if(!bundleNBT.contains(CONCEAL_KEY)){
            bundleNBT.putBoolean(CONCEAL_KEY, getDefaultConcealed());
            return getDefaultConcealed();
        }else{
            return bundleNBT.getBoolean(CONCEAL_KEY);
        }
    }
    
    public int getTotalWeight(CompoundTag bundleNBT){
        int totalWeight = 0;
        for(ItemStack itemStack : getAllItems(bundleNBT)){
            totalWeight += BundleItem.getWeight(itemStack) * itemStack.getCount();
        }
        return totalWeight;
    }
    
    public float getFullness(CompoundTag tag){
        return (float) getTotalWeight(tag) / (float)getMaxCapacity(tag);
    }
    
    protected abstract int getDefaultCapacity();
    protected abstract boolean getDefaultConcealed();
    
    /**
     * Remove an item stack from the bundle. This action should be deterministic. (i.e if an item has the same NBT data it would always remove the same item)
     * @param bundleNBT The bundle's NBT data
     * @return The item that was removed. {@link ItemStack#EMPTY} if no item was removed.
     */
    public abstract ItemStack removeFirstItem(CompoundTag bundleNBT);

    public abstract ItemStack removeSingleItem(CompoundTag bundleNBT);
    
    /**
     * Inserts as much of an item into the bundle as possible.
     * @param bundleNBT The bundle's NBT data
     * @param itemStack The item to insert. This itemStack will NOT be modified by the method.
     * @return The amount of the item that was inserted.
     */
    public abstract int addItem(CompoundTag bundleNBT, ItemStack itemStack);
    
    public abstract ItemStack removeFirst(CompoundTag bundleNBT, Predicate<ItemStack> condition);
    
    public abstract List<ItemStack> getAllItems(CompoundTag bundleNBT);
    
    public abstract void clear(CompoundTag tag);
    
    public abstract int getNumItems(CompoundTag tag);
    
    public abstract ItemStack getStack(CompoundTag tag, int index);
    
    public abstract void setStack(CompoundTag tag, int index, ItemStack stack);
    
    public abstract ItemStack removeStack(CompoundTag tag, int index);
    
    public abstract ItemStack removeStack(CompoundTag tag, int index, int amount);
    
    public abstract boolean canAdd(CompoundTag tag, ItemStack stack);
}
