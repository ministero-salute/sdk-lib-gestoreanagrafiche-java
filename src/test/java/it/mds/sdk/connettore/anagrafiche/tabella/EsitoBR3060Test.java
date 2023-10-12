package it.mds.sdk.connettore.anagrafiche.tabella;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EsitoBR3060Test {
    @Test
    void validaCostruttore() {
        EsitoBR3060 esitoBR3060 = new EsitoBR3060();
        assertTrue(esitoBR3060 instanceof EsitoBR3060);
    }

    @Test
    void validaObject() {
        EsitoBR3060 esitoBR3060 = new EsitoBR3060(1, "assistito", "ids");
        assertTrue(esitoBR3060 instanceof EsitoBR3060);
    }

    @Test
    void validaToString() {
        EsitoBR3060 esitoBR3060 = new EsitoBR3060(1, "assistito", "ids");
        assertFalse(esitoBR3060.toString().isEmpty());
    }

    @Test
    void validaGetter() {
        EsitoBR3060 esitoBR3060 = new EsitoBR3060();
        int result = 1;
        esitoBR3060.setResult(1);
        int resultFromInstance = esitoBR3060.getResult();
        assertEquals(result, resultFromInstance);
    }
}