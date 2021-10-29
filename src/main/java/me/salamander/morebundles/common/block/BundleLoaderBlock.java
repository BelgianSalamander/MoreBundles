package me.salamander.morebundles.common.block;

import me.salamander.morebundles.MoreBundles;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecated")
public class BundleLoaderBlock extends BlockWithEntity {
    public static Block BLOCK = Registry.register(Registry.BLOCK, MoreBundles.ID("bundle_loader"), new BundleLoaderBlock(FabricBlockSettings.of(Material.WOOD).strength(4.0f)));

    private static final VoxelShape INSIDE_SHAPE;
    private static final VoxelShape TOP_SHAPE;
    private static final VoxelShape BOTTOM_SHAPE;
    private static final VoxelShape DEFAULT_SHAPE;

    public static BooleanProperty POWERED;

    protected BundleLoaderBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BundleLoaderBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return DEFAULT_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return DEFAULT_SHAPE;
    }

    @Override

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(!world.isClient){
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if(screenHandlerFactory != null){
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        if(POWERED == null){
            POWERED = BooleanProperty.of("powered");
        }

        builder.add(POWERED);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BundleLoaderBlockEntity.TYPE, (world1, pos, state1, be) -> BundleLoaderBlockEntity.tick(world1, pos, state1, be));
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if(state.getBlock() != newState.getBlock()){
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof BundleLoaderBlockEntity bundleLoader){
                ItemScatterer.spawn(world, pos, DefaultedList.copyOf(ItemStack.EMPTY, bundleLoader.getStack(0)));
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return DEFAULT_SHAPE;
    }

    @Override
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return direction != Direction.UP;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if(blockEntity instanceof BundleLoaderBlockEntity bundleLoader){
            return bundleLoader.comparatorOutput();
        }
        return 0;
    }

    private void updatePowered(World world, BlockPos pos, BlockState state) {
        boolean bl = world.isReceivingRedstonePower(pos);
        if (bl != state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, bl), 4);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            this.updatePowered(world, pos, state);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        updatePowered(world, pos, state);
    }

    static{
        TOP_SHAPE = Block.createCuboidShape(0.0, 3.0, 0.0, 16.0, 16.0, 16.0);
        INSIDE_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(2.0, 15.0, 2.0, 14.0, 16.0, 14.0),
                Block.createCuboidShape(3.0, 14.0, 3.0, 13.0, 15.0, 13.0)
        );
        BOTTOM_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 1.0, 9.0),
                Block.createCuboidShape(6.0, 1.0, 6.0, 10.0, 2.0, 10.0),
                Block.createCuboidShape(5.0, 2.0, 5.0, 11.0, 3.0, 11.0)
        );

        DEFAULT_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.combineAndSimplify(TOP_SHAPE, BOTTOM_SHAPE, BooleanBiFunction.OR), INSIDE_SHAPE, BooleanBiFunction.ONLY_FIRST);

        //POWERED = BooleanProperty.of("powered");
    }
}
