package dansplugins.sethomesystem.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private static final String TELEPORT_DELAY_SECONDS_KEY = "teleport-delay-seconds";
    private static final int DEFAULT_TELEPORT_DELAY_SECONDS = 3;
    private static final String USAGE_REPORTING_SECTION_KEY = "usage-reporting";
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

    /**
     * Writes the bundled usage-reporting block into a config.yml that predates it.
     * saveDefaultConfig() never touches an existing file, so a server upgraded from
     * before usage reporting kept reporting (the getters below fall through to the
     * jar's defaults) without the switch ever appearing on disk. The values are the
     * bundled defaults themselves, not a second copy of them. A file that already
     * has the block -- including one where enabled has been set to false -- is left
     * exactly as it is.
     */
    public void saveUsageReportingDefaultsIfMissing() {
        FileConfiguration config = plugin.getConfig();
        if (config.isSet(USAGE_REPORTING_SECTION_KEY)) {
            return;
        }
        Configuration defaults = config.getDefaults();
        if (defaults == null) {
            return;
        }
        config.set(USAGE_REPORTING_ENABLED_KEY, defaults.get(USAGE_REPORTING_ENABLED_KEY));
        config.set(USAGE_REPORTING_ENDPOINT_KEY, defaults.get(USAGE_REPORTING_ENDPOINT_KEY));
        config.set(USAGE_REPORTING_KEY_KEY, defaults.get(USAGE_REPORTING_KEY_KEY));
        plugin.saveConfig();
    }

    public int getTeleportDelaySeconds() {
        return plugin.getConfig().getInt(TELEPORT_DELAY_SECONDS_KEY, DEFAULT_TELEPORT_DELAY_SECONDS);
    }

    // The one-argument getters, deliberately. saveDefaultConfig() never touches a
    // config.yml that already exists, so a server upgraded from a version before
    // usage reporting has no usage-reporting block on disk. Bukkit registers the
    // jar's config.yml as the defaults for that file, and the one-argument
    // getters fall through to them -- but the two-argument getters return their
    // explicit fallback instead, which for the key would be "" and would turn
    // reporting off on every existing installation. Verified against
    // YamlConfiguration, not assumed.

    public boolean isUsageReportingEnabled() {
        return plugin.getConfig().getBoolean(USAGE_REPORTING_ENABLED_KEY);
    }

    public String getUsageReportingEndpoint() {
        String endpoint = plugin.getConfig().getString(USAGE_REPORTING_ENDPOINT_KEY);
        return endpoint != null ? endpoint : DEFAULT_USAGE_REPORTING_ENDPOINT;
    }

    /** Empty when no key is configured or bundled, which the client treats as "off". */
    public String getUsageReportingKey() {
        String key = plugin.getConfig().getString(USAGE_REPORTING_KEY_KEY);
        return key != null ? key : "";
    }
}
