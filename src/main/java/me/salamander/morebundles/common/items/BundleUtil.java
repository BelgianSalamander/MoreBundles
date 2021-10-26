package me.salamander.morebundles.common.items;

import me.salamander.morebundles.common.ExtraBundleInfo;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Removes a single item from this bundle
     * @param bundle The bundle
     * @return An item. If the bundle is empty returns ItemStack.EMPTY
     */
    public static @NotNull ItemStack removeSingleItem(@NotNull ItemStack bundle){
        NbtCompound nbt = bundle.getOrCreateNbt();

        if(bundle.getItem() instanceof SingleItemBundle){
            ItemStack item = SingleItemBundle.getItem(bundle);
            if(item.isEmpty()) return ItemStack.EMPTY;

            item.setCount(1);

            NbtCompound itemInfo = nbt.getCompound("item");
            itemInfo.putInt("Count", itemInfo.getInt("Count") - 1);

            return item;
        }else if(bundle.getItem() instanceof BundleItem){
            if (!nbt.contains("Items")) {
                nbt.put("Items", new NbtList());
            }

            NbtList itemList = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
            int index = itemList.size() - 1;
            NbtCompound itemNbt = itemList.getCompound(index);
            ItemStack item = ItemStack.fromNbt(itemNbt);
            if(item.getCount() == 1){
                itemList.remove(index);
            }else{
                itemNbt.putByte("Count", (byte) (item.getCount() - 1));
            }

            item.setCount(1);

            return item;
        }else{
            throw new IllegalStateException("bundle param is not a bundle");
        }
    }
}
