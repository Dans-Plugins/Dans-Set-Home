package dansplugins.sethomesystem.objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class HomeRecord {
    private String playerName = "";
    private Location homeLocation = null;
    // the saved world name and coordinates of a home whose world could not be found when the record
    // was loaded, kept so that the home is written back unchanged rather than dropped from disk
    private List<String> unresolvedHomeLines = null;

    public void setPlayerName(String name) {
        playerName = name;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setHomeLocation(Location location) {
        homeLocation = location;
        unresolvedHomeLines = null;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    /**
     * The name of the world this record's home is in, when that world did not exist when the
     * record was loaded; {@code null} otherwise.
     */
    public String getMissingWorldName() {
        return unresolvedHomeLines == null ? null : unresolvedHomeLines.get(0);
    }

    /**
     * Writes the record to {@code <playerName>.txt} in the given folder. A record that cannot be
     * written is reported through the given logger at {@link Level#WARNING}, since the player
     * would otherwise find their home missing after the next restart with nothing in the log
     * to say why.
     */
    public void save(File saveFolder, Logger logger) {
        try {
            if (!saveFolder.exists()) {
                saveFolder.mkdirs();
            }
            File saveFile = new File(saveFolder, playerName + ".txt");
            saveFile.createNewFile();

            FileWriter saveWriter = new FileWriter(saveFile);

            // actual saving takes place here
            saveWriter.write(playerName + "\n");

            if (homeLocation != null) {
                saveWriter.write(homeLocation.getWorld().getName() + "\n");
                saveWriter.write(homeLocation.getX() + "\n");
                saveWriter.write(homeLocation.getY() + "\n");
                saveWriter.write(homeLocation.getZ() + "\n");
                saveWriter.write(homeLocation.getYaw() + "\n");
                saveWriter.write(homeLocation.getPitch() + "\n");
            }
            else if (unresolvedHomeLines != null) {
                for (String line : unresolvedHomeLines) {
                    saveWriter.write(line + "\n");
                }
            }

            saveWriter.close();

        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not save the home record of " + playerName + " to "
                    + new File(saveFolder, playerName + ".txt").getPath() + ".", e);
        }
    }

    /**
     * Reads the record from the given file. A file that cannot be read, or whose home location
     * is malformed, is reported through the given logger at {@link Level#WARNING}; a record with
     * no home location at all is a player who has not run {@code /sethome}, and is not a failure.
     *
     * <p>A world that is not loaded is only loaded if its folder exists in the world container,
     * because {@code createWorld} would otherwise generate a new world under the saved name. A home
     * in a world that does not exist is not loaded, but is kept, and written back by {@link #save}.
     */
    public void load(File loadFile, Logger logger) {
        try {
            Scanner loadReader = new Scanner(loadFile);

            // actual loading
            if (loadReader.hasNextLine()) {
                setPlayerName(loadReader.nextLine());
            }

            String worldName = null;
            List<String> homeLines = new ArrayList<>();
            Double x = null;
            Double y = null;
            Double z = null;
            // the facing direction was not saved before 1.5.2, so a file without it still loads,
            // facing the way new Location(world, x, y, z) does
            float yaw = 0;
            float pitch = 0;

            try {
                if (loadReader.hasNextLine()) {
                    worldName = readLine(loadReader, homeLines);
                }
                if (loadReader.hasNextLine()) {
                    x = Double.parseDouble(readLine(loadReader, homeLines));
                }
                if (loadReader.hasNextLine()) {
                    y = Double.parseDouble(readLine(loadReader, homeLines));
                }
                if (loadReader.hasNextLine()) {
                    z = Double.parseDouble(readLine(loadReader, homeLines));
                }
                if (loadReader.hasNextLine()) {
                    yaw = Float.parseFloat(readLine(loadReader, homeLines));
                }
                if (loadReader.hasNextLine()) {
                    pitch = Float.parseFloat(readLine(loadReader, homeLines));
                }

                // set location - each coordinate is tracked by whether it was read at all, so that
                // a legitimately saved 0 is not mistaken for a missing value
                if (worldName != null && x != null && y != null && z != null) {
                    World world = findWorld(worldName);
                    if (world != null) {
                        homeLocation = new Location(world, x, y, z, yaw, pitch);
                    }
                    else {
                        unresolvedHomeLines = homeLines;
                        logger.warning("The home of " + playerName + " in " + loadFile.getPath() + " is in the world "
                                + worldName + ", which does not exist. The home was not loaded, and is kept on disk"
                                + " until " + playerName + " sets a new one.");
                    }
                }
                else if (worldName != null) {
                    // a world line with no complete set of coordinates after it is a truncated
                    // record, not a player without a home
                    logger.warning("The home record of " + playerName + " in " + loadFile.getPath()
                            + " is missing one or more coordinates. The home was not loaded.");
                }

            }
            catch(Exception e) {
                logger.log(Level.WARNING, "The home record of " + playerName + " in " + loadFile.getPath()
                        + " could not be read as a home location. The home was not loaded.", e);
            }

            loadReader.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Could not read the home record " + loadFile.getPath()
                    + ". The home of " + playerName + " was not loaded.", e);
        }
    }

    private static String readLine(Scanner reader, List<String> lines) {
        String line = reader.nextLine();
        lines.add(line);
        return line;
    }

    private static World findWorld(String name) {
        World world = getServer().getWorld(name);
        if (world == null && new File(getServer().getWorldContainer(), name).isDirectory()) {
            world = getServer().createWorld(new WorldCreator(name));
        }
        return world;
    }
}
