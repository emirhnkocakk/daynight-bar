package com.emirh.daynightbar.client.hud;

import com.emirh.daynightbar.client.util.TimeUtils;
import com.emirh.daynightbar.common.config.ConfigProvider;
import com.emirh.daynightbar.common.config.ConfigProviderHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

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
    
    public static void render(GuiGraphics guiGraphics, float partialTick) {
        ConfigProvider provider = ConfigProviderHolder.getProvider();
        if (provider == null) return;
        
        ConfigProvider.GeneralConfig general = provider.getGeneral();
        ConfigProvider.PositionConfig position = provider.getPosition();
        ConfigProvider.AppearanceConfig appearance = provider.getAppearance();
        
        if (!general.isEnabled()) return;
        
        Level level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) return;
        
        // Check game mode visibility
        var player = net.minecraft.client.Minecraft.getInstance().player;
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
        
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        
        // Bar dimensions - positioned above hotbar (hotbar at y = screenHeight - 22)
        int barWidth = position.getRadius() * 4;
        int barHeight = Math.max(2, position.getThickness() / 2); // Thinner bar
        int centerX = screenWidth / 2 + position.getOffsetX();
        
        // Position above hotbar (hotbar typically at screenHeight - 22)
        int hotbarY = screenHeight - 22;
        int centerY = hotbarY - 45 + position.getOffsetY(); // 45 pixels above hotbar
        
        int barWidthHalf = barWidth / 2;
        int startX = centerX - barWidth / 2;
        int endX = centerX + barWidth / 2;
        int barY = centerY - position.getThickness() / 2;
        int barHeightActual = position.getThickness();
        
        // Get time progress (0.0 = dawn 6:00, 1.0 = next dawn)
        // Shift by -1 hour (1/24) so 07:00 becomes 0.0 (bar empty at 07:00)
        float progress = TimeUtils.getTransitionProgress(level, partialTick) - (1.0f / 24.0f);
        if (progress < 0.0f) progress += 1.0f;
        
        // Render background bar (full width, thin)
        renderBackgroundBar(guiGraphics, centerX - barWidth / 2, centerY - barHeight / 2, barWidth, barHeight, appearance.getBackgroundColor());
        
        // Render day/night gradient bar with custom color timeline
        renderProgressBar(guiGraphics, centerX - barWidth / 2, centerY - barHeight / 2, barWidth, barHeight, progress);
        
        // Render sun/moon icons at ends
        if (appearance.isShowIcons()) {
            renderSunMoonIcons(guiGraphics, centerX - barWidth / 2, centerX + barWidth / 2, 
                centerY - barHeight / 2, barHeight, appearance.getIconScale());
        }
        
        // Render time text centered above bar
        if (appearance.isShowTimeText()) {
            renderTimeText(guiGraphics, guiGraphics.guiWidth() / 2 + position.getOffsetX(), 
                centerY - barHeight / 2 - 18, level, appearance);
        }
    }
    
    private static void renderBackgroundBar(GuiGraphics guiGraphics, int x, int y, int width, int height, long color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Matrix4f matrix = guiGraphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        int argb = (int) color;
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int argbColor = (a << 24) | (r << 16) | (g << 8) | b;
        
        buffer.vertex(guiGraphics.pose().last().pose(), x, y + height, 0).color(argbColor).uv(0, 0).endVertex();
        buffer.vertex(guiGraphics.pose().last().pose(), x + width, y + height, 0).color(argbColor).uv(0, 0).endVertex();
        buffer.vertex(guiGraphics.pose().last().pose(), x + width, y, 0).color(argbColor).uv(0, 0).endVertex();
        buffer.vertex(guiGraphics.pose().last().pose(), x, y, 0).color(argbColor).uv(0, 0).endVertex();
        
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
    
    private static void renderProgressBar(GuiGraphics guiGraphics, int x, int y, int width, int height, float progress) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Matrix4f matrix = guiGraphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
        int segments = 64;
        float segmentWidth = (float) width / segments;
        
        for (int i = 0; i < segments; i++) {
            float segmentProgress = (float) i / segments;
            float nextSegmentProgress = (float) (i + 1) / segments;
            
            // Only draw segments that are "filled" based on current time progress
            if (segmentProgress >= progress) break;
            
            int color = getColorForProgress(segmentProgress);
            
            int x1 = x + (int) (i * width / (float) 64);
            int x2 = x + (int) ((i + 1) * width / (float) 64);
            
            int argb = color;
            buffer.vertex(guiGraphics.pose().last().pose(), x1, y + height, 0).color(argb).uv(0, 0).endVertex();
            buffer.vertex(guiGraphics.pose().last().pose(), x2, y + height, 0).color(argb).uv(0, 0).endVertex();
            buffer.vertex(guiGraphics.pose().last().pose(), x2, y, 0).color(argb).uv(0, 0).endVertex();
            buffer.vertex(guiGraphics.pose().last().pose(), x1, y, 0).color(argb).uv(0, 0).endVertex();
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
    
    private static void renderSunMoonIcons(GuiGraphics guiGraphics, int startX, int endX, int barY, int barHeight, float iconScale) {
        int iconSize = (int) (16 * 1.2f); // Slightly larger icons
        int halfIcon = iconSize / 2;
        // Center vertically on the bar
        int iconY = barY + barHeight / 2 - halfIcon;
        
        // Sun at left (dawn)
        guiGraphics.blit(new net.minecraft.resources.ResourceLocation("daynightbar", "hud/sun.png"), 
            startX - halfIcon, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        
        // Moon at right (dusk)
        guiGraphics.blit(new net.minecraft.resources.ResourceLocation("daynightbar", "hud/moon.png"), 
            endX - halfIcon, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
    }
    
    private static void renderTimeText(GuiGraphics guiGraphics, int centerX, int y, Level level, ConfigProvider.AppearanceConfig appearance) {
        TimeUtils.TimeOfDay time = TimeUtils.getTimeOfDay(level);
        String timeStr = time.format24h();
        
        var font = Minecraft.getInstance().font;
        int textX = centerX - font.width(timeStr) / 2;
        int color = appearance.getTextColor();
        guiGraphics.drawString(font, timeStr, textX, y, color, appearance.isTextShadow());
    }
}