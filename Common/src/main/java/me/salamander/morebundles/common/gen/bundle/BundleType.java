package me.salamander.morebundles.common.gen.bundle;

import com.google.gson.JsonObject;
import me.salamander.morebundles.common.gen.ErrorTracker;
import me.salamander.morebundles.common.items.BreadBowlItem;
import me.salamander.morebundles.common.items.DyeableBundleItem;
import me.salamander.morebundles.common.items.MoreBundlesInfo;
import me.salamander.morebundles.common.items.handlers.DefaultBundleHandler;
import me.salamander.morebundles.common.items.handlers.InextractibleBundleHandler;
import me.salamander.morebundles.common.items.handlers.SingleItemBundleHandler;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public abstract class BundleType {
    public static final BundleType BREAD_BOWL = new BreadBowlBundleType();
    public static final BundleType REGULAR = new RegularBundleType();
    public static final BundleType SINGLE_ITEM = new LargeBundleType();
    
    public abstract BundleItem generate(JsonObject json, int capacity);
    
    public static BundleType fromString(String name, ErrorTracker errorTracker) {
        switch (name) {
            case "bread_bowl":
                return BREAD_BOWL;
            case "regular_bundle":
                return REGULAR;
            case "single_item_bundle":
                return SINGLE_ITEM;
            default:
                errorTracker.addError("Unknown bundle type: " + name);
                return null;
        }
    }
    
    private static class BreadBowlBundleType extends BundleType {
        @Override
        public BundleItem generate(JsonObject json, int capacity) {
            BundleItem item = new BreadBowlItem(new Item.Properties().tab(CreativeModeTab.TAB_FOOD).food(BreadBowlItem.FOOD_PROPERTIES).stacksTo(1));
            ((MoreBundlesInfo) item).setHandler(new InextractibleBundleHandler(capacity, true));
            
            return item;
        }
    }
    
    private static class RegularBundleType extends BundleType {
        @Override
        public BundleItem generate(JsonObject json, int capacity) {
            BundleItem item;
            if (GsonHelper.getAsBoolean(json, "dyeable", false)) {
                item = new DyeableBundleItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1));
            } else {
                item = new BundleItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1));
            }
            
            ((MoreBundlesInfo) item).setHandler(new DefaultBundleHandler(capacity, false));
            
            return item;
        }
    }
    
    private static class LargeBundleType extends BundleType {
        @Override
        public BundleItem generate(JsonObject json, int capacity) {
            BundleItem item;
            if (GsonHelper.getAsBoolean(json, "dyeable", false)) {
                item = new DyeableBundleItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1));
            } else {
                item = new BundleItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1));
            }
            
            ((MoreBundlesInfo) item).setHandler(new SingleItemBundleHandler(capacity, false));
            
            return item;
        }
    }
}
