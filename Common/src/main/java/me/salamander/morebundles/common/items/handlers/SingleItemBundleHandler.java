package me.salamander.morebundles.common.items.handlers;

import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.items.BundleHandler;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SingleItemBundleHandler extends BundleHandler {
    private final int capacity;
    private final boolean conceal;
    
    public SingleItemBundleHandler(int capacity, boolean conceal) {
        this.capacity = capacity;
        this.conceal = conceal;
    }
    
    @Override
    protected int getDefaultCapacity() {
        return capacity;
    }
    
    @Override
    protected boolean getDefaultConcealed() {
        return conceal;
    }
    
    @Override
    public ItemStack removeFirstItem(CompoundTag bundleNBT) {
        ItemStack curr = getItem(bundleNBT);
        if(curr.isEmpty()){
            return ItemStack.EMPTY;
        }
        
        int remove = Math.min(curr.getCount(), curr.getItem().getMaxStackSize());
        bundleNBT.putInt("count", curr.getCount() - remove);
        curr.setCount(remove);
        return curr;
    }
    
    @Override
    public ItemStack removeSingleItem(CompoundTag bundleNBT) {
        ItemStack curr = getItem(bundleNBT);
        if(curr.isEmpty()){
            return ItemStack.EMPTY;
        }
        
        bundleNBT.putInt("count", curr.getCount() - 1);
        curr.setCount(1);
        return curr;
    }
    
    @Override
    public int addItem(CompoundTag bundleNBT, ItemStack itemStack) {
        ItemStack curr = getItem(bundleNBT);
        if(curr.isEmpty()){
            int fillWith = Math.min(itemStack.getCount(), getMaxCapacity(bundleNBT));
            bundleNBT.putString("item", Registry.ITEM.getKey(itemStack.getItem()).toString());
            bundleNBT.putInt("count", fillWith);
            bundleNBT.put("data", itemStack.getOrCreateTag().copy());
            return fillWith;
        }
        
        itemStack.getOrCreateTag(); //Makes sure the tag is present
        
        if(ItemStack.isSameItemSameTags(curr, itemStack)){
            int fillWith = Math.min(itemStack.getCount(), getMaxCapacity(bundleNBT) - curr.getCount());
            bundleNBT.putInt("count", curr.getCount() + fillWith);
            return fillWith;
        }
        
        return 0;
    }
    
    @Override
    public ItemStack removeFirst(CompoundTag bundleNBT, Predicate<ItemStack> condition) {
        ItemStack curr = getItem(bundleNBT);
        
        int amount = curr.getCount();
        
        if(amount >= curr.getMaxStackSize()){
            curr.setCount(curr.getMaxStackSize());
            if(condition.test(curr)){
                bundleNBT.putInt("count", amount - curr.getMaxStackSize());
                return curr;
            }
        }
        
        int residual = amount % curr.getMaxStackSize();
        if(residual > 0){
            curr.setCount(residual);
            if(condition.test(curr)){
                bundleNBT.putInt("count", amount - residual);
                return curr;
            }
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public List<ItemStack> getAllItems(CompoundTag bundleNBT){
        ItemStack curr = getItem(bundleNBT);
        if(curr.isEmpty()){
            return List.of();
        }
        
        int fullStacks = curr.getCount() / curr.getMaxStackSize();
        int residual = curr.getCount() % curr.getMaxStackSize();
        
        int totalSize = fullStacks + (residual > 0 ? 1 : 0);
        List<ItemStack> items = new ArrayList<>(totalSize);
    
        curr.setCount(curr.getMaxStackSize());
        for(int i = 0; i < fullStacks; i++) {
            items.add(curr.copy());
        }
        
        if(residual > 0){
            curr.setCount(residual);
            items.add(curr);
        }
        
        return items;
    }
    
    @Override
    public void clear(CompoundTag tag) {
        tag.putInt("count", 0);
        tag.put("data", new CompoundTag());
    }
    
    @Override
    public int getNumItems(CompoundTag bundleNBT) {
        ItemStack curr = getItem(bundleNBT);
        if(curr.isEmpty()){
            return 0;
        }
    
        int fullStacks = curr.getCount() / curr.getMaxStackSize();
        int residual = curr.getCount() % curr.getMaxStackSize();
    
        return fullStacks + (residual > 0 ? 1 : 0);
    }
    
    @Override
    public ItemStack getStack(CompoundTag tag, int index) {
        ItemStack curr = getItem(tag);
        if(curr.isEmpty()){
            return ItemStack.EMPTY;
        }
        
        int fullStacks = curr.getCount() / curr.getMaxStackSize();
        if(index < fullStacks){
            curr.setCount(curr.getMaxStackSize());
            return curr;
        }else if(index == fullStacks){
            int residual = curr.getCount() % curr.getMaxStackSize();
            curr.setCount(residual);
            return curr;
        }else{
            return ItemStack.EMPTY;
        }
    }
    
    @Override
    public void setStack(CompoundTag tag, int index, ItemStack stack) {
    
    }
    
    @Override
    public ItemStack removeStack(CompoundTag tag, int index) {
        ItemStack curr = getItem(tag);
        if(curr.isEmpty()){
            return ItemStack.EMPTY;
        }
        
        int fullStacks = curr.getCount() / curr.getMaxStackSize();
        int amount = curr.getCount();
        if(index < fullStacks){
            curr.setCount(curr.getMaxStackSize());
            tag.putInt("count", amount - curr.getMaxStackSize());
            return curr;
        }else if(index == fullStacks){
            int residual = curr.getCount() % curr.getMaxStackSize();
            curr.setCount(residual);
            tag.putInt("count", amount - residual);
            return curr;
        }else{
            return ItemStack.EMPTY;
        }
    }
    
    @Override
    public ItemStack removeStack(CompoundTag tag, int index, int removeAmount) {
        ItemStack curr = getItem(tag);
        if(curr.isEmpty()){
            return ItemStack.EMPTY;
        }
        
        int fullStacks = curr.getCount() / curr.getMaxStackSize();
        int amount = curr.getCount();
        if(index < fullStacks){
            int remove = Math.min(removeAmount, curr.getMaxStackSize());
            curr.setCount(remove);
            tag.putInt("count", amount - remove);
            return curr;
        }else if(index == fullStacks){
            int residual = curr.getCount() % curr.getMaxStackSize();
            int remove = Math.min(removeAmount, residual);
            curr.setCount(remove);
            tag.putInt("count", amount - remove);
            return curr;
        }else{
            return ItemStack.EMPTY;
        }
    }
    
    @Override
    public boolean canAdd(CompoundTag tag, ItemStack stack) {
        ItemStack curr = getItem(tag);
        if(curr.isEmpty()){
            return true;
        }
        
        stack.getOrCreateTag(); //Makes sure the tag is present
        
        if(ItemStack.isSameItemSameTags(curr, stack)){
            return curr.getCount() != getMaxCapacity(tag);
        }
        
        return false;
    }
    
    private ItemStack getItem(CompoundTag nbt){
        String id;
        CompoundTag data;
        int count;
        
        if(!nbt.contains("item")){
            nbt.putString("item", "minecraft:air");
            id = "minecraft:air";
        }else{
            id = nbt.getString("item");
        }
        
        if(!nbt.contains("count")){
            nbt.putInt("count", 0);
            count = 0;
        }else{
            count = nbt.getInt("count");
        }
        
        if(!nbt.contains("data")){
            nbt.put("data", new CompoundTag());
        }
        data = nbt.getCompound("data");
    
        Item item = Registry.ITEM.get(new ResourceLocation(id));
        ItemStack stack = new ItemStack(item, count);
        stack.setTag(data);
        return stack;
    }
}
