package me.salamander.betterbundles.common.recipe;

import me.salamander.betterbundles.BetterBundles;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class NbtRetainingShapedRecipe extends ShapedRecipe {
    private final String[] keepTags;
    private final int x, y;

    public NbtRetainingShapedRecipe(Identifier id, String group, int width, int height, DefaultedList<Ingredient> input, ItemStack output, String[] keepTags, int x, int y) {
        super(id, group, width, height, input, output);

        this.keepTags = keepTags;
        this.x = x;
        this.y = y;
    }

    public NbtRetainingShapedRecipe(ShapedRecipe original, String[] keepTags, int x, int y){
        super(original.getId(), original.getGroup(), original.getWidth(), original.getHeight(), original.getIngredients(), original.getOutput());

        this.keepTags = keepTags;
        this.x = x;
        this.y = y;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        ItemStack originalCraft = super.craft(craftingInventory);

        ItemStack itemToCopyFrom = craftingInventory.getStack(x + y * craftingInventory.getWidth());
        NbtCompound itemNBT = itemToCopyFrom.getOrCreateNbt();
        NbtCompound copyInto = originalCraft.getOrCreateNbt();

        for(String tag: keepTags){
            NbtElement data = itemNBT.get(tag);
            if(data != null)
                copyInto.put(tag, data.copy());
        }

        originalCraft.setNbt(copyInto);
        return originalCraft;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public String[] getKeepTags() {
        return keepTags;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NbtRetainingShapedRecipeSerializer.INSTANCE;
    }
}
