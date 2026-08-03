package dansplugins.sethomesystem.config;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private static final String TELEPORT_DELAY_SECONDS_KEY = "teleport-delay-seconds";
    private static final int DEFAULT_TELEPORT_DELAY_SECONDS = 3;

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
}
