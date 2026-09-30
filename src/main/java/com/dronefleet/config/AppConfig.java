package com.dronefleet.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private final Properties p = new Properties();
    public AppConfig() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) throw new IllegalStateException("application.properties not found");
            p.load(in);
        } catch (IOException e) { throw new IllegalStateException("Could not load configuration", e); }
    }
    public String databaseUrl(){ return p.getProperty("database.url"); }
    public int simulationMillisPerMinute(){ return Integer.parseInt(p.getProperty("simulation.millis.per.minute","100")); }
    public int maxDrones(){ return Integer.parseInt(p.getProperty("fleet.max.drones","50")); }
    public int lowBatteryThreshold(){ return Integer.parseInt(p.getProperty("battery.low.threshold","20")); }
    public int rechargeDurationSeconds(){ return Math.max(1, Integer.parseInt(p.getProperty("recharge.duration.seconds","5"))); }
}
