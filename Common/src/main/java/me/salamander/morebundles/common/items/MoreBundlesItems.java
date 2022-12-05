package me.salamander.morebundles.common.items;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.blockentity.BundleLoaderBlock;
import me.salamander.morebundles.common.gen.MoreBundlesConfig;
import me.salamander.morebundles.common.items.handlers.DefaultBundleHandler;
import me.salamander.morebundles.common.items.handlers.InextractibleBundleHandler;
import me.salamander.morebundles.common.items.handlers.SingleItemBundleHandler;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MoreBundlesItems {
    private static final List<Pair<String, Supplier<BundleItem>>> customBundles = new ArrayList<>();
    
    public static final Supplier<BundleItem> BUNDLE = () -> (BundleItem) Items.BUNDLE;

    public static final Supplier<BlockItem> BUNDLE_LOADER = Suppliers.memoize(
            () -> new BlockItem(BundleLoaderBlock.INSTANCE.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE))
    );
    
    public static void addCustomBundle(String id, Supplier<BundleItem> bundle) {
        customBundles.add(new Pair<>(id, Suppliers.memoize(bundle::get)));
    }
    
    public static List<Pair<String, Supplier<BundleItem>>> getCustomBundles() {
        return customBundles;
    }
}
