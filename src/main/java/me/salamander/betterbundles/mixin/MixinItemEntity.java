package me.salamander.betterbundles.mixin;

import me.salamander.betterbundles.common.ExtraBundleInfo;
import me.salamander.betterbundles.common.enchantment.AbsorbEnchantment;
import me.salamander.betterbundles.common.enchantment.Enchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    public MixinItemEntity(EntityType<?> type, World world) {
        super(type, world);
    }
    @Redirect(method = "onPlayerCollision",  at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean absorbToBundle(PlayerInventory playerInventory, ItemStack stack){
        ItemStack stackCopy = stack.copy();
        for(int i = 0; i < 36; i++){
            ItemStack bundleStack = playerInventory.getStack(i);

            if(bundleStack.getItem() instanceof BundleItem && EnchantmentHelper.getLevel(AbsorbEnchantment.INSTANCE, bundleStack) > 0){
                stackCopy.decrement(BundleItem.addToBundle(bundleStack, stackCopy));

                if(stackCopy.isEmpty()){
                    break;
                }
            }
        }

        stack.setCount(stackCopy.getCount());
        if(!stackCopy.isEmpty()){
            return playerInventory.insertStack(stack);
        }else{
            return true;
        }
    }

    /*@Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void absorbToBundle(PlayerEntity player, CallbackInfo ci, ItemStack stack, Item item, int k){
        ItemStack stackCopy = stack.copy();
        int itemsUsed = 0;
        for(int i = 0; i < 36; i++){
            ItemStack bundleStack = player.getInventory().getStack(i);

            if(bundleStack.getItem() instanceof BundleItem && EnchantmentHelper.getLevel(AbsorbEnchantment.INSTANCE, bundleStack) > 0){
                stackCopy.decrement(itemsUsed += BundleItem.addToBundle(bundleStack, stackCopy));

                if(stackCopy.isEmpty()){
                    break;
                }
            }
        }

        //stack.setCount(stackCopy.getCount());

        if(itemsUsed != 0 && stackCopy.getCount() == 0){
            player.sendPickup(this, k);

            discard();
            stack.setCount(k);

            player.increaseStat(Stats.PICKED_UP.getOrCreateStat(item), k);
            player.triggerItemPickedUpByEntityCriteria((ItemEntity) (Entity) this);
        }
    }*/
}
