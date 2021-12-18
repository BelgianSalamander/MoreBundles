package me.salamander.morebundles.common.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.UpgradeRecipe;

public class NbtRemoveSmithingRecipe extends UpgradeRecipe {
    private final String[] tags;

    public NbtRemoveSmithingRecipe(UpgradeRecipe original, String[] removeTags){
        super(original.getId(), original.base, original.addition, original.result);
        this.tags = removeTags;
    }

    @Override
    public ItemStack assemble(Container inventory) {
        ItemStack originalResult = super.assemble(inventory);

        for(String tag: tags){
            originalResult.removeTagKey(tag);
        }

        return originalResult;
    }

    public String[] getTags() {
        return tags;
    }
}
