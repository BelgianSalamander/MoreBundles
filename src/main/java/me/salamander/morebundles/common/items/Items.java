package me.salamander.morebundles.common.items;

import me.salamander.morebundles.MoreBundles;
import me.salamander.morebundles.common.ExtraBundleInfo;
import me.salamander.morebundles.common.block.BundleLoaderBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.*;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class Items {
    /*public static final Item IRON_BUNDLE;
    public static final Item GOLD_BUNDLE;
    public static final Item DIAMOND_BUNDLE;
    public static final Item NETHERITE_BUNDLE;
    public static final Item SANTAS_BAG;*/

    public static final Item LARGE_BUNDLE;
    /*public static final Item LARGE_IRON_BUNDLE;
    public static final Item LARGE_GOLD_BUNDLE;
    public static final Item LARGE_DIAMOND_BUNDLE;
    public static final Item LARGE_NETHERITE_BUNDLE;*/

    public static final Item BREAD_BOWL;

    public static final Item BUNDLE_LOADER;

    private static final Map<String, BundleItem> customBundles = new HashMap<>();

    public static void addCustomBundle(String name, BundleItem bundleItem, int capacity){
        customBundles.put(name, register(name, setDefaultStorage(bundleItem, capacity)));
    }

    public static void addCustomBundle(String name, SingleItemBundle bundle) {
        customBundles.put(name, register(name, bundle));
    }

    private static <T extends Item> T register(String id, T item){
        return Registry.register(Registry.ITEM, MoreBundles.ID(id), item);
    }

    private static BundleItem setDefaultStorage(BundleItem bundleItem, int maxStorage){
        ((ExtraBundleInfo.Access) bundleItem).getExtraBundleInfo().setDefaultMaxStorage(maxStorage);
        return bundleItem;
    }

    public static void registerAllItems(){
        //Loads the class
    }

    static {
        setDefaultStorage((BundleItem) net.minecraft.item.Items.BUNDLE, 64);

        LARGE_BUNDLE = register("large_bundle", new DyeableSingleItemBundle(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), 256));
        BREAD_BOWL = register("bread_bowl", new BreadBowlItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1).food(BreadBowlItem.FOOD_COMPONENT), 32));

        BUNDLE_LOADER = register("bundle_loader", new BlockItem(BundleLoaderBlock.BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE)));
    }
}
