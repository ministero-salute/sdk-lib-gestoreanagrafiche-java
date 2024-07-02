/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.tabella;

import it.mds.sdk.connettore.anagrafiche.exception.AnagraficaException;
import it.mds.sdk.connettore.anagrafiche.tabella.TabellaAnagrafica;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TabellaAnagraficaTest {
    @Test
    void TabellaAnagraficaCostruttore(){

        List<String> list = new ArrayList<>();
        TabellaAnagrafica t = new TabellaAnagrafica("", list);
        assertTrue(t instanceof TabellaAnagrafica);
    }
}
