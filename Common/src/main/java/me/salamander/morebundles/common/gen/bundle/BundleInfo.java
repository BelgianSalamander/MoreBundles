package me.salamander.morebundles.common.gen.bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.gen.CustomResourcePack;
import me.salamander.morebundles.common.gen.ErrorTracker;
import me.salamander.morebundles.common.gen.assets.AssetGenerator;
import me.salamander.morebundles.common.gen.assets.ModelGenerator;
import me.salamander.morebundles.common.gen.assets.TextureGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BundleItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BundleInfo {
    private final String id;
    private final int capacity;
    private final BundleType type;
    private final JsonObject json;

    private final List<AssetGenerator> assetGenerators;

    public BundleInfo(JsonObject json, ErrorTracker errorTracker) {
        this.json = json;
        
        if (!json.has("id")) {
            errorTracker.addError("Bundle is missing id");
            this.id = "missing_id";
        } else {
            this.id = GsonHelper.getAsString(json, "id");
        }
        
        this.capacity = GsonHelper.getAsInt(json, "capacity", 64);
        
        this.type = BundleType.fromString(GsonHelper.getAsString(json, "type"), errorTracker);
        
        this.assetGenerators = new ArrayList<>();
        
        if (json.has("generators")) {
            JsonArray generators = GsonHelper.getAsJsonArray(json, "generators");
            for (JsonElement element : generators) {
                if (element.isJsonObject()) {
                    JsonObject generator = element.getAsJsonObject();
                    String generatorType = GsonHelper.getAsString(generator, "type");
                    
                    AssetGenerator assetGenerator = switch(generatorType) {
                        case "model" -> new ModelGenerator(Common.makeID(id), Common.makeID(id + "_filled"), Common.makeID(id), generator, errorTracker);
                        case "texture" -> new TextureGenerator(type == BundleType.SINGLE_ITEM, generator, errorTracker);
                        default -> {
                            errorTracker.addError("Unknown generator type: " + generatorType);
                            yield null;
                        }
                    };
                    
                    if (assetGenerator != null) {
                        assetGenerators.add(assetGenerator);
                    }
                } else {
                    errorTracker.addError("Generator is not an object");
                }
            }
        }
        
        this.assetGenerators.sort(Comparator.comparingInt(AssetGenerator::priority));
        
        for (AssetGenerator assetGenerator : assetGenerators) {
            assetGenerator.link(assetGenerators, errorTracker);
        }
    }
    
    public BundleInfo(String id, int capacity, BundleType type, boolean dyeable) {
        this.id = id;
        this.capacity = capacity;
        this.type = type;
        
        this.json = new JsonObject();
        json.addProperty("dyeable", dyeable);
        this.assetGenerators = new ArrayList<>();
    }
    
    private BundleInfo(String id, int capacity, BundleType type, JsonObject json, List<AssetGenerator> assetGenerators, ErrorTracker errorTracker) {
        this.id = id;
        this.capacity = capacity;
        this.type = type;
        this.json = json;
        this.assetGenerators = assetGenerators;
        
        assetGenerators.sort(Comparator.comparingInt(AssetGenerator::priority));
        
        for (AssetGenerator assetGenerator : assetGenerators) {
            assetGenerator.link(assetGenerators, errorTracker);
        }
    }

    public BundleItem createItem() {
        return type.generate(json, capacity);
    }
    
    public void generateAssets(CustomResourcePack pack, boolean isServer) {
        for (AssetGenerator assetGenerator : assetGenerators) {
            if (assetGenerator.isClientOnly() && isServer) continue;
            
            assetGenerator.generate(pack);
        }
    }
    
    /**
     * LEGACY CONFIG SUPPORT
     * Loads a bundle info from a json element
     * @param json the json element
     * @return the bundle info. null if there was an error
     */
    public static List<BundleInfo> deserialize(JsonElement json, ErrorTracker errorTracker){
        if(!json.isJsonObject()){
            errorTracker.addError("Bundle info must be an object");
            return null;
        }
        
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement element;
        
        JsonElement idElement = jsonObject.get("id");
        if(idElement == null){
            errorTracker.addError("Bundle info must have an id");
        }else if(!idElement.isJsonPrimitive()){
            errorTracker.addError("Bundle info id must be a string");
        }
        String id = idElement.getAsString();
        
        boolean hasNormal = true, hasLarge = true;
        
        if((element = jsonObject.get("create_normal")) != null){
            if(element.isJsonPrimitive()) {
                hasNormal = element.getAsBoolean();
            }else{
                errorTracker.addError("Bundle info create_normal must be a boolean");
            }
        }
        
        if((element = jsonObject.get("create_large")) != null){
            if(element.isJsonPrimitive()) {
                hasLarge = element.getAsBoolean();
            }else{
                errorTracker.addError("Bundle info create_large must be a boolean");
            }
        }
        
        int regularCapacity = -1, largeCapacity = -1;
        
        if(hasNormal){
            regularCapacity = jsonObject.get("capacity").getAsInt();
        }
        if(hasLarge){
            largeCapacity = jsonObject.get("large_capacity").getAsInt();
        }
        
        List<AssetGenerator> assetGeneratorsSmall = new ArrayList<>();
        List<AssetGenerator> assetGeneratorsLarge = new ArrayList<>();
        
        JsonObject texturesJson = jsonObject.getAsJsonObject("textures");
        
        if(texturesJson != null) {
            if (texturesJson.has("regular")) {
                JsonElement regularElement = texturesJson.get("regular");
                if (regularElement.isJsonObject()) {
                    JsonObject obj = regularElement.getAsJsonObject();
                    
                    if (obj.has("material")) {
                        ResourceLocation material = new ResourceLocation(obj.get("material").getAsString());
                        
                        assetGeneratorsSmall.add(
                                new TextureGenerator(
                                        false,
                                        material,
                                        Common.makeID(id),
                                        Common.makeID(id + "_filled")
                                )
                        );
                    }
                    
                    
                }else{
                    errorTracker.addError("Bundle info textures regular must be an object");
                }
            }
            
            if (texturesJson.has("large")) {
                JsonElement largeElement = texturesJson.get("large");
                if (largeElement.isJsonObject()) {
                    JsonObject obj = largeElement.getAsJsonObject();
                    
                    if (obj.has("material")) {
                        ResourceLocation material = new ResourceLocation(obj.get("material").getAsString());
                        assetGeneratorsLarge.add(
                                new TextureGenerator(
                                        true,
                                        material,
                                        Common.makeID("large_" + id),
                                        Common.makeID("large_" + id + "_filled")
                                )
                        );
                    }
                }else{
                    errorTracker.addError("Bundle info textures large must be an object");
                }
            }
        }
        
        boolean generateModels = GsonHelper.getAsBoolean(jsonObject, "generate_models", false);
        
        if(errorTracker.failed()){
            errorTracker.addError("Errors occurred while loading bundle info!");
            return null;
        }
        
        if (generateModels) {
            assetGeneratorsSmall.add(new ModelGenerator(Common.makeID(id), Common.makeID(id + "_filled"), Common.makeID(id)));
            assetGeneratorsLarge.add(new ModelGenerator(Common.makeID("large_" + id), Common.makeID("large_" + id + "_filled"), Common.makeID("large_" + id)));
        }
        
        List<BundleInfo> bundleInfos = new ArrayList<>();
        
        if(hasNormal){
            bundleInfos.add(new BundleInfo(id, regularCapacity, BundleType.REGULAR, new JsonObject(), assetGeneratorsSmall, errorTracker));
        }
        
        if(hasLarge){
            bundleInfos.add(new BundleInfo("large_" + id, largeCapacity, BundleType.SINGLE_ITEM, new JsonObject(), assetGeneratorsLarge, errorTracker));
        }
        
        return bundleInfos;
    }
    
    public String getId() {
        return id;
    }
    
}
