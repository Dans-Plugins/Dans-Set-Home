package dansplugins.sethomesystem;

import dansplugins.sethomesystem.commands.*;
import dansplugins.sethomesystem.managers.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            return command.execute(sender);
        }

        // home command
        if (label.equalsIgnoreCase("home")) {
            HomeCommand command = new HomeCommand();
            return command.execute(sender);
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
                HelpCommand command = new HelpCommand();
                return command.execute(sender);
            }

            // forcesave
            if (args[0].equalsIgnoreCase("forcesave")) {
                ForceSaveCommand command = new ForceSaveCommand();
                return command.execute(sender);
            }

            // forceload
            if (args[0].equalsIgnoreCase("forceload")) {
                ForceLoadCommand command = new ForceLoadCommand();
                return command.execute(sender);
            }
        }

        return false;
    }

}
