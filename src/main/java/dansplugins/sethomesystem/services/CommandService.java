package dansplugins.sethomesystem.services;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.commands.*;
import dansplugins.sethomesystem.data.PersistentData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandService {
    private final PersistentData persistentData;
    private final MedievalSetHome medievalSetHome;
    private final StorageService storageService;

    public CommandService(PersistentData persistentData, MedievalSetHome medievalSetHome, StorageService storageService) {
        this.persistentData = persistentData;
        this.medievalSetHome = medievalSetHome;
        this.storageService = storageService;
    }

    public boolean interpretCommand(CommandSender sender, String label, String[] args) {

        // sethome command
        if (label.equalsIgnoreCase("sethome")) {
            SetHomeCommand command = new SetHomeCommand(persistentData);
            return command.execute(sender);
        }

        // home command
        if (label.equalsIgnoreCase("home")) {
            HomeCommand command = new HomeCommand(persistentData, medievalSetHome);
            return command.execute(sender, args);
        }

        // dsh command
        if (label.equalsIgnoreCase("dsh")) {
            // args check
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Try /dsh help");
                return false;
            }

            // help command
            if (args[0].equalsIgnoreCase("help")) {
                HelpCommand command = new HelpCommand();
                return command.execute(sender);
            }

            // forcesave
            if (args[0].equalsIgnoreCase("forcesave")) {
                ForceSaveCommand command = new ForceSaveCommand(storageService);
                return command.execute(sender);
            }

            // forceload
            if (args[0].equalsIgnoreCase("forceload")) {
                ForceLoadCommand command = new ForceLoadCommand(storageService);
                return command.execute(sender);
            }

            sender.sendMessage(ChatColor.RED + "Medieval Set Home doesn't recognize that command.");
        }

        return false;
    }

}
