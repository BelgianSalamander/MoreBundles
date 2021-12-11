package me.salamander.morebundles.common.gen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
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
import org.lwjgl.system.CallbackI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static net.devtech.arrp.json.models.JModel.*;

public class MoreBundlesConfig {
    private static final String[] BUILTIN_BUNDLES = {"regular", "large", "bread_bowl"};
    private static final int[] BUILTIN_BUNDLE_DEFAULT_CAPACITIES = {64, 256, 32};

    private final Map<String, BuiltinBundleInfo> builtinBundles = new HashMap<>();

    private final boolean dispenseBundledItems;

    private final RuntimeResourcePack resourcePack = RuntimeResourcePack.create(MoreBundles.ID("bundles"));

    private BundleInfo[] bundles;

    //TODO: Clean all of this up
    public MoreBundlesConfig(JsonObject jsonObject){
        //Get fail policy
        ErrorPolicy policy;
        JsonElement failPolicy = jsonObject.get("fail_policy");
        if(failPolicy == null){
            System.err.println("No fail policy specified, defaulting to 'warn'");
            policy = ErrorPolicy.WARN;
        }else if(failPolicy.isJsonPrimitive()){
            String failPolicyString = failPolicy.getAsString();
            if(failPolicyString.equalsIgnoreCase("ignore")) {
                policy = ErrorPolicy.IGNORE;
            }else if(failPolicyString.equalsIgnoreCase("warn")){
                policy = ErrorPolicy.WARN;
            }else if(failPolicyString.equalsIgnoreCase("fail on error")){
                policy = ErrorPolicy.FAIL_ON_ERROR;
            }else if(failPolicyString.equalsIgnoreCase("fail on warning")) {
                policy = ErrorPolicy.FAIL_ON_WARNING;
            } else{
                throw new IllegalArgumentException("Invalid fail policy: " + failPolicyString);
            }
        }else {
            throw new IllegalArgumentException("Invalid fail policy: " + failPolicy);
        }

        ErrorTracker errorTracker = new ErrorTracker(policy == ErrorPolicy.FAIL_ON_WARNING);

        JsonElement builtinBundleInfo = jsonObject.get("builtin_bundles");
        JsonElement defaultBundleOutdated = jsonObject.get("regular_bundle");

        if(defaultBundleOutdated != null){
            errorTracker.addWarning("The 'regular_bundle' key is deprecated, use 'default_bundle' instead. For more information see https://github.com/BelgianSalamander/MoreBundles/wiki/Config-Changes#013"); //TODO: link to documentation
        }

        if(builtinBundleInfo != null) {
            JsonObject builtinBundleInfoObject = builtinBundleInfo.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : builtinBundleInfoObject.entrySet()) {
                String name = entry.getKey().toLowerCase();
                JsonObject bundleInfo = entry.getValue().getAsJsonObject();
                JsonElement enabledElement = bundleInfo.get("enabled");

                boolean enabled = true;
                if(enabledElement == null && !name.equals("regular")){
                    errorTracker.addWarning("The 'enabled' key is missing for builtin bundle '" + name + "'");
                }else if(enabledElement != null && !enabledElement.isJsonPrimitive()){
                    errorTracker.addWarning("The 'enabled' key for builtin bundle '" + name + "' is not a boolean. Assuming true");
                }else{
                    if(enabledElement != null){
                        JsonPrimitive enabledPrimitive = enabledElement.getAsJsonPrimitive();
                        if(enabledPrimitive.isBoolean()){
                            enabled = enabledPrimitive.getAsBoolean();
                        }else{
                            errorTracker.addWarning("The 'enabled' key for builtin bundle '" + name + "' is not a boolean. Assuming true");
                        }
                    }
                }

                JsonElement capacityElement = bundleInfo.get("capacity");
                int capacity = 64;
                //Get Default
                for (int i = 0; i < BUILTIN_BUNDLES.length; i++) {
                    if(name.equals(BUILTIN_BUNDLES[i])){
                        capacity = BUILTIN_BUNDLE_DEFAULT_CAPACITIES[i];
                    }
                }
                if(capacityElement == null){
                    errorTracker.addWarning("The 'capacity' key is missing for builtin bundle '" + name + "'");
                }else if(!capacityElement.isJsonPrimitive()){
                    errorTracker.addWarning("The 'capacity' key for builtin bundle '" + name + "' is not a number. Assuming " + capacity);
                }else{
                    capacity = capacityElement.getAsInt();
                }

                if(name.equals("regular") && !enabled){
                    errorTracker.addError("Cannot disable the regular bundle!");
                }

                boolean isBuiltin = false;
                for(String builtinBundle : BUILTIN_BUNDLES){
                    if(!name.equals(builtinBundle)){
                        isBuiltin = true;
                    }
                }

                if(!isBuiltin){
                    errorTracker.addWarning("Builtin bundle entry '" + name + "' is not a builtin bundle! The current builtin bundles are: [" + String.join(", ", BUILTIN_BUNDLES) + "]");
                }

                builtinBundles.put(name, new BuiltinBundleInfo(enabled, capacity));
            }

            for (int i = 0; i < BUILTIN_BUNDLES.length; i++) {
                String name = BUILTIN_BUNDLES[i];
                if(!builtinBundles.containsKey(name)){
                    errorTracker.addError("Builtin bundle '" + name + "' is missing from the builtin bundle list!");
                    builtinBundles.put(name, new BuiltinBundleInfo(true, BUILTIN_BUNDLE_DEFAULT_CAPACITIES[i]));
                }
            }
        }else if(defaultBundleOutdated != null){
            int regularCapacity = JsonHelper.getInt(defaultBundleOutdated.getAsJsonObject(), "capacity", 64);
            int largeCapacity = JsonHelper.getInt(defaultBundleOutdated.getAsJsonObject(), "large_capacity", 256);
            int breadBowlCapacity = JsonHelper.getInt(defaultBundleOutdated.getAsJsonObject(), "bread_bowl_capacity", 32);

            builtinBundles.put("regular", new BuiltinBundleInfo(true, regularCapacity));
            builtinBundles.put("large", new BuiltinBundleInfo(true, largeCapacity));
            builtinBundles.put("bread_bowl", new BuiltinBundleInfo(true, breadBowlCapacity));
        }else{
            errorTracker.addError("More Bundles config does not contain any info for builtin bundles.");

            builtinBundles.put("regular", new BuiltinBundleInfo(true, 64));
            builtinBundles.put("large", new BuiltinBundleInfo(true, 256));
            builtinBundles.put("bread_bowl", new BuiltinBundleInfo(true, 32));
        }

        JsonArray bundleInfo = jsonObject.getAsJsonArray("bundles");

        List<BundleInfo> bundleInfoTemp = new ArrayList<>();
        for (int i = 0; i < bundleInfo.size(); i++) {
            BundleInfo info = BundleInfo.deserialize(bundleInfo.get(i), errorTracker.sub(false));
            if(info != null){
                bundleInfoTemp.add(info);
            }
        }
        this.bundles = bundleInfoTemp.toArray(new BundleInfo[0]);

        JsonElement dispenseBundledItemsJson = jsonObject.get("dispense_bundled_items");
        boolean dispenseBundledItems = true;
        if(dispenseBundledItemsJson == null){
            //errorTracker.addError("More Bundles config does not contain the 'dispense_bundled_items' key!");
        }else if(!dispenseBundledItemsJson.isJsonPrimitive()){
            //errorTracker.addError("More Bundles config contains an invalid value for 'dispense_bundled_items'!");
        }else{
            dispenseBundledItems = dispenseBundledItemsJson.getAsBoolean();
        }
        this.dispenseBundledItems = dispenseBundledItems;

        //Log errors and warnings
        if(policy != ErrorPolicy.IGNORE){
            boolean crash = false;

            if(errorTracker.errors().size() > 0){
                System.err.println("Errors were found while loading " + MoreBundles.MOD_ID + " config!");
                for(String error : errorTracker.errors()){
                    System.err.println("  - " + error);
                }

                if(policy == ErrorPolicy.FAIL_ON_ERROR || policy == ErrorPolicy.FAIL_ON_WARNING){
                    crash = true;
                }
            }

            if(errorTracker.warnings().size() > 0){
                System.err.println("Warnings were found while loading " + MoreBundles.MOD_ID + " config!");
                for(String warning : errorTracker.warnings()){
                    System.err.println("  - " + warning);
                }

                if(policy == ErrorPolicy.FAIL_ON_WARNING){
                    crash = true;
                }
            }

            if(crash){
                throw new RuntimeException("Failed to load " + MoreBundles.MOD_ID + " config!");
            }
        }
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

    public BuiltinBundleInfo getBuiltin(String name) {
        return builtinBundles.get(name);
    }

    enum ErrorPolicy{
        IGNORE,
        WARN,
        FAIL_ON_ERROR,
        FAIL_ON_WARNING;
    }
}
