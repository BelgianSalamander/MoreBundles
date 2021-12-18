package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.BundleDispenserBehavior;
import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.items.MoreBundlesInfo;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlock.class)
public class MixinDispenserBlock {
    @Inject(method = "getDispenseMethod", at = @At("HEAD"), cancellable = true)
    private void dispenseBundledItems(ItemStack stack, CallbackInfoReturnable<DispenseItemBehavior> cir){
        if(Common.CONFIG.dispenseBundledItems() && stack.getItem() instanceof MoreBundlesInfo){
            cir.setReturnValue(BundleDispenserBehavior.INSTANCE);
        }
    }
}
