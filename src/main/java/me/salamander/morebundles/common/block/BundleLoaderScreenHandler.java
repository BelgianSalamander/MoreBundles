package me.salamander.morebundles.common.block;

import net.fabricmc.fabric.impl.transfer.item.InventoryStorageImpl;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class BundleLoaderScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public BundleLoaderScreenHandler(int syncID, PlayerInventory playerInventory) {
        this(syncID, playerInventory, new SimpleInventory(1));
    }

    public BundleLoaderScreenHandler(int syncID, PlayerInventory playerInventory, Inventory inventory) {
        super(BundleLoaderBlockEntity.SCREEN_HANDLER_TYPE, syncID);

        this.inventory = inventory;

        //Bundle
        this.addSlot(new Slot(inventory, 0, 80, 20));

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
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot != null && slot.getStack().getItem() instanceof BundleItem){
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if(index != 0) {
                if (inventory.getStack(0).isEmpty()) {
                    ItemStack transfer = originalStack.copy();
                    transfer.setCount(1);
                    inventory.setStack(0, transfer);
                    originalStack.decrement(1);
                    slot.markDirty();
                    slots.get(0).markDirty();
                }else{
                    return ItemStack.EMPTY;
                }
            }else{
                if(!this.insertItem(originalStack, 1, slots.size(), true)){
                    return ItemStack.EMPTY;
                }else{
                    slot.markDirty();
                }
            }
        }

        return newStack;
    }
}
