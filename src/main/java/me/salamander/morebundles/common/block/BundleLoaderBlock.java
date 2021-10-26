package me.salamander.morebundles.common.block;

import me.salamander.morebundles.MoreBundles;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BundleLoaderBlock extends Block implements BlockEntityProvider {
    public static Block BLOCK = Registry.register(Registry.BLOCK, MoreBundles.ID("bundle_loader"), new BundleLoaderBlock(FabricBlockSettings.of(Material.WOOD).strength(4.0f)));

    protected BundleLoaderBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BundleLoaderBlockEntity(pos, state);
    }
}
