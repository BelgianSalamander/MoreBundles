package me.salamander.morebundles.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.salamander.morebundles.client.MoreBundlesTooltipComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(Screen.class)
public abstract class MixinScreen {
    
    @Shadow
    protected abstract void renderTooltipInternal(PoseStack p_169384_, List<ClientTooltipComponent> p_169385_, int p_169386_, int p_169387_);
    
    @Inject(method="renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;Ljava/util/Optional;II)V", at=@At("HEAD"), cancellable = true)
    private void renderTooltip(PoseStack matrices, List<Component> tooltip, Optional<TooltipComponent> data, int x, int y, CallbackInfo ci) {
        data.ifPresent(tooltipComponent -> {
            if(tooltipComponent instanceof BundleTooltip bundleTooltip){
                List<ClientTooltipComponent> defaultTooltipComponents = tooltip.stream().map(t -> ClientTooltipComponent.create(t.getVisualOrderText())).collect(Collectors.toList());
                
                MoreBundlesTooltipComponent component = new MoreBundlesTooltipComponent(bundleTooltip);
                
                defaultTooltipComponents.add(
                        1,
                        new MoreBundlesTooltipComponent(bundleTooltip)
                );
                
                if (component.hasExcessItems()) {
                    defaultTooltipComponents.add(
                            2,
                            ClientTooltipComponent.create(new TranslatableComponent("morebundles.excess_items").getVisualOrderText())
                    );
                }
                
                renderTooltipInternal(matrices, defaultTooltipComponents, x, y);
                ci.cancel();
            }
        });
    }
}
