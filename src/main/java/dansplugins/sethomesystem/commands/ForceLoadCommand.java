package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.managers.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceLoadCommand {

    public boolean execute(CommandSender sender) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used from the console.");
            return false;
        }

        System.out.println("Medieval Set Home is loading...");
        StorageManager.getInstance().saveHomeRecordFileNames();
        StorageManager.getInstance().saveHomeRecords();
        return true;

    }

}
