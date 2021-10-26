package me.salamander.morebundles.common.items;

import me.salamander.morebundles.common.ExtraBundleInfo;
import me.salamander.morebundles.mixin.MixinBundleItem;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Implements;

import java.util.Optional;
import java.util.function.Predicate;

public class SingleItemBundle extends BundleItem {
    public SingleItemBundle(Item.Settings settings, int defaultCapacity) {
        super(settings);
        ((ExtraBundleInfo.Access) this).getExtraBundleInfo().setDefaultMaxStorage(defaultCapacity);
    }

    public static Optional<ItemStack> removeSingleStackIf(ItemStack bundle, Predicate<ItemStack> condition) {
        ItemStack item = getItem(bundle);

        if(bundle == ItemStack.EMPTY) return Optional.empty();

        item.setCount(Math.min(item.getCount(), item.getItem().getMaxCount()));

        if(condition.test(item)){
            NbtCompound itemInfo = bundle.getOrCreateNbt().getCompound("item");
            itemInfo.putInt("Count", itemInfo.getInt("Count") - item.getCount());

            return Optional.of(item);
        }

        return Optional.empty();
    }

    @Override
    public void postProcessNbt(NbtCompound nbt) {
        super.postProcessNbt(nbt);

        if(!nbt.contains("item")) {
            NbtCompound itemData = new NbtCompound();
            ItemStack.EMPTY.writeNbt(itemData);

            nbt.put("item", itemData);
        }
    }

    public static ItemStack getItem(ItemStack bundle){
        NbtCompound nbt = bundle.getOrCreateNbt();
        if(nbt.contains("item")){
            NbtCompound itemInfo = nbt.getCompound("item");
            Item item = Registry.ITEM.get(new Identifier(itemInfo.getString("id")));
            int count = itemInfo.getInt("Count");
            NbtCompound tag = itemInfo.getCompound("tag");

            ItemStack stack = new ItemStack(item, count);
            if(stack.isEmpty()){
                return ItemStack.EMPTY;
            }
            if(tag.getSize() != 0) {
                stack.setNbt(tag);
            }
            return stack;
        }else{
            return ItemStack.EMPTY;
        }
    }

    public static int getSingleItemBundleOccupancy(ItemStack bundle) {
        ItemStack stack = getItem(bundle);

        return getItemOccupancy(stack) * stack.getCount();
    }

    public static int addToBundle(ItemStack bundle, ItemStack stack){
        if(!stack.isEmpty() && stack.getItem().canBeNested()){
            ItemStack currentItem = getItem(bundle);

            if(currentItem != ItemStack.EMPTY) {
                if (!ItemStack.canCombine(stack, currentItem)) return 0;
            }

            int spaceLeft = BundleUtil.getMaxStorage(bundle) - getSingleItemBundleOccupancy(bundle);
            int spaceNeeded = getItemOccupancy(stack);

            int amountThatCanBeAdded = Math.min(stack.getCount(), spaceLeft / spaceNeeded);

            if(amountThatCanBeAdded != 0){
                NbtCompound bundleNbt = bundle.getOrCreateNbt();

                if(currentItem == ItemStack.EMPTY){
                    NbtCompound itemInfo = new NbtCompound();

                    itemInfo.putInt("Count", amountThatCanBeAdded);
                    itemInfo.putString("id", Registry.ITEM.getId(stack.getItem()).toString());
                    itemInfo.put("tag", stack.getOrCreateNbt().copy());

                    bundleNbt.put("item", itemInfo);
                }else{
                    NbtCompound itemInfo = bundleNbt.getCompound("item");

                    itemInfo.putInt("Count", itemInfo.getInt("Count") + amountThatCanBeAdded);
                }

                return amountThatCanBeAdded;
            }
        }

        return 0;
    }

    @Override
    public boolean onStackClicked(ItemStack bundle, Slot slot, ClickType clickType, PlayerEntity player) {
        if(clickType == ClickType.RIGHT){
            ItemStack stackInSlot = slot.getStack();
            if(stackInSlot.isEmpty()){
                removeFirstStack(bundle).ifPresent(slot::insertStack);
            }else{
                stackInSlot.decrement(addToBundle(bundle, stackInSlot));
            }
            return true;
        }

        return false;
    }

    public static Optional<ItemStack> removeFirstStack(ItemStack bundle){
        ItemStack item = getItem(bundle);
        if(item == ItemStack.EMPTY){
            return Optional.empty();
        }else{
            int amountTaken = Math.min(item.getCount(), item.getItem().getMaxCount());
            NbtCompound itemInfo = bundle.getOrCreateNbt().getCompound("item");
            itemInfo.putInt("Count", item.getCount() - amountTaken);

            item.setCount(amountTaken);

            return Optional.of(item);
        }
    }

    public static boolean dropAllItems(ItemStack bundle, PlayerEntity player){
        ItemStack stack = getItem(bundle);

        if(stack == ItemStack.EMPTY) return false;

        int amountFullStacks = stack.getCount() / stack.getItem().getMaxCount();
        int residual = stack.getCount() % stack.getItem().getMaxCount();

        if(player instanceof ServerPlayerEntity) {
            for (int i = 0; i < amountFullStacks; i++) {
                ItemStack fullStack = stack.copy();
                fullStack.setCount(stack.getItem().getMaxCount());
                player.dropItem(fullStack, true);
            }

            if(residual > 0){
                stack.setCount(residual);
                player.dropItem(stack, true);
            }

            NbtCompound itemInfo = bundle.getOrCreateNbt().getCompound("item");
            itemInfo.putInt("Count", 0);
        }

        return true;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack bundle) {
        if(BundleUtil.shouldHideContents(bundle)){
            return Optional.empty();
        }

        DefaultedList<ItemStack> items = DefaultedList.of();
        ItemStack item = getItem(bundle);
        if(item != ItemStack.EMPTY){
            items.add(item);
        }

        return Optional.of(new BundleTooltipData(items, getSingleItemBundleOccupancy(bundle)));
    }
}
