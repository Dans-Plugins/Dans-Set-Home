package sethomesystem;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Main extends JavaPlugin {

    ArrayList<HomeRecord> homeRecords = new ArrayList<>();

    @Override
    public void onEnable() {
        System.out.println("Medieval Set Home plugin enabling...");

        loadHomeRecords();

        System.out.println("Medieval Set Home plugin enabled.");
    }

    @Override
    public void onDisable() {
        System.out.println("Medieval Set Home plugin disabling...");

        saveHomeRecordFileNames();
        saveHomeRecords();

        System.out.println("Medieval Set Home plugin disabled.");
    }

    public void saveHomeRecordFileNames() {

    }

    public void saveHomeRecords() {

    }

    public void loadHomeRecords() {

    }

}
