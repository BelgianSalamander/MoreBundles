package me.salamander.morebundles.common.recipe;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.salamander.morebundles.common.Common;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class NbtRetainingShapedRecipeSerializer extends ShapedRecipe.Serializer {
    public static ResourceLocation ID = Common.makeID("nbt_retain_shaped");
    public static Supplier<NbtRetainingShapedRecipeSerializer> INSTANCE = Suppliers.memoize(NbtRetainingShapedRecipeSerializer::new);

    private NbtRetainingShapedRecipeSerializer() {}

    @Override
    public ShapedRecipe fromJson(ResourceLocation identifier, JsonObject jsonObject) {
        ShapedRecipe original = super.fromJson(identifier, jsonObject);

        int x = jsonObject.get("keep_x").getAsInt();
        int y = jsonObject.get("keep_y").getAsInt();

        JsonArray keepTags = jsonObject.get("keep_tags").getAsJsonArray();
        String[] tags = new String[keepTags.size()];

        for(int i = 0; i < keepTags.size(); i++){
            tags[i] = keepTags.get(i).getAsString();
        }

        return new NbtRetainingShapedRecipe(original, tags, x, y);
    }

    @Override
    public ShapedRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
        ShapedRecipe original = super.fromNetwork(identifier, packetByteBuf);

        int x = packetByteBuf.readInt();
        int y = packetByteBuf.readInt();
        int length = packetByteBuf.readInt();

        String[] tags = new String[length];

        for (int i = 0; i < length; i++) {
            tags[i] = packetByteBuf.readUtf();
        }

        return new NbtRetainingShapedRecipe(original, tags, x, y);
    }

    @Override
    public void toNetwork(FriendlyByteBuf packetByteBuf, ShapedRecipe shapedRecipe) {
        NbtRetainingShapedRecipe recipe = (NbtRetainingShapedRecipe) shapedRecipe;

        super.toNetwork(packetByteBuf, shapedRecipe);
        packetByteBuf.writeInt(recipe.getX());
        packetByteBuf.writeInt(recipe.getY());
        packetByteBuf.writeInt(recipe.getKeepTags().length);
        for(String tag: recipe.getKeepTags()){
            packetByteBuf.writeUtf(tag);
        }
    }
}
