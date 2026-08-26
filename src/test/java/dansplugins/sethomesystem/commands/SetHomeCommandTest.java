package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.exceptions.HomeRecordNotFoundException;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetHomeCommandTest {
    private PersistentData persistentData;
    private SetHomeCommand setHomeCommand;
    private World world;

    @BeforeEach
    void setUp() {
        persistentData = new PersistentData();
        setHomeCommand = new SetHomeCommand(persistentData);
        world = mock(World.class);
    }

    @Test
    void execute_nonPlayerSender_sendsMessageAndReturnsFalse() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = setHomeCommand.execute(sender);

        assertFalse(result);
        verify(sender).sendMessage("Only players can use this command.");
        assertTrue(persistentData.getHomeRecords().isEmpty());
    }

    @Test
    void execute_playerWithoutPermission_returnsFalse() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.sethome")).thenReturn(false);

        boolean result = setHomeCommand.execute(player);

        assertFalse(result);
        verify(player).sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        verify(player, never()).getLocation();
        assertTrue(persistentData.getHomeRecords().isEmpty());
    }

    @Test
    void execute_existingRecord_updatesLocationInPlace() throws HomeRecordNotFoundException {
        Player player = playerNamed("Steve");
        Location location = new Location(world, 11, 22, 33);
        when(player.getLocation()).thenReturn(location);

        HomeRecord existing = new HomeRecord();
        existing.setPlayerName("Steve");
        persistentData.addHomeRecord(existing);

        boolean result = setHomeCommand.execute(player);

        assertTrue(result);
        verify(player).sendMessage(ChatColor.GREEN + "Home set!");
        assertEquals(1, persistentData.getHomeRecords().size());
        assertSame(existing, persistentData.getHomeRecord("Steve"));
        assertEquals(location, existing.getHomeLocation());
    }

    @Test
    void execute_noRecordYet_createsRecordAndSetsHome() throws HomeRecordNotFoundException {
        // players already online when the plugin is enabled never fire PlayerJoinEvent, so no
        // record exists for them - /sethome used to fail silently in that state
        Player player = playerNamed("Steve");
        Location location = new Location(world, 11, 22, 33);
        when(player.getLocation()).thenReturn(location);

        boolean result = setHomeCommand.execute(player);

        assertTrue(result);
        verify(player).sendMessage(ChatColor.GREEN + "Home set!");
        assertEquals(1, persistentData.getHomeRecords().size());
        assertEquals(location, persistentData.getHomeRecord("Steve").getHomeLocation());
    }

    @Test
    void execute_recordStoredUnderDifferentCasing_reusesThatRecord() {
        Player player = playerNamed("Steve");
        when(player.getLocation()).thenReturn(new Location(world, 11, 22, 33));

        HomeRecord existing = new HomeRecord();
        existing.setPlayerName("steve");
        persistentData.addHomeRecord(existing);

        boolean result = setHomeCommand.execute(player);

        assertTrue(result);
        assertEquals(1, persistentData.getHomeRecords().size());
        assertEquals("steve", persistentData.getHomeRecords().get(0).getPlayerName());
    }

    private Player playerNamed(String name) {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.hasPermission("dsh.sethome")).thenReturn(true);
        return player;
    }
}
