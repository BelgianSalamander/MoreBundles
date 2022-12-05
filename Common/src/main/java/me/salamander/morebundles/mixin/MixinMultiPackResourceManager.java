package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.Common;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

@Mixin(MultiPackResourceManager.class)
public abstract class MixinMultiPackResourceManager {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), index = 2, argsOnly = true)
    private static List<PackResources> addCustomPack(List<PackResources> value){
        System.out.println("[MoreBundles] Adding custom pack");
        List<PackResources> list = new ArrayList<>(value);
        list.add(Common.RESOURCE_PACK);
        return list;
    }
}
