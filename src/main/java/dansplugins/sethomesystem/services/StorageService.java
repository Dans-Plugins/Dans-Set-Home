package dansplugins.sethomesystem.services;

import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.objects.HomeRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class StorageService {
    /**
     * The folder home records were written to before the plugin was renamed. Bukkit derives the
     * current folder from the plugin's name, so records written under the old name are left behind
     * unless they are migrated across on startup.
     */
    public static final File LEGACY_DATA_FOLDER = new File("./plugins/Medieval-Set-Home/");

    private static final String FILENAME_INDEX = "home-record-filenames.txt";

    private final PersistentData persistentData;
    private final File dataFolder;
    private final File legacyDataFolder;

    private final boolean debug = false;

    public StorageService(PersistentData persistentData, File dataFolder) {
        this(persistentData, dataFolder, LEGACY_DATA_FOLDER);
    }

    StorageService(PersistentData persistentData, File dataFolder, File legacyDataFolder) {
        this.persistentData = persistentData;
        this.dataFolder = dataFolder;
        this.legacyDataFolder = legacyDataFolder;
    }

    /**
     * Moves home records written under the plugin's former name into the current data folder.
     *
     * <p>Nothing is moved unless the legacy folder holds a filename index and the current data
     * folder does not, so a server that has already started on the current folder is never
     * overwritten by older records.
     *
     * @return the number of files moved
     */
    public int migrateLegacyDataFolder() {
        if (!legacyDataFolder.isDirectory()) {
            return 0;
        }
        if (!new File(legacyDataFolder, FILENAME_INDEX).isFile()) {
            return 0;
        }
        if (new File(dataFolder, FILENAME_INDEX).isFile()) {
            return 0;
        }
        File[] legacyFiles = legacyDataFolder.listFiles(file -> file.isFile() && file.getName().endsWith(".txt"));
        if (legacyFiles == null || legacyFiles.length == 0) {
            return 0;
        }
        if (!dataFolder.isDirectory() && !dataFolder.mkdirs()) {
            if (debug) { System.out.println("Could not create the data folder to migrate home records into."); }
            return 0;
        }

        int migrated = 0;
        for (File legacyFile : legacyFiles) {
            try {
                Files.move(legacyFile.toPath(), new File(dataFolder, legacyFile.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                migrated++;
            } catch (IOException e) {
                if (debug) { System.out.println("An error occurred migrating " + legacyFile.getName() + "."); }
            }
        }
        return migrated;
    }

    public void saveHomeRecordFileNames() {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File saveFile = new File(dataFolder, FILENAME_INDEX);
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
            record.save(dataFolder);
        }
    }

    public void loadHomeRecords() {
        try {
            if (debug) { System.out.println("Attempting to load home records..."); }
            File loadFile = new File(dataFolder, FILENAME_INDEX);
            Scanner loadReader = new Scanner(loadFile);

            // actual loading
            while (loadReader.hasNextLine()) {
                String nextName = loadReader.nextLine();
                HomeRecord temp = new HomeRecord();
                temp.setPlayerName(nextName);
                temp.load(new File(dataFolder, nextName)); // provides owner field among other things

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
