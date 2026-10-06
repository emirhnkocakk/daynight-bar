package com.emirh.daynightbar.common.config;

public interface ConfigProvider {
    
    GeneralConfig getGeneral();
    PositionConfig getPosition();
    AppearanceConfig getAppearance();
    
    interface GeneralConfig {
        boolean isEnabled();
        boolean isRequireClock();
        boolean isShowInCreative();
        boolean isShowInSurvival();
    }
    
    interface PositionConfig {
        HorizontalPosition getHorizontal();
        VerticalPosition getVertical();
        int getOffsetX();
        int getOffsetY();
        int getRadius();
        int getThickness();
        
        enum HorizontalPosition { LEFT, CENTER, RIGHT }
        enum VerticalPosition { TOP, BOTTOM }
    }
    
    interface AppearanceConfig {
        boolean isShowTimeText();
        boolean isShowIcons();
        int getDayColor();
        int getNightColor();
        int getBackgroundColor();
        int getTextColor();
        boolean isTextShadow();
        float getIconScale();
        boolean isSmoothAnimation();
    }
}