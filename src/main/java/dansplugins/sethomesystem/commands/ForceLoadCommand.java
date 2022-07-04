package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.services.StorageService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceLoadCommand {
    private final StorageService storageService;

    public ForceLoadCommand(StorageService storageService) {
        this.storageService = storageService;
    }

    public boolean execute(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("dsh.forceload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return false;
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Medieval Set Home is loading...");
        storageService.saveHomeRecordFileNames();
        storageService.saveHomeRecords();
        return true;
    }

}
