package dansplugins.sethomesystem.data;

import dansplugins.sethomesystem.objects.HomeRecord;

import java.util.ArrayList;

public class PersistentData {

    private static PersistentData instance;

    private ArrayList<HomeRecord> homeRecords = new ArrayList<>();

    private PersistentData() {

    }

    public static PersistentData getInstance() {
        if (instance == null) {
            instance = new PersistentData();
        }
        return instance;
    }

    public ArrayList<HomeRecord> getHomeRecords() {
        return homeRecords;
    }

    public boolean hasHomeRecord(String playerName) {
        for (HomeRecord record : PersistentData.getInstance().getHomeRecords()) {
            if (record.getPlayerName().equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

}
