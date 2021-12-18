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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Common {
    public static final String MOD_ID = "morebundles";
    public static final CustomResourcePack RESOURCE_PACK = new CustomResourcePack(MOD_ID);
    
    public static boolean IS_CLIENT = false;
    
    public static MoreBundlesConfig CONFIG;
    
    public static EnchantmentCategory BUNDLE_ENCHANTMENT_CATEGORY;
    
    public static Path CONFIG_FOLDER;
    public static Path GAME_FOLDER;
    
    public static DataGenerator DATA_GENERATOR;
    
    public static ResourceLocation makeID(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
    
    public static void registerAll() {
        Registrar.BLOCK.register("bundle_loader", BundleLoaderBlock.INSTANCE);
        
        if(CONFIG.getBuiltin("large").enabled()) {
            Registrar.ITEM.register("large_bundle", MoreBundlesItems.LARGE_BUNDLE);
        }
        
        if(CONFIG.getBuiltin("bread_bowl").enabled()){
            Registrar.ITEM.register("bread_bowl", MoreBundlesItems.BREAD_BOWL);
        }
        
        if(CONFIG.bundleLoaderEnabled()) {
            Registrar.ITEM.register("bundle_loader", MoreBundlesItems.BUNDLE_LOADER);
        }
        
        for(var entry: MoreBundlesItems.getCustomBundles()){
            Registrar.ITEM.register(entry.getFirst(), entry.getSecond());
        }
        
        Registrar.ENCHANTMENT.register("absorb", MoreBundlesEnchantments.ABSORB);
        Registrar.ENCHANTMENT.register("extract", MoreBundlesEnchantments.EXTRACT);
        
        Registrar.BLOCK_ENTITY.register("bundle_loader", BundleLoaderBlockEntity.TYPE);
        
        Registrar.MENU.register("bundle_loader", BundleLoaderBlockEntity.MENU_TYPE);
        
        Registrar.RECIPE.register("nbt_retain_shaped", NbtRetainingShapedRecipeSerializer.INSTANCE);
        Registrar.RECIPE.register("nbt_remove_smithing", NbtRemoveSmithingRecipeSerializer.INSTANCE);
        
        Registrar.LOOT_FUNCTION.register("set_storage", SetStorageFunction.Type.INSTANCE);
    }
    
    public static void initClient(){
        IS_CLIENT = true;
        
        MenuScreens.register(BundleLoaderBlockEntity.MENU_TYPE, BundleLoaderScreen::new);
    
        CONFIG.registerAssets(true, RESOURCE_PACK);
    }
    
    public static void initServer(){
        IS_CLIENT = false;
        
        CONFIG.registerAssets(false, RESOURCE_PACK);
    }
    
    public static void checkConstants(){
        //Check that nothing is null
        if(BUNDLE_ENCHANTMENT_CATEGORY == null){
            throw new RuntimeException("BUNDLE_ENCHANTMENT_CATEGORY was null");
        }
        
        if(CONFIG_FOLDER == null){
            throw new RuntimeException("CONFIG_FOLDER was null");
        }
        
        if(GAME_FOLDER == null){
            throw new RuntimeException("GAME_FOLDER was null");
        }
    }
    
    public static MoreBundlesConfig loadConfig(){
        JsonParser parser = new JsonParser();
        Path configPath = CONFIG_FOLDER.resolve(MOD_ID + "/");
    
        if(!Files.exists(configPath)){
            try {
                configPath.toFile().mkdirs();
            
                Path defaultConfigPath = getDefaultConfigPath();
            
                Files.list(defaultConfigPath).forEach((path) -> {
                    try {
                        Path to = configPath.resolve(path.getFileName().toString());
                        copyFile(path, to);
                    } catch (IOException e) {
                        throw new IllegalStateException("Couldn't copy file", e);
                    }
                });
            }catch (IOException e){
                throw new IllegalStateException("Couldn't create config file", e);
            }
        }
    
        try {
            Path configFile = configPath.resolve("config.json");
            if(!Files.exists(configFile)){
                throw new IllegalStateException("Config file for More Bundles doesn't exist. To fix this, delete the config folder for More Bundles (" + configPath.toAbsolutePath() + ") and restart the game.");
            }
            Reader reader = new FileReader(configPath.resolve("config.json").toFile());
            MoreBundlesConfig config = new MoreBundlesConfig(parser.parse(reader).getAsJsonObject());
            reader.close();
            CONFIG = config;
            return config;
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read config", e);
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
}
