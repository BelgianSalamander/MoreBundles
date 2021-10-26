package me.salamander.morebundles.common.block;

import me.salamander.morebundles.MoreBundles;
import me.salamander.morebundles.common.items.BundleDispenserBehaviour;
import me.salamander.morebundles.common.items.BundleUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BundleLoaderBlockEntity extends BlockEntity implements SidedInventory{
    public static final BlockEntityType<BundleLoaderBlockEntity> TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, MoreBundles.ID("bundle_loader"), FabricBlockEntityTypeBuilder.create(BundleLoaderBlockEntity::new, BundleLoaderBlock.BLOCK).build(null));
    protected static final int BUNDLE_SLOT = 0;
    protected static final int INPUT_SLOT = 1;

    protected static final int[] HORIZONTAL = new int[]{INPUT_SLOT};
    protected static final int[] VERTICAL = new int[]{BUNDLE_SLOT};

    private ItemStack bundle = ItemStack.EMPTY;

    public BundleLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return bundle.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if(slot != INPUT_SLOT) return bundle;
        else{
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if(slot == BUNDLE_SLOT){
            bundle.decrement(amount);
            return bundle;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if(slot == BUNDLE_SLOT) {
            bundle.setCount(0);
            return bundle;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        slot = getSlot(slot, stack);

        if(slot == BUNDLE_SLOT){
            bundle = stack;
        }else if(slot == INPUT_SLOT){
            BundleItem.addToBundle(bundle, stack);
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        bundle = ItemStack.EMPTY;
    }


    @Override
    public int[] getAvailableSlots(Direction side) {
        if(side.getAxis() == Direction.Axis.Y){
            return VERTICAL;
        }else{
            return HORIZONTAL;
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if(dir == null){
            if(bundle == null && stack.getItem() instanceof BundleItem){
                dir = Direction.UP;
            }else{
                dir = Direction.SOUTH;
            }
        }

        boolean isVertical = dir.getAxis() == Direction.Axis.Y;

        slot = getSlot(slot, stack);

        if(slot == BUNDLE_SLOT && isVertical){
            return stack.getItem() instanceof BundleItem && bundle.isEmpty();
        }else if(!isVertical){
            if(bundle != null){
                int bundleCapacity = BundleUtil.getMaxStorage(bundle);
                int bundleOccupancy = BundleItem.getBundleOccupancy(bundle);
                int itemOccupancy = BundleItem.getItemOccupancy(stack);

                return bundleOccupancy + itemOccupancy <= bundleCapacity;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == BUNDLE_SLOT && bundle != null;
    }



    /**
     * Takes any integer and returns either BUNDLE_SLOT ot INPUT_SLOT
     * @param slot The given slot
     * @return BUNDLE_SLOT or INPUT_SLOT
     */
    protected int getSlot(int slot, ItemStack stack){
        if(slot == 0) return 0;
        else if(slot == 1) return 1;
        else if(bundle == null && stack.getItem() instanceof BundleItem){
            return BUNDLE_SLOT;
        }else{
            return INPUT_SLOT;
        }
    }
}
