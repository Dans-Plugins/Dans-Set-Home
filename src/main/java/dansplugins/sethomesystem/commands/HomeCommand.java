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
    private final PersistentData persistentData;
    private final MedievalSetHome medievalSetHome;

    public HomeCommand(PersistentData persistentData, MedievalSetHome medievalSetHome) {
        this.persistentData = persistentData;
        this.medievalSetHome = medievalSetHome;
    }

    public boolean execute(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("msh.home")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return false;
            }

            for (HomeRecord record : persistentData.getHomeRecords()) {
                if (record.getPlayerName().equalsIgnoreCase(player.getName())) {
                    if (record.getHomeLocation() != null) {
                        player.sendMessage(ChatColor.GREEN + "Teleporting in 3 seconds...");
                        int seconds = 3;

                        Location initialLocation = player.getLocation();

                        getServer().getScheduler().runTaskLater(medievalSetHome, new Runnable() {
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
