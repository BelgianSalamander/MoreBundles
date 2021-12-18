package me.salamander.morebundles.common;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public abstract class Registrar<T> {
    public static Registrar<Item> ITEM;
    public static Registrar<Block> BLOCK;
    public static Registrar<BlockEntityType<?>> BLOCK_ENTITY;
    public static Registrar<Enchantment> ENCHANTMENT;
    public static Registrar<MenuType<?>> MENU;
    public static Registrar<RecipeSerializer<?>> RECIPE;
    public static Registrar<LootItemFunctionType> LOOT_FUNCTION;
    
    abstract public void register(String name, T value);
}
