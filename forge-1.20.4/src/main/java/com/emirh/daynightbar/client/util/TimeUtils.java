package com.emirh.daynightbar.client.util;

import net.minecraft.world.level.Level;

public final class TimeUtils {
    
    private TimeUtils() {}
    
    /** Ticks per full day/night cycle */
    public static final long TICKS_PER_DAY = 24000L;
    
    /** Tick offsets for key times */
    public static final long DAWN_TICKS = 0L;       // 6:00
    public static final long NOON_TICKS = 6000L;    // 12:00
    public static final long DUSK_TICKS = 12000L;   // 18:00
    public static final long MIDNIGHT_TICKS = 18000L; // 0:00
    
    /**
     * Gets the current time of day as a progress value from 0.0 to 1.0
     * 0.0 = dawn (6:00), 0.25 = noon (12:00), 0.5 = dusk (18:00), 0.75 = midnight (0:00)
     */
    public static float getDayProgress(Level level) {
        if (level == null) return 0.0f;
        long dayTime = level.getDayTime() % TICKS_PER_DAY;
        return (float) (dayTime / (double) TICKS_PER_DAY);
    }
    
    /**
     * Gets the current time of day as an angle in radians for half-circle rendering
     * 0 = dawn (rightmost), PI = dusk (leftmost), going counter-clockwise
     */
    public static float getDayAngle(Level level) {
        float progress = getDayProgress(level);
        return progress * (float) Math.PI * 2.0f;
    }
    
    /**
     * Gets the angle for half-circle (0 to PI)
     * 0 = dawn (right), PI/2 = noon (top), PI = dusk (left)
     */
    public static float getHalfCircleAngle(Level level) {
        float progress = getDayProgress(level);
        // Map 0-1 to 0-PI for half circle
        return progress * (float) Math.PI;
    }
    
    /**
     * Gets the current time as hour (0-23) and minute (0-59)
     */
    public static TimeOfDay getTimeOfDay(Level level) {
        if (level == null) return new TimeOfDay(6, 0);
        
        long dayTime = level.getDayTime() % TICKS_PER_DAY;
        // Each tick = 50ms, 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour
        // But Minecraft time: 1000 ticks = 1 hour, so 1 tick = 3.6 seconds
        // 24000 ticks = 24 hours = 1 day
        
        // Convert to minutes since dawn (6:00)
        long ticksSinceDawn = (dayTime + 6000) % TICKS_PER_DAY; // Shift so 0 = 6:00
        long totalMinutes = (ticksSinceDawn * 24 * 60) / TICKS_PER_DAY;
        
        int hour = (int) (totalMinutes / 60);
        int minute = (int) (totalMinutes % 60);
        
        return new TimeOfDay(hour, minute);
    }
    
    /**
     * Checks if it's currently day time (between dawn and dusk)
     */
    public static boolean isDay(Level level) {
        if (level == null) return true;
        long dayTime = level.getDayTime() % TICKS_PER_DAY;
        return dayTime >= DAWN_TICKS && dayTime < DUSK_TICKS;
    }
    
    /**
     * Gets the interpolation factor for smooth day/night transitions
     * Returns a value between 0.0 and 1.0 for the current transition phase
     */
    public static float getTransitionProgress(Level level, float partialTick) {
        if (level == null) return 0.0f;
        long dayTime = level.getDayTime();
        long nextDayTime = dayTime + 1;
        
        float currentProgress = (float) ((dayTime % TICKS_PER_DAY) / (double) TICKS_PER_DAY);
        float nextProgress = (float) ((nextDayTime % TICKS_PER_DAY) / (double) TICKS_PER_DAY);
        
        // Handle wrap-around
        if (nextProgress < currentProgress) {
            nextProgress += 1.0f;
        }
        
        return currentProgress + (nextProgress - currentProgress) * partialTick;
    }
    
    public static record TimeOfDay(int hour, int minute) {
        public String format24h() {
            return String.format("%02d:%02d", hour, minute);
        }
        
        public String format12h() {
            int displayHour = hour % 12;
            if (displayHour == 0) displayHour = 12;
            String amPm = hour < 12 ? "AM" : "PM";
            return String.format("%d:%02d %s", displayHour, minute, amPm);
        }
    }
}