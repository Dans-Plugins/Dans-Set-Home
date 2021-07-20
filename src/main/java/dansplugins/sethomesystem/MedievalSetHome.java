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
