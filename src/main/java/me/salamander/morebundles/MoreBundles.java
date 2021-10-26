package me.salamander.morebundles;

import me.salamander.morebundles.common.enchantment.CustomEnchantmentTarget;
import me.salamander.morebundles.common.enchantment.Enchantments;
import me.salamander.morebundles.common.items.Items;
import me.salamander.morebundles.common.loot.SetBundleStorageFunction;
import me.salamander.morebundles.common.recipe.NbtRemoveSmithingRecipeSerializer;
import me.salamander.morebundles.common.recipe.NbtRetainingShapedRecipeSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MoreBundles implements ModInitializer {
    public static final String MOD_ID = "morebundles";

    @Override
    public void onInitialize() {
        CustomEnchantmentTarget.loadValues();

        Items.registerAllItems();

        Registry.register(Registry.RECIPE_SERIALIZER, NbtRetainingShapedRecipeSerializer.ID, NbtRetainingShapedRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, NbtRemoveSmithingRecipeSerializer.ID, NbtRemoveSmithingRecipeSerializer.INSTANCE);

        Registry.register(Registry.LOOT_FUNCTION_TYPE, ID("set_storage"), SetBundleStorageFunction.Type.INSTANCE);

        Enchantments.registerAll();
    }

    public static Identifier ID(String id){
        Identifier identifier = new Identifier(MOD_ID, id);
        return identifier;
    }
}
