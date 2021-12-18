package me.salamander.morebundles;

import com.chocohead.mm.api.ClassTinkerers;
import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.Registrar;
import me.salamander.morebundles.common.items.MoreBundlesItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MoreBundles implements ModInitializer {
    @Override
    public void onInitialize() {
        Common.BUNDLE_ENCHANTMENT_CATEGORY = ClassTinkerers.getEnum(EnchantmentCategory.class, "BUNDLE");
        Common.GAME_FOLDER = FabricLoader.getInstance().getGameDirectory().toPath();
        Common.CONFIG_FOLDER = FabricLoader.getInstance().getConfigDirectory().toPath();
        Common.loadConfig();
        Common.CONFIG.createItems();
        
        Common.IS_CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        
        Registrar.BLOCK = new FabricRegistrar<>(Registry.BLOCK);
        Registrar.ITEM = new FabricRegistrar<>(Registry.ITEM);
        Registrar.ENCHANTMENT = new FabricRegistrar<>(Registry.ENCHANTMENT);
        Registrar.BLOCK_ENTITY = new FabricRegistrar<>(Registry.BLOCK_ENTITY_TYPE);
        Registrar.MENU = new FabricRegistrar<>(Registry.MENU);
        Registrar.RECIPE = new FabricRegistrar<>(Registry.RECIPE_SERIALIZER);
        Registrar.LOOT_FUNCTION = new FabricRegistrar<>(Registry.LOOT_FUNCTION_TYPE);
    
        MoreBundlesItems.init(Common.CONFIG);
        
        Common.registerAll();
        
        if(Common.IS_CLIENT) {
            Common.initClient();
        }else{
            Common.initServer();
        }
    }
    
    private static class FabricRegistrar<T> extends Registrar<T>{
        private final Registry<T> registry;
        
        public FabricRegistrar(Registry<T> registry) {
            this.registry = registry;
        }
        
        @Override
        public void register(String name, T value) {
            Registry.register(registry, new ResourceLocation(Common.MOD_ID, name), value);
        }
    }
}
