package me.salamander.morebundles.common.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public interface ItemWithLoot {
    void generateLoot(ItemStack itemStack, LootContext lootContext);
}
