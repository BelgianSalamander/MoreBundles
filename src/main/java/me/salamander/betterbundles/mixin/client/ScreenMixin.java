package me.salamander.betterbundles.mixin.client;

import me.salamander.betterbundles.client.BetterBundleTooltipComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(Screen.class)
public abstract class ScreenMixin{
    @Shadow protected abstract void renderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y);

    @Inject(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;Ljava/util/Optional;II)V", at = @At("HEAD"), cancellable = true)
    private void renderCustomBundleTooltip(MatrixStack matrices, List<Text> lines, Optional<TooltipData> data, int x, int y, CallbackInfo ci){
        data.ifPresent((tooltipData -> {
            if(tooltipData instanceof BundleTooltipData){
                List<TooltipComponent> defaultTooltipComponents = lines.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
                defaultTooltipComponents.add(1, new BetterBundleTooltipComponent((BundleTooltipData) tooltipData));
                renderTooltipFromComponents(matrices, defaultTooltipComponents, x, y);
                ci.cancel();
            }
        }));
    }
}
