package me.salamander.betterbundles.common;

import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;

public class ExtraBundleInfo {
    private int defaultMaxStorage = BundleItem.MAX_STORAGE;

    public int getDefaultMaxStorage(){
        return defaultMaxStorage;
    }

    public void setDefaultMaxStorage(int maxStorage) {
        this.defaultMaxStorage = maxStorage;
    }

    public interface Access{
        ExtraBundleInfo getExtraBundleInfo();

        NbtList getItems(ItemStack itemStack);
    }
}
