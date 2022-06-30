package dansplugins.sethomesystem.objects;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import static org.bukkit.Bukkit.getServer;

public class HomeRecord {
    private final boolean debug = false;

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

    public void save() {
        try {
            File saveFolder = new File("./plugins/Medieval-Set-Home/");
            if (!saveFolder.exists()) {
                saveFolder.mkdir();
            }
            File saveFile = new File("./plugins/Medieval-Set-Home/" + playerName + ".txt");
            if (saveFile.createNewFile()) {
                if (debug) { System.out.println("Save file for record of " + playerName + " created."); }
            } else {
                if (debug) { System.out.println("Save file for record of " + playerName + " already exists. Altering."); }
            }

            FileWriter saveWriter = new FileWriter("./plugins/Medieval-Set-Home/" + playerName + ".txt");

            // actual saving takes place here
            saveWriter.write(playerName + "\n");

            if (homeLocation != null) {
                // save faction details
                saveWriter.write(homeLocation.getWorld().getName() + "\n");
                saveWriter.write(homeLocation.getX() + "\n");
                saveWriter.write(homeLocation.getY() + "\n");
                saveWriter.write(homeLocation.getZ() + "\n");
            }


            saveWriter.close();

            if (debug) { System.out.println("Successfully saved record belonging to  " + playerName + "."); }

        } catch (IOException e) {
            if (debug) { System.out.println("An error occurred saving the record belonging to " + playerName); }
            e.printStackTrace();
        }
    }

    public void load(String filename) {
        try {
            File loadFile = new File("./plugins/Medieval-Set-Home/" + filename);
            Scanner loadReader = new Scanner(loadFile);

            // actual loading
            if (loadReader.hasNextLine()) {
                setPlayerName(loadReader.nextLine());
            }

            World world = null;
            double x = 0;
            double y = 0;
            double z = 0;

            try {
                if (debug) { System.out.println("Attempting to load home location for " + playerName + "..."); }

                if (loadReader.hasNextLine()) {
                    world = getServer().createWorld(new WorldCreator(loadReader.nextLine()));
                    if (debug) { System.out.println("World successfully acquired."); }
                }
                else {
                    if (debug) { System.out.println("World name not found in file!"); }
                }
                if (loadReader.hasNextLine()) {
                    x = Double.parseDouble(loadReader.nextLine());
                }
                else {
                    if (debug) { System.out.println("X position not found in file!"); }
                }
                if (loadReader.hasNextLine()) {//
                    y = Double.parseDouble(loadReader.nextLine());
                }
                else {
                    if (debug) { System.out.println("Y position not found in file!"); }
                }
                if (loadReader.hasNextLine()) {
                    z = Double.parseDouble(loadReader.nextLine());
                }
                else {
                    if (debug) { System.out.println("Z position not found in file!"); }
                }

                // set location
                if (world != null && x != 0 && y != 0 && z != 0) {
                    homeLocation = new Location(world, x, y, z);
                    if (debug) { System.out.println("Home of " + playerName + " successfully set to " + x + ", " + y + ", " + z + "."); }
                }
                else {
                    if (debug) { System.out.println("One of the variables the home location depends on wasn't loaded!"); }
                }

            }
            catch(Exception e) {
                if (debug) { System.out.println("An error occurred loading the home position."); }
                e.printStackTrace();
            }

            loadReader.close();
            if (debug) { System.out.println("Home of " + playerName + " successfully loaded."); }
        } catch (FileNotFoundException e) {
            if (debug) { System.out.println("An error occurred loading the file " + filename + "."); }
            e.printStackTrace();
        }
    }
}
