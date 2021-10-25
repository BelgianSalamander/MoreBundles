package me.salamander.betterbundles;

import me.salamander.betterbundles.common.enchantment.CustomEnchantmentTarget;
import me.salamander.betterbundles.common.enchantment.Enchantments;
import me.salamander.betterbundles.common.items.Items;
import me.salamander.betterbundles.common.loot.SetBundleStorageFunction;
import me.salamander.betterbundles.common.recipe.NbtRemoveSmithingRecipeSerializer;
import me.salamander.betterbundles.common.recipe.NbtRetainingShapedRecipeSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BetterBundles implements ModInitializer {
    public static final String MOD_ID = "betterbundles";

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
        System.out.println("Created identifier '" + identifier + "'") ;
        return identifier;
    }
}
