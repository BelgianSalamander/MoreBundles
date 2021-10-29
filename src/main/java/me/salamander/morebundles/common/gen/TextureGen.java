package me.salamander.morebundles.common.gen;

import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TextureGen {
    private static final int[] BUNDLE_COLORS = new int[]{0xFF421E01, 0xFF4F2B10, 0xFF623220, 0xFF7D4034, 0xFFA6572C, 0xFFCD7B46};

    private static final BufferedImage BUNDLE = getMinecraftTexture("bundle");
    private static final BufferedImage BUNDLE_FILLED = getMinecraftTexture("bundle_filled");

    private static final BufferedImage LARGE_BUNDLE = getImage(TextureGen.class.getResourceAsStream("/assets/large_base.png"));
    private static final BufferedImage LARGE_BUNDLE_FILLED = getImage(TextureGen.class.getResourceAsStream("/assets/large_base_filled.png"));

    public static void main(String[] args) {
        saveImage(createBundle("lava_bucket", false, false), Path.of("run", "test.png"));
    }

    public static BufferedImage createBundle(Identifier materialIdentifier, boolean large, boolean filled) {
        return createBundle(getImage(MinecraftVersion.class.getResourceAsStream("/assets/" + materialIdentifier.getNamespace() + "/textures/item/" + materialIdentifier.getPath() + ".png")), large, filled);
    }

    public static BufferedImage createBundle(BufferedImage material, boolean large, boolean filled){
        Set<Integer> colors = new HashSet<>();
        for(int x = 0; x < material.getWidth(); x++){
            for (int y = 0; y < material.getHeight(); y++) {
                int color = material.getRGB(x, y);
                if(ColorModel.getRGBdefault().getAlpha(color) == 255){ //Alpha = 255
                    colors.add(color);
                }
            }
        }

        //System.out.println("Num Colors: " + colors.size());

        List<HSVColor> hsvColors = colors.stream().map(HSVColor::fromRGB).collect(Collectors.toList());
        hsvColors.sort(Comparator.comparingDouble(color -> color.v));

        List<List<HSVColor>> colorGroups = new ArrayList<>();
        colorGroups.add(new ArrayList<>());
        colorGroups.get(0).add(hsvColors.get(0));

        for (int i = 1; i < hsvColors.size(); i++) {
            HSVColor color = hsvColors.get(i);

            boolean foundGroup = false;
            for(List<HSVColor> group: colorGroups){
                if(hueDiff(color, group.get(group.size() - 1)) < 0.5f){
                    group.add(color);
                    foundGroup = true;
                    break;
                }
            }

            if(!foundGroup){
                List<HSVColor> newGroup = new ArrayList<>();
                newGroup.add(color);
                colorGroups.add(newGroup);
            }
        }

        List<HSVColor> colorGroup;

        if(Collections.max(colorGroups, Comparator.comparingInt(List::size)).size() < 6){
            //throw new IllegalStateException("Couldn't find enough colors");
            colorGroup = hsvColors;
        }else{
            colorGroup = colorGroups.stream().filter(l -> l.size() >= 6).findFirst().get();
        }

        //System.out.println("Num Groups: " + colorGroups.size());

        spread(colorGroup, BUNDLE_COLORS.length);
        int[] palette = colorGroup.stream().mapToInt(HSVColor::toRGB).toArray();

        return createBundleFromPalette(palette, large, filled);
    }

    public static BufferedImage createBundle(String materialName, boolean large, boolean filled){
        return createBundle(getMinecraftTexture(materialName), large, filled);
    }

    public static void spread(List<HSVColor> colors, int target){
        int remove = colors.size() - target;

        if(remove <= 0) return;

        if(colors.get(colors.size() - 1).v >= 0.95f){
            colors.remove(colors.size() - 1);
            remove--;
        }

        if(remove <= 0) return;

        float step = colors.size() / (float) remove;

        for(int i = remove - 1; i >= 0; i--){
            colors.remove((int) (step / 2 + step * i));
        }
    }

    public static BufferedImage createBundleFromPalette(int[] palette, boolean large, boolean filled){
        BufferedImage bundleImage = large ? (filled ? LARGE_BUNDLE_FILLED : LARGE_BUNDLE) : (filled ? BUNDLE_FILLED : BUNDLE);

        BufferedImage outBundle = new BufferedImage(bundleImage.getWidth(), bundleImage.getHeight(), bundleImage.getType());
        bundleImage.copyData(outBundle.getRaster());

        for (int x = 0; x < outBundle.getWidth(); x++) {
            for (int y = 0; y < outBundle.getHeight(); y++) {
                int color = outBundle.getRGB(x, y);
                for (int i = 0; i < 6; i++) {
                    if(color == BUNDLE_COLORS[i]){
                        outBundle.setRGB(x, y, palette[i]);
                        break;
                    }
                }
            }
        }

        return outBundle;
    }

    public static float hueDiff(HSVColor colorOne, HSVColor colorTwo){
        float diff1 = Math.abs(colorOne.h - colorTwo.h);

        float newH1 = colorOne.h, newH2 = colorTwo.h;

        if(colorOne.h > colorTwo.h){
            newH1 -= Math.PI * 2;
        }else{
            newH2 -= Math.PI * 2;
        }

        float diff2 = Math.abs(newH1 - newH2);

        return Math.min(diff1, diff2);
    }

    public static @Nullable BufferedImage getImage(InputStream is){
        try {
            return ImageIO.read(is);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static void saveImage(BufferedImage img, Path path){
        try {
            if(!Files.exists(path)){
                path.getParent().toFile().mkdirs();
                Files.createFile(path);
            }

            ImageIO.write(img, "PNG", path.toFile());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static BufferedImage getMinecraftTexture(String name){
        return getImage(MinecraftClient.class.getResourceAsStream("/assets/minecraft/textures/item/" + name + ".png"));
    }

    private static record HSVColor(float h, float s, float v){
        private static final float SIXTY_DEGREES = (float) (Math.PI * 2 / 6);

        public int toRGB(){
            float c = v * s;
            float x = c * (1 - Math.abs((h / SIXTY_DEGREES) % 2 - 1));

            float m = v - c;

            float red, green, blue;

            if(h < SIXTY_DEGREES){
                red = c;
                green = x;
                blue = 0;
            }else if(h < SIXTY_DEGREES * 2){
                red = x;
                green = c;
                blue = 0;
            }else if(h < SIXTY_DEGREES * 3){
                red = 0;
                green = c;
                blue = x;
            }else if(h < SIXTY_DEGREES * 4){
                red = 0;
                green = x;
                blue = c;
            }else if(h < SIXTY_DEGREES * 5){
                red = x;
                green = 0;
                blue = c;
            }else{
                red = c;
                green = 0;
                blue = x;
            }

            int r = (int) ((red + m) * 255);
            int g = (int) ((green + m) * 255);
            int b = (int) ((blue + m) * 255);

            return 0xff000000 + (r << 16) + (g << 8) + b;
        }

        public static HSVColor fromRGB(int rgb){
            int r = ColorModel.getRGBdefault().getRed(rgb);
            int g = ColorModel.getRGBdefault().getGreen(rgb);
            int b = ColorModel.getRGBdefault().getBlue(rgb);

            float red = r / 255.f;
            float green = g / 255.f;
            float blue = b / 255.f;

            float cmax = Math.max(red, Math.max(blue, green));
            float cmin = Math.min(red, Math.min(blue, green));

            float delta = cmax - cmin;

            float hue, saturation, value;

            if(delta == 0){
                hue = 0;
            }else if(cmax == red){
                hue = ((green - blue) / delta) % 6;
            }else if(cmax == green){
                hue = ((blue - red) / delta) + 2;
            }else{
                hue = ((red - green) / delta) + 4;
            }

            hue *= SIXTY_DEGREES; //Multiply by 60 degrees

            if(cmax == 0) saturation = 0;
            else saturation = delta / cmax;

            value = cmax;

            return new HSVColor(hue, saturation, value);
        }
    }
}
