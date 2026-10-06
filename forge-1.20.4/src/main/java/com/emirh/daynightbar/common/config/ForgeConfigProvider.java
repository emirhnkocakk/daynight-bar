package com.emirh.daynightbar.common.config;

import com.emirh.daynightbar.common.config.ModConfig;
import com.emirh.daynightbar.common.config.ConfigProvider.PositionConfig.HorizontalPosition;
import com.emirh.daynightbar.common.config.ConfigProvider.PositionConfig.VerticalPosition;

public class ForgeConfigProvider implements ConfigProvider {
    
    @Override
    public GeneralConfig getGeneral() {
        return new GeneralConfig() {
            @Override
            public boolean isEnabled() {
                return ModConfig.GENERAL.enabled.get();
            }
            
            @Override
            public boolean isRequireClock() {
                return ModConfig.GENERAL.requireClock.get();
            }
            
            @Override
            public boolean isShowInCreative() {
                return ModConfig.GENERAL.showInCreative.get();
            }
            
            @Override
            public boolean isShowInSurvival() {
                return ModConfig.GENERAL.showInSurvival.get();
            }
        };
    }
    
    @Override
    public PositionConfig getPosition() {
        return new PositionConfig() {
            @Override
            public HorizontalPosition getHorizontal() {
                return convertHorizontal(ModConfig.POSITION.getHorizontal());
            }
            
            @Override
            public VerticalPosition getVertical() {
                return convertVertical(ModConfig.POSITION.getVertical());
            }
            
            @Override
            public int getOffsetX() {
                return ModConfig.POSITION.offsetX.get();
            }
            
            @Override
            public int getOffsetY() {
                return ModConfig.POSITION.offsetY.get();
            }
            
            @Override
            public int getRadius() {
                return ModConfig.POSITION.radius.get();
            }
            
            @Override
            public int getThickness() {
                return ModConfig.POSITION.thickness.get();
            }
        };
    }
    
    @Override
    public AppearanceConfig getAppearance() {
        return new AppearanceConfig() {
            @Override
            public boolean isShowTimeText() {
                return ModConfig.APPEARANCE.showTimeText.get();
            }
            
            @Override
            public boolean isShowIcons() {
                return ModConfig.APPEARANCE.showIcons.get();
            }
            
            @Override
            public int getDayColor() {
                return ModConfig.APPEARANCE.dayColor.get().intValue();
            }
            
            @Override
            public int getNightColor() {
                return ModConfig.APPEARANCE.nightColor.get().intValue();
            }
            
            @Override
            public int getBackgroundColor() {
                return ModConfig.APPEARANCE.backgroundColor.get().intValue();
            }
            
            @Override
            public int getTextColor() {
                return ModConfig.APPEARANCE.textColor.get().intValue();
            }
            
            @Override
            public boolean isTextShadow() {
                return ModConfig.APPEARANCE.textShadow.get();
            }
            
            @Override
            public float getIconScale() {
                return ModConfig.APPEARANCE.iconScale.get().floatValue();
            }
            
            @Override
            public boolean isSmoothAnimation() {
                return ModConfig.APPEARANCE.smoothAnimation.get();
            }
        };
    }
    
    private HorizontalPosition convertHorizontal(ModConfig.Position.HorizontalPosition pos) {
        return switch (pos) {
            case LEFT -> HorizontalPosition.LEFT;
            case CENTER -> HorizontalPosition.CENTER;
            case RIGHT -> HorizontalPosition.RIGHT;
        };
    }
    
    private VerticalPosition convertVertical(ModConfig.Position.VerticalPosition pos) {
        return switch (pos) {
            case TOP -> VerticalPosition.TOP;
            case BOTTOM -> VerticalPosition.BOTTOM;
        };
    }
}