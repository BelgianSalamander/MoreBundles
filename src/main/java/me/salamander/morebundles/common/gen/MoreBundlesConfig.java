package me.salamander.morebundles.common.gen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.salamander.morebundles.MoreBundles;
import me.salamander.morebundles.common.items.Items;
import me.salamander.morebundles.common.items.SingleItemBundle;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.impl.RuntimeResourcePackImpl;
import net.fabricmc.fabric.api.client.texture.DependentSprite;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.TextureHelper;
import net.fabricmc.fabric.impl.registry.sync.FabricRegistry;
import net.fabricmc.fabric.mixin.client.texture.MixinSpriteAtlasTexture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static net.devtech.arrp.json.models.JModel.*;

public class MoreBundlesConfig {
    private final int regularCapacity;
    private final int largeCapacity;

    private final boolean dispenseBundledItems;

    private final RuntimeResourcePack resourcePack = RuntimeResourcePack.create(MoreBundles.ID("bundles"));

    private BundleInfo[] bundles;

    public MoreBundlesConfig(JsonObject jsonObject){
        JsonElement defaultBundle = jsonObject.get("regular_bundle");

        if(defaultBundle != null){
            regularCapacity = JsonHelper.getInt(defaultBundle.getAsJsonObject(), "capacity", 64);
            largeCapacity = JsonHelper.getInt(defaultBundle.getAsJsonObject(), "large_capacity", 64);
        }else{
            regularCapacity = 64;
            largeCapacity = 256;
        }

        JsonArray bundleInfo = jsonObject.getAsJsonArray("bundles");
        bundles = new BundleInfo[bundleInfo.size()];

        for (int i = 0; i < bundles.length; i++) {
            bundles[i] = BundleInfo.deserialize(bundleInfo.get(i));
        }

        this.dispenseBundledItems = JsonHelper.getBoolean(jsonObject, "dispense_bundled_items", true);
    }

    public boolean dispenseBundledItems(){
        return dispenseBundledItems;
    }

    public void registerItems() {
        for(BundleInfo bundleInfo: bundles){
            if(bundleInfo.hasNormal()){
                BundleItem bundle = new BundleItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1));
                Items.addCustomBundle(bundleInfo.getId(), bundle, bundleInfo.getRegularCapacity());
            }

            if(bundleInfo.hasLarge()){
                Items.addCustomBundle("large_" + bundleInfo.getId(), new SingleItemBundle(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1), bundleInfo.getLargeCapacity()));
            }
        }
    }

    public void registerTextures() {
        long startTime = System.currentTimeMillis();

        Path dir = FabricLoader.getInstance().getGameDir().getParent().resolve("bundle-out/");
        if(!Files.exists(dir)){
            dir.toFile().mkdirs();
        }

        for(BundleInfo bundleInfo: bundles){
            for(BundleInfo.BundleTextureInfo textureInfo: bundleInfo.getBundleTextures()){
                BufferedImage image = textureInfo.imageSupplier().get();
                resourcePack.addTexture(textureInfo.id(), image);

                Path path = dir.resolve(textureInfo.id().getPath());
                try {
                    if(!Files.exists(path)){
                        path.getParent().toFile().mkdirs();
                        Files.createFile(path);
                    }

                    FileOutputStream fout = new FileOutputStream(path.toFile());
                    ImageIO.write(image, "PNG", fout);
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //System.out.println("Registered texture " + textureInfo.id());
            }

            if(bundleInfo.generateModels()){
                if(bundleInfo.hasNormal()){
                    addBundleModel(bundleInfo.getId());
                }
                if(bundleInfo.hasLarge()){
                    addBundleModel("large_" + bundleInfo.getId());
                }
            }
        }

        try {
            Path resourcePath = FabricLoader.getInstance().getConfigDir().resolve("morebundles/resources/");
            //Manually load cause the .load method is bad
            Stream<Path> files = Files.walk(resourcePath);
            for(Path file: (Iterable<Path>) () -> files.filter(Files::isRegularFile).map(resourcePath::relativize).iterator()){
                Path fullPath = resourcePath.resolve(file);
                String fileName = file.toString();
                if(fileName.startsWith("assets")) {
                    String path = fileName.substring("assets".length() + 1);
                    int sep = path.indexOf(File.separator);
                    String namespace = path.substring(0, sep);
                    String dataPath = path.substring(sep + 1).replace(File.separatorChar, '/');
                    resourcePack.addAsset(new Identifier(namespace, dataPath), Files.readAllBytes(fullPath));
                } else if(fileName.startsWith("data")) {
                    String path = fileName.substring("data".length() + 1);
                    int sep = path.indexOf(File.separator);
                    String namespace = path.substring(0, sep);
                    String dataPath = path.substring(sep + 1).replace(File.separatorChar, '/');
                    resourcePack.addData(new Identifier(namespace, dataPath), Files.readAllBytes(fullPath));
                }
            }

        }catch (IOException e){
            throw new IllegalStateException("Couldn't load more bundles config resources!", e);
        }

        RRPCallback.BEFORE_VANILLA.register(a -> a.add(resourcePack));

        System.out.println("Generated and loaded `More Bundles!` resources in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private void addBundleModel(String name){
        resourcePack.addModel(
                model("minecraft:item/generated").textures(
                        textures().layer0("morebundles:item/" + name)
                ).addOverride(override(condition().parameter("filled", 0.00000000001), new Identifier("morebundles:item/" + name + "_filled"))),
                new Identifier("morebundles", "item/" + name)
        );

        resourcePack.addModel(
                model("morebundles:item/" + name).textures(
                        textures().layer0("morebundles:item/" + name + "_filled")
                ),
                new Identifier("morebundles", "item/" + name + "_filled")
        );
    }
}
