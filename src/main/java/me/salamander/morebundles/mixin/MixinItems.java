package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.items.DyeableBundleItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(Items.class)
public class MixinItems {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Items;createEmptyOptional(Ljava/lang/Object;)Ljava/util/Optional;", ordinal = 1))
    private static Optional createActualOptional(Object value){
        return Optional.of((ItemGroup) value);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "Lnet/minecraft/item/BundleItem;", ordinal = 0))
    private static BundleItem createColoredBundle(Item.Settings settings){
        return new DyeableBundleItem(settings);
    }
}
