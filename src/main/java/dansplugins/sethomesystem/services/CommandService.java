package dansplugins.sethomesystem.services;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.commands.*;
import dansplugins.sethomesystem.config.ConfigManager;
import dansplugins.sethomesystem.data.PersistentData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandService {
    private final PersistentData persistentData;
    private final MedievalSetHome medievalSetHome;
    private final StorageService storageService;
    private final ConfigManager configManager;

    public CommandService(PersistentData persistentData, MedievalSetHome medievalSetHome, StorageService storageService, ConfigManager configManager) {
        this.persistentData = persistentData;
        this.medievalSetHome = medievalSetHome;
        this.storageService = storageService;
        this.configManager = configManager;
    }

    public boolean interpretCommand(CommandSender sender, Command cmd, String[] args) {
        // the registered name rather than the label, which is whatever was typed: /danssethome:home
        // arrives with the label danssethome:home, and is the only way to reach these commands when
        // another plugin also registers /home or /sethome
        String name = cmd.getName();

        // sethome command
        if (name.equalsIgnoreCase("sethome")) {
            SetHomeCommand command = new SetHomeCommand(persistentData);
            return command.execute(sender);
        }

        // home command
        if (name.equalsIgnoreCase("home")) {
            HomeCommand command = new HomeCommand(persistentData, medievalSetHome, configManager);
            return command.execute(sender, args);
        }

        // dsh command
        if (name.equalsIgnoreCase("dsh")) {
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

            sender.sendMessage(ChatColor.RED + "Dans Set Home doesn't recognize that command. Try /dsh help");
        }

        return false;
    }

}
