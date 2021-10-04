package me.salamander.betterbundles.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.salamander.betterbundles.BetterBundles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class NbtRetainingShapedRecipeSerializer extends ShapedRecipe.Serializer {
    public static Identifier ID = BetterBundles.ID("nbt_retain_shaped");
    public static NbtRetainingShapedRecipeSerializer INSTANCE = new NbtRetainingShapedRecipeSerializer();

    private NbtRetainingShapedRecipeSerializer() {}

    @Override
    public ShapedRecipe read(Identifier identifier, JsonObject jsonObject) {
        ShapedRecipe original = super.read(identifier, jsonObject);

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
    public ShapedRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
        ShapedRecipe original = super.read(identifier, packetByteBuf);

        int x = packetByteBuf.readInt();
        int y = packetByteBuf.readInt();
        int length = packetByteBuf.readInt();

        String[] tags = new String[length];

        for (int i = 0; i < length; i++) {
            tags[i] = packetByteBuf.readString();
        }

        return new NbtRetainingShapedRecipe(original, tags, x, y);
    }

    @Override
    public void write(PacketByteBuf packetByteBuf, ShapedRecipe shapedRecipe) {
        NbtRetainingShapedRecipe recipe = (NbtRetainingShapedRecipe) shapedRecipe;

        super.write(packetByteBuf, shapedRecipe);
        packetByteBuf.writeInt(recipe.getX());
        packetByteBuf.writeInt(recipe.getY());
        packetByteBuf.writeInt(recipe.getKeepTags().length);
        for(String tag: recipe.getKeepTags()){
            packetByteBuf.writeString(tag);
        }
    }
}
