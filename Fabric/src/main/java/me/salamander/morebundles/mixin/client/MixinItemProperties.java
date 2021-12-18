package me.salamander.morebundles.mixin.client;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * By default the "filled" predicate is only applied to {@link Items#BUNDLE}. This makes it applicable to ALL bundle items.
 */
@Mixin(ItemProperties.class)
public abstract class MixinItemProperties {
    @Shadow
    private static ClampedItemPropertyFunction registerGeneric(ResourceLocation resourceLocation, ClampedItemPropertyFunction clampedItemPropertyFunction) {
    
        return null;
    }
    
    @Inject(method = "register(Lnet/minecraft/world/item/Item;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/item/ClampedItemPropertyFunction;)V", at = @At("HEAD"), cancellable = true)
    private static void registerGlobally(Item $$0, ResourceLocation $$1, ClampedItemPropertyFunction $$2, CallbackInfo ci) {
        if($$0 == Items.BUNDLE) {
            registerGeneric($$1, $$2);
            ci.cancel();
        }
    }
}
