package com.santiagogd.holobuilder;

import com.santiagogd.holobuilder.client.KeyBindings;
import com.santiagogd.holobuilder.client.ClientEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientInit {
    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientInit::onClientSetup);
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        KeyBindings.register();
    }
}
