package me.salamander.morebundles.common.items;

import com.mojang.math.Vector3d;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BreadBowlItem extends BundleItem {
    public static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder().alwaysEat().nutrition(10).saturationMod(1.2f).build();
    
    public BreadBowlItem(Properties $$0) {
        super($$0);
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Vec3 eyes = entity.getEyePosition();
        BundleHandler handler = ((MoreBundlesInfo) this).getHandler();
        for(ItemStack drop: handler.getAllItems(stack.getOrCreateTag())){
            Containers.dropItemStack(level, eyes.x, eyes.y, eyes.z, drop);
        }
        handler.clear(stack.getOrCreateTag());
        return entity.eat(level, stack);
    }
    
    @Override
    public boolean isEnchantable(ItemStack $$0) {
        return false;
    }
    
}
