package me.salamander.morebundles.common.blockentity;

import me.salamander.morebundles.common.items.BundleHandler;
import me.salamander.morebundles.common.items.MoreBundlesInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class BundleLoaderBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final BlockEntityType<BundleLoaderBlockEntity> TYPE = BlockEntityType.Builder.of(BundleLoaderBlockEntity::new, BundleLoaderBlock.INSTANCE).build(null);
    public static final MenuType<BundleLoaderContainerMenu> MENU_TYPE = new MenuType<>(BundleLoaderContainerMenu::new);
    
    private ItemStack bundle = ItemStack.EMPTY;
    private BundleInventory inventory = BundleInventory.EMPTY;
    
    private static final int EXTRACT_COOLDOWN = 0;
    private int extractCooldown = EXTRACT_COOLDOWN;
    
    private boolean isPowered;
    
    public BundleLoaderBlockEntity(BlockPos $$1, BlockState $$2) {
        super(TYPE, $$1, $$2);
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, BundleLoaderBlockEntity be) {
        be.isPowered = state.getValue(BundleLoaderBlock.POWERED);
        
        if(!be.bundle.isEmpty()){
            if(be.extractCooldown > 0){
                be.extractCooldown--;
            }
        }else{
            be.extractCooldown = EXTRACT_COOLDOWN;
        }
    }
    
    @Override
    public int[] getSlotsForFace(Direction side) {
        if(side.getAxis() == Direction.Axis.Y){
            if(!isPowered || side == Direction.UP) {
                return new int[]{0};
            }
        }
        int[] available = new int[inventory.getContainerSize()];
        for (int i = 0; i < available.length; i++) {
            available[i] = i + 1;
        }
        return available;
    }
    
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if(slot == -1) slot = 0;
    
        if(direction == null){
            if(bundle.isEmpty() && stack.getItem() instanceof MoreBundlesInfo){
                direction = Direction.UP;
            }else{
                direction = Direction.SOUTH;
            }
        }
    
        boolean isVertical = direction.getAxis() == Direction.Axis.Y;
    
        if(slot == 0 && isVertical){
            return stack.getItem() instanceof MoreBundlesInfo && bundle.isEmpty();
        }else if(!isVertical){
            if(!bundle.isEmpty()){
                return slot == inventory.getContainerSize() && inventory.canAdd(stack);
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    
    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if(extractCooldown == 0) {
            if (!isPowered) {
                return slot == 0 && !bundle.isEmpty();
            } else {
                return slot != 0 && !bundle.isEmpty();
            }
        }
        return false;
    }
    
    @Override
    public boolean canPlaceItem(int $$0, ItemStack $$1) {
        if($$0 == -1) $$0 = 0;
        
        if($$0 == 0){
            return $$1.getItem() instanceof MoreBundlesInfo && bundle.isEmpty();
        }
        
        return true;
    }
    
    @Override
    public int getContainerSize() {
        return 1 + inventory.getContainerSize();
    }
    
    @Override
    public boolean isEmpty() {
        return bundle == null;
    }
    
    @Override
    public ItemStack getItem(int i) {
        if(i == 0){
            return bundle;
        }else{
            return inventory.getItem(i - 1);
        }
    }
    
    @Override
    public ItemStack removeItem(int i, int i1) {
        extractCooldown = EXTRACT_COOLDOWN;
        if(i == 0){
            ItemStack copy = bundle.copy();
            bundle.shrink(i1);
            copy.setCount(i1);
            if(bundle.isEmpty()){
                inventory = BundleInventory.EMPTY;
            }
            return copy;
        }else{
            return inventory.removeItem(i - 1, i1);
        }
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int i) {
        if(i == 0){
            ItemStack copy = bundle.copy();
            bundle = ItemStack.EMPTY;
            inventory = BundleInventory.EMPTY;
            return copy;
        }else{
            return inventory.removeItemNoUpdate(i - 1);
        }
    }
    
    @Override
    public void setItem(int i, ItemStack itemStack) {
        if(i == -1) i =0;
        
        if(i == 0){
            bundle = itemStack;
            if(bundle.getItem() instanceof MoreBundlesInfo info){
                inventory = new BundleInventory(bundle.getOrCreateTag(), info.getHandler());
            }else{
                inventory = BundleInventory.EMPTY;
            }
        }else{
            inventory.setItem(i - 1, itemStack);
        }
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    @Override
    public void clearContent() {
        bundle = ItemStack.EMPTY;
        inventory = BundleInventory.EMPTY;
    }
    
    @Override
    public void load(CompoundTag $$0) {
        super.load($$0);
        
        bundle = ItemStack.of($$0.getCompound("bundle"));
        if(bundle.isEmpty()){
            inventory = BundleInventory.EMPTY;
        }else if(bundle.getItem() instanceof MoreBundlesInfo info){
            inventory = new BundleInventory(bundle.getOrCreateTag(), info.getHandler());
        }else{
            inventory = BundleInventory.EMPTY;
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag $$0) {
        super.saveAdditional($$0);
        
        $$0.put("bundle", bundle.save(new CompoundTag()));
    }
    
    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.morebundles.bundle_loader");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BundleLoaderContainerMenu(i, inventory, this);
    }
    
    /*
    If there is a single item - return 1, if it is completely full return 15
     */
    public int comparatorOutput() {
        if(bundle.isEmpty()) return 0;
        //It just works
        return (int) (1 + (14 * ((inventory.handler.getTotalWeight(inventory.tag) - 1) / (float) (inventory.handler.getMaxCapacity(inventory.tag) - 1))));
    }
    
    public void reloadInventory() {
        if(!bundle.isEmpty() && bundle.getItem() instanceof MoreBundlesInfo info){
            inventory = new BundleInventory(bundle.getOrCreateTag(), info.getHandler());
        }else{
            inventory = BundleInventory.EMPTY;
        }
    }
    
    protected static class BundleInventory implements Container{
        public static final BundleInventory EMPTY = new BundleInventory(null, new BundleHandler() {
            @Override
            protected int getDefaultCapacity() {
                return 0;
            }
    
            @Override
            protected boolean getDefaultConcealed() {
                return false;
            }
    
            @Override
            public ItemStack removeFirstItem(CompoundTag bundleNBT) {
                return ItemStack.EMPTY;
            }
    
            @Override
            public ItemStack removeSingleItem(CompoundTag bundleNBT) {
                return ItemStack.EMPTY;
            }
    
            @Override
            public int addItem(CompoundTag bundleNBT, ItemStack itemStack) {
        
                return 0;
            }
    
            @Override
            public ItemStack removeFirst(CompoundTag bundleNBT, Predicate<ItemStack> condition) {
                return ItemStack.EMPTY;
            }
    
            @Override
            public List<ItemStack> getAllItems(CompoundTag bundleNBT) {
                return List.of();
            }
    
            @Override
            public void clear(CompoundTag tag) {
            }
    
            @Override
            public int getNumItems(CompoundTag tag) {
                return 0;
            }
    
            @Override
            public ItemStack getStack(CompoundTag tag, int index) {
                return ItemStack.EMPTY;
            }
    
            @Override
            public void setStack(CompoundTag tag, int index, ItemStack stack) {
        
            }
    
            @Override
            public ItemStack removeStack(CompoundTag tag, int index) {
                return ItemStack.EMPTY;
            }
    
            @Override
            public ItemStack removeStack(CompoundTag tag, int index, int amount) {
                return ItemStack.EMPTY;
            }
    
            @Override
            public boolean canAdd(CompoundTag tag, ItemStack stack) {
                return false;
            }
        });
        
        private final CompoundTag tag;
        private final BundleHandler handler;
        
        public BundleInventory(CompoundTag tag, BundleHandler handler) {
            this.tag = tag;
            this.handler = handler;
        }
    
        @Override
        public int getContainerSize() {
            return handler.getNumItems(tag) + 1;
        }
    
        @Override
        public boolean isEmpty() {
            return handler.getNumItems(tag) == 0;
        }
    
        @Override
        public ItemStack getItem(int i) {
            return handler.getStack(tag, i);
        }
    
        @Override
        public ItemStack removeItem(int i, int i1) {
            return handler.removeStack(tag, i, i1);
        }
    
        @Override
        public ItemStack removeItemNoUpdate(int i) {
            return handler.removeStack(tag, i);
        }
    
        @Override
        public void setItem(int i, ItemStack itemStack) {
            if(i < handler.getNumItems(tag)) {
                handler.setStack(tag, i, itemStack);
            }else{
                handler.addItem(tag, itemStack);
            }
        }
    
        @Override
        public void setChanged() {
        
        }
    
        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    
        @Override
        public void clearContent() {
            handler.clear(tag);
        }
    
        public boolean canAdd(ItemStack stack) {
            return handler.canAdd(tag, stack);
        }
    
    }
}
