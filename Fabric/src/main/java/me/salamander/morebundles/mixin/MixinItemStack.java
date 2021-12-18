package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.items.MoreBundlesInfo;
import me.salamander.morebundles.common.enchantment.MoreBundlesEnchantments;
import me.salamander.morebundles.util.MBUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(
        value = ItemStack.class,
        priority = 500
)
public class MixinItemStack {
    @Inject(method = "useOn", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void tryExtract(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir, Player player, BlockPos blockPos, BlockInWorld blockInWorld, Item item, InteractionResult result){
        if(result.consumesAction()){
            if(player.getItemInHand(useOnContext.getHand()).isEmpty()){
                System.out.println("Emptied!");
                
                ItemStack retrieved = ItemStack.EMPTY;
                for(ItemStack bundle: MBUtil.iterate(player.getInventory().items, player.getInventory().offhand)){
                    if(!bundle.isEmpty() && bundle.getItem() instanceof MoreBundlesInfo info && EnchantmentHelper.getItemEnchantmentLevel(MoreBundlesEnchantments.EXTRACT, bundle) > 0){
                        retrieved = info.getHandler().removeFirst(bundle.getOrCreateTag(), (stack) -> stack.getItem() == item);
                        if(!retrieved.isEmpty()){
                            break;
                        }
                    }
                }
                
                if(!retrieved.isEmpty()){
                    player.setItemInHand(useOnContext.getHand(), retrieved);
                }
            }
        }
    }
}
