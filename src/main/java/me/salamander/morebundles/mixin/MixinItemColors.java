package me.salamander.morebundles.mixin;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemColors.class)
public class MixinItemColors {

    @Inject(method = "create", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addBundleToItemColors(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir, ItemColors itemColors){
        Item[] dyeableItems;
        if(me.salamander.morebundles.common.items.Items.LARGE_BUNDLE == null){
            dyeableItems = new Item[]{Items.BUNDLE};
        }else{
            dyeableItems = new Item[]{Items.BUNDLE, me.salamander.morebundles.common.items.Items.LARGE_BUNDLE};
        }

        itemColors.register(
                (stack, tintIndex) -> {
                    if(tintIndex > 0) return -1;
                    else{
                        return ((DyeableItem) stack.getItem()).getColor(stack);
                    }
                },
                dyeableItems
        );
    }
}
