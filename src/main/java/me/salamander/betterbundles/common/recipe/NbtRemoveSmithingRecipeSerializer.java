package me.salamander.betterbundles.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.salamander.betterbundles.BetterBundles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;

public class NbtRemoveSmithingRecipeSerializer extends SmithingRecipe.Serializer {
    public static Identifier ID = BetterBundles.ID("nbt_remove_smithing");
    public static NbtRemoveSmithingRecipeSerializer INSTANCE = new NbtRemoveSmithingRecipeSerializer();

    private NbtRemoveSmithingRecipeSerializer() {}

    @Override
    public SmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
        SmithingRecipe original = super.read(identifier, jsonObject);

        JsonArray removeTags = jsonObject.get("remove_tags").getAsJsonArray();
        String[] tags = new String[removeTags.size()];

        for(int i = 0; i < removeTags.size(); i++){
            tags[i] = removeTags.get(i).getAsString();
        }

        return new NbtRemoveSmithingRecipe(original, tags);
    }

    @Override
    public SmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
        SmithingRecipe original = super.read(identifier, packetByteBuf);

        int length = packetByteBuf.readInt();

        String[] tags = new String[length];

        for (int i = 0; i < length; i++) {
            tags[i] = packetByteBuf.readString();
        }

        return new NbtRemoveSmithingRecipe(original, tags);
    }

    @Override
    public void write(PacketByteBuf packetByteBuf, SmithingRecipe smithingRecipe) {
        NbtRemoveSmithingRecipe original = (NbtRemoveSmithingRecipe) smithingRecipe;

        super.write(packetByteBuf, smithingRecipe);

        packetByteBuf.writeInt(original.getTags().length);

        for(String tag: original.getTags()){
            packetByteBuf.writeString(tag);
        }
    }
}
