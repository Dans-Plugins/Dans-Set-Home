package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand {

    public boolean execute(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("msh.sethome")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return false;
            }

            for (HomeRecord record : PersistentData.getInstance().getHomeRecords()) {
                if (record.getPlayerName().equalsIgnoreCase(player.getName())) {
                    record.setHomeLocation(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Home set!");
                    return true;
                }
            }
        }
        return false;
    }
}
