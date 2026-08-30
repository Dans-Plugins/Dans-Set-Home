package dansplugins.sethomesystem;

import dansplugins.sethomesystem.bstats.Metrics;
import dansplugins.sethomesystem.config.ConfigManager;
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
    private final ConfigManager configManager = new ConfigManager(this);

    // getDataFolder() is only populated once the plugin has been initialised by the server, so
    // anything depending on it is constructed in onEnable() rather than in a field initialiser
    private StorageService storageService;
    private CommandService commandService;

    @Override
    public void onEnable() {

        // load config
        configManager.saveDefaultConfig();

        storageService = new StorageService(persistentData, getDataFolder());
        commandService = new CommandService(persistentData, this, storageService, configManager);

        // register events
        eventRegistry.registerEvents();

        // move records left in the folder named after the plugin's former name
        int migrated = storageService.migrateLegacyDataFolder();
        if (migrated > 0) {
            getLogger().info("Migrated " + migrated + " file(s) from " + StorageService.LEGACY_DATA_FOLDER.getPath()
                    + " into " + getDataFolder().getPath() + ".");
        }

        // load save files
        storageService.loadHomeRecords();

        // bStats
        int pluginId = 12126;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        // onDisable() is still called when onEnable() failed part way through, in which case
        // there is nothing to save
        if (storageService == null) {
            return;
        }
        storageService.saveHomeRecordFileNames();
        storageService.saveHomeRecords();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return commandService.interpretCommand(sender, label, args);
    }
}