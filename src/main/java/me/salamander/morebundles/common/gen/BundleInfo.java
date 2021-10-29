package me.salamander.morebundles.common.gen;

import com.google.gson.*;
import me.salamander.morebundles.MoreBundles;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
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

    public static BundleInfo deserialize(JsonElement json){
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement element;

        String id = jsonObject.get("id").getAsString();

        boolean hasNormal = true, hasLarge = true;

        if((element = jsonObject.get("create_normal")) != null){
            hasNormal = element.getAsBoolean();
        }

        if((element = jsonObject.get("create_large")) != null){
            hasLarge = element.getAsBoolean();
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
                textures.add(BundleTextureInfo.fromJson(texturesJson.get("regular").getAsJsonObject(), MoreBundles.ID("item/" + id), false, false));
                textures.add(BundleTextureInfo.fromJson(texturesJson.get("regular").getAsJsonObject(), MoreBundles.ID("item/" + id + "_filled"), false, true));
            }

            if (texturesJson.has("large")) {
                textures.add(BundleTextureInfo.fromJson(texturesJson.get("large").getAsJsonObject(), MoreBundles.ID("item/large_" + id), true, false));
                textures.add(BundleTextureInfo.fromJson(texturesJson.get("large").getAsJsonObject(), MoreBundles.ID("item/large_" + id + "_filled"), true, true));
            }
        }

        boolean generateModels = JsonHelper.getBoolean(jsonObject, "generate_models", false);

        return new BundleInfo(id, hasNormal, hasLarge, regularCapacity, largeCapacity, textures.toArray(new BundleTextureInfo[0]), generateModels);
    }

    public boolean generateModels() {
        return generateModels;
    }

    public static record BundleTextureInfo(Identifier id, Supplier<BufferedImage> imageSupplier){
        public static BundleTextureInfo fromJson(JsonObject object, Identifier id, boolean large, boolean filled){
            String type = object.get("type").getAsString();

            if(type.equals("material")){
                String material = object.get("material").getAsString();

                Identifier materialIdentifier = new Identifier(material);

                return new BundleTextureInfo(id, () -> TextureGen.createBundle(materialIdentifier, large, filled));
            }

            throw new IllegalStateException("Unknown texture generator type '" + type + "'");
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
