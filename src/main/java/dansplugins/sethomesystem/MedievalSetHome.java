package dansplugins.sethomesystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MedievalSetHome extends JavaPlugin implements Listener {

    private boolean debug = false;

    private static MedievalSetHome instance;

    public static MedievalSetHome getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        if (debug) { System.out.println("Medieval Set Home plugin enabling..."); }

        instance = this;

        this.getServer().getPluginManager().registerEvents(this, this);

        StorageManager.getInstance().loadHomeRecords();

        if (debug) { System.out.println("Medieval Set Home plugin enabled."); }
    }

    @Override
    public void onDisable() {
        if (debug) { System.out.println("Medieval Set Home plugin disabling..."); }

        StorageManager.getInstance().saveHomeRecordFileNames();
        StorageManager.getInstance().saveHomeRecords();

        if (debug) { System.out.println("Medieval Set Home plugin disabled."); }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return CommandInterpreter.getInstance().interpretCommand(sender, label, args);
    }

    @EventHandler()
    public void onJoin(PlayerJoinEvent event) {
        if (!hasHomeRecord(event.getPlayer().getName())) {
            HomeRecord newRecord = new HomeRecord();
            newRecord.setPlayerName(event.getPlayer().getName());

            PersistentData.getInstance().getHomeRecords().add(newRecord);
        }
    }

    public boolean hasHomeRecord(String playerName) {
        for (HomeRecord record : PersistentData.getInstance().getHomeRecords()) {
            if (record.getPlayerName().equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

}
