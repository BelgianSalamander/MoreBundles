package me.salamander.morebundles.common.items.handlers;

import me.salamander.morebundles.common.items.BundleHandler;
import me.salamander.morebundles.util.MBUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DefaultBundleHandler extends BundleHandler {
    private final int capacity;
    private final boolean concealed;
    
    public DefaultBundleHandler(int capacity, boolean concealed) {
        this.capacity = capacity;
        this.concealed = concealed;
    }
    
    @Override
    protected int getDefaultCapacity() {
        return capacity;
    }
    
    @Override
    protected boolean getDefaultConcealed() {
        return concealed;
    }
    
    @Override
    public ItemStack removeFirstItem(CompoundTag bundleNBT) {
        ListTag itemList = MBUtil.getOrCreateList(bundleNBT, ITEMS_LIST_KEY, 10);
        if(itemList.size() > 0){
            CompoundTag itemTag = itemList.getCompound(0);
            ItemStack itemStack = ItemStack.of(itemTag);
            itemList.remove(0);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack removeSingleItem(CompoundTag bundleNBT) {
        ListTag itemList = MBUtil.getOrCreateList(bundleNBT, ITEMS_LIST_KEY, 10);
        if(itemList.size() == 0){
            return ItemStack.EMPTY;
        }
        
        CompoundTag itemTag = itemList.getCompound(0);
        ItemStack itemStack = ItemStack.of(itemTag);
        
        if(itemStack.getCount() == 1){
            itemList.remove(0);
        }else {
            itemTag.putInt("Count", itemStack.getCount() - 1);
        }
        
        itemStack.setCount(1);
        
        return itemStack;
    }
    
    @Override
    public int addItem(CompoundTag bundleNBT, ItemStack itemStack) {
        if(!itemStack.isEmpty() && itemStack.getItem().canFitInsideContainerItems()){
            ListTag itemList = MBUtil.getOrCreateList(bundleNBT, ITEMS_LIST_KEY, 10);
            
            int currCapacity = getTotalWeight(bundleNBT);
            int newItemWeight = BundleItem.getWeight(itemStack);
            
            int spaceLeft = getMaxCapacity(bundleNBT) - currCapacity;
            int canAdd = Math.min(itemStack.getCount(), spaceLeft / newItemWeight);
            int canAddCopy = canAdd;

            for(int i = 0; i < itemList.size() && canAdd > 0; i++) {
                CompoundTag itemTag = itemList.getCompound(i);
                ItemStack currItem = ItemStack.of(itemTag);
                
                if(ItemStack.isSameItemSameTags(currItem, itemStack)){
                    int addToThisItem = Math.min(canAdd, currItem.getItem().getMaxStackSize() - currItem.getCount());
                    itemTag.putInt("Count", currItem.getCount() + addToThisItem);
                    canAdd -= addToThisItem;
                }
            }
            
            if(canAdd > 0){ //There are still items left to add
                ItemStack newItem = itemStack.copy();
                newItem.setCount(canAdd);
                itemList.add(newItem.save(new CompoundTag()));
            }
            
            return canAddCopy;
        }
        return 0;
    }
    
    @Override
    public ItemStack removeFirst(CompoundTag bundleNBT, Predicate<ItemStack> condition) {
        ListTag itemList = MBUtil.getOrCreateList(bundleNBT, ITEMS_LIST_KEY, 10);
        
        for(int i = 0; i < itemList.size(); i++) {
            CompoundTag itemTag = itemList.getCompound(i);
            ItemStack itemStack = ItemStack.of(itemTag);
            if(condition.test(itemStack)){
                itemList.remove(i);
                return itemStack;
            }
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public List<ItemStack> getAllItems(CompoundTag bundleNBT) {
        ListTag itemList = MBUtil.getOrCreateList(bundleNBT, ITEMS_LIST_KEY, 10);
        List<ItemStack> items = new ArrayList<>(itemList.size());
        
        for(int i = 0; i < itemList.size(); i++) {
            CompoundTag itemTag = itemList.getCompound(i);
            items.add(ItemStack.of(itemTag));
        }
        
        return items;
    }
    
    @Override
    public void clear(CompoundTag tag) {
        tag.put(ITEMS_LIST_KEY, new ListTag());
    }
    
    @Override
    public int getNumItems(CompoundTag tag) {
        return MBUtil.getOrCreateList(tag, ITEMS_LIST_KEY, 10).size();
    }
    
    @Override
    public ItemStack getStack(CompoundTag tag, int index) {
        return ItemStack.of(MBUtil.getOrCreateList(tag, ITEMS_LIST_KEY, 10).getCompound(index));
    }
    
    @Override
    public void setStack(CompoundTag tag, int index, ItemStack stack) {
        MBUtil.getOrCreateList(tag, ITEMS_LIST_KEY, 10).set(index, stack.save(new CompoundTag()));
    }
    
    @Override
    public ItemStack removeStack(CompoundTag tag, int index) {
        return ItemStack.of((CompoundTag) MBUtil.getOrCreateList(tag, ITEMS_LIST_KEY, 10).remove(index));
    }
    
    @Override
    public ItemStack removeStack(CompoundTag tag, int index, int amount) {
        ListTag itemList = MBUtil.getOrCreateList(tag, ITEMS_LIST_KEY, 10);
        CompoundTag itemTag = itemList.getCompound(index);
        ItemStack itemStack = ItemStack.of(itemTag);
        
        int currCount = itemStack.getCount();
        int removeCount = Math.min(amount, currCount);
        int remainingCount = currCount - removeCount;
        
        if(remainingCount > 0){
            itemTag.putInt("Count", remainingCount);
        }else{
            itemList.remove(index);
        }
        
        itemStack.setCount(removeCount);
        return itemStack;
    }
    
    @Override
    public boolean canAdd(CompoundTag tag, ItemStack stack) {
        return getMaxCapacity(tag) - getTotalWeight(tag) >= BundleItem.getWeight(stack) * stack.getCount();
    }
    
}
