package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.items.BundleDispenserBehaviour;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlock.class)
public class MixinDispenserBlock {
    @Inject(method = "getBehaviorForItem", at = @At("HEAD"), cancellable = true)
    private void getBundleBehaviour(ItemStack stack, CallbackInfoReturnable<DispenserBehavior> cir){
        if(stack.getItem() instanceof BundleItem){
            cir.setReturnValue(BundleDispenserBehaviour.INSTANCE);
        }
    }
}
