package com.timegun.mod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TimeGun.MODID, value = Dist.CLIENT)
public class TimeGunClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // We will put our visual effects and client-side setup here in Phase 7!
    }
}