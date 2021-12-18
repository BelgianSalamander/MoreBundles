package me.salamander.morebundles.common.blockentity;

import me.salamander.morebundles.common.items.MoreBundlesInfo;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BundleLoaderContainerMenu extends AbstractContainerMenu {
    private final Container container;
    
    public BundleLoaderContainerMenu(int syncID, Inventory playerInventory) {
        this(syncID, playerInventory, new SimpleContainer(1));
    }
    
    protected BundleLoaderContainerMenu(int syncID, Inventory playerInventory, Container container) {
        super(BundleLoaderBlockEntity.MENU_TYPE, syncID);
        
        this.container = container;
    
        //Bundle
        this.addSlot(new Slot(container, 0, 80, 20));
    
        //The player inventory
        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, y * 18 + 51));
            }
        }
    
        for(int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 109));
        }
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if(slot != null && slot.getItem().getItem() instanceof MoreBundlesInfo info) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            
            if(index != 0){
                if(container.getItem(0).isEmpty()){
                    ItemStack transfer = originalStack.copy();
                    transfer.setCount(1);
                    container.setItem(0, transfer);
                    originalStack.shrink(1);
                    slot.setChanged();
                    slots.get(0).setChanged();
                }else{
                    newStack = ItemStack.EMPTY;
                }
            }else{
                if(!this.moveItemStackTo(originalStack, 1, slots.size(), true)){
                    newStack = ItemStack.EMPTY;
                }else{
                    slot.setChanged();
                }
            }
        }
        
        if(container instanceof BundleLoaderBlockEntity be){
            be.reloadInventory();
        }
        
        return newStack;
    }
}
