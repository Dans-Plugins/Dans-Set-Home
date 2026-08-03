package dansplugins.sethomesystem.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
}
