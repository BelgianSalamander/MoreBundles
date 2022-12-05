package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.items.DyeableBundleItem;
import me.salamander.morebundles.common.items.MoreBundlesInfo;
import me.salamander.morebundles.common.items.handlers.DefaultBundleHandler;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(Items.class)
public class MixinItems {
    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "Lnet/minecraft/world/item/BundleItem;", ordinal = 0))
    private static BundleItem createColoredBundle(Item.Properties settings){
        DyeableBundleItem bundle = new DyeableBundleItem(settings);
        ((MoreBundlesInfo) bundle).setHandler(
                new DefaultBundleHandler(Common.getConfig().regularBundleCapacity(), false)
        );
        
        return bundle;
    }
}
