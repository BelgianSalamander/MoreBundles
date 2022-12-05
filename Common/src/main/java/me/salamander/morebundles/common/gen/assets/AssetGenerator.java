package me.salamander.morebundles.common.gen.assets;

import me.salamander.morebundles.common.gen.CustomResourcePack;
import me.salamander.morebundles.common.gen.ErrorTracker;

import java.util.List;

public interface AssetGenerator {
    boolean isClientOnly();
    
    void generate(CustomResourcePack pack);
    
    int priority();
    
    void link(List<AssetGenerator> generators, ErrorTracker errorTracker);
}
