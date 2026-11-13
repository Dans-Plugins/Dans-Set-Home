package dansplugins.sethomesystem.services;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.config.ConfigManager;
import dansplugins.sethomesystem.data.PersistentData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommandServiceTest {
    private StorageService storageService;
    private CommandService commandService;

    @BeforeEach
    void setUp() {
        storageService = mock(StorageService.class);
        commandService = new CommandService(
                new PersistentData(),
                mock(MedievalSetHome.class),
                storageService,
                mock(ConfigManager.class));
    }

    @Test
    void interpretCommand_dshWithoutArguments_pointsAtHelp() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("dsh"), new String[]{});

        assertTrue(result);
        verify(sender).sendMessage(ChatColor.RED + "Try /dsh help");
    }

    @Test
    void interpretCommand_unrecognisedDshSubcommand_usesCurrentPluginNameAndPointsAtHelp() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("dsh"), new String[]{"bogus"});

        assertTrue(result);
        verify(sender).sendMessage(ChatColor.RED + "Dans Set Home doesn't recognize that command. Try /dsh help");
        verifyNoInteractions(storageService);
    }

    @Test
    void interpretCommand_unrecognisedCommand_sendsNothing() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("unrelated"), new String[]{});

        assertFalse(result);
        verifyNoInteractions(sender);
    }

    // /danssethome:dsh forcesave arrives with the label danssethome:dsh; the command's name is still dsh
    @Test
    void interpretCommand_dshSubcommand_dispatchesOnCommandName() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("dsh"), new String[]{"forcesave"});

        assertTrue(result);
        verify(storageService).saveHomeRecords();
    }

    @Test
    void interpretCommand_sethome_dispatchesOnCommandName() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Alice");
        when(player.hasPermission("dsh.sethome")).thenReturn(true);

        boolean result = commandService.interpretCommand(player, command("sethome"), new String[]{});

        assertTrue(result);
        verify(player).sendMessage(ChatColor.GREEN + "Home set!");
    }

    @Test
    void interpretCommand_home_dispatchesOnCommandName() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Alice");
        when(player.hasPermission("dsh.home")).thenReturn(true);

        boolean result = commandService.interpretCommand(player, command("home"), new String[]{});

        assertTrue(result);
        verify(player).sendMessage(ChatColor.RED + "You don't have a home set.");
    }

    // the arguments reach HomeCommand, so /home <player> is checked against dsh.home.others
    @Test
    void interpretCommand_homeWithPlayerArgument_passesArgumentsToHomeCommand() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Alice");
        when(player.hasPermission("dsh.home")).thenReturn(true);

        boolean result = commandService.interpretCommand(player, command("home"), new String[]{"Bob"});

        assertTrue(result);
        verify(player).sendMessage(ChatColor.RED + "You don't have permission to teleport to other players' homes.");
    }

    @Test
    void interpretCommand_dshHelp_listsCommands() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("dsh"), new String[]{"help"});

        assertTrue(result);
        verify(sender).sendMessage(ChatColor.AQUA + "/dsh help - View a list of helpful commands.");
        verifyNoInteractions(storageService);
    }

    @Test
    void interpretCommand_dshForceload_loadsWithoutSaving() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("dsh"), new String[]{"forceload"});

        assertTrue(result);
        verify(storageService).loadHomeRecords();
        verify(storageService, never()).saveHomeRecords();
        verify(storageService, never()).saveHomeRecordFileNames();
    }

    @Test
    void interpretCommand_dshSubcommand_ignoresCase() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("dsh"), new String[]{"FORCESAVE"});

        assertTrue(result);
        verify(storageService).saveHomeRecords();
    }

    @Test
    void interpretCommand_dshSubcommand_ignoresArgumentsAfterTheFirst() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("dsh"), new String[]{"forceload", "extra"});

        assertTrue(result);
        verify(storageService).loadHomeRecords();
    }

    private static Command command(String name) {
        Command command = mock(Command.class);
        when(command.getName()).thenReturn(name);
        return command;
    }
}
