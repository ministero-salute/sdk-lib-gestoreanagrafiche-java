/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.gestore.anagrafica;

import it.mds.sdk.anagrafiche.client.DownloaderClient;
import it.mds.sdk.anagrafiche.client.entities.Datum;
import it.mds.sdk.anagrafiche.client.entities.Registry;
import it.mds.sdk.anagrafiche.client.exceptions.MalformedRegistryException;
import it.mds.sdk.anagrafiche.client.exceptions.RegistryNotFoundException;
import it.mds.sdk.connettore.anagrafiche.conf.Configurazione;
import it.mds.sdk.connettore.anagrafiche.tabella.RecordAnagrafica;
import it.mds.sdk.connettore.anagrafiche.tabella.TabellaAnagrafica;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DownloaderManagerTest {

    private DownloadManager downloadManager;
    @Mock
    private DownloaderClient downloaderClient;
    @Mock
    private Configurazione configuration;

    @BeforeEach
    void init() {
        downloadManager = new DownloadManager(configuration, downloaderClient);
    }


    @Test
    void anagraficaDaManagerForce_lanciaRetriveAnagraficaException() throws MalformedRegistryException, RegistryNotFoundException {
        String nomeTabella = "tabellaprova";
        downloaderClient.retrieveRegistry(nomeTabella);

        Assertions.assertThrows(RegistryNotFoundException.class, () -> downloadManager.anagraficaDaManager(nomeTabella));
    }

    @Test
    void anagraficaDaManagerForce_tabellanull_lanciaRetriveAnagraficaException() throws MalformedRegistryException, RegistryNotFoundException {
        String nomeTabella = "tabellaprova";
        when(downloaderClient.retrieveRegistry(nomeTabella, true)).thenReturn(null);

        Assertions.assertThrows(RegistryNotFoundException.class, () -> downloadManager.anagraficaDaManager(nomeTabella,
                true));
    }

    @Test
    void anagraficaDaManagerNoForce_isNewFalse_responseSenzaTabella() throws MalformedRegistryException, RegistryNotFoundException {
        String nomeTabella = "tabellaProva1";
        Registry reg = new Registry();
        reg.setNew(false);
        reg.setData(null);
        reg.setName(nomeTabella);
        reg.setLastUpdate(new Date(10000L));
        reg.setNextUpdate(new Date(10000L));
        TabellaAnagrafica tabellaAnagrafica = new TabellaAnagrafica();
        tabellaAnagrafica.setNome(nomeTabella);
        DownloadManagerResp respExp = new DownloadManagerResp(true, tabellaAnagrafica, new Date(10000L),
                new Date(10000L));
        when(downloaderClient.retrieveRegistry(nomeTabella, false)).thenReturn(reg);

        DownloadManagerResp respActual = downloadManager.anagraficaDaManager(nomeTabella, false);

        Assertions.assertEquals(respExp, respActual, "La response non è la stessa");

    }

    @Test
    void anagraficaDaManagerForce_isNewTrue_responseCompleta() throws MalformedRegistryException, RegistryNotFoundException {
        String nomeTabella = "tabellaProva2";
        Registry reg = new Registry();
        List<Datum> datumList = new ArrayList<>();
        Datum datum = new Datum(new Date(10000L), new Date(10000L), "valore");
        datumList.add(datum);
        Stream<Datum> datumStream = datumList.stream();
        reg.setNew(true);
        reg.setData(datumStream);
        reg.setName(nomeTabella);
        reg.setLastUpdate(new Date(10000L));
        reg.setNextUpdate(new Date(10000L));
        TabellaAnagrafica tabellaAnagrafica = new TabellaAnagrafica();
        tabellaAnagrafica.setNome(nomeTabella);
        Date data = new Date(10000L);
        LocalDateTime ldt = LocalDateTime.ofInstant(data.toInstant(), ZoneId.systemDefault());
        RecordAnagrafica recordAnagrafica = new RecordAnagrafica(ldt, ldt, "valore");
        List<RecordAnagrafica> recordAnagraficaList = new ArrayList<>();
        recordAnagraficaList.add(recordAnagrafica);
        tabellaAnagrafica.setRecordsAnagrafica(recordAnagraficaList);
        DownloadManagerResp respExp = new DownloadManagerResp(false, tabellaAnagrafica, new Date(10000L),
                new Date(10000L));
        when(downloaderClient.retrieveRegistry(nomeTabella, false)).thenReturn(reg);

        DownloadManagerResp respActual = downloadManager.anagraficaDaManager(nomeTabella, false);

        Assertions.assertEquals(respExp, respActual, "La response non è la stessa");
    }
}
