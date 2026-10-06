package com.emirh.daynightbar.common.config;

public final class ConfigProviderHolder {
    
    private static ConfigProvider provider;
    
    private ConfigProviderHolder() {}
    
    public static void setProvider(ConfigProvider provider) {
        ConfigProviderHolder.provider = provider;
    }
    
    public static ConfigProvider getProvider() {
        return provider;
    }
}