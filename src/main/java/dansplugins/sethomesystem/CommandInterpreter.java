package dansplugins.sethomesystem;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class CommandInterpreter {

    private static CommandInterpreter instance;

    private CommandInterpreter() {

    }

    public static CommandInterpreter getInstance() {
        if (instance == null) {
            instance = new CommandInterpreter();
        }
        return instance;
    }

    public boolean interpretCommand(CommandSender sender, String label, String[] args) {

        // sethome command
        if (label.equalsIgnoreCase("sethome")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                for (HomeRecord record : PersistentData.getInstance().getHomeRecords()) {
                    if (record.getPlayerName().equalsIgnoreCase(player.getName())) {
                        record.setHomeLocation(player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "Home set!");
                        return true;
                    }
                }
            }
        }

        // home command
        if (label.equalsIgnoreCase("home")) {
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
        }

        // msh command
        if (label.equalsIgnoreCase("msh")) {
            // args check
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Try /msh help");
                return false;
            }

            // help command
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.AQUA + "/help - View a list of helpful commands.");
                sender.sendMessage(ChatColor.AQUA + "/sethome - Set your home location.");
                sender.sendMessage(ChatColor.AQUA + "/home - Teleport to your home location.");
                sender.sendMessage(ChatColor.AQUA + "/forcesave - Force a save from the console.");
                sender.sendMessage(ChatColor.AQUA + "/forceload - Force a load from the console.");
                return true;
            }

            // forcesave
            if (args[0].equalsIgnoreCase("forcesave")) {
                if (sender instanceof Player) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used from the console.");
                    return false;
                }

                System.out.println("Medieval Set Home is saving...");
                StorageManager.getInstance().saveHomeRecords();
                return true;
            }

            // forceload
            if (args[0].equalsIgnoreCase("forceload")) {
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

        return false;
    }

}
