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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class HomeRecordTest {
    @TempDir
    Path tempDir;

    private MockedStatic<Bukkit> bukkitMock;
    private World world;

    @BeforeEach
    void setUp() {
        world = mock(World.class);
        Server server = mock(Server.class);
        when(server.createWorld(any(WorldCreator.class))).thenReturn(world);
        bukkitMock = mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getServer).thenReturn(server);
    }

    @AfterEach
    void tearDown() {
        bukkitMock.close();
    }

    @Test
    void load_readsPlayerNameAndLocation() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "11.5", "64.0", "-33.25"));

        assertEquals("Steve", record.getPlayerName());
        assertNotNull(record.getHomeLocation());
        assertEquals(11.5, record.getHomeLocation().getX());
        assertEquals(64.0, record.getHomeLocation().getY());
        assertEquals(-33.25, record.getHomeLocation().getZ());
        assertEquals(world, record.getHomeLocation().getWorld());
    }

    @Test
    void load_coordinateOfExactlyZero_stillLoadsLocation() throws IOException {
        // a home on the x = 0 or z = 0 axis used to be discarded, because 0 doubled as the
        // sentinel for "this coordinate was missing from the file"
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "0.0", "64.0", "0.0"));

        assertNotNull(record.getHomeLocation());
        assertEquals(0.0, record.getHomeLocation().getX());
        assertEquals(64.0, record.getHomeLocation().getY());
        assertEquals(0.0, record.getHomeLocation().getZ());
    }

    @Test
    void load_allCoordinatesZero_stillLoadsLocation() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "0.0", "0.0", "0.0"));

        assertNotNull(record.getHomeLocation());
        assertEquals(0.0, record.getHomeLocation().getY());
    }

    @Test
    void load_missingCoordinateLines_leavesLocationNull() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve", "world", "11.5"));

        assertEquals("Steve", record.getPlayerName());
        assertNull(record.getHomeLocation());
    }

    @Test
    void load_nameOnly_leavesLocationNull() throws IOException {
        HomeRecord record = new HomeRecord();

        record.load(saveFile("Steve.txt", "Steve"));

        assertEquals("Steve", record.getPlayerName());
        assertNull(record.getHomeLocation());
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
}
