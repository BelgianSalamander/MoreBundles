package me.salamander.morebundles.common.gen.assets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.gen.CustomResourcePack;
import me.salamander.morebundles.common.gen.ErrorTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.List;

public class ModelGenerator implements AssetGenerator {
    private final ResourceLocation output;
    
    private ResourceLocation empty;
    private boolean defaultEmpty = true;
    private ResourceLocation filled;
    private boolean defaultFilled = true;
    
    public ModelGenerator(ResourceLocation defaultEmpty, ResourceLocation defaultFilled, ResourceLocation output, JsonObject json, ErrorTracker errorTracker) {
        this.output = output;
        
        this.empty = json.has("empty") ? new ResourceLocation(GsonHelper.getAsString(json, "empty")) : defaultEmpty;
        this.filled = json.has("filled") ? new ResourceLocation(GsonHelper.getAsString(json, "filled")) : defaultFilled;
        
        if (!json.has("empty")) {
            this.defaultEmpty = true;
        }
        
        if (!json.has("filled")) {
            this.defaultFilled = true;
        }
        
        if (!this.output.getNamespace().equals(Common.MOD_ID)) {
            errorTracker.addError("Model generator output must be in the " + Common.MOD_ID + " namespace");
        }
    }
    
    public ModelGenerator(ResourceLocation defaultEmpty, ResourceLocation defaultFilled, ResourceLocation output) {
        this.output = output;
        
        this.empty = defaultEmpty;
        this.filled = defaultFilled;
    }
    
    @Override
    public boolean isClientOnly() {
        return true;
    }
    
    @Override
    public void generate(CustomResourcePack pack) {
        ResourceLocation filledLoc = new ResourceLocation(output.getNamespace(), output.getPath() + "_filled");
        
        JsonObject empty = new JsonObject();
        JsonObject filled = new JsonObject();
        
        empty.addProperty("parent", "item/generated");
        filled.addProperty("parent", addItem(output).toString());
        
        JsonObject emptyTexture = new JsonObject();
        JsonObject filledTexture = new JsonObject();
        
        emptyTexture.addProperty("layer0", addItem(this.empty).toString());
        filledTexture.addProperty("layer0", addItem(this.filled).toString());
        
        empty.add("textures", emptyTexture);
        filled.add("textures", filledTexture);
    
        JsonArray overrides = new JsonArray();
        JsonObject override = new JsonObject();
        JsonObject predicate = new JsonObject();
        predicate.addProperty("filled", 0.0000001);
        override.add("predicate", predicate);
        override.addProperty("model", addItem(filledLoc).toString());
        overrides.add(override);
        empty.add("overrides", overrides);
        
        pack.addItemModel(output, empty);
        pack.addItemModel(filledLoc, filled);
    }
    
    private ResourceLocation addItem(ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), "item/" + location.getPath());
    }
    
    @Override
    public int priority() {
        return 1;
    }
    
    @Override
    public void link(List<AssetGenerator> generators, ErrorTracker errorTracker) {
        for (AssetGenerator generator : generators) {
            if (generator instanceof TextureGenerator textureGenerator) {
                if (defaultFilled) {
                    filled = textureGenerator.getOutputFilled();
                    defaultFilled = false;
                } else if (!filled.equals(textureGenerator.getOutputFilled())) {
                    errorTracker.addError("Mismatched filled textures for model " + output);
                }
                
                if (defaultEmpty) {
                    empty = textureGenerator.getOutputEmpty();
                    defaultEmpty = false;
                } else if (!empty.equals(textureGenerator.getOutputEmpty())) {
                    errorTracker.addError("Mismatched empty textures for model " + output);
                }
            }
        }
    }
}
