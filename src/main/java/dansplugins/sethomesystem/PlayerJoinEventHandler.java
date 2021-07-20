package dansplugins.sethomesystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinEventHandler implements Listener {

    @EventHandler()
    public void handle(PlayerJoinEvent event) {
        if (!PersistentData.getInstance().hasHomeRecord(event.getPlayer().getName())) {
            HomeRecord newRecord = new HomeRecord();
            newRecord.setPlayerName(event.getPlayer().getName());

            PersistentData.getInstance().getHomeRecords().add(newRecord);
        }
    }

}
