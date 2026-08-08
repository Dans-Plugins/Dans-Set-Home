package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.services.StorageService;
import org.bukkit.ChatColor;
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

class ForceLoadCommandTest {
    private StorageService storageService;
    private ForceLoadCommand forceLoadCommand;

    @BeforeEach
    void setUp() {
        storageService = mock(StorageService.class);
        forceLoadCommand = new ForceLoadCommand(storageService);
    }

    @Test
    void execute_playerWithoutPermission_returnsFalse() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.forceload")).thenReturn(false);

        boolean result = forceLoadCommand.execute(player);

        assertFalse(result);
        verify(player).sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        verifyNoInteractions(storageService);
    }

    @Test
    void execute_playerWithPermission_loadsHomeRecords() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.forceload")).thenReturn(true);

        boolean result = forceLoadCommand.execute(player);

        assertTrue(result);
        verify(player).sendMessage(ChatColor.GREEN + "Medieval Set Home is loading...");
        verify(storageService).loadHomeRecords();
    }

    @Test
    void execute_consoleSender_loadsHomeRecords() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = forceLoadCommand.execute(sender);

        assertTrue(result);
        verify(storageService).loadHomeRecords();
    }

    @Test
    void execute_doesNotOverwriteSavedDataOnDisk() {
        CommandSender sender = mock(CommandSender.class);

        forceLoadCommand.execute(sender);

        verify(storageService, never()).saveHomeRecords();
        verify(storageService, never()).saveHomeRecordFileNames();
    }
}
