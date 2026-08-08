package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.services.StorageService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ForceSaveCommandTest {
    private StorageService storageService;
    private ForceSaveCommand forceSaveCommand;

    @BeforeEach
    void setUp() {
        storageService = mock(StorageService.class);
        forceSaveCommand = new ForceSaveCommand(storageService);
    }

    @Test
    void execute_playerWithoutPermission_returnsFalse() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.forcesave")).thenReturn(false);

        boolean result = forceSaveCommand.execute(player);

        assertFalse(result);
        verify(player).sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        verifyNoInteractions(storageService);
    }

    @Test
    void execute_playerWithPermission_savesRecordsAndFilenameIndex() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.forcesave")).thenReturn(true);

        boolean result = forceSaveCommand.execute(player);

        assertTrue(result);
        verify(player).sendMessage(ChatColor.GREEN + "Medieval Set Home is saving...");
        verify(storageService).saveHomeRecordFileNames();
        verify(storageService).saveHomeRecords();
    }

    @Test
    void execute_consoleSender_writesFilenameIndexBeforeRecords() {
        CommandSender sender = mock(CommandSender.class);

        boolean result = forceSaveCommand.execute(sender);

        assertTrue(result);
        InOrder inOrder = inOrder(storageService);
        inOrder.verify(storageService).saveHomeRecordFileNames();
        inOrder.verify(storageService).saveHomeRecords();
    }

    @Test
    void execute_doesNotReloadRecordsFromDisk() {
        CommandSender sender = mock(CommandSender.class);

        forceSaveCommand.execute(sender);

        verify(storageService, never()).loadHomeRecords();
    }
}
