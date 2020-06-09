package sethomesystem;

import org.bukkit.Location;

public class HomeRecord {
    private String playerName;
    private Location homeLocation;

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
}
