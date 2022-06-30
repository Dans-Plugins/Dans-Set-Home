package dansplugins.sethomesystem.data;

import dansplugins.sethomesystem.objects.HomeRecord;

import java.util.ArrayList;

public class PersistentData {
    private ArrayList<HomeRecord> homeRecords = new ArrayList<>();

    public ArrayList<HomeRecord> getHomeRecords() {
        return homeRecords;
    }

    public boolean hasHomeRecord(String playerName) {
        for (HomeRecord record : getHomeRecords()) {
            if (record.getPlayerName().equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

}
