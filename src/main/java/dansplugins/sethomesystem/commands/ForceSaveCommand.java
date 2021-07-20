package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.managers.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceSaveCommand {

    public boolean execute(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("msh.forcesave")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return false;
            }
        }

        sender.sendMessage("Medieval Set Home is saving...");
        StorageManager.getInstance().saveHomeRecords();
        return true;
    }

}
