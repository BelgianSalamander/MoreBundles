package me.salamander.morebundles.common.gen.assets;

import com.google.gson.JsonObject;
import me.salamander.morebundles.common.Common;
import me.salamander.morebundles.common.gen.CustomResourcePack;
import me.salamander.morebundles.common.gen.ErrorTracker;
import me.salamander.morebundles.common.gen.TextureGen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TextureGenerator implements AssetGenerator {
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    
    private final boolean large;
    
    private final ResourceLocation material;
    
    private final ResourceLocation outputEmpty;
    private final ResourceLocation outputFilled;
    
    public TextureGenerator(boolean large, JsonObject json, ErrorTracker errorTracker) {
        this.large = large;
        
        if (!json.has("material")) {
            errorTracker.addError("Texture generator is missing material");
            this.material = new ResourceLocation("missing_material");
        } else {
            this.material = new ResourceLocation(GsonHelper.getAsString(json, "material"));
        }
        
        if (json.has("output_empty")) {
            this.outputEmpty = new ResourceLocation(GsonHelper.getAsString(json, "output_empty"));
        } else {
            this.outputEmpty = new ResourceLocation("morebundles", "bundle_" + (large ? "large" : "small") + "_empty_" + idCounter.getAndIncrement() + ".png");
        }
        
        if (json.has("output_filled")) {
            this.outputFilled = new ResourceLocation(GsonHelper.getAsString(json, "output_filled"));
        } else {
            this.outputFilled = new ResourceLocation("morebundles", "bundle_" + (large ? "large" : "small") + "_filled_" + idCounter.getAndIncrement() + ".png");
        }
        
        if (!this.outputEmpty.getNamespace().equals(Common.MOD_ID)) {
            errorTracker.addError("Texture generator output_empty must be in the " + Common.MOD_ID + " namespace");
        }
        
        if (!this.outputFilled.getNamespace().equals(Common.MOD_ID)) {
            errorTracker.addError("Texture generator output_filled must be in the " + Common.MOD_ID + " namespace");
        }
    }
    
    public TextureGenerator(boolean large, ResourceLocation material, ResourceLocation outputEmpty, ResourceLocation outputFilled) {
        this.large = large;
        this.material = material;
        this.outputEmpty = outputEmpty;
        this.outputFilled = outputFilled;
    }
    
    public boolean isLarge() {
        return large;
    }
    
    public ResourceLocation getOutputEmpty() {
        return outputEmpty;
    }
    
    public ResourceLocation getOutputFilled() {
        return outputFilled;
    }
    
    @Override
    public boolean isClientOnly() {
        return true;
    }
    
    @Override
    public void generate(CustomResourcePack pack) {
        pack.addItemTexture(
                outputEmpty,
                TextureGen.createBundle(material, large, false)
        );
        
        pack.addItemTexture(
                outputFilled,
                TextureGen.createBundle(material, large, true)
        );
    }
    
    @Override
    public int priority() {
        return 0;
    }
    
    @Override
    public void link(List<AssetGenerator> generators, ErrorTracker errorTracker) {
    
    }
}
