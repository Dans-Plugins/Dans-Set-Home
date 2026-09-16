package dansplugins.sethomesystem.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigManagerTest {
    private JavaPlugin plugin;
    private FileConfiguration config;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        config = mock(FileConfiguration.class);
        when(plugin.getConfig()).thenReturn(config);
        configManager = new ConfigManager(plugin);
    }

    @Test
    void saveDefaultConfig_delegatesToPlugin() {
        configManager.saveDefaultConfig();

        verify(plugin).saveDefaultConfig();
    }

    @Test
    void getTeleportDelaySeconds_returnsConfiguredValue() {
        when(config.getInt(eq("teleport-delay-seconds"), anyInt())).thenReturn(7);

        assertEquals(7, configManager.getTeleportDelaySeconds());
    }

    @Test
    void getTeleportDelaySeconds_fallsBackToDefaultOfThree() {
        when(config.getInt(eq("teleport-delay-seconds"), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        assertEquals(3, configManager.getTeleportDelaySeconds());
    }

    @Test
    void usageReporting_readsThroughToTheBundledDefaultsWhenTheFileHasNoBlock() {
        // A server upgraded from before usage reporting has no usage-reporting
        // block in its config.yml. Bukkit's one-argument getters fall through
        // to the jar's defaults; the two-argument ones would return their
        // fallback and turn reporting off on every existing installation.
        when(config.getBoolean("usage-reporting.enabled")).thenReturn(true);
        when(config.getString("usage-reporting.endpoint")).thenReturn("https://trace.danielstephenson.dev");
        when(config.getString("usage-reporting.key")).thenReturn("bundled-key");

        assertTrue(configManager.isUsageReportingEnabled());
        assertEquals("https://trace.danielstephenson.dev", configManager.getUsageReportingEndpoint());
        assertEquals("bundled-key", configManager.getUsageReportingKey());
        verify(config, never()).getString(eq("usage-reporting.key"), anyString());
        verify(config, never()).getBoolean(eq("usage-reporting.enabled"), anyBoolean());
    }

    @Test
    void saveUsageReportingDefaultsIfMissing_putsTheBundledBlockOnDiskWhenTheFileLacksIt() {
        // A config.yml from before usage reporting, with the jar's config.yml
        // registered as its defaults the way JavaPlugin.reloadConfig() does.
        YamlConfiguration onDisk = new YamlConfiguration();
        onDisk.set("teleport-delay-seconds", 5);
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.set("usage-reporting.enabled", true);
        bundled.set("usage-reporting.endpoint", "https://trace.danielstephenson.dev");
        bundled.set("usage-reporting.key", "bundled-key");
        onDisk.setDefaults(bundled);
        when(plugin.getConfig()).thenReturn(onDisk);

        configManager.saveUsageReportingDefaultsIfMissing();

        verify(plugin).saveConfig();
        String saved = onDisk.saveToString();
        assertTrue(saved.contains("enabled: true"), saved);
        assertTrue(saved.contains("endpoint: https://trace.danielstephenson.dev"), saved);
        assertTrue(saved.contains("key: bundled-key"), saved);
        assertTrue(saved.contains("teleport-delay-seconds: 5"), "existing settings must survive: " + saved);
        assertTrue(onDisk.isSet("usage-reporting"), "the block must now be a real value, not a default");
    }

    @Test
    void saveUsageReportingDefaultsIfMissing_leavesAFileThatHasTheBlockAlone() {
        // In particular an operator's enabled: false must never be undone.
        YamlConfiguration onDisk = new YamlConfiguration();
        onDisk.set("usage-reporting.enabled", false);
        YamlConfiguration bundled = new YamlConfiguration();
        bundled.set("usage-reporting.enabled", true);
        bundled.set("usage-reporting.key", "bundled-key");
        onDisk.setDefaults(bundled);
        when(plugin.getConfig()).thenReturn(onDisk);

        configManager.saveUsageReportingDefaultsIfMissing();

        verify(plugin, never()).saveConfig();
        assertFalse(onDisk.getBoolean("usage-reporting.enabled"));
        assertNull(onDisk.get("usage-reporting.key", null), "nothing must be added beside the operator's switch");
    }

    @Test
    void saveUsageReportingDefaultsIfMissing_doesNothingWithoutBundledDefaults() {
        YamlConfiguration onDisk = new YamlConfiguration();
        when(plugin.getConfig()).thenReturn(onDisk);

        configManager.saveUsageReportingDefaultsIfMissing();

        verify(plugin, never()).saveConfig();
        assertFalse(onDisk.isSet("usage-reporting"));
    }

    @Test
    void usageReporting_isOffWithNoKeyAnywhere() {
        when(config.getString("usage-reporting.key")).thenReturn(null);
        when(config.getString("usage-reporting.endpoint")).thenReturn(null);

        assertEquals("", configManager.getUsageReportingKey(), "no key anywhere must read as off, not as null");
        assertEquals("https://trace.danielstephenson.dev", configManager.getUsageReportingEndpoint());
    }

    @Test
    void usageReporting_readsTheConfiguredValues() {
        when(config.getBoolean("usage-reporting.enabled")).thenReturn(false);
        when(config.getString("usage-reporting.endpoint")).thenReturn("http://localhost:8080");
        when(config.getString("usage-reporting.key")).thenReturn("abc");

        assertFalse(configManager.isUsageReportingEnabled());
        assertEquals("http://localhost:8080", configManager.getUsageReportingEndpoint());
        assertEquals("abc", configManager.getUsageReportingKey());
    }
}
