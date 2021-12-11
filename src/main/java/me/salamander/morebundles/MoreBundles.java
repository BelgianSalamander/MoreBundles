package me.salamander.morebundles;

import com.google.gson.JsonParser;
import me.salamander.morebundles.common.enchantment.CustomEnchantmentTarget;
import me.salamander.morebundles.common.enchantment.Enchantments;
import me.salamander.morebundles.common.gen.MoreBundlesConfig;
import me.salamander.morebundles.common.items.Items;
import me.salamander.morebundles.common.loot.SetBundleStorageFunction;
import me.salamander.morebundles.common.recipe.NbtRemoveSmithingRecipeSerializer;
import me.salamander.morebundles.common.recipe.NbtRetainingShapedRecipeSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.HashMap;

public class MoreBundles implements ModInitializer {
    public static final String MOD_ID = "morebundles";
    private static MoreBundlesConfig config;

    @Override
    public void onInitialize() {
        CustomEnchantmentTarget.loadValues();

        Registry.register(Registry.RECIPE_SERIALIZER, NbtRetainingShapedRecipeSerializer.ID, NbtRetainingShapedRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, NbtRemoveSmithingRecipeSerializer.ID, NbtRemoveSmithingRecipeSerializer.INSTANCE);

        Registry.register(Registry.LOOT_FUNCTION_TYPE, ID("set_storage"), SetBundleStorageFunction.Type.INSTANCE);

        Enchantments.registerAll();

        JsonParser parser = new JsonParser();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/");

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
            Reader reader = new FileReader(configPath.resolve("config.json").toFile());
            config = new MoreBundlesConfig(parser.parse(reader).getAsJsonObject());
            reader.close();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read config", e);
        }

        Items.registerBuiltinItems(config);

        config.registerItems();
        config.registerTextures();
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
            resourcePathExample = MoreBundles.class.getResource("/default_morebundles_config/").toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Couldn't get filesystem", e);
        }

        if(resourcePathExample.toString().contains("!")){
            URI uri = URI.create(resourcePathExample.toString().split("!")[0]);
            FileSystem fs;
            try {
                fs = FileSystems.newFileSystem(uri, new HashMap<>());
            }catch (FileSystemAlreadyExistsException e){
                fs = FileSystems.getFileSystem(uri);
            }
            //System.out.println("Filesystem: " + fs);
            return fs.getPath("default_morebundles_config/");
        }else{
            return Path.of(resourcePathExample);
        }
    }

    public static Identifier ID(String id){
        Identifier identifier = new Identifier(MOD_ID, id);
        return identifier;
    }

    public static MoreBundlesConfig getConfig() {
        return config;
    }
}
