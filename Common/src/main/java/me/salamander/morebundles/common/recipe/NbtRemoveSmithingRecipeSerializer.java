package me.salamander.morebundles.common.recipe;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.salamander.morebundles.common.Common;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.UpgradeRecipe;

public class NbtRemoveSmithingRecipeSerializer extends UpgradeRecipe.Serializer {
    public static ResourceLocation ID = Common.makeID("nbt_remove_smithing");
    public static Supplier<NbtRemoveSmithingRecipeSerializer> INSTANCE = Suppliers.memoize(NbtRemoveSmithingRecipeSerializer::new);

    private NbtRemoveSmithingRecipeSerializer() {}

    @Override
    public UpgradeRecipe fromJson(ResourceLocation identifier, JsonObject jsonObject) {
        UpgradeRecipe original = super.fromJson(identifier, jsonObject);

        JsonArray removeTags = jsonObject.get("remove_tags").getAsJsonArray();
        String[] tags = new String[removeTags.size()];

        for(int i = 0; i < removeTags.size(); i++){
            tags[i] = removeTags.get(i).getAsString();
        }

        return new NbtRemoveSmithingRecipe(original, tags);
    }

    @Override
    public UpgradeRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
        UpgradeRecipe original = super.fromNetwork(identifier, packetByteBuf);

        int length = packetByteBuf.readInt();

        String[] tags = new String[length];

        for (int i = 0; i < length; i++) {
            tags[i] = packetByteBuf.readUtf();
        }

        return new NbtRemoveSmithingRecipe(original, tags);
    }

    @Override
    public void toNetwork(FriendlyByteBuf packetByteBuf, UpgradeRecipe smithingRecipe) {
        NbtRemoveSmithingRecipe original = (NbtRemoveSmithingRecipe) smithingRecipe;

        super.toNetwork(packetByteBuf, smithingRecipe);

        packetByteBuf.writeInt(original.getTags().length);

        for(String tag: original.getTags()){
            packetByteBuf.writeUtf(tag);
        }
    }
}
