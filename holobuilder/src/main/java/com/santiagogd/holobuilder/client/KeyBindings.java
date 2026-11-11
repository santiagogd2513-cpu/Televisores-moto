package com.santiagogd.holobuilder.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class KeyBindings {
    public static final String CATEGORY = "key.categories.holobuilder";
    public static KeyMapping OPEN_MENU;

    public static void register() {
        // registration happens via event
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_MENU = new KeyMapping(
                "key.holobuilder.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                CATEGORY
        );
        event.register(OPEN_MENU);
    }
}
