package dansplugins.sethomesystem.objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class HomeRecord {
    private String playerName = "";
    private Location homeLocation = null;

    public void setPlayerName(String name) {
        playerName = name;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setHomeLocation(Location location) {
        homeLocation = location;
    }

    public Location getHomeLocation() {
        return homeLocation;
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
     */
    public void load(File loadFile, Logger logger) {
        try {
            Scanner loadReader = new Scanner(loadFile);

            // actual loading
            if (loadReader.hasNextLine()) {
                setPlayerName(loadReader.nextLine());
            }

            World world = null;
            Double x = null;
            Double y = null;
            Double z = null;

            try {
                if (loadReader.hasNextLine()) {
                    world = getServer().createWorld(new WorldCreator(loadReader.nextLine()));
                }
                if (loadReader.hasNextLine()) {
                    x = Double.parseDouble(loadReader.nextLine());
                }
                if (loadReader.hasNextLine()) {
                    y = Double.parseDouble(loadReader.nextLine());
                }
                if (loadReader.hasNextLine()) {
                    z = Double.parseDouble(loadReader.nextLine());
                }

                // set location - each coordinate is tracked by whether it was read at all, so that
                // a legitimately saved 0 is not mistaken for a missing value
                if (world != null && x != null && y != null && z != null) {
                    homeLocation = new Location(world, x, y, z);
                }
                else if (world != null) {
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
}
