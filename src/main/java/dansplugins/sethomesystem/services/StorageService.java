package dansplugins.sethomesystem.services;

import dansplugins.sethomesystem.objects.HomeRecord;
import dansplugins.sethomesystem.data.PersistentData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class StorageService {
    private final PersistentData persistentData;

    private final boolean debug = false;

    public StorageService(PersistentData persistentData) {
        this.persistentData = persistentData;
    }

    public void saveHomeRecordFileNames() {
        try {
            File saveFolder = new File("./plugins/Medieval-Set-Home/");
            if (!saveFolder.exists()) {
                saveFolder.mkdir();
            }
            File saveFile = new File("./plugins/Medieval-Set-Home/" + "home-record-filenames.txt");
            if (saveFile.createNewFile()) {
                if (debug) { System.out.println("Save file for home record filenames created."); }
            } else {
                if (debug) { System.out.println("Save file for home record filenames already exists. Overwriting."); }
            }

            FileWriter saveWriter = new FileWriter(saveFile);

            // actual saving takes place here
            for (HomeRecord record : persistentData.getHomeRecords()) {
                saveWriter.write(record.getPlayerName() + ".txt" + "\n");
            }

            saveWriter.close();

        } catch (IOException e) {
            if (debug) { System.out.println("An error occurred while saving home record filenames."); }
        }
    }

    public void saveHomeRecords() {
        for (HomeRecord record : persistentData.getHomeRecords()) {
            record.save();
        }
    }

    public void loadHomeRecords() {
        try {
            if (debug) { System.out.println("Attempting to load home records..."); }
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
                for (int i = 0; i < persistentData.getHomeRecords().size(); i++) {
                    if (persistentData.getHomeRecords().get(i).getPlayerName().equalsIgnoreCase(temp.getPlayerName())) {
                        persistentData.getHomeRecords().remove(i);
                    }
                }

                persistentData.getHomeRecords().add(temp);

            }

            loadReader.close();
            if (debug) { System.out.println("Home records successfully loaded."); }
        } catch (FileNotFoundException e) {
            if (debug) { System.out.println("Error loading the factions!"); }
        }
    }
}
