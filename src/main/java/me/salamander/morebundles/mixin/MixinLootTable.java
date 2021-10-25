package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.items.ItemWithLoot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class MixinLootTable {
    @Redirect(method = "generateLoot(Lnet/minecraft/loot/context/LootContext;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V"))
    private void generateExtraLoot(LootTable lootTable, LootContext context, Consumer<ItemStack> lootConsumer){
        lootTable.generateLoot(context, lootConsumer.andThen((stack -> {
            if(stack.getItem() instanceof ItemWithLoot item){
                item.process(stack, context);
            }
        }) ));
    }
}
