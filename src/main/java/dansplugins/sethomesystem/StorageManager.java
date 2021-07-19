package dansplugins.sethomesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class StorageManager {

    private boolean debug = false;

    private static StorageManager instance;

    public static StorageManager getInstance() {
        if (instance == null) {
            instance = new StorageManager();
        }
        return instance;
    }

    public void saveHomeRecords() {
        for (HomeRecord record : MedievalSetHome.getInstance().homeRecords) {
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
                for (int i = 0; i < MedievalSetHome.getInstance().homeRecords.size(); i++) {
                    if (MedievalSetHome.getInstance().homeRecords.get(i).getPlayerName().equalsIgnoreCase(temp.getPlayerName())) {
                        MedievalSetHome.getInstance().homeRecords.remove(i);
                    }
                }

                MedievalSetHome.getInstance().homeRecords.add(temp);

            }

            loadReader.close();
            if (debug) { System.out.println("Home records successfully loaded."); }
        } catch (FileNotFoundException e) {
            if (debug) { System.out.println("Error loading the factions!"); }
        }
    }
}
