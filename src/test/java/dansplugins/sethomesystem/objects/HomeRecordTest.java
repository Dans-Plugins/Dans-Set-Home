package dansplugins.sethomesystem.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomeRecordTest {
    @TempDir
    Path tempDir;

    private MockedStatic<Bukkit> bukkitMock;
    private World world;
    private Server server;
    private File worldContainer;
    private RecordingHandler log;
    private Logger logger;

    @BeforeEach
    void setUp() {
        world = mock(World.class);
        server = mock(Server.class);
        when(server.getWorld("world")).thenReturn(world);
        worldContainer = tempDir.resolve("worlds").toFile();
        worldContainer.mkdirs();
        when(server.getWorldContainer()).thenReturn(worldContainer);
        // what createWorld does for a name with no folder: generate a new world
        when(server.createWorld(any(WorldCreator.class))).thenReturn(mock(World.class));
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
    void load_withFacingDirection_restoresYawAndPitch() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "11.5", "64.0", "-33.25", "135.5", "-20.0"), logger);

        assertNotNull(record.getHomeLocation());
        assertEquals(135.5f, record.getHomeLocation().getYaw());
        assertEquals(-20.0f, record.getHomeLocation().getPitch());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void load_fileWithoutFacingDirection_stillLoadsLocationFacingDefault() throws IOException {
        // files written before the facing direction was saved end after z
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "11.5", "64.0", "-33.25"), logger);

        assertNotNull(record.getHomeLocation());
        assertEquals(0.0f, record.getHomeLocation().getYaw());
        assertEquals(0.0f, record.getHomeLocation().getPitch());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void saveThenLoad_keepsTheFacingDirection() {
        when(world.getName()).thenReturn("world");
        HomeRecord saved = new HomeRecord();
        saved.setPlayerName("Steve");
        saved.setHomeLocation(new Location(world, 11.5, 64.0, -33.25, 135.5f, -20.0f));
        File saveFolder = tempDir.resolve("DansSetHome").toFile();
        saved.save(saveFolder, logger);

        HomeRecord loaded = new HomeRecord();
        loaded.load(new File(saveFolder, "Steve.txt"), logger);

        assertNotNull(loaded.getHomeLocation());
        assertEquals(11.5, loaded.getHomeLocation().getX());
        assertEquals(64.0, loaded.getHomeLocation().getY());
        assertEquals(-33.25, loaded.getHomeLocation().getZ());
        assertEquals(135.5f, loaded.getHomeLocation().getYaw());
        assertEquals(-20.0f, loaded.getHomeLocation().getPitch());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void load_worldNotLoadedButOnDisk_loadsTheWorld() throws IOException {
        // a world kept by a world-management plugin that has not loaded it yet still has its folder
        World unloaded = mock(World.class);
        new File(worldContainer, "nether_hub").mkdirs();
        when(server.createWorld(any(WorldCreator.class))).thenReturn(unloaded);
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "nether_hub", "11.5", "64.0", "-33.25"), logger);

        assertNotNull(record.getHomeLocation());
        assertEquals(unloaded, record.getHomeLocation().getWorld());
        assertNull(record.getMissingWorldName());
        assertEquals(0, log.warnings().size());
    }

    @Test
    void load_worldThatDoesNotExist_doesNotCreateItAndWarns() throws IOException {
        // createWorld would generate a brand new world under the saved name
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "old_world", "11.5", "64.0", "-33.25"), logger);

        verify(server, never()).createWorld(any(WorldCreator.class));
        assertEquals("Steve", record.getPlayerName());
        assertNull(record.getHomeLocation());
        assertEquals("old_world", record.getMissingWorldName());
        List<String> warnings = log.warningMessages();
        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("Steve"), warnings.get(0));
        assertTrue(warnings.get(0).contains("old_world"), warnings.get(0));
    }

    @Test
    void load_worldThatDoesNotExist_thenSave_keepsTheHomeOnDisk() throws IOException {
        File saveFolder = tempDir.resolve("DansSetHome").toFile();
        saveFolder.mkdirs();
        String original = "Steve\nold_world\n11.5\n64.0\n-33.25\n135.5\n-20.0\n";
        File saved = new File(saveFolder, "Steve.txt");
        Files.write(saved.toPath(), original.getBytes(StandardCharsets.UTF_8));
        HomeRecord record = new HomeRecord();
        record.load(saved, logger);

        record.save(saveFolder, logger);

        assertEquals(original, new String(Files.readAllBytes(saved.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    void setHomeLocation_afterAHomeInAMissingWorld_replacesIt() throws IOException {
        when(world.getName()).thenReturn("world");
        File saveFolder = tempDir.resolve("DansSetHome").toFile();
        HomeRecord record = new HomeRecord();
        record.load(saveFile("Steve.txt", "Steve", "old_world", "11.5", "64.0", "-33.25"), logger);

        record.setHomeLocation(new Location(world, 1.0, 2.0, 3.0, 4.0f, 5.0f));
        record.save(saveFolder, logger);

        assertNull(record.getMissingWorldName());
        assertEquals("Steve\nworld\n1.0\n2.0\n3.0\n4.0\n5.0\n",
                new String(Files.readAllBytes(new File(saveFolder, "Steve.txt").toPath()), StandardCharsets.UTF_8));
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
