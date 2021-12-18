package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.Common;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

@Mixin(SimpleReloadableResourceManager.class)
public abstract class MixinSimpleReloadableResourceManager {
    @Shadow
    public abstract void add(PackResources packResources);
    
    @Inject(method = "createReload", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", shift = At.Shift.BEFORE))
    private void addCustomPack(Executor executor1, Executor executor2, CompletableFuture<Unit> initialStage, List<PackResources> resources, CallbackInfoReturnable<ReloadInstance> cir){
        System.out.println("[MoreBundles] Adding custom pack");
        add(Common.RESOURCE_PACK);
    }
}
