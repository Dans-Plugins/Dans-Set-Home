package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.exceptions.HomeRecordNotFoundException;
import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;
import static org.bukkit.ChatColor.RED;

public class HomeCommand {
    private final PersistentData persistentData;
    private final MedievalSetHome medievalSetHome;

    public HomeCommand(PersistentData persistentData, MedievalSetHome medievalSetHome) {
        this.persistentData = persistentData;
        this.medievalSetHome = medievalSetHome;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("dsh.home")) {
            player.sendMessage(RED + "You don't have permission to use this command.");
            return false;
        }

        String name;
        if (args.length > 0) {
            if (!player.hasPermission("dsh.home.others")) {
                player.sendMessage(RED + "Only operators can teleport to other players' homes.");
                return false;
            }
            name = args[0];
        }
        else {
            name = player.getName();
        }

        HomeRecord record;
        try {
            record = persistentData.getHomeRecord(name);
        } catch (HomeRecordNotFoundException e) {
            if (player.getName().equalsIgnoreCase(name)) {
                player.sendMessage(RED + "You don't have a home set.");
            }
            else {
                player.sendMessage(RED + "That player doesn't have a home set.");
            }
            return false;
        }

        if (record.getHomeLocation() == null) {
            player.sendMessage(RED + "Home location was null. Please contact the developer.");
            return false;
        }

        int seconds = 3;
        player.sendMessage(ChatColor.GREEN + "Teleporting in " + seconds + " seconds...");

        Location initialLocation = player.getLocation();

        HomeRecord finalRecord = record;
        getServer().getScheduler().runTaskLater(medievalSetHome, () -> {
            if (initialLocation.getX() == player.getLocation().getX() &&
                    initialLocation.getY() == player.getLocation().getY() &&
                    initialLocation.getZ() == player.getLocation().getZ()) {

                // teleport the player
                player.teleport(finalRecord.getHomeLocation());
            }
            else {
                player.sendMessage(RED + "Your teleport was cancelled because your location changed.");
            }

        }, seconds * 20);

        return true;
    }
}
