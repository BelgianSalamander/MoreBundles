package me.salamander.morebundles.mixin.client;

import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.gen.BuiltinBundleInfo;
import me.salamander.morebundles.common.items.MoreBundlesItems;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemColors.class)
public class MixinItemColors {
    @Inject(method = "createDefault", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onCreateDefault(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir, ItemColors colors) {
        Item[] dyeableItems;
        BuiltinBundleInfo large = Common.CONFIG.getBuiltin("large");
        if(large.enabled()){
            dyeableItems = new Item[] {Items.BUNDLE, MoreBundlesItems.LARGE_BUNDLE};
        }else {
            dyeableItems = new Item[] {Items.BUNDLE};
        }
        
        colors.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((DyeableLeatherItem) stack.getItem()).getColor(stack), dyeableItems);
    }
}
