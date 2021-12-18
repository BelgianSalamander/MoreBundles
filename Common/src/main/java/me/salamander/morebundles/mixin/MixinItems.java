package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.items.DyeableBundleItem;
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
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Items;ifPart2(Ljava/lang/Object;)Ljava/util/Optional;", ordinal = 1))
    private static Optional createActual(Object obj) {
        return Optional.of(CreativeModeTab.TAB_TOOLS);
    }
    
    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "Lnet/minecraft/world/item/BundleItem;", ordinal = 0))
    private static BundleItem createColoredBundle(Item.Properties settings){
        return new DyeableBundleItem(settings);
    }
}
