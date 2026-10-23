package dansplugins.sethomesystem.utils;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.listeners.JoinListener;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventRegistryTest {
    private MedievalSetHome plugin;
    private PluginManager pluginManager;
    private PersistentData persistentData;
    private EventRegistry eventRegistry;

    @BeforeEach
    void setUp() {
        plugin = mock(MedievalSetHome.class);
        Server server = mock(Server.class);
        pluginManager = mock(PluginManager.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        persistentData = new PersistentData();
        eventRegistry = new EventRegistry(plugin, persistentData);
    }

    @Test
    void registerEvents_registersOneJoinListenerForThePlugin() {
        eventRegistry.registerEvents();

        verify(pluginManager, times(1)).registerEvents(any(Listener.class), same(plugin));
        ArgumentCaptor<Listener> listener = ArgumentCaptor.forClass(Listener.class);
        verify(pluginManager).registerEvents(listener.capture(), same(plugin));
        assertInstanceOf(JoinListener.class, listener.getValue());
    }

    @Test
    void registerEvents_registeredListenerWritesToThePluginsPersistentData() {
        eventRegistry.registerEvents();
        ArgumentCaptor<Listener> listener = ArgumentCaptor.forClass(Listener.class);
        verify(pluginManager).registerEvents(listener.capture(), same(plugin));

        // a listener given a different PersistentData would leave the plugin's records untouched
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Alice");
        PlayerJoinEvent event = mock(PlayerJoinEvent.class);
        when(event.getPlayer()).thenReturn(player);
        ((JoinListener) listener.getValue()).handle(event);

        assertEquals(1, persistentData.getHomeRecords().size());
        assertEquals("Alice", persistentData.getHomeRecords().get(0).getPlayerName());
    }
}
