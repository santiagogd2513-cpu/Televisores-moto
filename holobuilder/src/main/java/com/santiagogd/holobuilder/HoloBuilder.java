package com.santiagogd.holobuilder;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(HoloBuilder.MOD_ID)
public class HoloBuilder {
    public static final String MOD_ID = "holobuilder";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HoloBuilder() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientInit.init();
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("HoloBuilder client setup complete");
    }
}
