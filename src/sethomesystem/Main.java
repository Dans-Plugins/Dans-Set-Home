package sethomesystem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

public class Main extends JavaPlugin implements Listener {

    ArrayList<HomeRecord> homeRecords = new ArrayList<>();

    @Override
    public void onEnable() {
        System.out.println("Medieval Set Home plugin enabling...");

        this.getServer().getPluginManager().registerEvents(this, this);

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
        try {
            File saveFolder = new File("./plugins/Medieval-Set-Home/");
            if (!saveFolder.exists()) {
                saveFolder.mkdir();
            }
            File saveFile = new File("./plugins/Medieval-Set-Home/" + "home-record-filenames.txt");
            if (saveFile.createNewFile()) {
                System.out.println("Save file for home record filenames created.");
            } else {
                System.out.println("Save file for home record filenames already exists. Overwriting.");
            }

            FileWriter saveWriter = new FileWriter(saveFile);

            // actual saving takes place here
            for (HomeRecord record : homeRecords) {
                saveWriter.write(record.getPlayerName() + ".txt" + "\n");
            }

            saveWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred while saving home record filenames.");
        }
    }

    public void saveHomeRecords() {
        for (HomeRecord record : homeRecords) {
            record.save();
        }
    }

    public void loadHomeRecords() {
        try {
            System.out.println("Attempting to load home records...");
            File loadFile = new File("./plugins/Medieval-Set-Home/" + "home-record-filenames.txt");
            Scanner loadReader = new Scanner(loadFile);

            // actual loading
            while (loadReader.hasNextLine()) {
                String nextName = loadReader.nextLine();
                HomeRecord temp = new HomeRecord();
                temp.setPlayerName(nextName);
                temp.load(nextName + ".txt"); // provides owner field among other things

                // existence check
                boolean exists = false;
                for (int i = 0; i < homeRecords.size(); i++) {
                    if (homeRecords.get(i).getPlayerName().equalsIgnoreCase(temp.getPlayerName())) {
                        homeRecords.remove(i);
                    }
                }

                homeRecords.add(temp);

            }

            loadReader.close();
            System.out.println("Home records successfully loaded.");
        } catch (FileNotFoundException e) {
            System.out.println("Error loading the factions!");
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // sethome command
        if (label.equalsIgnoreCase("sethome")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                for (HomeRecord record : homeRecords) {
                    if (record.getPlayerName().equalsIgnoreCase(player.getName())) {
                        record.setHomeLocation(player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "Home set!");
                        return true;
                    }
                }
            }
        }

        // home command
        if (label.equalsIgnoreCase("home")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                for (HomeRecord record : homeRecords) {
                    if (record.getPlayerName().equalsIgnoreCase(player.getName())) {
                        if (record.getHomeLocation() != null) {
                            player.teleport(record.getHomeLocation());
                        }
                        else {
                            player.sendMessage(ChatColor.RED + "Home not set!");
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    @EventHandler()
    public void onJoin(PlayerJoinEvent event) {
        if (!hasHomeRecord(event.getPlayer().getName())) {
            HomeRecord newRecord = new HomeRecord();
            newRecord.setPlayerName(event.getPlayer().getName());

            homeRecords.add(newRecord);
        }
    }

    public boolean hasHomeRecord(String playerName) {
        for (HomeRecord record : homeRecords) {
            if (record.getPlayerName().equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

}
