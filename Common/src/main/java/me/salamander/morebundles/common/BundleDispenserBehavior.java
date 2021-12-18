package me.salamander.morebundles.common;

import me.salamander.morebundles.common.items.BundleHandler;
import me.salamander.morebundles.common.items.MoreBundlesInfo;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;

public class BundleDispenserBehavior extends DefaultDispenseItemBehavior {
    public static final BundleDispenserBehavior INSTANCE = new BundleDispenserBehavior();
    
    protected BundleDispenserBehavior(){}
    
    @Override
    public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        BundleHandler handler = ((MoreBundlesInfo) itemStack.getItem()).getHandler();
        ItemStack itemInBundle = handler.removeSingleItem(itemStack.getOrCreateTag());
        if (itemInBundle.isEmpty()) {
            return super.execute(blockSource, itemStack);
        }else{
            super.execute(blockSource, itemInBundle);
            return itemStack;
        }
    }
    
}
