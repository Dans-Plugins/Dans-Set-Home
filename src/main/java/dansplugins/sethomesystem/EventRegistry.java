package dansplugins.sethomesystem;

import dansplugins.sethomesystem.eventhandlers.PlayerJoinEventHandler;
import org.bukkit.plugin.PluginManager;

public class EventRegistry {

    private static EventRegistry instance;

    private EventRegistry() {

    }

    public static EventRegistry getInstance() {
        if (instance == null) {
            instance = new EventRegistry();
        }
        return instance;
    }

    public void registerEvents() {

        MedievalSetHome mainInstance = MedievalSetHome.getInstance();
        PluginManager manager = mainInstance.getServer().getPluginManager();

        // join event handler
        manager.registerEvents(new PlayerJoinEventHandler(), mainInstance);
    }
}
