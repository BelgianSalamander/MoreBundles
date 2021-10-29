package me.salamander.morebundles.common.items;

import me.salamander.morebundles.common.ExtraBundleInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BreadBowlItem extends BundleItem {
    public static final FoodComponent FOOD_COMPONENT = new FoodComponent.Builder().alwaysEdible().hunger(10).saturationModifier(1.2f).build();

    public BreadBowlItem(Settings settings, int capacity) {
        super(settings);

        ((ExtraBundleInfo.Access) this).getExtraBundleInfo().setDefaultMaxStorage(capacity);
        ((ExtraBundleInfo.Access) this).setShouldHideContents(true);
    }

    @Override
    public void postProcessNbt(NbtCompound nbt) {
        super.postProcessNbt(nbt);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if(clickType == ClickType.RIGHT){
            ItemStack itemStack = slot.getStack();
            if(!itemStack.isEmpty() && itemStack.getItem().canBeNested()){
                addToBundle(stack, slot.takeStackRange(itemStack.getCount(), (64 - getBundleOccupancy(stack)) / getItemOccupancy(itemStack), player));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if(slot.canTakePartial(player) && clickType == ClickType.RIGHT && !otherStack.isEmpty()){
            otherStack.decrement(addToBundle(stack, otherStack));
            return true;
        }
        return false;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if(stack.getOrCreateNbt().contains("Items")) {
            Vec3d pos = user.getEyePos();
            getBundledStacks(stack).forEach((item) -> {
                ItemScatterer.spawn(world, pos.x, pos.y, pos.z, item);
            });
            stack.removeSubNbt("Items");
        }
        return user.eatFood(world, stack);
    }
}
