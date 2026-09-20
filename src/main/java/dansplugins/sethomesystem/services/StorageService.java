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
import java.util.logging.Level;
import java.util.logging.Logger;

public class StorageService {
    /**
     * The name of the folder home records were written to before the plugin was renamed. Bukkit
     * derives the current folder from the plugin's name, so records written under the old name are
     * left behind unless they are migrated across on startup.
     */
    private static final String LEGACY_FOLDER_NAME = "Medieval-Set-Home";

    private static final String FILENAME_INDEX = "home-record-filenames.txt";

    private final PersistentData persistentData;
    private final File dataFolder;
    private final File legacyDataFolder;
    private final Logger logger;

    /**
     * The legacy folder is looked for beside the given data folder rather than under a path
     * relative to the working directory, so that it is found wherever the server keeps its
     * plugins.
     *
     * <p>Failures are reported through the given logger at {@link Level#WARNING}, so that a
     * record which could not be saved, loaded or migrated shows up in the server log rather
     * than as a home that is silently missing.
     */
    public StorageService(PersistentData persistentData, File dataFolder, Logger logger) {
        this(persistentData, dataFolder, new File(dataFolder.getAbsoluteFile().getParentFile(), LEGACY_FOLDER_NAME), logger);
    }

    StorageService(PersistentData persistentData, File dataFolder, File legacyDataFolder, Logger logger) {
        this.persistentData = persistentData;
        this.dataFolder = dataFolder;
        this.legacyDataFolder = legacyDataFolder;
        this.logger = logger;
    }

    public File getLegacyDataFolder() {
        return legacyDataFolder;
    }

    /**
     * Moves home records written under the plugin's former name into the current data folder.
     *
     * <p>Nothing is moved unless the legacy folder holds a filename index and the current data
     * folder does not, so a server that has already started on the current folder is never
     * overwritten by older records.
     *
     * <p>A file that cannot be moved is left where it is and reported as a warning. Once the
     * index has moved, the current folder counts as in use and the migration is not attempted
     * again, so anything left behind has to be moved by hand.
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
            logger.warning("Could not create " + dataFolder.getPath() + " to migrate home records into. "
                    + legacyFiles.length + " file(s) were left in " + legacyDataFolder.getPath() + ".");
            return 0;
        }

        int migrated = 0;
        for (File legacyFile : legacyFiles) {
            try {
                Files.move(legacyFile.toPath(), new File(dataFolder, legacyFile.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                migrated++;
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not move " + legacyFile.getName() + " from "
                        + legacyDataFolder.getPath() + " to " + dataFolder.getPath() + ".", e);
            }
        }
        if (migrated < legacyFiles.length) {
            logger.warning((legacyFiles.length - migrated) + " of " + legacyFiles.length + " file(s) were left in "
                    + legacyDataFolder.getPath() + ". The migration is not retried, so they have to be moved into "
                    + dataFolder.getPath() + " by hand.");
        }
        return migrated;
    }

    public void saveHomeRecordFileNames() {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File saveFile = new File(dataFolder, FILENAME_INDEX);
            saveFile.createNewFile();

            FileWriter saveWriter = new FileWriter(saveFile);

            // actual saving takes place here
            for (HomeRecord record : persistentData.getHomeRecords()) {
                saveWriter.write(record.getPlayerName() + ".txt" + "\n");
            }

            saveWriter.close();

        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not write the home record index " + new File(dataFolder, FILENAME_INDEX).getPath()
                    + ". Homes will not be loaded on the next start until it can be written.", e);
        }
    }

    public void saveHomeRecords() {
        for (HomeRecord record : persistentData.getHomeRecords()) {
            record.save(dataFolder, logger);
        }
    }

    public void loadHomeRecords() {
        File loadFile = new File(dataFolder, FILENAME_INDEX);
        // a server that has never saved has no index yet, which is not a failure
        if (!loadFile.exists()) {
            logger.fine("No home record index at " + loadFile.getPath() + "; nothing to load.");
            return;
        }
        try {
            Scanner loadReader = new Scanner(loadFile);

            // actual loading
            while (loadReader.hasNextLine()) {
                String nextName = loadReader.nextLine();
                HomeRecord temp = new HomeRecord();
                temp.setPlayerName(nextName);
                temp.load(new File(dataFolder, nextName), logger); // provides owner field among other things

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
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Could not read the home record index " + loadFile.getPath()
                    + ". No homes were loaded.", e);
        }
    }
}
