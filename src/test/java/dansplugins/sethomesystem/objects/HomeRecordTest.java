package dansplugins.sethomesystem.objects;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HomeRecordTest {
    @TempDir
    Path tempDir;

    private MockedStatic<Bukkit> bukkitMock;
    private World world;
    private RecordingHandler log;
    private Logger logger;

    @BeforeEach
    void setUp() {
        world = mock(World.class);
        Server server = mock(Server.class);
        when(server.createWorld(any(WorldCreator.class))).thenReturn(world);
        bukkitMock = mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getServer).thenReturn(server);
        log = new RecordingHandler();
        logger = Logger.getLogger("HomeRecordTest." + tempDir.getFileName());
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(log);
    }

    @AfterEach
    void tearDown() {
        bukkitMock.close();
    }

    @Test
    void load_readsPlayerNameAndLocation() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "11.5", "64.0", "-33.25"), logger);

        assertEquals("Steve", record.getPlayerName());
        assertNotNull(record.getHomeLocation());
        assertEquals(11.5, record.getHomeLocation().getX());
        assertEquals(64.0, record.getHomeLocation().getY());
        assertEquals(-33.25, record.getHomeLocation().getZ());
        assertEquals(world, record.getHomeLocation().getWorld());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void load_coordinateOfExactlyZero_stillLoadsLocation() throws IOException {
        // a home on the x = 0 or z = 0 axis used to be discarded, because 0 doubled as the
        // sentinel for "this coordinate was missing from the file"
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "0.0", "64.0", "0.0"), logger);

        assertNotNull(record.getHomeLocation());
        assertEquals(0.0, record.getHomeLocation().getX());
        assertEquals(64.0, record.getHomeLocation().getY());
        assertEquals(0.0, record.getHomeLocation().getZ());
    }

    @Test
    void load_allCoordinatesZero_stillLoadsLocation() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "0.0", "0.0", "0.0"), logger);

        assertNotNull(record.getHomeLocation());
        assertEquals(0.0, record.getHomeLocation().getY());
    }

    @Test
    void load_missingCoordinateLines_leavesLocationNull() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "11.5"), logger);

        assertEquals("Steve", record.getPlayerName());
        assertNull(record.getHomeLocation());
        // a world with no complete set of coordinates is a truncated record, which is worth a warning
        List<String> warnings = log.warningMessages();
        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("Steve"), warnings.get(0));
    }

    @Test
    void load_nameOnly_leavesLocationNull() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve"), logger);

        assertEquals("Steve", record.getPlayerName());
        assertNull(record.getHomeLocation());
        // a player who has never run /sethome has a record with no location, which is not a failure
        assertEquals(0, log.warnings().size());
    }

    @Test
    void load_malformedCoordinate_leavesLocationNullAndWarns() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "eleven", "64.0", "-33.25"), logger);

        assertEquals("Steve", record.getPlayerName());
        assertNull(record.getHomeLocation());
        List<LogRecord> warnings = log.warnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains("Steve"), warnings.get(0).getMessage());
        assertTrue(warnings.get(0).getThrown() instanceof NumberFormatException);
    }

    @Test
    void load_missingFile_leavesRecordUntouchedAndWarns() {
        HomeRecord record = new HomeRecord();
        record.setPlayerName("Steve");
        File missing = tempDir.resolve("Steve.txt").toFile();

        record.load(missing, logger);

        assertEquals("Steve", record.getPlayerName());
        assertNull(record.getHomeLocation());
        List<LogRecord> warnings = log.warnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains(missing.getPath()), warnings.get(0).getMessage());
        assertTrue(warnings.get(0).getMessage().contains("Steve"), warnings.get(0).getMessage());
    }

    @Test
    void save_writesTheRecordIntoTheGivenFolder() throws IOException {
        HomeRecord record = new HomeRecord();
        record.setPlayerName("Steve");
        File saveFolder = tempDir.resolve("DansSetHome").toFile();

        record.save(saveFolder, logger);

        File saved = new File(saveFolder, "Steve.txt");
        assertEquals("Steve\n", new String(Files.readAllBytes(saved.toPath()), StandardCharsets.UTF_8));
        assertEquals(0, log.warnings().size());
    }

    @Test
    void save_whenTheFolderCannotBeCreated_warnsForThePlayer() throws IOException {
        HomeRecord record = new HomeRecord();
        record.setPlayerName("Steve");
        // a regular file where the folder should be: mkdirs() fails and the record cannot be created
        File saveFolder = tempDir.resolve("DansSetHome").toFile();
        Files.write(saveFolder.toPath(), "not a folder".getBytes(StandardCharsets.UTF_8));

        record.save(saveFolder, logger);

        List<LogRecord> warnings = log.warnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains("Steve"), warnings.get(0).getMessage());
        assertTrue(warnings.get(0).getMessage().contains(new File(saveFolder, "Steve.txt").getPath()),
                warnings.get(0).getMessage());
        assertNotNull(warnings.get(0).getThrown());
    }

    private File saveFile(String filename, String... lines) throws IOException {
        Path file = tempDir.resolve(filename);
        StringBuilder contents = new StringBuilder();
        for (String line : lines) {
            contents.append(line).append("\n");
        }
        Files.write(file, contents.toString().getBytes(StandardCharsets.UTF_8));
        return file.toFile();
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

        List<String> warningMessages() {
            return warnings().stream().map(LogRecord::getMessage).collect(Collectors.toList());
        }

        @Override public void flush() { }
        @Override public void close() { }
    }
}
