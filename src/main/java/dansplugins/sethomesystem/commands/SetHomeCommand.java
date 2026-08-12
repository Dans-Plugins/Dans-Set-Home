package dansplugins.sethomesystem.commands;

import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.exceptions.HomeRecordNotFoundException;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand {
    private final PersistentData persistentData;

    public SetHomeCommand(PersistentData persistentData) {
        this.persistentData = persistentData;
    }

    public boolean execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("dsh.sethome")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }

        HomeRecord record;
        try {
            record = persistentData.getHomeRecord(player.getName());
        } catch (HomeRecordNotFoundException e) {
            // players who were already online when the plugin was enabled never went through
            // JoinListener, so their record is created on demand here
            record = new HomeRecord();
            record.setPlayerName(player.getName());
            persistentData.addHomeRecord(record);
        }

        record.setHomeLocation(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Home set!");
        return true;
    }
}
