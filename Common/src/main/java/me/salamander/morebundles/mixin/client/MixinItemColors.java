package me.salamander.morebundles.mixin.client;

import com.mojang.datafixers.util.Pair;
import me.salamander.morebundles.common.items.MoreBundlesItems;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ItemColors.class)
public class MixinItemColors {
    @Inject(method = "createDefault", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onCreateDefault(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir, ItemColors colors) {
        List<Item> items = new ArrayList<>();
        items.add(MoreBundlesItems.BUNDLE.get());
    
        for(Pair<String, Supplier<BundleItem>> customBundle : MoreBundlesItems.getCustomBundles()) {
            //The supplier is memoized, so this is safe
            BundleItem bundle = customBundle.getSecond().get();
            if (bundle instanceof DyeableLeatherItem) {
                items.add(bundle);
            }
        }
        
        Item[] itemArray = items.toArray(new Item[0]);
        
        colors.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((DyeableLeatherItem) stack.getItem()).getColor(stack), itemArray);
    }
}
