package me.salamander.morebundles.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class MoreBundlesTooltipComponent implements TooltipComponent {
    private final DefaultedList<ItemStack> items;
    private final int occupancy;
    private final int itemAmount;
    public static float itemScale = 1.0f;

    public MoreBundlesTooltipComponent(BundleTooltipData bundleData){
        this.items = bundleData.getInventory();
        this.occupancy = bundleData.getBundleOccupancy();

        int itemAmount = 0;
        for(ItemStack itemStack: items){
            itemAmount += itemStack.getCount();
        }

        this.itemAmount = itemAmount;
    }

    @Override
    public int getHeight() {
        if(itemAmount == 0) return 0;

        return (int) ((Math.ceil(itemAmount / 20.f) + 1.6f) * 8 * itemScale);
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        if(itemAmount == 0) return 0;
        return (int) ((Math.min(20, itemAmount) + 1) * 8 * itemScale);
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z, TextureManager textureManager) {
        itemScale = 0.7f;
        if(items.size() == 0) return;

        //Split items into those that need light and those that don't while creating the models
        List<ItemDrawInfo> modelsThatNeedLight = new ArrayList<>();
        List<ItemDrawInfo> modelsThatDontNeedLight = new ArrayList<>();

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        int offset = 0;
        for(ItemStack stack: items){
            BakedModel model = itemRenderer.getHeldItemModel(stack, player.world, player, player.world.random.nextInt());

           (model.isSideLit() ? modelsThatNeedLight : modelsThatDontNeedLight).add(new ItemDrawInfo(stack, model, stack.getCount(), offset));
           offset += stack.getCount();
        }

        float zOffset = itemRenderer.zOffset + 50.0F;

        ItemStack seedItem = items.get(0);
        Random random = new Random(seedItem.getCount() + seedItem.getName().hashCode());

        int[] depths = new int[itemAmount];
        for(int j = 0; j < itemAmount; j++) depths[j] = j;
        shuffleArray(depths, random);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        itemRenderer.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableDepthTest();

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x, y, 100.0F + zOffset);
        matrixStack.translate(8.0D * itemScale, 8.0D * itemScale, 0.0D);
        matrixStack.scale(1.0F, -1.0F, 1.0F);
        matrixStack.scale(16.0F * itemScale, 16.0F * itemScale, 0.1f);
        RenderSystem.applyModelViewMatrix();

        MatrixStack extraTranslate = new MatrixStack();

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        addDrawsToBuffer(immediate, modelsThatDontNeedLight, depths, random, extraTranslate, itemRenderer);
        DiffuseLighting.disableGuiDepthLighting();
        immediate.draw();

        addDrawsToBuffer(immediate, modelsThatNeedLight, depths, random, extraTranslate, itemRenderer);
        DiffuseLighting.enableGuiDepthLighting();
        immediate.draw();

        matrixStack.pop();
    }

    private static void addDrawsToBuffer(VertexConsumerProvider vertexConsumerProvider, List<ItemDrawInfo> items, int[] depths, Random random, MatrixStack matrixStack, ItemRenderer itemRenderer){
        for(ItemDrawInfo drawInfo: items){
            int i = drawInfo.offsetFromStart;
            for(int j = 0; j < drawInfo.amount; j++){
                int drawIndex = i + j;

                float xOffset = drawIndex % 20;
                float yOffset = drawIndex / 20;

                xOffset += random.nextFloat() - 0.5f;
                yOffset += random.nextFloat() - 0.5f;

                matrixStack.push();
                matrixStack.translate(xOffset * 0.5f, - yOffset * 0.5f, depths[drawIndex]);

                itemRenderer.renderItem(drawInfo.itemStack, ModelTransformation.Mode.GUI, false, matrixStack, vertexConsumerProvider, 15728880, OverlayTexture.DEFAULT_UV, drawInfo.model);

                matrixStack.pop();
            }
        }
    }

    private static void shuffleArray(int[] array, Random random){
        for(int i = array.length - 1; i > 0; i--){
            int index = random.nextInt(i + 1);

            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private static record ItemDrawInfo(ItemStack itemStack, BakedModel model, int amount, int offsetFromStart){}
}
