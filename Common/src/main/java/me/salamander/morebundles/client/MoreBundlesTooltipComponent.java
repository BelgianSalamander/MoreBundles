package me.salamander.morebundles.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MoreBundlesTooltipComponent implements ClientTooltipComponent {
    private final NonNullList<ItemStack> items;
    private final int occupancy;
    private final int itemAmount;
    public static float itemScale = 1.0f;

    public MoreBundlesTooltipComponent(BundleTooltip bundleData){
        this.items = bundleData.getItems();
        this.occupancy = bundleData.getWeight();

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
    public int getWidth(Font font) {
        if(itemAmount == 0) return 0;
        return (int) ((Math.min(20, itemAmount) + 1) * 8 * itemScale);
    }
    
    @Override
    public void renderImage(Font font, int x, int y, PoseStack matrices, ItemRenderer itemRenderer, int z) {
        itemScale = 0.7f;
        if(items.size() == 0) return;

        //Split items into those that need light and those that don't while creating the models
        List<ItemDrawInfo> modelsThatNeedLight = new ArrayList<>();
        List<ItemDrawInfo> modelsThatDontNeedLight = new ArrayList<>();

        LocalPlayer player = Minecraft.getInstance().player;

        int offset = 0;
        for(ItemStack stack: items){
            BakedModel model = itemRenderer.getModel(stack, player.level, player, player.level.random.nextInt());

           (model.usesBlockLight() ? modelsThatNeedLight : modelsThatDontNeedLight).add(new ItemDrawInfo(stack, model, stack.getCount(), offset));
           offset += stack.getCount();
        }

        float zOffset = itemRenderer.blitOffset + 50.0F;

        ItemStack seedItem = items.get(0);
        Random random = new Random(seedItem.getCount() + seedItem.getDescriptionId().hashCode());

        int[] depths = new int[itemAmount];
        for(int j = 0; j < itemAmount; j++) depths[j] = j;
        shuffleArray(depths, random);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        itemRenderer.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableDepthTest();

        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        matrixStack.translate(x, y, 100.0F + zOffset);
        matrixStack.translate(8.0D * itemScale, 8.0D * itemScale, 0.0D);
        matrixStack.scale(1.0F, -1.0F, 1.0F);
        matrixStack.scale(16.0F * itemScale, 16.0F * itemScale, 0.1f);
        RenderSystem.applyModelViewMatrix();

        PoseStack extraTranslate = new PoseStack();

        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();

        addDrawsToBuffer(immediate, modelsThatDontNeedLight, depths, random, extraTranslate, itemRenderer);
        Lighting.setupForFlatItems();
        immediate.endBatch();

        addDrawsToBuffer(immediate, modelsThatNeedLight, depths, random, extraTranslate, itemRenderer);
        Lighting.setupFor3DItems();
        immediate.endBatch();

        matrixStack.popPose();
    }

    private static void addDrawsToBuffer(MultiBufferSource.BufferSource vertexConsumerProvider, List<ItemDrawInfo> items, int[] depths, Random random, PoseStack matrixStack, ItemRenderer itemRenderer){
        for(ItemDrawInfo drawInfo: items){
            int i = drawInfo.offsetFromStart;
            for(int j = 0; j < drawInfo.amount; j++){
                int drawIndex = i + j;

                float xOffset = drawIndex % 20;
                float yOffset = drawIndex / 20;

                xOffset += random.nextFloat() - 0.5f;
                yOffset += random.nextFloat() - 0.5f;

                matrixStack.pushPose();
                matrixStack.translate(xOffset * 0.5f, - yOffset * 0.5f, depths[drawIndex]);

                itemRenderer.render(drawInfo.itemStack, ItemTransforms.TransformType.GUI, false, matrixStack, vertexConsumerProvider, 15728880, OverlayTexture.NO_OVERLAY, drawInfo.model);

                matrixStack.popPose();
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
