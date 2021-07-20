package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class HomeCommand {

    public boolean execute(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            for (HomeRecord record : PersistentData.getInstance().getHomeRecords()) {
                if (record.getPlayerName().equalsIgnoreCase(player.getName())) {
                    if (record.getHomeLocation() != null) {
                        player.sendMessage(ChatColor.GREEN + "Teleporting in 3 seconds...");
                        int seconds = 3;

                        Location initialLocation = player.getLocation();

                        getServer().getScheduler().runTaskLater(MedievalSetHome.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                if (initialLocation.getX() == player.getLocation().getX() &&
                                        initialLocation.getY() == player.getLocation().getY() &&
                                        initialLocation.getZ() == player.getLocation().getZ()) {

                                    // teleport the player
                                    player.teleport(record.getHomeLocation());

                                }
                                else {
                                    player.sendMessage(ChatColor.RED + "Movement Detected. Teleport cancelled.");
                                }

                            }
                        }, seconds * 20);
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "Home not set!");
                    }

                    return true;
                }
            }
        }
        return false;
    }
}
