package dansplugins.sethomesystem.listeners;

import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.exceptions.HomeRecordNotFoundException;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JoinListenerTest {
    private PersistentData persistentData;
    private JoinListener joinListener;

    @BeforeEach
    void setUp() {
        persistentData = new PersistentData();
        joinListener = new JoinListener(persistentData);
    }

    private static PlayerJoinEvent joinEventFor(String playerName) {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn(playerName);
        PlayerJoinEvent event = mock(PlayerJoinEvent.class);
        when(event.getPlayer()).thenReturn(player);
        return event;
    }

    @Test
    void handle_newPlayer_addsRecordWithPlayerNameAndNoHome() throws HomeRecordNotFoundException {
        joinListener.handle(joinEventFor("Alice"));

        assertEquals(1, persistentData.getHomeRecords().size());
        HomeRecord homeRecord = persistentData.getHomeRecord("Alice");
        assertEquals("Alice", homeRecord.getPlayerName());
        assertNull(homeRecord.getHomeLocation());
    }

    @Test
    void handle_returningPlayer_keepsExistingRecord() throws HomeRecordNotFoundException {
        HomeRecord existing = new HomeRecord();
        existing.setPlayerName("Alice");
        Location home = mock(Location.class);
        existing.setHomeLocation(home);
        persistentData.addHomeRecord(existing);

        joinListener.handle(joinEventFor("Alice"));

        assertEquals(1, persistentData.getHomeRecords().size());
        assertSame(existing, persistentData.getHomeRecord("Alice"));
        assertSame(home, existing.getHomeLocation());
    }

    @Test
    void handle_returningPlayerWithDifferentCase_doesNotAddRecord() {
        HomeRecord existing = new HomeRecord();
        existing.setPlayerName("Alice");
        persistentData.addHomeRecord(existing);

        joinListener.handle(joinEventFor("alice"));

        assertEquals(1, persistentData.getHomeRecords().size());
    }

    @Test
    void handle_twoNewPlayers_addsOneRecordEach() {
        joinListener.handle(joinEventFor("Alice"));
        joinListener.handle(joinEventFor("Bob"));
        joinListener.handle(joinEventFor("Alice"));

        assertEquals(2, persistentData.getHomeRecords().size());
    }
}
