/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.tabella;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EsitoCDMTest {
    @Test
    void validaCostruttore() {
        EsitoCDM esitoCDM = new EsitoCDM();
        assertTrue(esitoCDM instanceof EsitoCDM);
    }

    @Test
    void validaObject() {
        EsitoCDM esitoCDM = new EsitoCDM("assistito", "ids");
        assertTrue(esitoCDM instanceof EsitoCDM);
    }

    @Test
    void validaToString() {
        EsitoCDM esitoCDM = new EsitoCDM("assistito", "ids");
        assertFalse(esitoCDM.toString().isEmpty());
    }

    @Test
    void validaGetter() {
        EsitoCDM esitoCDM = new EsitoCDM();
        String asl = ("1234");
        esitoCDM.setAsl("1234");
        String resultFromInstance = esitoCDM.getAsl();
        assertEquals(asl, resultFromInstance);
    }
}