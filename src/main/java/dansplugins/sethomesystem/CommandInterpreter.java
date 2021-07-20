package dansplugins.sethomesystem;

import dansplugins.sethomesystem.commands.HomeCommand;
import dansplugins.sethomesystem.commands.SetHomeCommand;
import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.managers.StorageManager;
import dansplugins.sethomesystem.objects.HomeRecord;
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
            SetHomeCommand command = new SetHomeCommand();
            return command.setHome(sender);
        }

        // home command
        if (label.equalsIgnoreCase("home")) {
            HomeCommand command = new HomeCommand();
            return command.teleportPlayerHome(sender);
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
