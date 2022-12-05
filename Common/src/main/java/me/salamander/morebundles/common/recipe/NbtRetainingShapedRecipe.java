package me.salamander.morebundles.common.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class NbtRetainingShapedRecipe extends ShapedRecipe {
    private final String[] keepTags;
    private final int x, y;

    public NbtRetainingShapedRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> input, ItemStack output, String[] keepTags, int x, int y) {
        super(id, group, width, height, input, output);

        this.keepTags = keepTags;
        this.x = x;
        this.y = y;
    }

    public NbtRetainingShapedRecipe(ShapedRecipe original, String[] keepTags, int x, int y){
        super(original.getId(), original.getGroup(), original.getWidth(), original.getHeight(), original.getIngredients(), original.getResultItem());

        this.keepTags = keepTags;
        this.x = x;
        this.y = y;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingInventory) {
        ItemStack originalCraft = super.assemble(craftingInventory);

        ItemStack itemToCopyFrom = craftingInventory.getItem(x + y * craftingInventory.getWidth());
        CompoundTag itemNBT = itemToCopyFrom.getOrCreateTag();
        CompoundTag copyInto = originalCraft.getOrCreateTag();

        for(String tag: keepTags){
            Tag data = itemNBT.get(tag);
            if(data != null)
                copyInto.put(tag, data.copy());
        }

        originalCraft.setTag(copyInto);
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
        return NbtRetainingShapedRecipeSerializer.INSTANCE.get();
    }
}
