/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.tabella;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecordCDMCT2Test {

    @Test
    void validaCostruttore() {
        RecordCDMCT2 recordCDMCT2 = new RecordCDMCT2();
        assertTrue(recordCDMCT2 instanceof RecordCDMCT2);
    }

    @Test
    void validaObject() {
        RecordCDMCT2 recordCDMCT2 = new RecordCDMCT2(1, "prova", "020", "abcdefg", "CF","AB");
        assertTrue(recordCDMCT2 instanceof RecordCDMCT2);
    }

    @Test
    void validaToString() {
        RecordCDMCT2 recordCDMCT2 = new RecordCDMCT2(1, "prova", "020", "abcdefg", "CF","AB");
        assertFalse(recordCDMCT2.toString().isEmpty());
    }

    @Test
    void validaGetter() {
        RecordCDMCT2 recordCDMCT2 = new RecordCDMCT2();
        int idRecord = 1;
        recordCDMCT2.setIdRecord(1);
        int idRecordFromInstance = recordCDMCT2.getIdRecord();
        assertEquals(idRecord, idRecordFromInstance);
    }
}
