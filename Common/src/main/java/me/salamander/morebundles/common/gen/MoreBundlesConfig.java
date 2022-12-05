package me.salamander.morebundles.common.gen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.gen.bundle.BundleInfo;
import me.salamander.morebundles.common.gen.bundle.BundleType;
import me.salamander.morebundles.common.items.MoreBundlesItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MoreBundlesConfig {
    private static final Logger LOGGER = Common.LOGGER;
    
    private boolean dispenseBundledItems;
    private boolean bundleLoaderEnabled;
    private int defaultBundleCapacity;

    private final List<BundleInfo> bundles = new ArrayList<>();
    
    public MoreBundlesConfig(JsonObject jsonObject){
        int configVersion = GsonHelper.getAsInt(jsonObject, "config_version", 0);
        
        //Get fail policy
        ErrorPolicy policy;
        JsonElement failPolicy = jsonObject.get("fail_policy");
        if(failPolicy == null){
            LOGGER.warning("No fail policy specified, defaulting to 'fail_on_error'");
            policy = ErrorPolicy.FAIL_ON_ERROR;
        }else if(failPolicy.isJsonPrimitive()){
            String failPolicyString = failPolicy.getAsString();
            if(failPolicyString.equalsIgnoreCase("fail on error")){
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
    
        if (configVersion == 0) {
            LOGGER.severe("No config_version specified, loading legacy config! This will be completely removed in the future! Please update your configs to the " +
                    "new format! This can easily be done by deleting the 'morebundles' config folder and re-launching the game which will regenerate the config files.");
            loadLegacyConfig(jsonObject, policy, errorTracker);
        } else if (configVersion == 1) {
            LOGGER.info("Loading config version 1");
            loadConfigV1(jsonObject, policy, errorTracker);
        } else {
            errorTracker.addError("Invalid config version: " + configVersion);
        }
    
        boolean crash = false;
    
        if(errorTracker.errors().size() > 0){
            LOGGER.severe("Errors were found while loading " + Common.MOD_ID + " config!");
            for(String error : errorTracker.errors()){
                LOGGER.severe("  - " + error);
            }
    
            crash = true;
        }
    
        if(errorTracker.warnings().size() > 0){
            LOGGER.warning("Warnings were found while loading " + Common.MOD_ID + " config!");
            for(String warning : errorTracker.warnings()){
                LOGGER.warning("  - " + warning);
            }
        
            if(policy == ErrorPolicy.FAIL_ON_WARNING){
                crash = true;
            }
        }
    
        if(crash){
            throw new RuntimeException("Failed to load " + Common.MOD_ID + " config!");
        }
    }
    
    private void loadConfigV1(JsonObject jsonObject, ErrorPolicy policy, ErrorTracker errorTracker) {
        this.bundleLoaderEnabled = GsonHelper.getAsBoolean(jsonObject, "bundle_loader_enabled", true);
        this.defaultBundleCapacity = GsonHelper.getAsInt(jsonObject, "regular_bundle_capacity", 64);
        this.dispenseBundledItems = GsonHelper.getAsBoolean(jsonObject, "dispense_bundled_items", true);
        
        if (!jsonObject.has("bundles") || !jsonObject.get("bundles").isJsonArray()) {
            errorTracker.addError("No bundles specified!");
            return;
        }
    
        JsonArray bundles = GsonHelper.getAsJsonArray(jsonObject, "bundles");
        
        for (JsonElement bundleElement : bundles) {
            if (!bundleElement.isJsonObject()) {
                errorTracker.addError("Invalid bundle: " + bundleElement);
                continue;
            }
        
            JsonObject bundle = bundleElement.getAsJsonObject();
            
            this.bundles.add(new BundleInfo(bundle, errorTracker));
        }
    }
    
    private void loadLegacyConfig(JsonObject jsonObject, ErrorPolicy policy, ErrorTracker errorTracker) {
        
        JsonElement builtinBundleInfo = jsonObject.get("builtin_bundles");
        JsonElement defaultBundleOutdated = jsonObject.get("regular_bundle");
        
        if(defaultBundleOutdated != null){
            errorTracker.addWarning("The 'regular_bundle' key is deprecated, use 'default_bundle' instead. For more information see https://github.com/BelgianSalamander/MoreBundles/wiki/Config-Changes#013"); //TODO: link to documentation
        }
        
        String[] BUILTIN_BUNDLES = {"regular", "large", "bread_bowl"};
        int[] BUILTIN_BUNDLE_DEFAULT_CAPACITIES = {64, 256, 32};
        
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

                if (name.equals("regular")) {
                    defaultBundleCapacity = capacity;
                } else if (name.equals("large") && enabled) {
                    bundles.add(new BundleInfo("large_bundle", capacity, BundleType.SINGLE_ITEM, true));
                } else if (name.equals("bread_bowl") && enabled) {
                    bundles.add(new BundleInfo("bread_bowl", capacity, BundleType.BREAD_BOWL, false));
                }
            }

            for (int i = 0; i < BUILTIN_BUNDLES.length; i++) {
                String name = BUILTIN_BUNDLES[i];
                if(!builtinBundleInfoObject.has(name)){
                    errorTracker.addError("Builtin bundle '" + name + "' is missing from the builtin bundle list!");
                }
            }
        }else if(defaultBundleOutdated != null){
            int regularCapacity = GsonHelper.getAsInt(defaultBundleOutdated.getAsJsonObject(), "capacity", 64);
            int largeCapacity = GsonHelper.getAsInt(defaultBundleOutdated.getAsJsonObject(), "large_capacity", 256);
            int breadBowlCapacity = GsonHelper.getAsInt(defaultBundleOutdated.getAsJsonObject(), "bread_bowl_capacity", 32);

            this.defaultBundleCapacity = regularCapacity;
            bundles.add(new BundleInfo("large_bundle", largeCapacity, BundleType.SINGLE_ITEM, true));
            bundles.add(new BundleInfo("bread_bowl", breadBowlCapacity, BundleType.BREAD_BOWL, false));
        }else{
            errorTracker.addError("More Bundles config does not contain any info for builtin bundles.");

            this.defaultBundleCapacity = 64;
            bundles.add(new BundleInfo("large_bundle", 256, BundleType.SINGLE_ITEM, true));
            bundles.add(new BundleInfo("bread_bowl", 32, BundleType.BREAD_BOWL, false));
        }
        
        JsonArray bundleInfo = jsonObject.getAsJsonArray("bundles");
        
        for (int i = 0; i < bundleInfo.size(); i++) {
            var result = BundleInfo.deserialize(bundleInfo.get(i), errorTracker.sub(false));
            if (result != null) {
                bundles.addAll(result);
            }
        }
        
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
        
        JsonElement enableBundleLoaderJson = jsonObject.get("bundle_loader_enabled");
        boolean enableBundleLoader = true;
        if(enableBundleLoaderJson != null && enableBundleLoaderJson.isJsonPrimitive()){
            enableBundleLoader = enableBundleLoaderJson.getAsBoolean();
        }
        this.bundleLoaderEnabled = enableBundleLoader;
    }
    
    public boolean dispenseBundledItems(){
        return dispenseBundledItems;
    }

    public void createItems() {
        for(BundleInfo bundleInfo: bundles){
            MoreBundlesItems.addCustomBundle(bundleInfo.getId(), bundleInfo::createItem);
        }
    }

    public void registerAssets(boolean isClient, CustomResourcePack resourcePack) {
        long startTime = System.currentTimeMillis();

        for (BundleInfo bundleInfo : bundles) {
            bundleInfo.generateAssets(resourcePack, !isClient);
        }

        try {
            long resourceImportStart = System.currentTimeMillis();
            Path resourcePath = Common.CONFIG_FOLDER.resolve("morebundles/resources/");
            //Manually load cause the .load method is bad
            Stream<Path> files = Files.walk(resourcePath);
            for(Path file: (Iterable<Path>) () -> files.filter(Files::isRegularFile).map(resourcePath::relativize).iterator()){
                Path fullPath = resourcePath.resolve(file);
                String fileName = file.toString();
                if(fileName.startsWith("assets") && isClient) {
                    String path = fileName.substring("assets".length() + 1);
                    int sep = path.indexOf(File.separator);
                    String namespace = path.substring(0, sep);
                    String dataPath = path.substring(sep + 1).replace(File.separatorChar, '/');
                    resourcePack.addAsset(new ResourceLocation(namespace, dataPath), Files.readAllBytes(fullPath));
                } else if(fileName.startsWith("data")) {
                    String path = fileName.substring("data".length() + 1);
                    int sep = path.indexOf(File.separator);
                    String namespace = path.substring(0, sep);
                    String dataPath = path.substring(sep + 1).replace(File.separatorChar, '/');
                    resourcePack.addData(new ResourceLocation(namespace, dataPath), Files.readAllBytes(fullPath));
                }
            }
            LOGGER.info("Imported resources in " + (System.currentTimeMillis() - resourceImportStart) + "ms");
        }catch (IOException e){
            throw new IllegalStateException("Couldn't load more bundles config resources!", e);
        }

        LOGGER.info("Generated and loaded `More Bundles!` resources in " + (System.currentTimeMillis() - startTime) + " ms");
    }
    
    public boolean bundleLoaderEnabled(){
        return bundleLoaderEnabled;
    }
    
    public int regularBundleCapacity() {
        return defaultBundleCapacity;
    }
    
    enum ErrorPolicy{
        FAIL_ON_ERROR,
        FAIL_ON_WARNING;
    }
}
