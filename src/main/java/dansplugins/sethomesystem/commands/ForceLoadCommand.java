package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.managers.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceLoadCommand {

    public boolean execute(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("msh.forceload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return false;
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Medieval Set Home is loading...");
        StorageManager.getInstance().saveHomeRecordFileNames();
        StorageManager.getInstance().saveHomeRecords();
        return true;
    }

}
