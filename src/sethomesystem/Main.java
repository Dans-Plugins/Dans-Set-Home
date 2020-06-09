package sethomesystem;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

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

}
