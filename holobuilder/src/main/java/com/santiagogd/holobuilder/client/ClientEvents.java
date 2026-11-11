package com.santiagogd.holobuilder.client;

import com.santiagogd.holobuilder.client.gui.HoloMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (KeyBindings.OPEN_MENU != null && KeyBindings.OPEN_MENU.consumeClick()) {
            mc.setScreen(new HoloMenuScreen());
        }
    }
}
