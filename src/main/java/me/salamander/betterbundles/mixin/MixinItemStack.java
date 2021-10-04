package me.salamander.betterbundles.mixin;

import me.salamander.betterbundles.common.ExtraBundleInfo;
import me.salamander.betterbundles.common.enchantment.ExtractEnchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @Shadow private int count;

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult useOnBlockAndRestock(Item item, ItemUsageContext context){
        if(context.getPlayer().isCreative()) return item.useOnBlock(context);

        int prevCount = context.getStack().getCount();
        ActionResult result = item.useOnBlock(context);

        if(prevCount > 0 && context.getStack().getCount() == 0){
            for(ItemStack invItem: context.getPlayer().getInventory().main){
                if(invItem.getItem() instanceof ExtraBundleInfo.Access bundle){
                    if(EnchantmentHelper.getLevel(ExtractEnchantment.INSTANCE, invItem) != 0) {
                        NbtList bundleItems = bundle.getItems(invItem);
                        for (int i = 0; i < bundleItems.size(); i++) {
                            ItemStack stackFromBundle = ItemStack.fromNbt(bundleItems.getCompound(i));
                            if (stackFromBundle.getItem() == item) {
                                bundleItems.remove(i);
                                context.getPlayer().getInventory().setStack(context.getPlayer().getInventory().selectedSlot, stackFromBundle);
                                return result;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
