package me.salamander.morebundles.client;

import me.salamander.morebundles.MoreBundles;
import me.salamander.morebundles.common.block.BundleLoaderBlockEntity;
import me.salamander.morebundles.common.block.BundleLoaderScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class MoreBundlesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BundleLoaderBlockEntity.SCREEN_HANDLER_TYPE, BundleLoaderScreen::new);
    }
}
