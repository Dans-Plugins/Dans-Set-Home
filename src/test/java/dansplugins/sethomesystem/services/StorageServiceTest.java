package dansplugins.sethomesystem.services;

import dansplugins.sethomesystem.data.PersistentData;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageServiceTest {
    private static final String FILENAME_INDEX = "home-record-filenames.txt";

    @TempDir
    Path tempDir;

    private File dataFolder;
    private File legacyDataFolder;
    private PersistentData persistentData;
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        dataFolder = tempDir.resolve("DansSetHome").toFile();
        legacyDataFolder = tempDir.resolve("Medieval-Set-Home").toFile();
        persistentData = new PersistentData();
        storageService = new StorageService(persistentData, dataFolder, legacyDataFolder);
    }

    @Test
    void saveHomeRecordFileNames_writesIndexIntoTheDataFolder() {
        persistentData.addHomeRecord(recordFor("Steve"));

        storageService.saveHomeRecordFileNames();

        assertEquals("Steve.txt\n", contentsOf(new File(dataFolder, FILENAME_INDEX)));
        assertFalse(new File(legacyDataFolder, FILENAME_INDEX).exists());
    }

    @Test
    void saveHomeRecords_writesRecordsIntoTheDataFolder() {
        persistentData.addHomeRecord(recordFor("Steve"));

        storageService.saveHomeRecords();

        assertEquals("Steve\n", contentsOf(new File(dataFolder, "Steve.txt")));
        assertFalse(new File(legacyDataFolder, "Steve.txt").exists());
    }

    @Test
    void loadHomeRecords_readsRecordsFromTheDataFolder() throws IOException {
        write(dataFolder, FILENAME_INDEX, "Steve.txt\n");
        write(dataFolder, "Steve.txt", "Steve\n");

        storageService.loadHomeRecords();

        assertEquals(1, persistentData.getHomeRecords().size());
        assertEquals("Steve", persistentData.getHomeRecords().get(0).getPlayerName());
    }

    @Test
    void migrateLegacyDataFolder_movesRecordsIntoTheDataFolder() throws IOException {
        write(legacyDataFolder, FILENAME_INDEX, "Steve.txt\n");
        write(legacyDataFolder, "Steve.txt", "Steve\n");

        assertEquals(2, storageService.migrateLegacyDataFolder());

        assertEquals("Steve.txt\n", contentsOf(new File(dataFolder, FILENAME_INDEX)));
        assertEquals("Steve\n", contentsOf(new File(dataFolder, "Steve.txt")));
        assertFalse(new File(legacyDataFolder, "Steve.txt").exists());
    }

    @Test
    void migrateLegacyDataFolder_thenLoad_makesLegacyHomesVisible() throws IOException {
        write(legacyDataFolder, FILENAME_INDEX, "Steve.txt\n");
        write(legacyDataFolder, "Steve.txt", "Steve\n");

        storageService.migrateLegacyDataFolder();
        storageService.loadHomeRecords();

        assertEquals(1, persistentData.getHomeRecords().size());
        assertEquals("Steve", persistentData.getHomeRecords().get(0).getPlayerName());
    }

    @Test
    void migrateLegacyDataFolder_whenDataFolderAlreadyHasAnIndex_movesNothing() throws IOException {
        write(legacyDataFolder, FILENAME_INDEX, "Steve.txt\n");
        write(legacyDataFolder, "Steve.txt", "Steve\n");
        write(dataFolder, FILENAME_INDEX, "Alex.txt\n");
        write(dataFolder, "Alex.txt", "Alex\n");

        assertEquals(0, storageService.migrateLegacyDataFolder());

        assertEquals("Alex.txt\n", contentsOf(new File(dataFolder, FILENAME_INDEX)));
        assertTrue(new File(legacyDataFolder, "Steve.txt").exists());
    }

    @Test
    void migrateLegacyDataFolder_whenLegacyFolderIsAbsent_movesNothing() {
        assertEquals(0, storageService.migrateLegacyDataFolder());
        assertFalse(dataFolder.exists());
    }

    @Test
    void migrateLegacyDataFolder_whenLegacyFolderHasNoIndex_movesNothing() throws IOException {
        write(legacyDataFolder, "Steve.txt", "Steve\n");

        assertEquals(0, storageService.migrateLegacyDataFolder());

        assertTrue(new File(legacyDataFolder, "Steve.txt").exists());
        assertFalse(new File(dataFolder, "Steve.txt").exists());
    }

    @Test
    void getLegacyDataFolder_defaultsToASiblingOfTheDataFolder() {
        StorageService service = new StorageService(persistentData, dataFolder);

        assertEquals(tempDir.resolve("Medieval-Set-Home").toFile().getAbsoluteFile(),
                service.getLegacyDataFolder());
    }

    private HomeRecord recordFor(String playerName) {
        HomeRecord record = new HomeRecord();
        record.setPlayerName(playerName);
        return record;
    }

    private void write(File folder, String filename, String contents) throws IOException {
        Files.createDirectories(folder.toPath());
        Files.write(new File(folder, filename).toPath(), contents.getBytes(StandardCharsets.UTF_8));
    }

    private String contentsOf(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Expected " + file + " to be readable.", e);
        }
    }
}
