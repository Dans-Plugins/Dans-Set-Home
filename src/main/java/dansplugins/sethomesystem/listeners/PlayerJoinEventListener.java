package dansplugins.sethomesystem.listeners;

import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinEventListener implements Listener {
    private final PersistentData persistentData;

    public PlayerJoinEventListener(PersistentData persistentData) {
        this.persistentData = persistentData;
    }

    @EventHandler()
    public void handle(PlayerJoinEvent event) {
        if (!persistentData.hasHomeRecord(event.getPlayer().getName())) {
            HomeRecord newRecord = new HomeRecord();
            newRecord.setPlayerName(event.getPlayer().getName());

            persistentData.getHomeRecords().add(newRecord);
        }
    }

}
