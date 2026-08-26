package dansplugins.sethomesystem.services;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.config.ConfigManager;
import dansplugins.sethomesystem.data.PersistentData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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

        boolean result = commandService.interpretCommand(sender, "dsh", new String[]{});

        assertFalse(result);
        verify(sender).sendMessage(ChatColor.RED + "Try /dsh help");
    }

    @Test
    void interpretCommand_unrecognisedDshSubcommand_usesCurrentPluginNameAndPointsAtHelp() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, "dsh", new String[]{"bogus"});

        assertFalse(result);
        verify(sender).sendMessage(ChatColor.RED + "Dans Set Home doesn't recognize that command. Try /dsh help");
        verifyNoInteractions(storageService);
    }

    @Test
    void interpretCommand_unrecognisedLabel_sendsNothing() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = commandService.interpretCommand(sender, "unrelated", new String[]{});

        assertFalse(result);
        verifyNoInteractions(sender);
    }
}
