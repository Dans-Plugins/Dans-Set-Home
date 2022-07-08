package dansplugins.sethomesystem.data;

import dansplugins.sethomesystem.exceptions.HomeRecordNotFoundException;
import dansplugins.sethomesystem.objects.HomeRecord;

import java.util.ArrayList;
import java.util.List;

public class PersistentData {
    private final List<HomeRecord> homeRecords = new ArrayList<>();

    public boolean hasHomeRecord(String playerName) {
        try {
            getHomeRecord(playerName);
            return true;
        } catch(HomeRecordNotFoundException e) {
            return false;
        }
    }

    public void addHomeRecord(HomeRecord homeRecord) {
        getHomeRecords().add(homeRecord);
    }

    public HomeRecord getHomeRecord(String playerName) throws HomeRecordNotFoundException {
        for (HomeRecord homeRecord : getHomeRecords()) {
            if (homeRecord.getPlayerName().equalsIgnoreCase(playerName)) {
                return homeRecord;
            }
        }
        throw new HomeRecordNotFoundException();
    }

    /**
     * This is public for storage purposes.
     * @return A list of home records.
     */
    public List<HomeRecord> getHomeRecords() {
        return homeRecords;
    }

}
