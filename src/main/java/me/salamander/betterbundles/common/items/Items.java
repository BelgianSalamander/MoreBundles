package me.salamander.betterbundles.common.items;

import me.salamander.betterbundles.BetterBundles;
import me.salamander.betterbundles.common.ExtraBundleInfo;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class Items {
    public static final Item IRON_BUNDLE;
    public static final Item GOLD_BUNDLE;
    public static final Item DIAMOND_BUNDLE;
    public static final Item NETHERITE_BUNDLE;
    public static final Item SANTAS_BAG;

    public static final Item LARGE_BUNDLE;
    public static final Item LARGE_IRON_BUNDLE;
    public static final Item LARGE_GOLD_BUNDLE;
    public static final Item LARGE_DIAMOND_BUNDLE;
    public static final Item LARGE_NETHERITE_BUNDLE;

    private static <T extends Item> T register(String id, T item){
        return Registry.register(Registry.ITEM, BetterBundles.ID(id), item);
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

        IRON_BUNDLE = setDefaultStorage(register("iron_bundle", new BundleItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1))), 128);
        GOLD_BUNDLE = setDefaultStorage(register("gold_bundle", new BundleItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1))), 256);
        DIAMOND_BUNDLE = setDefaultStorage(register("diamond_bundle", new BundleItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1))), 512);
        NETHERITE_BUNDLE = setDefaultStorage(register("netherite_bundle", new BundleItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1))), 1024);
        SANTAS_BAG = setDefaultStorage(register("santas_bag", new BundleItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1))), Integer.MAX_VALUE);

        LARGE_BUNDLE = register("large_bundle", new DyeableSingleItemBundle(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), 256));
        LARGE_IRON_BUNDLE = register("large_iron_bundle", new SingleItemBundle(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), 512));
        LARGE_GOLD_BUNDLE = register("large_gold_bundle", new SingleItemBundle(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), 1024));
        LARGE_DIAMOND_BUNDLE = register("large_diamond_bundle", new SingleItemBundle(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), 2048));
        LARGE_NETHERITE_BUNDLE = register("large_netherite_bundle", new SingleItemBundle(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), 4096));
    }
}
