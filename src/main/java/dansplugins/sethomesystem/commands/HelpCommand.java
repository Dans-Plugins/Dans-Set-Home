package dansplugins.sethomesystem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommand {

    public boolean execute(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "/msh help - View a list of helpful commands.");
        sender.sendMessage(ChatColor.AQUA + "/sethome - Set your home location.");
        sender.sendMessage(ChatColor.AQUA + "/home - Teleport to your home location.");
        sender.sendMessage(ChatColor.AQUA + "/msh forcesave - Force a save from the console.");
        sender.sendMessage(ChatColor.AQUA + "/msh forceload - Force a load from the console.");
        return true;
    }

}
