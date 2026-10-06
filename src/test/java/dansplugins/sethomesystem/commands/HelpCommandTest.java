package dansplugins.sethomesystem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HelpCommandTest {
    private static final String HELP_LINE = ChatColor.AQUA + "/dsh help - View a list of helpful commands.";
    private static final String SETHOME_LINE = ChatColor.AQUA + "/sethome - Set your home location.";
    private static final String HOME_LINE = ChatColor.AQUA + "/home - Teleport to your home location.";
    private static final String HOME_OTHERS_LINE = ChatColor.AQUA + "/home <ign> - Teleport to a player's home location.";
    private static final String FORCESAVE_LINE = ChatColor.AQUA + "/dsh forcesave - Force a save from the console.";
    private static final String FORCELOAD_LINE = ChatColor.AQUA + "/dsh forceload - Force a load from the console.";

    private HelpCommand helpCommand;

    @BeforeEach
    void setUp() {
        helpCommand = new HelpCommand();
    }

    @Test
    void execute_playerWithoutPermission_returnsFalseAndListsNothing() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.help")).thenReturn(false);

        boolean result = helpCommand.execute(player);

        assertFalse(result);
        verify(player).sendMessage(ChatColor.RED + "You don't have permission to use this command.");
        verify(player, times(1)).sendMessage(anyString());
    }

    @Test
    void execute_playerWithOnlyDefaultPermissions_listsOnlyDefaultCommands() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.help")).thenReturn(true);

        boolean result = helpCommand.execute(player);

        assertTrue(result);
        InOrder inOrder = inOrder(player);
        inOrder.verify(player).sendMessage(HELP_LINE);
        inOrder.verify(player).sendMessage(SETHOME_LINE);
        inOrder.verify(player).sendMessage(HOME_LINE);
        verify(player, times(3)).sendMessage(anyString());
    }

    @Test
    void execute_playerWithHomeOthers_listsHomeOthersLine() {
        Player player = mock(Player.class);
        when(player.hasPermission("dsh.help")).thenReturn(true);
        when(player.hasPermission("dsh.home.others")).thenReturn(true);

        helpCommand.execute(player);

        verify(player).sendMessage(HOME_OTHERS_LINE);
        verify(player, times(4)).sendMessage(anyString());
    }

    @Test
    void execute_senderWithEveryPermission_listsEveryCommandInOrder() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);

        boolean result = helpCommand.execute(sender);

        assertTrue(result);
        InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).sendMessage(HELP_LINE);
        inOrder.verify(sender).sendMessage(SETHOME_LINE);
        inOrder.verify(sender).sendMessage(HOME_LINE);
        inOrder.verify(sender).sendMessage(HOME_OTHERS_LINE);
        inOrder.verify(sender).sendMessage(FORCESAVE_LINE);
        inOrder.verify(sender).sendMessage(FORCELOAD_LINE);
        verify(sender, times(6)).sendMessage(anyString());
    }

    @Test
    void execute_consoleSender_skipsHelpPermissionCheck() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(false);

        boolean result = helpCommand.execute(sender);

        assertTrue(result);
        verify(sender).sendMessage(HELP_LINE);
        verify(sender, times(3)).sendMessage(anyString());
    }
}
