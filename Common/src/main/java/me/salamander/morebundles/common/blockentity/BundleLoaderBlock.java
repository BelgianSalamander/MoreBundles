package me.salamander.morebundles.common.blockentity;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BundleLoaderBlock extends BaseEntityBlock {
    public static final Supplier<Block> INSTANCE = Suppliers.memoize(
            () -> new BundleLoaderBlock(Properties.of(Material.WOOD).strength(4.0f))
    );
    
    protected static final VoxelShape INSIDE_SHAPE;
    private static final VoxelShape TOP_SHAPE;
    private static final VoxelShape BOTTOM_SHAPE;
    private static final VoxelShape DEFAULT_SHAPE;
    
    public static BooleanProperty POWERED;
    
    protected BundleLoaderBlock(Properties settings) {
        
        super(settings);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(!level.isClientSide){
            MenuProvider menuProvider = state.getMenuProvider(level, pos);
            
            if(menuProvider != null) {
                player.openMenu(menuProvider);
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BundleLoaderBlockEntity(blockPos, blockState);
    }
    
    @Override
    public VoxelShape getInteractionShape(BlockState $$0, BlockGetter $$1, BlockPos $$2) {
        return DEFAULT_SHAPE;
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        return DEFAULT_SHAPE;
    }
    
    @Override
    public VoxelShape getShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        return DEFAULT_SHAPE;
    }
    
    @Override
    public boolean isCollisionShapeFullBlock(BlockState $$0, BlockGetter $$1, BlockPos $$2) {
        return false;
    }
    
    @Override
    public boolean skipRendering(BlockState $$0, BlockState $$1, Direction direction) {
        return direction != Direction.UP;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
        if(POWERED == null) {
            POWERED = BooleanProperty.create("powered");
        }
        
        $$0.add(POWERED);
    }
    
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if(newState.getBlock() != oldState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof BundleLoaderBlockEntity bundleLoader){
                Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, bundleLoader.getItem(0)));
            }
        }
    }
    
    @Override
    public boolean hasAnalogOutputSignal(BlockState $$0) {
        return true;
    }
    
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof BundleLoaderBlockEntity bundleLoader){
            return bundleLoader.comparatorOutput();
        }
        return 0;
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
        return createTickerHelper($$2, BundleLoaderBlockEntity.TYPE.get(), BundleLoaderBlockEntity::tick);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState $$0) {
        return RenderShape.MODEL;
    }
    
    private void updatePowered(Level level, BlockPos pos, BlockState state) {
        boolean powered = level.hasNeighborSignal(pos);
        
        if(powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), 4);
        }
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState newState, Level level, BlockPos pos, BlockState oldState, boolean notify) {
        if(!oldState.is(newState.getBlock())) {
            this.updatePowered(level, pos, newState);
        }
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos toPos, boolean notify) {
        updatePowered(level, pos, state);
    }
    
    static{
        TOP_SHAPE = Block.box(0.0, 3.0, 0.0, 16.0, 16.0, 16.0);
        INSIDE_SHAPE = Shapes.or(
                Block.box(2.0, 15.0, 2.0, 14.0, 16.0, 14.0),
                Block.box(3.0, 14.0, 3.0, 13.0, 15.0, 13.0)
        );
        BOTTOM_SHAPE = Shapes.or(
                Block.box(7.0, 0.0, 7.0, 9.0, 1.0, 9.0),
                Block.box(6.0, 1.0, 6.0, 10.0, 2.0, 10.0),
                Block.box(5.0, 2.0, 5.0, 11.0, 3.0, 11.0)
        );
        
        DEFAULT_SHAPE = Shapes.join(Shapes.join(TOP_SHAPE, BOTTOM_SHAPE, BooleanOp.OR), INSIDE_SHAPE, BooleanOp.ONLY_FIRST);
    }
}
