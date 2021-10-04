package me.salamander.betterbundles.mixin;

import com.google.gson.JsonElement;
import me.salamander.betterbundles.common.ExtraBundleInfo;
import me.salamander.betterbundles.common.items.ItemWithLoot;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.data.server.LootTablesProvider;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(BundleItem.class)
public abstract class MixinBundleItem extends Item implements ExtraBundleInfo.Access, ItemWithLoot {
    @Shadow
    private static int getBundleOccupancy(ItemStack stack) {
        throw new AssertionError();
    }

    @Shadow
    private static int addToBundle(ItemStack bundle, ItemStack stack) {
        throw new AssertionError();
    }

    @Shadow
    private static int getItemOccupancy(ItemStack stack) {
        return 0;
    }

    private static final String MAX_BUNDLE_STORAGE_KEY = "MaxBundleStorage";
    private static final String CONTENTS_HIDDEN_KEY = "BundleContentsHidden";

    private final ExtraBundleInfo extraBundleInfo = new ExtraBundleInfo();

    private MixinBundleItem(Settings settings) {
        super(settings);
    }

    @Override
    public void postProcessNbt(NbtCompound nbt) {
        if(!nbt.contains(MAX_BUNDLE_STORAGE_KEY)) {
            nbt.putInt(MAX_BUNDLE_STORAGE_KEY, extraBundleInfo.getDefaultMaxStorage());
        }

        if(!nbt.contains(CONTENTS_HIDDEN_KEY)){
            nbt.putBoolean(CONTENTS_HIDDEN_KEY, false);
        }
    }

    private static int getMaxStorage(ItemStack bundle){
        NbtCompound nbt = bundle.getOrCreateNbt();
        if(!nbt.contains(MAX_BUNDLE_STORAGE_KEY)){
            if(bundle.getItem() instanceof ExtraBundleInfo.Access bundleItem){
                nbt.putInt(MAX_BUNDLE_STORAGE_KEY, bundleItem.getExtraBundleInfo().getDefaultMaxStorage());
                return bundleItem.getExtraBundleInfo().getDefaultMaxStorage();
            }else{
                return 0;
            }
        }
        return bundle.getOrCreateNbt().getInt(MAX_BUNDLE_STORAGE_KEY);
    }

    private static boolean shouldHideContents(ItemStack bundle){
        return bundle.getOrCreateNbt().getBoolean(CONTENTS_HIDDEN_KEY);
    }

    @Override
    public ExtraBundleInfo getExtraBundleInfo() {
        return extraBundleInfo;
    }

    @ModifyConstant(method = "getAmountFilled", constant = @Constant(floatValue = 64))
    private static float getAmountFilledOverrideMaxStorage(float value, ItemStack itemStack){
        return getMaxStorage(itemStack);
    }

    @ModifyConstant(method = "addToBundle", constant = @Constant(intValue = 64))
    private static int getAmountFilledOverrideMaxStorage(int value, ItemStack bundle, ItemStack item){
        return getMaxStorage(bundle);
    }

    @ModifyConstant(method = {"onStackClicked", "getItemBarStep", "appendTooltip"}, constant = @Constant(intValue = 64))
    private int onStackClickedOverrideMaxStorage(int value, ItemStack stack){
        return getMaxStorage(stack);
    }

    @Redirect(method = "canMergeStack", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private static Stream mergeAccordingToStackSize(Stream stream, Predicate predicate, ItemStack stack, NbtList items){
        Predicate<NbtCompound> checkOne = (Predicate<NbtCompound>) predicate;
        return stream.filter(checkOne.and((item) -> {
            ItemStack itemStack = ItemStack.fromNbt(item);
            return itemStack.getCount() < itemStack.getItem().getMaxCount();
        }));
    }

    @Redirect(method = "getItemOccupancy", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0))
    private static boolean changeBundleCheckInGetItemOccupancy(ItemStack stack, Item item){
        return stack.getItem() instanceof BundleItem;
    }

    @Inject(method = "addToBundle", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/item/ItemStack;increment(I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void addPartToBundle(ItemStack bundle, ItemStack stack, CallbackInfoReturnable<Integer> cir, NbtCompound nbtCompound, int i, int j, int k, NbtList nbtList, Optional<NbtCompound> optional, NbtCompound nbtCompound2, ItemStack itemStack){
        int currentAmount = itemStack.getCount();
        int over = currentAmount - itemStack.getMaxCount();
        if(over > 0) {
            itemStack.decrement(over);
            ItemStack restStack = itemStack.copy();
            restStack.setCount(over);
            nbtList.add(0, restStack.writeNbt(new NbtCompound()));
        }
    }

    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void hideTooltipDataOptionally(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir){
        if(shouldHideContents(stack)){
            cir.setReturnValue(Optional.empty());
        }
    }


    @Inject(method = "appendTooltip", at = @At("HEAD"))
    private void sayContentsHidden(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci){
        if(shouldHideContents(stack) && (getBundleOccupancy(stack) != 0)){
            tooltip.add((new TranslatableText("item.betterbundles.bundle.hidden")).formatted(Formatting.DARK_PURPLE));
        }
    }

    @Override
    public void process(ItemStack itemStack, LootContext lootContext) {
        NbtCompound nbt = itemStack.getOrCreateNbt();

        String lootTableName = nbt.getString("LootTable");
        if(lootTableName != null){
            long lootTableSeed = nbt.getLong("LootTableSeed");

            LootTable lootTable = lootContext.getWorld().getServer().getLootManager().getTable(new Identifier(lootTableName));
            List<ItemStack> items = lootTable.generateLoot(lootContext);
            lootContext.getRandom().setSeed(lootContext.getRandom().nextLong() + lootTableSeed);

            if(nbt.getBoolean("Fit")){
                int totalSize = 1;
                for(ItemStack stack: items){
                    totalSize += getItemOccupancy(stack) * stack.getCount();
                }

                nbt.putInt(MAX_BUNDLE_STORAGE_KEY, (int) (totalSize * (1 + lootContext.getRandom().nextFloat() * 0.3f)));
            }

            /*if(!nbt.contains("Items")){
                nbt.put("Items", new NbtList());
            }

            NbtList itemList = nbt.getList("Items", NbtElement.COMPOUND_TYPE);

            for(ItemStack item: items){
                itemList.add(item.writeNbt(new NbtCompound()));
            }*/
            for(ItemStack stack: items){
                addToBundle(itemStack, stack);
            }
        }
    }

    @Override
    public NbtList getItems(ItemStack stack) {
        if(stack == null) return null;

        NbtCompound nbt = stack.getOrCreateNbt();
        if(nbt.contains("Items")){
            return nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        }else{
            NbtList list = new NbtList();
            nbt.put("Items", list);

            return list;
        }
    }
}
