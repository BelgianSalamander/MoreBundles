package me.salamander.morebundles.mixin;

import me.salamander.morebundles.common.items.BundleHandler;
import me.salamander.morebundles.common.items.ItemWithLoot;
import me.salamander.morebundles.common.items.handlers.DefaultBundleHandler;
import me.salamander.morebundles.common.items.MoreBundlesInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(BundleItem.class)
public abstract class MixinBundleItem extends Item implements MoreBundlesInfo, ItemWithLoot {
    
    @Shadow
    protected abstract void playInsertSound(Entity entity);
    
    private static final BundleHandler DEFAULT_HANDLER = new DefaultBundleHandler(64, false);
    
    private BundleHandler handler = DEFAULT_HANDLER;
    
    public MixinBundleItem(Properties $$0) {
        super($$0);
        throw new AssertionError();
    }
    
    @Inject(method = "getFullnessDisplay", at = @At("HEAD"), cancellable = true)
    private static void useHandlerToGetFullnessDisplay(ItemStack bundleItem, CallbackInfoReturnable<Float> cir){
        if(bundleItem.getItem() instanceof MoreBundlesInfo info){
            cir.setReturnValue(info.getHandler().getFullness(bundleItem.getOrCreateTag()));
        }
    }
    
    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void useHandlerToGetBarWidth(ItemStack bundleItem, CallbackInfoReturnable<Integer> cir){
        if(bundleItem.getItem() instanceof MoreBundlesInfo info){
            cir.setReturnValue(Math.min(1 + (int) (12 * info.getHandler().getFullness(bundleItem.getOrCreateTag())), 13));
        }
    }
    
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private static void useHandlerToAdd(ItemStack bundleItem, ItemStack itemStack, CallbackInfoReturnable<Integer> cir){
        if(bundleItem.getItem() instanceof MoreBundlesInfo info){
            cir.setReturnValue(info.getHandler().addItem(bundleItem.getOrCreateTag(), itemStack));
        }
    }
    
    @Inject(method = "dropContents", at = @At("HEAD"), cancellable = true)
    private static void useHandlerToDrop(ItemStack bundleItem, Player player, CallbackInfoReturnable<Boolean> cir){
        if(bundleItem.getItem() instanceof MoreBundlesInfo info) {
            BundleHandler handler = info.getHandler();
            CompoundTag tag = bundleItem.getOrCreateTag();
            boolean removedAnything = false;
            ItemStack drop;
            while(!(drop = handler.removeSingleItem(tag)).isEmpty()){
                removedAnything = true;
                player.drop(drop, true);
            }
            cir.setReturnValue(removedAnything);
        }
    }
    
    @Inject(method = "getTooltipImage" , at = @At("HEAD"), cancellable = true)
    private void useHandlerToGetTooltipImage(ItemStack bundleItem, CallbackInfoReturnable<Optional<TooltipComponent>> cir){
        if(bundleItem.getItem() instanceof MoreBundlesInfo info){
            if(info.getHandler().isConcealed(bundleItem.getOrCreateTag())){
                cir.setReturnValue(Optional.empty());
            }
        }
    }
    
    @Inject(method = "getContents", at = @At("HEAD"), cancellable = true)
    private static void useHandlerToGetContents(ItemStack bundleItem, CallbackInfoReturnable<Stream<ItemStack>> cir){
        if(bundleItem.getItem() instanceof MoreBundlesInfo info){
            cir.setReturnValue(info.getHandler().getAllItems(bundleItem.getOrCreateTag()).stream());
        }
    }
    
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void conceal(ItemStack stack, Level $$1, List<Component> componentList, TooltipFlag $$3, CallbackInfo ci){
        if(stack.getItem() instanceof MoreBundlesInfo info){
            if(info.getHandler().isConcealed(stack.getOrCreateTag())){
                componentList.add(new TranslatableComponent("item.morebundles.bundle.hidden").withStyle(ChatFormatting.DARK_PURPLE));
                ci.cancel();
            }
        }
    }
    
    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void hideBar(ItemStack bundleItem, CallbackInfoReturnable<Boolean> cir){
        if(bundleItem.getItem() instanceof MoreBundlesInfo info){
            if(info.getHandler().isConcealed(bundleItem.getOrCreateTag())){
                cir.setReturnValue(false);
            }
        }
    }
    
    @Inject(method = "removeOne", at = @At("HEAD"), cancellable = true)
    private static void removeOneFromHandler(ItemStack bundleItem, CallbackInfoReturnable<Optional<ItemStack>> cir){
        if(bundleItem.getItem() instanceof MoreBundlesInfo info){
            ItemStack stack = info.getHandler().removeFirstItem(bundleItem.getOrCreateTag());
            cir.setReturnValue(stack.isEmpty() ? Optional.empty() : Optional.of(stack));
        }
    }
    
    /**
     * By default, this method directly checks the NBT to remove just the right amount and the insert that. Instead, this replacement
     * will always try to add first and change the stack afterwards.
     */
    @Inject(method = "overrideStackedOnOther", at = @At(value = "CONSTANT", args = "intValue=64", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void dontCheckBeforeAdding(ItemStack bundle, Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> cir, ItemStack itemStack){
        if(bundle.getItem() instanceof MoreBundlesInfo info){
            int shrinkBy = info.getHandler().addItem(bundle.getOrCreateTag(), itemStack);
            itemStack.shrink(shrinkBy);
            if(shrinkBy > 0){
                this.playInsertSound(player);
            }
            cir.setReturnValue(true);
        }
    }
    
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void tryEat(Level $$0, Player $$1, InteractionHand $$2, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        if(this.isEdible()){
            cir.setReturnValue(super.use($$0, $$1, $$2));
        }
    }
    
    @Inject(method = "getWeight", at = @At("HEAD"), cancellable = true)
    private static void getWeight(ItemStack stack, CallbackInfoReturnable<Integer> cir){
        if(stack.getItem() instanceof MoreBundlesInfo info){
            cir.setReturnValue(4 + info.getHandler().getTotalWeight(stack.getOrCreateTag()));
        }
    }
    
    @ModifyConstant(method = {"overrideStackedOnOther", "appendHoverText"}, constant = @Constant(intValue = 64))
    private static int modifyMaxStackSize(int original, ItemStack bundle) {
        if(bundle.getItem() instanceof MoreBundlesInfo info){
            return info.getHandler().getMaxCapacity(bundle.getOrCreateTag());
        }
        return original;
    }
    
    @Override
    public boolean isEnchantable(ItemStack $$0) {
        return true;
    }
    
    @Override
    public int getEnchantmentValue() {
        return 9;
    }
    
    @Override
    public BundleHandler getHandler() {
        if(handler == DEFAULT_HANDLER){
            System.err.println("WARNING: Using default bundle handler for " + this.getClass().getName());
            //Print stack trace
            new Throwable().printStackTrace();
        }
        return handler;
    }
    
    @Override
    public void setHandler(BundleHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public void generateLoot(ItemStack itemStack, LootContext lootContext) {
        CompoundTag nbt = itemStack.getOrCreateTag();
    
        String lootTableName = nbt.getString("LootTable");
        if(!lootTableName.equals("")){
            long lootTableSeed = nbt.getLong("LootTableSeed");
        
            LootTable lootTable = lootContext.getLevel().getServer().getLootTables().get(new ResourceLocation(lootTableName));
            List<ItemStack> items = lootTable.getRandomItems(lootContext);
            lootContext.getRandom().setSeed(lootContext.getRandom().nextLong() + lootTableSeed);
        
            if(nbt.getBoolean("Fit")){
                int totalSize = 1;
                for(ItemStack stack: items){
                    totalSize += BundleItem.getWeight(stack) * stack.getCount();
                }
            
                nbt.putInt(BundleHandler.CAPACITY_KEY, (int) (totalSize * (1 + lootContext.getRandom().nextFloat() * 0.3f)));
            }
            
            BundleHandler handler = this.getHandler();
            if(handler != null) {
                for(ItemStack stack : items) {
                    handler.addItem(nbt, stack);
                }
            }
        }
    }
    
}
