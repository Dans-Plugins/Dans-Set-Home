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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageServiceTest {
    private static final String FILENAME_INDEX = "home-record-filenames.txt";

    @TempDir
    Path tempDir;

    private File dataFolder;
    private File legacyDataFolder;
    private PersistentData persistentData;
    private RecordingHandler log;
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        dataFolder = tempDir.resolve("DansSetHome").toFile();
        legacyDataFolder = tempDir.resolve("Medieval-Set-Home").toFile();
        persistentData = new PersistentData();
        log = new RecordingHandler();
        Logger logger = Logger.getLogger("StorageServiceTest." + tempDir.getFileName());
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(log);
        storageService = new StorageService(persistentData, dataFolder, legacyDataFolder, logger);
    }

    @Test
    void saveHomeRecordFileNames_writesIndexIntoTheDataFolder() {
        persistentData.addHomeRecord(recordFor("Steve"));

        storageService.saveHomeRecordFileNames();

        assertEquals("Steve.txt\n", contentsOf(new File(dataFolder, FILENAME_INDEX)));
        assertFalse(new File(legacyDataFolder, FILENAME_INDEX).exists());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void saveHomeRecordFileNames_whenTheDataFolderCannotBeCreated_warns() throws IOException {
        // a regular file where the data folder should be: mkdirs() fails and the index cannot be created
        Files.write(dataFolder.toPath(), "not a folder".getBytes(StandardCharsets.UTF_8));
        persistentData.addHomeRecord(recordFor("Steve"));

        storageService.saveHomeRecordFileNames();

        List<LogRecord> warnings = log.warnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains(new File(dataFolder, FILENAME_INDEX).getPath()),
                warnings.get(0).getMessage());
        assertNotNull(warnings.get(0).getThrown());
    }

    @Test
    void saveHomeRecords_writesRecordsIntoTheDataFolder() {
        persistentData.addHomeRecord(recordFor("Steve"));

        storageService.saveHomeRecords();

        assertEquals("Steve\n", contentsOf(new File(dataFolder, "Steve.txt")));
        assertFalse(new File(legacyDataFolder, "Steve.txt").exists());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void saveHomeRecords_whenARecordCannotBeWritten_warnsForThatPlayer() throws IOException {
        Files.write(dataFolder.toPath(), "not a folder".getBytes(StandardCharsets.UTF_8));
        persistentData.addHomeRecord(recordFor("Steve"));

        storageService.saveHomeRecords();

        List<LogRecord> warnings = log.warnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains("Steve"), warnings.get(0).getMessage());
    }

    @Test
    void loadHomeRecords_readsRecordsFromTheDataFolder() throws IOException {
        write(dataFolder, FILENAME_INDEX, "Steve.txt\n");
        write(dataFolder, "Steve.txt", "Steve\n");

        storageService.loadHomeRecords();

        assertEquals(1, persistentData.getHomeRecords().size());
        assertEquals("Steve", persistentData.getHomeRecords().get(0).getPlayerName());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void loadHomeRecords_whenThereIsNoIndexYet_loadsNothingWithoutWarning() {
        // a server that has never saved has no index, which is the normal first start
        storageService.loadHomeRecords();

        assertEquals(0, persistentData.getHomeRecords().size());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void loadHomeRecords_whenAnIndexedRecordFileIsMissing_warnsForThatFile() throws IOException {
        write(dataFolder, FILENAME_INDEX, "Steve.txt\n");

        storageService.loadHomeRecords();

        List<LogRecord> warnings = log.warnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains(new File(dataFolder, "Steve.txt").getPath()),
                warnings.get(0).getMessage());
    }

    @Test
    void migrateLegacyDataFolder_movesRecordsIntoTheDataFolder() throws IOException {
        write(legacyDataFolder, FILENAME_INDEX, "Steve.txt\n");
        write(legacyDataFolder, "Steve.txt", "Steve\n");

        assertEquals(2, storageService.migrateLegacyDataFolder());

        assertEquals("Steve.txt\n", contentsOf(new File(dataFolder, FILENAME_INDEX)));
        assertEquals("Steve\n", contentsOf(new File(dataFolder, "Steve.txt")));
        assertFalse(new File(legacyDataFolder, "Steve.txt").exists());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void migrateLegacyDataFolder_whenTheDataFolderCannotBeCreated_warnsAndMovesNothing() throws IOException {
        write(legacyDataFolder, FILENAME_INDEX, "Steve.txt\n");
        write(legacyDataFolder, "Steve.txt", "Steve\n");
        Files.write(dataFolder.toPath(), "not a folder".getBytes(StandardCharsets.UTF_8));

        assertEquals(0, storageService.migrateLegacyDataFolder());

        assertTrue(new File(legacyDataFolder, "Steve.txt").exists());
        List<LogRecord> warnings = log.warnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains(dataFolder.getPath()), warnings.get(0).getMessage());
        assertTrue(warnings.get(0).getMessage().contains("2 file(s)"), warnings.get(0).getMessage());
    }

    @Test
    void migrateLegacyDataFolder_whenOneFileCannotBeMoved_warnsAndCountsOnlyTheRest() throws IOException {
        write(legacyDataFolder, FILENAME_INDEX, "Steve.txt\n");
        write(legacyDataFolder, "Steve.txt", "Steve\n");
        // a non-empty directory where Steve.txt would land cannot be replaced, so that one move fails
        write(new File(dataFolder, "Steve.txt"), "child", "");

        assertEquals(1, storageService.migrateLegacyDataFolder());

        assertEquals("Steve.txt\n", contentsOf(new File(dataFolder, FILENAME_INDEX)));
        assertEquals("Steve\n", contentsOf(new File(legacyDataFolder, "Steve.txt")));
        List<String> warnings = log.warnings().stream().map(LogRecord::getMessage).collect(Collectors.toList());
        assertEquals(2, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("Steve.txt"), warnings.get(0));
        assertTrue(warnings.get(1).contains("1 of 2 file(s)"), warnings.get(1));
        assertTrue(warnings.get(1).contains(legacyDataFolder.getPath()), warnings.get(1));
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
        StorageService service = new StorageService(persistentData, dataFolder, Logger.getLogger("StorageServiceTest"));

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

    private static final class RecordingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        List<LogRecord> warnings() {
            return records.stream()
                    .filter(record -> record.getLevel().intValue() >= Level.WARNING.intValue())
                    .collect(Collectors.toList());
        }

        @Override public void flush() { }
        @Override public void close() { }
    }
}
