package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.config.ConfigManager;
import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomeCommandTest {
    private PersistentData persistentData;
    private MedievalSetHome medievalSetHome;
    private ConfigManager configManager;
    private HomeCommand homeCommand;
    private MockedStatic<Bukkit> bukkitMock;
    private BukkitScheduler scheduler;

    @BeforeEach
    void setUp() {
        persistentData = new PersistentData();
        medievalSetHome = mock(MedievalSetHome.class);
        configManager = mock(ConfigManager.class);
        homeCommand = new HomeCommand(persistentData, medievalSetHome, configManager);

        Server server = mock(Server.class);
        scheduler = mock(BukkitScheduler.class);
        when(server.getScheduler()).thenReturn(scheduler);
        bukkitMock = mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getServer).thenReturn(server);
    }

    @AfterEach
    void tearDown() {
        bukkitMock.close();
    }

    @Test
    void execute_nonPlayerSender_sendsMessageAndReturnsFalse() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = homeCommand.execute(sender, new String[0]);

        assertFalse(result);
        verify(sender).sendMessage("Only players can use this command.");
    }

    @Test
    void execute_playerWithoutHomePermission_returnsFalse() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.home")).thenReturn(false);

        boolean result = homeCommand.execute(player, new String[0]);

        assertFalse(result);
        verify(scheduler, never()).runTaskLater(any(), any(Runnable.class), anyLong());
    }

    @Test
    void execute_argsWithoutOthersPermission_returnsFalse() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.home")).thenReturn(true);
        when(player.hasPermission("dsh.home.others")).thenReturn(false);

        boolean result = homeCommand.execute(player, new String[]{"OtherPlayer"});

        assertFalse(result);
        verify(scheduler, never()).runTaskLater(any(), any(Runnable.class), anyLong());
    }

    @Test
    void execute_selfHomeNotFound_sendsMessage() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Steve");
        when(player.hasPermission("dsh.home")).thenReturn(true);

        boolean result = homeCommand.execute(player, new String[0]);

        assertFalse(result);
        verify(player).sendMessage(org.bukkit.ChatColor.RED + "You don't have a home set.");
    }

    @Test
    void execute_othersHomeNotFound_sendsMessage() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Steve");
        when(player.hasPermission("dsh.home")).thenReturn(true);
        when(player.hasPermission("dsh.home.others")).thenReturn(true);

        boolean result = homeCommand.execute(player, new String[]{"Alex"});

        assertFalse(result);
        verify(player).sendMessage(org.bukkit.ChatColor.RED + "That player doesn't have a home set.");
    }

    @Test
    void execute_homeLocationNull_sendsMessage() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Steve");
        when(player.hasPermission("dsh.home")).thenReturn(true);
        persistentData.addHomeRecord(recordFor("Steve", null));

        boolean result = homeCommand.execute(player, new String[0]);

        assertFalse(result);
        verify(player).sendMessage(org.bukkit.ChatColor.RED + "Home location was null. Please contact the developer.");
    }

    @Test
    void execute_successfulTeleport_whenLocationUnchanged() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Steve");
        when(player.hasPermission("dsh.home")).thenReturn(true);
        when(configManager.getTeleportDelaySeconds()).thenReturn(3);

        World world = mock(World.class);
        Location currentLocation = new Location(world, 1, 2, 3);
        Location homeLocation = new Location(world, 10, 20, 30);
        when(player.getLocation()).thenReturn(currentLocation);
        persistentData.addHomeRecord(recordFor("Steve", homeLocation));

        boolean result = homeCommand.execute(player, new String[0]);

        assertTrue(result);
        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskLater(any(), taskCaptor.capture(), org.mockito.ArgumentMatchers.eq(60L));

        taskCaptor.getValue().run();

        verify(player).teleport(homeLocation);
    }

    @Test
    void execute_teleportCancelled_whenLocationChanged() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Steve");
        when(player.hasPermission("dsh.home")).thenReturn(true);
        when(configManager.getTeleportDelaySeconds()).thenReturn(3);

        World world = mock(World.class);
        Location initialLocation = new Location(world, 1, 2, 3);
        Location movedLocation = new Location(world, 5, 2, 3);
        Location homeLocation = new Location(world, 10, 20, 30);
        when(player.getLocation()).thenReturn(initialLocation, movedLocation);
        persistentData.addHomeRecord(recordFor("Steve", homeLocation));

        homeCommand.execute(player, new String[0]);

        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskLater(any(), taskCaptor.capture(), anyLong());

        taskCaptor.getValue().run();

        verify(player, never()).teleport(any(Location.class));
        verify(player).sendMessage(org.bukkit.ChatColor.RED + "Your teleport was cancelled because your location changed.");
    }

    private HomeRecord recordFor(String playerName, Location location) {
        HomeRecord record = new HomeRecord();
        record.setPlayerName(playerName);
        record.setHomeLocation(location);
        return record;
    }
}
