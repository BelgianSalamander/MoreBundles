package me.salamander.morebundles.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import me.salamander.morebundles.common.items.BundleHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetStorageFunction extends LootItemConditionalFunction {
    private final NumberProvider storage;
    
    private SetStorageFunction(NumberProvider storage, LootItemCondition[] conditions) {
        super(conditions);
        this.storage = storage;
    }
    
    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        itemStack.getOrCreateTag().putInt(BundleHandler.CAPACITY_KEY, this.storage.getInt(lootContext));
        return itemStack;
    }
    
    @Override
    public LootItemFunctionType getType() {
        return Type.INSTANCE;
    }
    
    private static class SetStorageSerializer extends LootItemConditionalFunction.Serializer<SetStorageFunction>{
        @Override
        public SetStorageFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new SetStorageFunction(context.deserialize(json.get("storage"), NumberProvider.class), conditions);
        }
    
        @Override
        public void serialize(JsonObject json, SetStorageFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
            json.add("storage", context.serialize(function.storage));
        }
    
    }
    
    public static class Type extends LootItemFunctionType{
        private Type() {
            super(new SetStorageSerializer());
        }
    
        public static final Type INSTANCE = new Type();
    }
}
