package dansplugins.sethomesystem;

import dansplugins.sethomesystem.bstats.Metrics;
import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.services.CommandService;
import dansplugins.sethomesystem.services.StorageService;
import dansplugins.sethomesystem.utils.EventRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MedievalSetHome extends JavaPlugin implements Listener {
    private final PersistentData persistentData = new PersistentData();
    private final EventRegistry eventRegistry = new EventRegistry(this, persistentData);
    private final StorageService storageService = new StorageService(persistentData);
    private final CommandService commandService = new CommandService(persistentData, this, storageService);

    @Override
    public void onEnable() {

        // register events
        eventRegistry.registerEvents();

        // load save files
        storageService.loadHomeRecords();

        // bStats
        int pluginId = 12126;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        storageService.saveHomeRecordFileNames();
        storageService.saveHomeRecords();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return commandService.interpretCommand(sender, label, args);
    }

}
