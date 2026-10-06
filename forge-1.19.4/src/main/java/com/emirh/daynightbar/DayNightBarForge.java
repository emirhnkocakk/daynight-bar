package com.emirh.daynightbar;

import com.emirh.daynightbar.client.hud.DayNightHudRenderer;
import com.emirh.daynightbar.common.config.ModConfigHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("daynightbar")
public class DayNightBarForge {
    
    public DayNightBarForge() {
        // Register config on client setup
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        
        // Register the HUD renderer on the Forge event bus (1.19.4 renderer has built-in event handler)
        FMLJavaModLoadingContext.get().getModEventBus().register(DayNightHudRenderer.class);
    }
    
    private void clientSetup(FMLClientSetupEvent event) {
        ModConfigHolder.register();
    }
}