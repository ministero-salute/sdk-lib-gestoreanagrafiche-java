package it.mds.sdk.connettore.anagrafiche.tabella;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecordBR3060Test {

    @Test
    void validaCostruttore() {
        RecordBR3060 recordBR3060 = new RecordBR3060();
        assertTrue(recordBR3060 instanceof RecordBR3060);
    }

    @Test
    void validaObject() {
        RecordBR3060 recordBR3060 = new RecordBR3060(1, "prova", "020", "abcdefg", "CF", "2022-10-20", "AF", 2);
        assertTrue(recordBR3060 instanceof RecordBR3060);
    }

    @Test
    void validaToString() {
        RecordBR3060 recordBR3060 = new RecordBR3060(1, "prova", "020", "abcdefg", "CF", "2022-10-20", "AF", 2);
        assertFalse(recordBR3060.toString().isEmpty());
    }

    @Test
    void validaGetter() {
        RecordBR3060 recordBR3060 = new RecordBR3060();
        int idRecord = 1;
        recordBR3060.setIdRecord(1);
        int idRecordFromInstance = recordBR3060.getIdRecord();
        assertEquals(idRecord, idRecordFromInstance);
    }
}
