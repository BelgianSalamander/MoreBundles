package me.salamander.morebundles.common.gen;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

//Some of this code is taken from https://github.com/Devan-Kerman/ARRP/blob/3330d436cbdf9f43bc796ca2b6619069b0d34694/src/main/java/net/devtech/arrp/impl/RuntimeResourcePackImpl.java
public class CustomResourcePack implements PackResources {
    private final String namespace;
    
    private final Map<String, byte[]> root = new HashMap<>();
    private final Map<ResourceLocation, byte[]> data = new HashMap<>();
    private final Map<ResourceLocation, byte[]> assets = new HashMap<>();
    
    public CustomResourcePack(String namespace) {
        this.namespace = namespace;
    }
    
    public void addItemTexture(ResourceLocation id, BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            ImageIO.write(image, "png", outputStream);
        }catch(IOException e){
            e.printStackTrace();
        }
        this.addAsset(generateProperLocation(id, "textures/item", "png"), outputStream.toByteArray());
    }
    
    public void addItemModel(ResourceLocation id, JsonObject json) {
        this.addAsset(generateProperLocation(id, "models/item", "json"), json.toString().getBytes());
    }
    
    public byte[] addRootResource(String path, byte[] data) {
        this.root.put(path, data);
        return data;
    }
    
    public byte[] addData(ResourceLocation path, byte[] data) {
        this.data.put(path, data);
        return data;
    }
    
    public byte[] addAsset(ResourceLocation path, byte[] data) {
        this.assets.put(path, data);
        return data;
    }
    
    @Nullable
    @Override
    public InputStream getRootResource(String s) throws IOException {
        return new ByteArrayInputStream(this.root.get(s));
    }
    
    @Override
    public InputStream getResource(PackType packType, ResourceLocation resourceLocation) throws IOException {
        if(packType == PackType.CLIENT_RESOURCES) {
            return new ByteArrayInputStream(this.assets.get(resourceLocation));
        }else{
            return new ByteArrayInputStream(this.data.get(resourceLocation));
        }
    }
    
    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String s, String s1, int i, Predicate<String> predicate) {
        Set<ResourceLocation> resources = new HashSet<>();
        Map<ResourceLocation, byte[]> map = packType == PackType.CLIENT_RESOURCES ? this.assets : this.data;
        
        for(ResourceLocation resourceLocation : map.keySet()) {
            if(resourceLocation.getNamespace().equals(s) && resourceLocation.getPath().startsWith(s1) && predicate.test(resourceLocation.getPath())) {
                resources.add(resourceLocation);
            }
        }
        
        return resources;
    }
    
    @Override
    public boolean hasResource(PackType packType, ResourceLocation resourceLocation) {
        if(packType == PackType.CLIENT_RESOURCES) {
            return this.assets.containsKey(resourceLocation);
        }else{
            return this.data.containsKey(resourceLocation);
        }
    }
    
    @Override
    public Set<String> getNamespaces(PackType packType) {
        return Set.of(this.namespace);
    }
    
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        if(metadataSectionSerializer.getMetadataSectionName().equals("pack")) {
            JsonObject obj = new JsonObject();
            obj.addProperty("pack_format", 8);
            obj.addProperty("description", "More Bundles Resources");
            return metadataSectionSerializer.fromJson(obj);
        }
        System.err.println("Unknown metadata section: " + metadataSectionSerializer.getMetadataSectionName());
        return metadataSectionSerializer.fromJson(new JsonObject());
    }
    
    @Override
    public String getName() {
        return "More Bundles Resources";
    }
    
    @Override
    public void close() {}
    
    private static ResourceLocation generateProperLocation(ResourceLocation original, String type, String extension){
        return new ResourceLocation(original.getNamespace(), type + "/" + original.getPath() + "." + extension);
    }
}
