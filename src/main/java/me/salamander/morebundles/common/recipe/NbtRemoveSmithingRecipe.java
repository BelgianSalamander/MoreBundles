package me.salamander.morebundles.common.recipe;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;

public class NbtRemoveSmithingRecipe extends SmithingRecipe {
    private final String[] tags;

    public NbtRemoveSmithingRecipe(SmithingRecipe original, String[] removeTags){
        super(original.getId(), original.base, original.addition, original.result);
        this.tags = removeTags;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        ItemStack originalResult = super.craft(inventory);

        for(String tag: tags){
            originalResult.removeSubNbt(tag);
        }

        return originalResult;
    }

    public String[] getTags() {
        return tags;
    }
}
