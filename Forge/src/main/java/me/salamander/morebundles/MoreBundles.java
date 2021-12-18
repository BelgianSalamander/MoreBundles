package me.salamander.morebundles;

import com.google.gson.JsonObject;
import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.Registrar;
import me.salamander.morebundles.common.enchantment.MoreBundlesEnchantments;
import me.salamander.morebundles.common.items.MoreBundlesInfo;
import me.salamander.morebundles.common.items.MoreBundlesItems;
import me.salamander.morebundles.common.loot.SetStorageFunction;
import me.salamander.morebundles.util.MBUtil;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(Common.MOD_ID)
public class MoreBundles {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Common.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Common.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Common.MOD_ID);
    private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Common.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Common.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Common.MOD_ID);
    
    public MoreBundles() {
        Common.BUNDLE_ENCHANTMENT_CATEGORY = EnchantmentCategory.create("BUNDLE", (item) -> item instanceof MoreBundlesInfo);
        Common.GAME_FOLDER = FMLPaths.GAMEDIR.get();
        Common.CONFIG_FOLDER = FMLPaths.CONFIGDIR.get();
        Common.loadConfig();
        Common.CONFIG.createItems();
        
        Common.IS_CLIENT = isClient();
    
        Registrar.BLOCK = new ForgeRegistrar<>(BLOCKS);
        Registrar.ITEM = new ForgeRegistrar<>(ITEMS);
        Registrar.BLOCK_ENTITY = new ForgeRegistrar<>(BLOCK_ENTITY_TYPES);
        Registrar.ENCHANTMENT = new ForgeRegistrar<>(ENCHANTMENTS);
        Registrar.MENU = new ForgeRegistrar<>(MENU_TYPES);
        Registrar.RECIPE = new ForgeRegistrar<>(RECIPES);
        Registrar.LOOT_FUNCTION = new MinecraftRegistrar<>(Registry.LOOT_FUNCTION_TYPE);
        
        MoreBundlesItems.init(Common.CONFIG);
        
        Common.registerAll();
        
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        
        if(Common.IS_CLIENT){
            setupClient();
        }else{
            setupServer();
        }
    
        MinecraftForge.EVENT_BUS.addListener(MoreBundles::onPlayerDestroyItem);
    }
    
    private boolean isClient(){
        return FMLEnvironment.dist.isClient();
    }
    
    public void setupClient() {
        Common.initClient();
    
        ItemProperties.registerGeneric(new ResourceLocation("minecraft", "filled"), (a, b, c, d) -> BundleItem.getFullnessDisplay(a));
    }
    
    public void setupServer() {
        Common.initServer();
    }
    
    private static void onPlayerDestroyItem(PlayerDestroyItemEvent event){
        if(event.getHand() != null) {
            ItemStack retrieved = ItemStack.EMPTY;
            Inventory inventory = event.getPlayer().getInventory();
            Item lookingFor = event.getOriginal().getItem();
    
            for(ItemStack bundle : MBUtil.iterate(inventory.items, inventory.offhand)) {
                if(!bundle.isEmpty() && bundle.getItem() instanceof MoreBundlesInfo info && EnchantmentHelper.getItemEnchantmentLevel(MoreBundlesEnchantments.EXTRACT, bundle) > 0) {
                    retrieved = info.getHandler()
                            .removeFirst(bundle.getOrCreateTag(), (stack) -> stack.getItem() == lookingFor);
                    if(!retrieved.isEmpty()) {
                        break;
                    }
                }
            }
    
            if(!retrieved.isEmpty()) {
                event.getPlayer().setItemInHand(event.getHand(), retrieved);
            }
        }
    }
    
    private static class ForgeRegistrar<T extends IForgeRegistryEntry<T>> extends Registrar<T>{
        private final DeferredRegister<T> reg;
    
        private ForgeRegistrar(DeferredRegister<T> reg) {
            this.reg = reg;
        }
    
        public void register(String id, T obj) {
            reg.register(id, () -> obj);
        }
    }
    
    private static class MinecraftRegistrar<T> extends Registrar<T>{
        private final Registry<T> reg;
        
        public MinecraftRegistrar(Registry<T> registry){
            this.reg = registry;
        }
    
        @Override
        public void register(String name, T value) {
            Registry.register(reg, new ResourceLocation(Common.MOD_ID, name), value);
        }
    
    }
}