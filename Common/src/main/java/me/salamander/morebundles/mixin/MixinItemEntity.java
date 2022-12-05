package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.items.MoreBundlesInfo;
import me.salamander.morebundles.common.enchantment.MoreBundlesEnchantments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {
    public MixinItemEntity(EntityType<?> $$0, Level $$1) {super($$0, $$1);}
    
    @Shadow
    private int pickupDelay;
    
    @Shadow
    private UUID owner;
    
    @Inject(method = "playerTouch", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void tryAddToBundles(Player player, CallbackInfo ci, ItemStack stack, Item item, int count) {
        if(!stack.isEmpty() && item.canFitInsideContainerItems() && this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUUID()))){
            Inventory inventory = player.getInventory();
    
            for(ItemStack itemStack : inventory.items){
                if(itemStack.getItem() instanceof MoreBundlesInfo info){
                    if(EnchantmentHelper.getItemEnchantmentLevel(MoreBundlesEnchantments.ABSORB.get(), itemStack) > 0) {
                        stack.shrink(info.getHandler().addItem(itemStack.getOrCreateTag(), stack));
                    }
                }
            }
            
            if(inventory.offhand.get(0).getItem() instanceof MoreBundlesInfo info){
                ItemStack offhand = inventory.offhand.get(0);
                if(EnchantmentHelper.getItemEnchantmentLevel(MoreBundlesEnchantments.ABSORB.get(), offhand) > 0) {
                    stack.shrink(info.getHandler().addItem(offhand.getOrCreateTag(), stack));
                }
            }
            
            if(stack.getCount() != count){
                //We have picked up items
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.getLevel().getRandom().nextFloat() * 0.4f);
            }
            
            if(stack.isEmpty()){
                player.take(this, count);
                
                discard();
                
                player.awardStat(Stats.ITEM_PICKED_UP.get(item), count);
                player.onItemPickup((ItemEntity) (Entity) this);
            }
        }
    }
}
