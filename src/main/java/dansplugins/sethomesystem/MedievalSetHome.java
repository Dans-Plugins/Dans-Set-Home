package dansplugins.sethomesystem;

import dansplugins.sethomesystem.bstats.Metrics;
import dansplugins.sethomesystem.config.ConfigManager;
import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.services.CommandService;
import dansplugins.sethomesystem.services.StorageService;
import dansplugins.sethomesystem.trace.TraceClient;
import dansplugins.sethomesystem.utils.EventRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class MedievalSetHome extends JavaPlugin implements Listener {
    private static final String USAGE_REPORTING_DETAILS_URL = "https://github.com/Stephenson-Software/trace#usage-reporting";
    private final PersistentData persistentData = new PersistentData();
    private final EventRegistry eventRegistry = new EventRegistry(this, persistentData);
    private final ConfigManager configManager = new ConfigManager(this);

    // getDataFolder() is only populated once the plugin has been initialised by the server, so
    // anything depending on it is constructed in onEnable() rather than in a field initialiser
    private StorageService storageService;
    private CommandService commandService;

    // A no-op until the config has been read, so a command arriving before
    // onEnable() finishes has something safe to report to.
    private TraceClient trace = TraceClient.disabled();

    @Override
    public void onEnable() {

        // load config
        configManager.saveDefaultConfig();

        storageService = new StorageService(persistentData, getDataFolder(), getLogger());
        commandService = new CommandService(persistentData, this, storageService, configManager);

        // register events
        eventRegistry.registerEvents();

        // move records left in the folder named after the plugin's former name
        int migrated = storageService.migrateLegacyDataFolder();
        if (migrated > 0) {
            getLogger().info("Migrated " + migrated + " file(s) from " + storageService.getLegacyDataFolder().getPath()
                    + " into " + getDataFolder().getPath() + ".");
        }

        // load save files
        storageService.loadHomeRecords();

        // bStats
        int pluginId = 12126;
        Metrics metrics = new Metrics(this, pluginId);

        // usage reporting: one event now, one per command; see config.yml. The
        // switch is put on disk first so it can be found, then the server-wide
        // plugins/trace/config.yml and the environment get the last word.
        configManager.saveUsageReportingDefaultsIfMissing();
        trace = TraceClient.builder(configManager.getUsageReportingEndpoint(), getName())
                .key(configManager.getUsageReportingKey())
                .enabled(configManager.isUsageReportingEnabled())
                .serverWideConfig(getDataFolder().getParentFile())
                .logger(getLogger())
                .build();
        logUsageReportingState();
        trace.report("startup", null, Collections.singletonMap("version", getDescription().getVersion()));
    }

    /** Says on every startup whether usage reporting is on, what is sent, and how to turn it off. */
    private void logUsageReportingState() {
        if (trace.isEnabled()) {
            getLogger().info("Usage reporting is on: " + getName() + " sends its name, version and command names to "
                    + configManager.getUsageReportingEndpoint() + " - nothing about players or the server. "
                    + "Turn it off with usage-reporting.enabled: false in this plugin's config.yml, "
                    + "or for every plugin with enabled: false in plugins/trace/config.yml. "
                    + "Details: " + USAGE_REPORTING_DETAILS_URL);
        } else {
            getLogger().info("Usage reporting is off (" + trace.disabledReason() + ").");
        }
    }

    @Override
    public void onDisable() {
        trace.close();

        // onDisable() is still called when onEnable() failed part way through, in which case
        // there is nothing to save
        if (storageService == null) {
            return;
        }
        storageService.saveHomeRecordFileNames();
        storageService.saveHomeRecords();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        trace.report("command", null, Collections.singletonMap("name", cmd.getName()));
        return commandService.interpretCommand(sender, label, args);
    }
}