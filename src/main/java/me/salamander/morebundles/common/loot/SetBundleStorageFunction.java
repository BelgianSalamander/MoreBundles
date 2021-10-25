package me.salamander.morebundles.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import me.salamander.morebundles.common.ExtraBundleInfo;
import me.salamander.morebundles.mixin.MixinBundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class SetBundleStorageFunction extends ConditionalLootFunction {
    private final LootNumberProvider sizeProvider;

    private SetBundleStorageFunction(LootNumberProvider sizeProvider, LootCondition[] conditions) {
        super(conditions);
        this.sizeProvider = sizeProvider;
    }

    @Override
    public LootFunctionType getType() {
        return null;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext lootContext) {
        stack.getOrCreateNbt().putInt("MaxBundleStorage", sizeProvider.nextInt(lootContext));
        return stack;
    }

    private static class Serializer extends ConditionalLootFunction.Serializer<SetBundleStorageFunction>{
        @Override
        public SetBundleStorageFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new SetBundleStorageFunction(context.deserialize(json.get("storage"), LootNumberProvider.class), conditions);
        }

        @Override
        public void toJson(JsonObject jsonObject, SetBundleStorageFunction conditionalLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, conditionalLootFunction, jsonSerializationContext);
            jsonObject.add("storage", jsonSerializationContext.serialize(conditionalLootFunction));
        }
    }

    public static class Type extends LootFunctionType{
        private Type() {
            super(new Serializer());
        }

        public static final Type INSTANCE = new Type();
    }
}
