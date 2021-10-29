package me.salamander.morebundles.common.block;

import me.salamander.morebundles.MoreBundles;
import me.salamander.morebundles.common.items.BundleDispenserBehaviour;
import me.salamander.morebundles.common.items.BundleUtil;
import me.salamander.morebundles.common.items.SingleItemBundle;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BundleLoaderBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
    public static final BlockEntityType<BundleLoaderBlockEntity> TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, MoreBundles.ID("bundle_loader"), FabricBlockEntityTypeBuilder.create(BundleLoaderBlockEntity::new, BundleLoaderBlock.BLOCK).build(null));
    public static final ScreenHandlerType<BundleLoaderScreenHandler> SCREEN_HANDLER_TYPE = Registry.register(Registry.SCREEN_HANDLER, MoreBundles.ID("bundle_loader"), new ScreenHandlerType<>(BundleLoaderScreenHandler::new));

    protected static final int BUNDLE_SLOT = 0;

    private @NotNull ItemStack bundle = ItemStack.EMPTY;
    private final Inventory bundleInventory = new BundleInventory();

    private boolean isPowered = false;

    private static final int EXTRACT_COOLDOWN = 4;
    private int extractCooldown = EXTRACT_COOLDOWN;

    public BundleLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        isPowered = state.get(BundleLoaderBlock.POWERED);
    }

    public static void tick(World world1, BlockPos pos, BlockState state1, BundleLoaderBlockEntity be) {
        be.isPowered = state1.get(BundleLoaderBlock.POWERED);

        if(!be.bundle.isEmpty()) {
            if (be.extractCooldown > 0) {
                be.extractCooldown--;
            }
        }else{
            be.extractCooldown = EXTRACT_COOLDOWN;
        }
    }


    @Override
    public int size() {
        return bundleInventory.size() + 1;
    }

    @Override
    public boolean isEmpty() {
        return bundle.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if(slot == BUNDLE_SLOT) return bundle;
        return bundleInventory.getStack(slot - 1);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        extractCooldown = EXTRACT_COOLDOWN;
        if(slot == BUNDLE_SLOT){
            ItemStack copy = bundle.copy();
            bundle.decrement(amount);
            return copy;
        }else{
            return bundleInventory.removeStack(slot - 1, amount);
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        //extractCooldown = EXTRACT_COOLDOWN;
        if(slot == BUNDLE_SLOT) {
            ItemStack copy = bundle.copy();
            bundle.setCount(0);
            return copy;
        }else{
            return bundleInventory.removeStack(slot - 1);
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if(slot == -1) slot = 0;

        if(slot == BUNDLE_SLOT){
            bundle = stack;
        }else{
            bundleInventory.setStack(slot - 1, stack);
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
            if(!isPowered) {
                return new int[]{0};
            }
        }
        int[] available = new int[bundleInventory.size()];
        for (int i = 0; i < available.length; i++) {
            available[i] = i + 1;
        }
        return available;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if(slot == -1) slot = 0;

        if(dir == null){
            if(bundle.isEmpty() && stack.getItem() instanceof BundleItem){
                dir = Direction.UP;
            }else{
                dir = Direction.SOUTH;
            }
        }

        boolean isVertical = dir.getAxis() == Direction.Axis.Y;

        if(slot == BUNDLE_SLOT && isVertical){
            return stack.getItem() instanceof BundleItem && bundle.isEmpty();
        }else if(!isVertical){
            if(!bundle.isEmpty()){
                int bundleCapacity = BundleUtil.getMaxStorage(bundle);
                int bundleOccupancy = BundleItem.getBundleOccupancy(bundle);
                int itemOccupancy = BundleItem.getItemOccupancy(stack);

                return bundleOccupancy + itemOccupancy <= bundleCapacity && slot == bundleInventory.size();
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if(slot == -1) slot = 0;

        if(slot == 0){
            return stack.getItem() instanceof BundleItem;
        }
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if(extractCooldown == 0) {
            if (!isPowered) {
                return slot == BUNDLE_SLOT && !bundle.isEmpty();
            } else {
                return slot != BUNDLE_SLOT && !bundle.isEmpty();
            }
        }
        return false;
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Bundle Loader");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BundleLoaderScreenHandler(syncId, inv, this);
    }

    /*
    If there is a single item - return 1, if it is completely full return 15
     */
    public int comparatorOutput() {
        if(bundle.isEmpty()) return 0;
        //It just works
        return (int) (1 + (14 * ((BundleItem.getBundleOccupancy(bundle) - 1) / (float) (BundleUtil.getMaxStorage(bundle) - 1))));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        bundle = ItemStack.fromNbt(nbt.getCompound("bundle"));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.put("bundle", bundle.writeNbt(new NbtCompound()));

        return nbt;
    }



    protected class BundleInventory implements Inventory{
        @Override
        public int size() {
            if(!bundle.isEmpty()){
                if(bundle.getItem() instanceof SingleItemBundle){
                    return 2;
                }else{
                    NbtCompound nbt = bundle.getOrCreateNbt();

                    if(!nbt.contains("Items")){
                        nbt.put("Items", new NbtList());
                    }

                    NbtList items = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
                    return items.size() + 1;
                }
            }else{
                return 0;
            }
        }

        @Override
        public boolean isEmpty() {
            if(bundle.isEmpty()) return true;
            return BundleItem.getBundleOccupancy(bundle) == 0;
        }

        @Override
        public ItemStack getStack(int slot) {
            if(bundle.isEmpty()) return ItemStack.EMPTY;

            if(bundle.getItem() instanceof SingleItemBundle){
                if(slot == 0) {
                    return SingleItemBundle.getItem(bundle);
                }else{
                    return ItemStack.EMPTY;
                }
            }else{
                NbtCompound nbt = bundle.getOrCreateNbt();

                if(!nbt.contains("Items")){
                    nbt.put("Items", new NbtList());
                }

                NbtList items = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
                return ItemStack.fromNbt(items.getCompound(slot));
            }
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            if(bundle.isEmpty()) return ItemStack.EMPTY;

            if(bundle.getItem() instanceof SingleItemBundle){
                ItemStack item = SingleItemBundle.getItem(bundle);
                if(item.isEmpty()) return ItemStack.EMPTY;

                SingleItemBundle.setCount(bundle, item.getCount() - amount);

                item.setCount(amount);
                return item;
            }else{
                NbtCompound nbt = bundle.getOrCreateNbt();

                if(!nbt.contains("Items")){
                    nbt.put("Items", new NbtList());
                }

                NbtList items = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
                NbtCompound itemNbt = items.getCompound(slot);

                itemNbt.putInt("Count", itemNbt.getInt("Count") - amount);
                ItemStack item = ItemStack.fromNbt(itemNbt);
                if(item.isEmpty()) items.remove(slot);
                item.setCount(amount);
                return item;
            }
        }

        @Override
        public ItemStack removeStack(int slot) {
            if(bundle.isEmpty()) return ItemStack.EMPTY;

            if(bundle.getItem() instanceof SingleItemBundle){
                ItemStack item = SingleItemBundle.getItem(bundle);
                if(item.isEmpty()) return ItemStack.EMPTY;

                SingleItemBundle.setCount(bundle, 0);

                return item;
            }else{
                NbtCompound nbt = bundle.getOrCreateNbt();

                if(!nbt.contains("Items")){
                    nbt.put("Items", new NbtList());
                }

                NbtList items = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
                NbtCompound itemNbt = items.getCompound(slot);

                ItemStack item = ItemStack.fromNbt(itemNbt);
                items.remove(slot);
                return item;
            }
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if(bundle.isEmpty()) return;

            int size = size();

            if(slot == size - 1){
                BundleItem.addToBundle(bundle, stack);
                return;
            }

            if(bundle.getItem() instanceof SingleItemBundle){
                SingleItemBundle.setItem(bundle, stack);
            }else{
                NbtList items = bundle.getOrCreateNbt().getList("Items", NbtElement.COMPOUND_TYPE);
                if(items.size() <= slot) return;
                items.set(slot, stack.writeNbt(new NbtCompound()));
            }
        }

        @Override
        public void markDirty() {

        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return true;
        }

        @Override
        public void clear() {
            if(bundle.isEmpty()) return;

            if(bundle.getItem() instanceof SingleItemBundle){
                SingleItemBundle.setItem(bundle, ItemStack.EMPTY);
            }else{
                bundle.setSubNbt("Items", new NbtList());
            }
        }
    }
}
