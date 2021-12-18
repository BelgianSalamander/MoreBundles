package me.salamander.morebundles.common.gen;

import com.google.gson.*;
import me.salamander.morebundles.common.Common;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BundleInfo {
    private final String id;

    private final boolean hasNormal;
    private final boolean hasLarge;

    private final int regularCapacity;
    private final int largeCapacity;

    private final boolean generateModels;

    private final BundleTextureInfo[] bundleTextures;

    public BundleInfo(String id, boolean hasNormal, boolean hasLarge, int regularCapacity, int largeCapacity, BundleTextureInfo[] bundleTextures, boolean generateModels) {
        this.id = id;
        this.hasNormal = hasNormal;
        this.hasLarge = hasLarge;
        this.regularCapacity = regularCapacity;
        this.largeCapacity = largeCapacity;
        this.bundleTextures = bundleTextures;
        this.generateModels = generateModels;
    }

    /**
     * Loads a bundle info from a json element
     * @param json the json element
     * @return the bundle info. null if there was an error
     */
    public static BundleInfo deserialize(JsonElement json, ErrorTracker errorTracker){
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

        List<BundleTextureInfo> textures = new ArrayList<>();

        JsonObject texturesJson = jsonObject.getAsJsonObject("textures");

        if(texturesJson != null) {
            if (texturesJson.has("regular")) {
                JsonElement regularElement = texturesJson.get("regular");
                if (regularElement.isJsonObject()) {
                    JsonObject obj = regularElement.getAsJsonObject();

                    BundleTextureInfo info = BundleTextureInfo.fromJson(obj, Common.makeID("item/" + id), false, false, errorTracker.sub(false));
                    if (info != null) {
                        textures.add(info);
                    }

                    info = BundleTextureInfo.fromJson(obj, Common.makeID("item/" + id + "_filled"), false, true, errorTracker.sub(false));
                    if (info != null) {
                        textures.add(info);
                    }
                }else{
                    errorTracker.addError("Bundle info textures regular must be an object");
                }
            }

            if (texturesJson.has("large")) {
                JsonElement largeElement = texturesJson.get("large");
                if (largeElement.isJsonObject()) {
                    JsonObject obj = largeElement.getAsJsonObject();

                    BundleTextureInfo info = BundleTextureInfo.fromJson(obj, Common.makeID("item/large_" + id), true, false, errorTracker.sub(false));
                    if(info != null){
                        textures.add(info);
                    }

                    info = BundleTextureInfo.fromJson(obj, Common.makeID("item/large_" + id + "_filled"), true, true, errorTracker.sub(false));
                    if(info != null){
                        textures.add(info);
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

        return new BundleInfo(id, hasNormal, hasLarge, regularCapacity, largeCapacity, textures.toArray(new BundleTextureInfo[0]), generateModels);
    }

    public boolean generateModels() {
        return generateModels;
    }

    public static record BundleTextureInfo(ResourceLocation id, Supplier<BufferedImage> imageSupplier){
        public static BundleTextureInfo fromJson(JsonObject object, ResourceLocation id, boolean large, boolean filled, ErrorTracker errorTracker){
            JsonElement typeElement = object.get("type");
            if(typeElement == null){
                errorTracker.addError("Bundle texture info must have a type");
                return null;
            }else if(!typeElement.isJsonPrimitive()){
                errorTracker.addError("Bundle texture info type must be a string");
                return null;
            }

            String type = typeElement.getAsString();

            if(type.equals("material")){
                JsonElement materialElement = object.get("material");
                if(materialElement == null){
                    errorTracker.addError("Bundle texture info material must be specified");
                    return null;
                }else if(!materialElement.isJsonPrimitive()){
                    errorTracker.addError("Bundle texture info material must be a string");
                    return null;
                }
                String material = materialElement.getAsString();

                ResourceLocation materialResourceLocation = new ResourceLocation(material);

                return new BundleTextureInfo(id, () -> TextureGen.createBundle(materialResourceLocation, large, filled));
            }

            errorTracker.addError("Unknown texture generator type '" + type + "'");
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public boolean hasNormal() {
        return hasNormal;
    }

    public boolean hasLarge() {
        return hasLarge;
    }

    public int getRegularCapacity() {
        return regularCapacity;
    }

    public int getLargeCapacity() {
        return largeCapacity;
    }

    public BundleTextureInfo[] getBundleTextures() {
        return bundleTextures;
    }
}
