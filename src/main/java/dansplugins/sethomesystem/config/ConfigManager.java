package dansplugins.sethomesystem.config;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private static final String TELEPORT_DELAY_SECONDS_KEY = "teleport-delay-seconds";
    private static final int DEFAULT_TELEPORT_DELAY_SECONDS = 3;
    private static final String USAGE_REPORTING_ENABLED_KEY = "usage-reporting.enabled";
    private static final String USAGE_REPORTING_ENDPOINT_KEY = "usage-reporting.endpoint";
    private static final String USAGE_REPORTING_KEY_KEY = "usage-reporting.key";
    private static final String DEFAULT_USAGE_REPORTING_ENDPOINT = "https://trace.danielstephenson.dev";

    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    public int getTeleportDelaySeconds() {
        return plugin.getConfig().getInt(TELEPORT_DELAY_SECONDS_KEY, DEFAULT_TELEPORT_DELAY_SECONDS);
    }

    public boolean isUsageReportingEnabled() {
        return plugin.getConfig().getBoolean(USAGE_REPORTING_ENABLED_KEY, true);
    }

    public String getUsageReportingEndpoint() {
        return plugin.getConfig().getString(USAGE_REPORTING_ENDPOINT_KEY, DEFAULT_USAGE_REPORTING_ENDPOINT);
    }

    /** Empty when no key has been configured, which the client treats as "off". */
    public String getUsageReportingKey() {
        return plugin.getConfig().getString(USAGE_REPORTING_KEY_KEY, "");
    }
}
