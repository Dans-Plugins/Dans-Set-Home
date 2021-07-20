package dansplugins.sethomesystem;

import dansplugins.sethomesystem.managers.StorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MedievalSetHome extends JavaPlugin implements Listener {

    private static MedievalSetHome instance;

    public static MedievalSetHome getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // register events
        EventRegistry.getInstance().registerEvents();

        // load save files
        StorageManager.getInstance().loadHomeRecords();
    }

    @Override
    public void onDisable() {
        StorageManager.getInstance().saveHomeRecordFileNames();
        StorageManager.getInstance().saveHomeRecords();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return CommandInterpreter.getInstance().interpretCommand(sender, label, args);
    }

}
