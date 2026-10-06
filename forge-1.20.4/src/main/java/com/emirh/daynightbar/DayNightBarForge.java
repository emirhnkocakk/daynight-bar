package com.emirh.daynightbar;

import com.emirh.daynightbar.client.hud.DayNightHudRenderer;
import com.emirh.daynightbar.common.config.ModConfigHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("daynightbar")
public class DayNightBarForge {
    
    public DayNightBarForge() {
        // Register config on client setup
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        
        // Register the HUD renderer on the Forge event bus
        FMLJavaModLoadingContext.get().getModEventBus().register(ForgeHudRenderer.class);
    }
    
    private void clientSetup(FMLClientSetupEvent event) {
        ModConfigHolder.register();
    }
    
    @Mod.EventBusSubscriber(modid = "daynightbar", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeHudRenderer {
        @SubscribeEvent
        public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
            // Render after the hotbar/health overlay
            if (event.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.PLAYER_HEALTH.id())) {
                DayNightHudRenderer.render(event.getGuiGraphics(), event.getPartialTick());
            }
        }
    }
}