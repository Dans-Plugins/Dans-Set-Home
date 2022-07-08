package dansplugins.sethomesystem.utils;

import dansplugins.sethomesystem.MedievalSetHome;
import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.listeners.JoinListener;
import org.bukkit.plugin.PluginManager;

public class EventRegistry {
    private final MedievalSetHome medievalSetHome;
    private final PersistentData persistentData;

    public EventRegistry(MedievalSetHome medievalSetHome, PersistentData persistentData) {
        this.medievalSetHome = medievalSetHome;
        this.persistentData = persistentData;
    }

    public void registerEvents() {

        MedievalSetHome mainInstance = medievalSetHome;
        PluginManager manager = mainInstance.getServer().getPluginManager();

        // join event handler
        manager.registerEvents(new JoinListener(persistentData), mainInstance);
    }
}
