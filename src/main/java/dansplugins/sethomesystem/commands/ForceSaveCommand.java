package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.services.StorageService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceSaveCommand {
    private final StorageService storageService;

    public ForceSaveCommand(StorageService storageService) {
        this.storageService = storageService;
    }

    public boolean execute(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("msh.forcesave")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return false;
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Medieval Set Home is saving...");
        storageService.saveHomeRecords();
        return true;
    }

}
