package dansplugins.sethomesystem.data;

import dansplugins.sethomesystem.exceptions.HomeRecordNotFoundException;
import dansplugins.sethomesystem.objects.HomeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentDataTest {
    private PersistentData persistentData;

    @BeforeEach
    void setUp() {
        persistentData = new PersistentData();
    }

    private static HomeRecord recordFor(String playerName) {
        HomeRecord homeRecord = new HomeRecord();
        homeRecord.setPlayerName(playerName);
        return homeRecord;
    }

    @Test
    void getHomeRecords_newInstance_isEmpty() {
        assertTrue(persistentData.getHomeRecords().isEmpty());
    }

    @Test
    void addHomeRecord_appendsToHomeRecords() {
        HomeRecord homeRecord = recordFor("Alice");

        persistentData.addHomeRecord(homeRecord);

        assertEquals(1, persistentData.getHomeRecords().size());
        assertSame(homeRecord, persistentData.getHomeRecords().get(0));
    }

    @Test
    void getHomeRecord_existingPlayer_returnsThatRecord() throws HomeRecordNotFoundException {
        HomeRecord alice = recordFor("Alice");
        HomeRecord bob = recordFor("Bob");
        persistentData.addHomeRecord(alice);
        persistentData.addHomeRecord(bob);

        assertSame(bob, persistentData.getHomeRecord("Bob"));
    }

    @Test
    void getHomeRecord_matchesPlayerNameIgnoringCase() throws HomeRecordNotFoundException {
        HomeRecord alice = recordFor("Alice");
        persistentData.addHomeRecord(alice);

        assertSame(alice, persistentData.getHomeRecord("aLICE"));
    }

    @Test
    void getHomeRecord_unknownPlayer_throwsHomeRecordNotFoundException() {
        persistentData.addHomeRecord(recordFor("Alice"));

        assertThrows(HomeRecordNotFoundException.class, () -> persistentData.getHomeRecord("Bob"));
    }

    @Test
    void getHomeRecord_duplicateNames_returnsFirstAdded() throws HomeRecordNotFoundException {
        HomeRecord first = recordFor("Alice");
        HomeRecord second = recordFor("alice");
        persistentData.addHomeRecord(first);
        persistentData.addHomeRecord(second);

        assertSame(first, persistentData.getHomeRecord("ALICE"));
    }

    @Test
    void hasHomeRecord_existingPlayer_returnsTrue() {
        persistentData.addHomeRecord(recordFor("Alice"));

        assertTrue(persistentData.hasHomeRecord("Alice"));
        assertTrue(persistentData.hasHomeRecord("alice"));
    }

    @Test
    void hasHomeRecord_unknownPlayer_returnsFalse() {
        persistentData.addHomeRecord(recordFor("Alice"));

        assertFalse(persistentData.hasHomeRecord("Bob"));
    }
}
