package me.salamander.morebundles.common.items;

import me.salamander.morebundles.common.ExtraBundleInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class BundleUtil {
    private static final String MAX_BUNDLE_STORAGE_KEY = "MaxBundleStorage";
    private static final String CONTENTS_HIDDEN_KEY = "BundleContentsHidden";

    public static int getMaxStorage(ItemStack bundle){
        NbtCompound nbt = bundle.getOrCreateNbt();
        if(!nbt.contains(MAX_BUNDLE_STORAGE_KEY)){
            if(bundle.getItem() instanceof ExtraBundleInfo.Access bundleItem){
                nbt.putInt(MAX_BUNDLE_STORAGE_KEY, bundleItem.getExtraBundleInfo().getDefaultMaxStorage());
                return bundleItem.getExtraBundleInfo().getDefaultMaxStorage();
            }else{
                return 0;
            }
        }
        return bundle.getOrCreateNbt().getInt(MAX_BUNDLE_STORAGE_KEY);
    }

    public static boolean shouldHideContents(ItemStack bundle){
        return bundle.getOrCreateNbt().getBoolean(CONTENTS_HIDDEN_KEY);
    }
}
