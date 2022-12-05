package me.salamander.morebundles.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.salamander.morebundles.common.items.ItemWithLoot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(LootTable.class)
public class MixinLootTable {
    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void processItemsWithLoot(LootContext $$0, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir, ObjectArrayList<ItemStack> createdItems) {
        createdItems.forEach(itemStack -> {
            if (itemStack.getItem() instanceof ItemWithLoot lootItem){
                lootItem.generateLoot(itemStack, $$0);
            }
        });
    }
}
