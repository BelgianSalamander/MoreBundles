package me.salamander.betterbundles;

import me.salamander.betterbundles.common.ExtraBundleInfo;
import me.salamander.betterbundles.common.enchantment.ExtractEnchantment;
import me.salamander.betterbundles.common.items.Items;
import me.salamander.betterbundles.common.loot.SetBundleStorageFunction;
import me.salamander.betterbundles.common.recipe.NbtRemoveSmithingRecipeSerializer;
import me.salamander.betterbundles.common.recipe.NbtRetainingShapedRecipe;
import me.salamander.betterbundles.common.recipe.NbtRetainingShapedRecipeSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.mixin.tag.extension.MixinRequiredTagListRegistry;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;

public class BetterBundles implements ModInitializer {
    public static final String MOD_ID = "betterbundles";

    @Override
    public void onInitialize() {
        Items.registerAllItems();

        Registry.register(Registry.RECIPE_SERIALIZER, NbtRetainingShapedRecipeSerializer.ID, NbtRetainingShapedRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, NbtRemoveSmithingRecipeSerializer.ID, NbtRemoveSmithingRecipeSerializer.INSTANCE);

        Registry.register(Registry.LOOT_FUNCTION_TYPE, ID("set_storage"), SetBundleStorageFunction.Type.INSTANCE);

        Registry.register(Registry.ENCHANTMENT, ID("extract"), ExtractEnchantment.INSTANCE);
    }

    public static Identifier ID(String id){
        Identifier identifier = new Identifier(MOD_ID, id);
        System.out.println("Created identifier '" + identifier + "'") ;
        return identifier;
    }



}
