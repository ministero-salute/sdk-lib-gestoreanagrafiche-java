package it.mds.sdk.connettore.anagrafiche.tabella;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecordCDMTest {

    @Test
    void validaCostruttore() {
        RecordCDM recordCDM = new RecordCDM();
        assertTrue(recordCDM instanceof RecordCDM);
    }

    @Test
    void validaObject() {
        RecordCDM recordCDM = new RecordCDM(1, "prova", "020", "abcdefg", "CF");
        assertTrue(recordCDM instanceof RecordCDM);
    }

    @Test
    void validaToString() {
        RecordCDM recordCDM = new RecordCDM(1, "prova", "020", "abcdefg", "CF");
        assertFalse(recordCDM.toString().isEmpty());
    }

    @Test
    void validaGetter() {
        RecordCDM recordCDM = new RecordCDM();
        int idRecord = 1;
        recordCDM.setIdRecord(1);
        int idRecordFromInstance = recordCDM.getIdRecord();
        assertEquals(idRecord, idRecordFromInstance);
    }
}
