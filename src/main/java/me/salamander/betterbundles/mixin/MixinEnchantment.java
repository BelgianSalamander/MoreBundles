package me.salamander.betterbundles.mixin;

import me.salamander.betterbundles.common.enchantment.CustomEnchantmentTarget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Enchantment.class)
public class MixinEnchantment {
    @Shadow @Final public EnchantmentTarget type;

    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    private void checkCustomEnchantmentTargets(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        Predicate<Item> check = CustomEnchantmentTarget.getPredicate(type);

        if(check != null){
            cir.setReturnValue(check.test(stack.getItem()));
        }
    }
}
