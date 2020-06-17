package sethomesystem;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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

import static org.bukkit.Bukkit.getServer;

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
                temp.load(nextName); // provides owner field among other things

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
                            player.sendMessage(ChatColor.GREEN + "Teleporting in 3 seconds...");
                            int seconds = 3;

                            Location initialLocation = player.getLocation();

                            getServer().getScheduler().runTaskLater(this, new Runnable() {
                                @Override
                                public void run() {
                                    if (initialLocation.getX() == player.getLocation().getX() &&
                                            initialLocation.getY() == player.getLocation().getY() &&
                                            initialLocation.getZ() == player.getLocation().getZ()) {

                                        // teleport the player
                                        player.teleport(record.getHomeLocation());

                                    }
                                    else {
                                        player.sendMessage(ChatColor.RED + "Movement Detected. Teleport cancelled.");
                                    }

                                }
                            }, seconds * 20);
                        }
                        else {
                            player.sendMessage(ChatColor.RED + "Home not set!");
                        }

                        return true;
                    }
                }
            }
        }

        if (label.equalsIgnoreCase("msh")) {
            // args check
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Try /msh help");
                return false;
            }

            // help command
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.AQUA + "/help - View a list of helpful commands.");
                sender.sendMessage(ChatColor.AQUA + "/sethome - Set your home location.");
                sender.sendMessage(ChatColor.AQUA + "/home - Teleport to your home location.");
                sender.sendMessage(ChatColor.AQUA + "/forcesave - Force a save from the console.");
                sender.sendMessage(ChatColor.AQUA + "/forceload - Force a load from the console.");
            }

            // forcesave
            if (args[0].equalsIgnoreCase("forcesave")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used from the console.");
                    return false;
                }

                System.out.println("Medieval Set Home is saving...");
                saveHomeRecords();
            }

            // forceload
            if (args[0].equalsIgnoreCase("forceload")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used from the console.");
                    return false;
                }

                System.out.println("Medieval Set Home is loading...");
                saveHomeRecordFileNames();
                saveHomeRecords();
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
