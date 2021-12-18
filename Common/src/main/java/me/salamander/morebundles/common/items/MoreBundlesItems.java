package me.salamander.morebundles.common.items;

import com.mojang.datafixers.util.Pair;
import me.salamander.morebundles.common.blockentity.BundleLoaderBlock;
import me.salamander.morebundles.common.gen.BuiltinBundleInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoreBundlesItems {
    private static final List<Pair<String, BundleItem>> customBundles = new ArrayList<>();
    
    public static final BundleItem BUNDLE = (BundleItem) Items.BUNDLE;
    public static final BundleItem LARGE_BUNDLE = new DyeableBundleItem(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).stacksTo(1));
    public static final BundleItem BREAD_BOWL = new BreadBowlItem(new Item.Properties().tab(CreativeModeTab.TAB_FOOD).stacksTo(1).food(BreadBowlItem.FOOD_PROPERTIES));
    
    public static final BlockItem BUNDLE_LOADER = new BlockItem(BundleLoaderBlock.INSTANCE, new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE));
    
    public static void addCustomBundle(String id, BundleItem bundle, BundleHandler handler) {
        customBundles.add(new Pair<>(id, bundle));
        ((MoreBundlesInfo) bundle).setHandler(handler);
    }
    
    public static List<Pair<String, BundleItem>> getCustomBundles() {
        return customBundles;
    }
    
    public static void init(MoreBundlesConfig config) {
        BuiltinBundleInfo regular = config.getBuiltin("regular");
        ((MoreBundlesInfo) BUNDLE).setHandler(new DefaultBundleHandler(regular.capacity(), false));
        
        BuiltinBundleInfo large = config.getBuiltin("large");
        ((MoreBundlesInfo) LARGE_BUNDLE).setHandler(new SingleItemBundleHandler(large.capacity(), false));
        
        BuiltinBundleInfo bread = config.getBuiltin("bread_bowl");
        ((MoreBundlesInfo) BREAD_BOWL).setHandler(new InextractibleBundleHandler(bread.capacity(), true));
    }
    
}
