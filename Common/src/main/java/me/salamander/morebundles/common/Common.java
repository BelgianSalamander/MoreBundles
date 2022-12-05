package me.salamander.morebundles.common;

import com.google.gson.JsonParser;
import me.salamander.morebundles.common.blockentity.BundleLoaderBlock;
import me.salamander.morebundles.common.blockentity.BundleLoaderBlockEntity;
import me.salamander.morebundles.common.blockentity.BundleLoaderScreen;
import me.salamander.morebundles.common.enchantment.MoreBundlesEnchantments;
import me.salamander.morebundles.common.gen.CustomResourcePack;
import me.salamander.morebundles.common.gen.MoreBundlesConfig;
import me.salamander.morebundles.common.items.MoreBundlesItems;
import me.salamander.morebundles.common.loot.SetStorageFunction;
import me.salamander.morebundles.common.recipe.NbtRemoveSmithingRecipe;
import me.salamander.morebundles.common.recipe.NbtRemoveSmithingRecipeSerializer;
import me.salamander.morebundles.common.recipe.NbtRetainingShapedRecipe;
import me.salamander.morebundles.common.recipe.NbtRetainingShapedRecipeSerializer;
import me.salamander.morebundles.mixin.MixinDispenserBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class Common {
    public static final String MOD_ID = "morebundles";
    public static final CustomResourcePack RESOURCE_PACK = new CustomResourcePack(MOD_ID);
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    
    public static boolean IS_CLIENT = false;
    
    private static MoreBundlesConfig CONFIG;
    private static final Lock CONFIG_LOCK = new ReentrantLock();
    private static final Lock BASICS_LOAD_LOCK = new ReentrantLock();
    
    public static EnchantmentCategory BUNDLE_ENCHANTMENT_CATEGORY;
    
    public static Path CONFIG_FOLDER;
    public static Path GAME_FOLDER;
    
    public static boolean basicsLoaded = false;
    
    public static ResourceLocation makeID(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
    
    public static EnchantmentCategory getBundleEnchantmentCategory() {
        ensureBasicsLoaded();
        return BUNDLE_ENCHANTMENT_CATEGORY;
    }
    
    public static void registerAll() {
        registerBlocks();
        registerItems();
        registerEnchantments();
        registerBlockEntities();
        registerRecipes();
        registerMenus();
        registerLootFunctions();
    }
    
    public static void registerBlocks() {
        Registrar.BLOCK.register("bundle_loader", BundleLoaderBlock.INSTANCE);
    }
    
    public static void registerItems() {
        getConfig().createItems();

        if(CONFIG.bundleLoaderEnabled()) {
            Registrar.ITEM.register("bundle_loader", MoreBundlesItems.BUNDLE_LOADER);
        }
        
        for(var entry: MoreBundlesItems.getCustomBundles()){
            Registrar.ITEM.register(entry.getFirst(), entry.getSecond());
        }
    }
    
    public static void registerEnchantments() {
        Registrar.ENCHANTMENT.register("absorb", MoreBundlesEnchantments.ABSORB);
        Registrar.ENCHANTMENT.register("extract", MoreBundlesEnchantments.EXTRACT);
    }
    
    public static void registerBlockEntities() {
        Registrar.BLOCK_ENTITY.register("bundle_loader", BundleLoaderBlockEntity.TYPE);
    }
    
    public static void registerMenus() {
        Registrar.MENU.register("bundle_loader", BundleLoaderBlockEntity.MENU_TYPE);
    }
    
    public static void registerRecipes() {
        Registrar.RECIPE.register("nbt_retain_shaped", NbtRetainingShapedRecipeSerializer.INSTANCE);
        Registrar.RECIPE.register("nbt_remove_smithing", NbtRemoveSmithingRecipeSerializer.INSTANCE);
    }
    
    public static void registerLootFunctions() {
        Registrar.LOOT_FUNCTION.register("set_storage", SetStorageFunction.Type.INSTANCE);
    }
    
    public static void initClient(){
        IS_CLIENT = true;
        
        MenuScreens.register(BundleLoaderBlockEntity.MENU_TYPE.get(), BundleLoaderScreen::new);
    
        CONFIG.registerAssets(true, RESOURCE_PACK);
    }
    
    public static void initServer(){
        IS_CLIENT = false;
        
        CONFIG.registerAssets(false, RESOURCE_PACK);
    }
    
    public static MoreBundlesConfig getConfig() {
        if (CONFIG == null) {
            loadConfig();
        }
        
        return CONFIG;
    }
    
    public static MoreBundlesConfig loadConfig(){
        ensureBasicsLoaded();
        CONFIG_LOCK.lock();
        try {
            if (CONFIG != null) {
                return CONFIG;
            }
            JsonParser parser = new JsonParser();
            Path configPath = CONFIG_FOLDER.resolve(MOD_ID + "/");
    
            if(!Files.exists(configPath)) {
                try {
                    configPath.toFile().mkdirs();
            
                    Path defaultConfigPath = getDefaultConfigPath();
            
                    Files.list(defaultConfigPath).forEach((path) -> {
                        try {
                            Path to = configPath.resolve(path.getFileName().toString());
                            if(Files.exists(to)) {
                                return;
                            }
                            copyFile(path, to);
                        } catch(IOException e) {
                            throw new IllegalStateException("Couldn't copy file", e);
                        }
                    });
                } catch(IOException e) {
                    throw new IllegalStateException("Couldn't create config file", e);
                }
            }
    
            try {
                Path configFile = configPath.resolve("config.json");
                if(!Files.exists(configFile)) {
                    throw new IllegalStateException("Config file for More Bundles doesn't exist. To fix this, delete the config folder for More Bundles (" + configPath.toAbsolutePath() + ") and restart the game.");
                }
                Reader reader = new FileReader(configPath.resolve("config.json").toFile());
                MoreBundlesConfig config = new MoreBundlesConfig(parser.parse(reader).getAsJsonObject());
                reader.close();
                CONFIG = config;
                return config;
            } catch(IOException e) {
                throw new IllegalStateException("Couldn't read config", e);
            }
        } finally {
            CONFIG_LOCK.unlock();
        }
    }
    
    private static void copyFile(Path from, Path to) throws IOException{
        if(Files.isDirectory(from)){
            if(!Files.exists(to)) {
                Files.createDirectory(to);
            }
            
            Files.list(from).forEach((path) -> {
                try {
                    copyFile(path, to.resolve(path.getFileName().toString()));
                } catch (IOException e) {
                    throw new IllegalStateException("Couldn't copy file", e);
                }
            });
        }else{
            if(!Files.exists(to)) {
                Files.createFile(to);
            }
            
            FileOutputStream fout = new FileOutputStream(to.toFile());
            
            fout.write(Files.readAllBytes(from));
            
            fout.close();
        }
    }
    
    private static Path getDefaultConfigPath() throws IOException {
        URI resourcePathExample = null;
        try {
            resourcePathExample = Common.class.getResource("/default_morebundles_config/").toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Couldn't get filesystem", e);
        }
        
        if(resourcePathExample.toString().contains("!")){
            URI uri = URI.create(resourcePathExample.toString().split("!")[0]);
            FileSystem fs;
            try {
                fs = FileSystems.getFileSystem(uri);
            }catch (FileSystemNotFoundException e){
                fs = FileSystems.newFileSystem(uri, new HashMap<>());
            }
            //System.out.println("Filesystem: " + fs);
            return fs.getPath("default_morebundles_config/");
        }else{
            return Path.of(resourcePathExample);
        }
    }
    
    private static void ensureBasicsLoaded() {
        BASICS_LOAD_LOCK.lock();
        try {
            if(!basicsLoaded) {
                try {
                    Class<?> modInitializer = Class.forName("me.salamander.morebundles.MoreBundles");
                    Method method = modInitializer.getMethod("loadBasics");
                    method.invoke(null);
                } catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Couldn't load basics", e);
                }
            }
        } finally {
            BASICS_LOAD_LOCK.unlock();
        }
    }
}
