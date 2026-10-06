package com.emirh.daynightbar.client.hud;

import com.emirh.daynightbar.client.util.TimeUtils;
import com.emirh.daynightbar.common.config.ConfigProvider;
import com.emirh.daynightbar.common.config.ConfigProviderHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DayNightHudRenderer {
    
    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation("daynightbar", "hud/sun.png");
    private static final ResourceLocation MOON_TEXTURE = new ResourceLocation("daynightbar", "hud/moon.png");
    
    private DayNightHudRenderer() {}
    
    // Color definitions (ARGB)
    private static final int PALE_YELLOW     = 0xFFE8E8A0; // 6:00-11:00
    private static final int BRIGHT_YELLOW   = 0xFFFFFF00; // 12:00-15:30
    private static final int PALE_ORANGE     = 0xFFF0B060; // 15:30-17:00
    private static final int SUNSET_ORANGE   = 0xFFFF6030; // 17:00-19:00
    private static final int TWILIGHT_BLUE1  = 0xFF4060A0; // 19:00-21:00
    private static final int TWILIGHT_BLUE2  = 0xFF203060; // 21:00-00:00
    private static final int MIDNIGHT_BLUE   = 0xFF080820; // 00:00-06:00
    
    @SubscribeEvent
    public static void onRenderGuiOverlay(net.minecraftforge.client.event.RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            return;
        }
        render(event.getPoseStack(), event.getPartialTick());
    }
    
    public static void render(PoseStack poseStack, float partialTick) {
        ConfigProvider provider = ConfigProviderHolder.getProvider();
        if (provider == null) return;
        
        ConfigProvider.GeneralConfig general = provider.getGeneral();
        if (!general.isEnabled()) return;
        
        ConfigProvider.PositionConfig position = provider.getPosition();
        ConfigProvider.AppearanceConfig appearance = provider.getAppearance();
        
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        
        // Check game mode visibility
        var player = Minecraft.getInstance().player;
        if (player != null) {
            boolean isCreative = player.getAbilities().instabuild;
            if (isCreative && !general.isShowInCreative()) return;
            if (!isCreative && !general.isShowInSurvival()) return;
        }
        
        // Check clock requirement
        if (general.isRequireClock()) {
            boolean hasClock = player != null && (
                player.getMainHandItem().is(net.minecraft.world.item.Items.CLOCK) ||
                player.getOffhandItem().is(net.minecraft.world.item.Items.CLOCK)
            );
            if (!hasClock) return;
        }
        
        // Get time progress (0.0 = dawn 6:00, 1.0 = next dawn)
        // Shift by -1 hour (1/24) so 07:00 becomes 0.0
        float progress = TimeUtils.getTransitionProgress(Minecraft.getInstance().level, partialTick) - (1.0f / 24.0f);
        if (progress < 0.0f) progress += 1.0f;
        
        // Bar dimensions
        int barWidth = provider.getPosition().getRadius() * 4;
        int barHeightThin = Math.max(2, provider.getPosition().getThickness() / 2);
        
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        
        // Render background bar (full width, thin)
        renderBackgroundBar(0, 0, provider.getPosition().getRadius() * 4, 2, provider.getAppearance().getBackgroundColor());
        
        // Render day/night gradient bar with custom color timeline
        renderProgressBar(0, 0, provider.getPosition().getRadius() * 4, 2, progress);
        
        // Render sun/moon icons at ends
        if (ConfigProviderHolder.getProvider().getAppearance().isShowIcons()) {
            renderSunMoonIcons(provider.getPosition().getRadius() * 4, provider.getAppearance().getIconScale());
        }
        
        // Render time text centered above bar
        if (ConfigProviderHolder.getProvider().getAppearance().isShowTimeText()) {
            renderTimeText();
        }
    }
    
    private static void renderBackgroundBar(int x, int y, int width, int height, long color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        PoseStack poseStack = new PoseStack();
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        int argb = (int) color;
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int argbColor = (a << 24) | (r << 16) | (g << 8) | b;
        
        buffer.vertex(poseStack.last().pose(), 0, 2, 0).color(argbColor).uv(0, 0).endVertex();
        buffer.vertex(poseStack.last().pose(), width, 2, 0).color(argbColor).uv(0, 0).endVertex();
        buffer.vertex(poseStack.last().pose(), width, y, 0).color(argbColor).uv(0, 0).endVertex();
        buffer.vertex(poseStack.last().pose(), x, y, 0).color(argbColor).uv(0, 0).endVertex();
        
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }
    
    // Custom color timeline based on Minecraft time (progress 0.0-1.0)
    private static int getColorForProgress(float progress) {
        // progress 0.0 = 6:00, 0.25 = 12:00, 0.5 = 18:00, 0.75 = 0:00, 1.0 = 6:00
        
        if (progress < 0.208f) { // 6:00-11:00 (0.0 - 0.208) - Pale yellow
            float t = progress / 0.208f;
            return interpolateColor(0xFFE8E8A0, 0xFFF5F5A0, t); // Pale yellow
        } 
        else if (progress < 0.25f) { // 11:00-12:00 (0.208-0.25) - Transition to bright yellow
            float t = (progress - 0.208f) / 0.042f;
            return interpolateColor(0xFFF5F5A0, 0xFFFFFF00, t); // To bright yellow
        }
        else if (progress < 0.396f) { // 12:00-15:30 (0.25-0.396) - Bright vivid yellow
            return 0xFFFFFF00; // Bright yellow
        }
        else if (progress < 0.458f) { // 15:30-17:00 (0.396-0.458) - Pale orange
            float t = (progress - 0.396f) / 0.062f;
            return interpolateColor(0xFFFFFF00, 0xFFF0B060, t); // Yellow to pale orange
        }
        else if (progress < 0.542f) { // 17:00-19:00 (0.458-0.542) - Sunset orange
            float t = (progress - 0.458f) / 0.084f;
            return interpolateColor(0xFFF0B060, 0xFFFF6030, t); // Pale orange to sunset orange
        }
        else if (progress < 0.625f) { // 19:00-21:00 (0.542-0.625) - Twilight blue 1
            float t = (progress - 0.542f) / 0.083f;
            return interpolateColor(0xFFFF6030, 0xFF4060A0, t); // Sunset to twilight blue 1
        }
        else if (progress < 0.75f) { // 21:00-00:00 (0.625-0.75) - Twilight blue 2
            float t = (progress - 0.625f) / 0.125f;
            return interpolateColor(0xFF4060A0, 0xFF203060, t); // Twilight blue 1 to twilight blue 2
        }
        else if (progress < 0.875f) { // 00:00-03:00 (0.75-0.875) - Midnight blue
            float t = (progress - 0.75f) / 0.125f;
            return interpolateColor(0xFF203060, 0xFF080820, t); // Twilight blue 2 to midnight
        }
        else { // 03:00-06:00 (0.875-1.0) - Deep night
            float t = (progress - 0.875f) / 0.125f;
            return interpolateColor(0xFF080820, 0xFF040410, t); // Deep midnight
        }
    }
    
    private static void renderProgressBar(int x, int y, int width, int height, float progress) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        PoseStack poseStack = new PoseStack();
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        int segments = 64;
        
        for (int i = 0; i < segments; i++) {
            float segmentProgress = (float) i / segments;
            
            // Only draw segments that are "filled" based on current time progress
            if (segmentProgress >= progress) break;
            
            int color = getColorForProgress(segmentProgress);
            
            int x1 = x + (int) (i * width / 64.0f);
            int x2 = x + (int) ((i + 1) * width / 64.0f);
            
            int argb = color;
            buffer.vertex(poseStack.last().pose(), x1, y + height, 0).color(argb).uv(0, 0).endVertex();
            buffer.vertex(poseStack.last().pose(), x2, y + height, 0).color(argb).uv(0, 0).endVertex();
            buffer.vertex(poseStack.last().pose(), x2, y, 0).color(argb).uv(0, 0).endVertex();
            buffer.vertex(poseStack.last().pose(), x1, y, 0).color(argb).uv(0, 0).endVertex();
        }
        
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }
    
    private static int interpolateColor(int start, int end, float t) {
        int a1 = (start >> 24) & 0xFF;
        int r1 = (start >> 16) & 0xFF;
        int g1 = (start >> 8) & 0xFF;
        int b1 = start & 0xFF;
        
        int a2 = (end >> 24) & 0xFF;
        int r2 = (end >> 16) & 0xFF;
        int g2 = (end >> 8) & 0xFF;
        int b2 = end & 0xFF;
        
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    private static void renderSunMoonIcons(int barWidth, float iconScale) {
        int iconSize = (int) (16 * 1.2f); // Slightly larger icons
        int halfIcon = iconSize / 2;
        
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int barY = screenHeight - 22 - 45 + 1;
        
        // Sun at left (dawn)
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        blit(centerX - barWidth / 2 - iconSize / 2, barY - iconSize / 2, iconSize, iconSize);
        
        // Moon at right (dusk)
        RenderSystem.setShaderTexture(0, MOON_TEXTURE);
        blit(centerX + barWidth / 2 - halfIcon, barY - iconSize / 2, iconSize, iconSize);
    }
    
    private static void blit(int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        
        PoseStack poseStack = new PoseStack();
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        buffer.vertex(poseStack.last().pose(), x, y + height, 0).uv(0.0f, 1.0f).endVertex();
        buffer.vertex(poseStack.last().pose(), x + width, y + height, 0).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(poseStack.last().pose(), x + width, y, 0).uv(1.0f, 0.0f).endVertex();
        buffer.vertex(poseStack.last().pose(), x, y, 0).uv(0.0f, 0.0f).endVertex();
        
        Tesselator.getInstance().end();
        RenderSystem.disableBlend();
    }
    
    private static void renderTimeText() {
        TimeUtils.TimeOfDay time = TimeUtils.getTimeOfDay(Minecraft.getInstance().level);
        String timeStr = time.format24h();
        
        Font font = Minecraft.getInstance().font;
        int centerX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
        int y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 22 - 45 - 18;
        int color = ConfigProviderHolder.getProvider().getAppearance().getTextColor();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(0, 0, 200);
        Minecraft.getInstance().font.draw(new PoseStack(), timeStr, Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - Minecraft.getInstance().font.width(timeStr) / 2, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 22 - 45 - 18, color);
        poseStack.popPose();
        RenderSystem.disableBlend();
    }
}