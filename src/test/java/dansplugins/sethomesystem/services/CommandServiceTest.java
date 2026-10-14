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

        assertFalse(result);
        verify(sender).sendMessage(ChatColor.RED + "Try /dsh help");
    }

    @Test
    void interpretCommand_unrecognisedDshSubcommand_usesCurrentPluginNameAndPointsAtHelp() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, command("dsh"), new String[]{"bogus"});

        assertFalse(result);
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

    private static Command command(String name) {
        Command command = mock(Command.class);
        when(command.getName()).thenReturn(name);
        return command;
    }
}
