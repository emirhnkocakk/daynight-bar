package com.emirh.daynightbar.common.config;

public final class ModConfigHolder {
    
    private ModConfigHolder() {}
    
    public static void register() {
        com.emirh.daynightbar.common.config.ConfigProviderHolder.setProvider(new ForgeConfigProvider());
    }
}