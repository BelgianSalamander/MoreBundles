package me.salamander.betterbundles.mixin;

import me.salamander.betterbundles.common.enchantment.CustomEnchantmentTarget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Mixin(EnchantmentTarget.class)
public class MixinEnchantmentTarget {
    @SuppressWarnings("ShadowTarget")
    @Shadow @Mutable private static @Final EnchantmentTarget[] field_9077;

    @Invoker("<init>")
    private static EnchantmentTarget newEnchantmentTarget(String internalName, int internalID){
        throw new AssertionError();
    }

    private static EnchantmentTarget createTarget(String internalName, int internalID, Predicate<Item> isAcceptable){
        EnchantmentTarget target = newEnchantmentTarget(internalName, internalID);

        CustomEnchantmentTarget.addTarget(target, isAcceptable);

        return target;
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "<clinit>", at = @At(value = "FIELD", opcode = Opcodes.PUTSTATIC,
            target = "Lnet/minecraft/enchantment/EnchantmentTarget;field_9077:[Lnet/minecraft/enchantment/EnchantmentTarget;", shift = At.Shift.AFTER))
    private static void addCustomVariant(CallbackInfo ci){
        List<EnchantmentTarget> variants = new ArrayList<>(Arrays.asList(field_9077));
        EnchantmentTarget last = variants.get(variants.size() - 1);

        EnchantmentTarget bundle = createTarget("BUNDLE", last.ordinal() + 1, (item) -> item instanceof BundleItem);
        CustomEnchantmentTarget.BUNDLE = bundle;
        variants.add(bundle);

        field_9077 = variants.toArray(new EnchantmentTarget[0]);
    }
}
