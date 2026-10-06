package com.emirh.daynightbar.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    
    public static final ForgeConfigSpec SPEC;
    public static final General GENERAL;
    public static final Position POSITION;
    public static final Appearance APPEARANCE;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        builder.push("general");
        GENERAL = new General(builder);
        builder.pop();
        
        builder.push("position");
        POSITION = new Position(builder);
        builder.pop();
        
        builder.push("appearance");
        APPEARANCE = new Appearance(builder);
        builder.pop();
        
        SPEC = builder.build();
    }
    
    public static class General {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue requireClock;
        public final ForgeConfigSpec.BooleanValue showInCreative;
        public final ForgeConfigSpec.BooleanValue showInSurvival;
        
        public General(ForgeConfigSpec.Builder builder) {
            builder.comment("General settings").push("general");
            enabled = builder
                .comment("Enable the day/night cycle bar")
                .define("enabled", true);
            requireClock = builder
                .comment("Show the bar only when holding a clock/item with time info")
                .define("requireClock", false);
            showInCreative = builder
                .comment("Show the bar in creative mode")
                .define("showInCreative", true);
            showInSurvival = builder
                .comment("Show the bar in survival mode")
                .define("showInSurvival", true);
            builder.pop();
        }
    }
    
    public static class Position {
        public final ForgeConfigSpec.ConfigValue<String> horizontal;
        public final ForgeConfigSpec.ConfigValue<String> vertical;
        public final ForgeConfigSpec.IntValue offsetX;
        public final ForgeConfigSpec.IntValue offsetY;
        public final ForgeConfigSpec.IntValue radius;
        public final ForgeConfigSpec.IntValue thickness;
        
        public Position(ForgeConfigSpec.Builder builder) {
            builder.comment("Position and size settings").push("position");
            horizontal = builder
                .comment("Horizontal position: LEFT, CENTER, RIGHT")
                .define("horizontal", "CENTER");
            vertical = builder
                .comment("Vertical position: TOP, BOTTOM")
                .define("vertical", "TOP");
            offsetX = builder
                .comment("X offset from the anchor position")
                .defineInRange("offsetX", 0, -1000, 1000);
            offsetY = builder
                .comment("Y offset from the anchor position")
                .defineInRange("offsetY", 10, -1000, 1000);
            radius = builder
                .comment("Radius of the half-circle in pixels")
                .defineInRange("radius", 50, 10, 500);
            thickness = builder
                .comment("Thickness of the progress arc in pixels")
                .defineInRange("thickness", 6, 1, 50);
            builder.pop();
        }
        
        public HorizontalPosition getHorizontal() {
            return HorizontalPosition.valueOf(horizontal.get());
        }
        
        public VerticalPosition getVertical() {
            return VerticalPosition.valueOf(vertical.get());
        }
        
        public enum HorizontalPosition { LEFT, CENTER, RIGHT }
        public enum VerticalPosition { TOP, BOTTOM }
    }
    
    public static class Appearance {
        public final ForgeConfigSpec.BooleanValue showTimeText;
        public final ForgeConfigSpec.BooleanValue showIcons;
        public final ForgeConfigSpec.LongValue dayColor;
        public final ForgeConfigSpec.LongValue nightColor;
        public final ForgeConfigSpec.LongValue backgroundColor;
        public final ForgeConfigSpec.LongValue textColor;
        public final ForgeConfigSpec.BooleanValue textShadow;
        public final ForgeConfigSpec.DoubleValue iconScale;
        public final ForgeConfigSpec.BooleanValue smoothAnimation;
        
        public Appearance(ForgeConfigSpec.Builder builder) {
            builder.comment("Visual appearance settings").push("appearance");
            showTimeText = builder
                .comment("Show the time text (HH:MM)")
                .define("showTimeText", true);
            showIcons = builder
                .comment("Show sun/moon icons")
                .define("showIcons", true);
            dayColor = builder
                .comment("Day color (ARGB hex, e.g., 0xFFFF8800)")
                .defineInRange("dayColor", 0xFFFF8800L, 0L, 0xFFFFFFFFL);
            nightColor = builder
                .comment("Night color (ARGB hex, e.g., 0xFF0044AA)")
                .defineInRange("nightColor", 0xFF0044AAL, 0L, 0xFFFFFFFFL);
            backgroundColor = builder
                .comment("Background color (ARGB hex, e.g., 0x80000000)")
                .defineInRange("backgroundColor", 0x80000000L, 0L, 0xFFFFFFFFL);
            textColor = builder
                .comment("Text color (ARGB hex)")
                .defineInRange("textColor", 0xFFFFFFFFL, 0L, 0xFFFFFFFFL);
            textShadow = builder
                .comment("Text shadow")
                .define("textShadow", true);
            iconScale = builder
                .comment("Icon size multiplier")
                .defineInRange("iconScale", 1.0, 0.1, 5.0);
            smoothAnimation = builder
                .comment("Smooth animation between day/night transitions")
                .define("smoothAnimation", true);
            builder.pop();
        }
    }
}