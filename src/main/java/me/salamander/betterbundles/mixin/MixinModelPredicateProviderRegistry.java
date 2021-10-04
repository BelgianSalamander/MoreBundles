package me.salamander.betterbundles.mixin;

import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPredicateProviderRegistry.class)
public abstract class MixinModelPredicateProviderRegistry {
    @Shadow
    private static UnclampedModelPredicateProvider register(Identifier id, UnclampedModelPredicateProvider provider) {
        return null;
    }

    @Inject(method = "register(Lnet/minecraft/item/Item;Lnet/minecraft/util/Identifier;Lnet/minecraft/client/item/UnclampedModelPredicateProvider;)V", at = @At("HEAD"), cancellable = true)
    private static void registerGlobally(Item item, Identifier id, UnclampedModelPredicateProvider provider, CallbackInfo ci){
        if(item == Items.BUNDLE){
            register(id, provider);
            ci.cancel();
        }
    }
}
