package me.salamander.morebundles.common.items;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;

public class BundleDispenserBehaviour extends ItemDispenserBehavior {
    public static BundleDispenserBehaviour INSTANCE = new BundleDispenserBehaviour();

    protected BundleDispenserBehaviour(){}

    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        ItemStack itemInBundle = BundleUtil.removeSingleItem(stack);

        if(itemInBundle.isEmpty()){
            return super.dispenseSilently(pointer, stack);
        }else{
            super.dispenseSilently(pointer, itemInBundle);
            return stack;
        }
    }
}
