package me.salamander.morebundles.common;

import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;

import java.util.Optional;
import java.util.function.Predicate;

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

        Optional<ItemStack> removeFirstStackIf(ItemStack bundle, Predicate<ItemStack> condition);

        boolean shouldHideContents();

        void setShouldHideContents(boolean b);
    }
}
