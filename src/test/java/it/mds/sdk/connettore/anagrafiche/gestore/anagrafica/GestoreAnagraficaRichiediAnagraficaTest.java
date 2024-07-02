/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.gestore.anagrafica;

import it.mds.sdk.anagrafiche.client.exceptions.MalformedRegistryException;
import it.mds.sdk.anagrafiche.client.exceptions.RegistryNotFoundException;
import it.mds.sdk.connettore.anagrafiche.tabella.TabellaAnagrafica;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GestoreAnagraficaRichiediAnagraficaTest {
    MockedStatic<CacheSQLite> sqLiteMockedStatic;
    MockedStatic<DownloadManager> downloadManagerMockedStatic;
    CacheSQLite cacheSQLite = Mockito.mock(CacheSQLite.class);
    DownloadManager downloadManager = Mockito.mock(DownloadManager.class);
    TabellaAnagrafica tabellaAnagrafica = Mockito.mock(TabellaAnagrafica.class);
    DownloadManagerResp downloadManagerResp = Mockito.mock(DownloadManagerResp.class);

    Date date = Mockito.mock(Date.class);

    @BeforeEach
    void init(){
        sqLiteMockedStatic = mockStatic(CacheSQLite.class);
        downloadManagerMockedStatic = mockStatic(DownloadManager.class);
    }
    /**
        - isAnagraficaCache: false
        - isNuovaAnagrafica: false
     */
    @Test
    void richiediAnagraficaTest() throws MalformedRegistryException, RegistryNotFoundException {

        sqLiteMockedStatic.when(CacheSQLite::getInstance).thenReturn(cacheSQLite);
        downloadManagerMockedStatic.when(DownloadManager::getInstanceWithDefConfigAndDownloaderClient).thenReturn(downloadManager);
        Mockito.when(cacheSQLite.isAnagrafica(any())).thenReturn(false);
        //Mockito.when(cacheSQLite.getAnagrafica(any())).thenReturn(tabellaAnagrafica);
        Mockito.when(downloadManager.anagraficaDaManager("nomeTabella", true)).thenReturn(downloadManagerResp);
        Mockito.when(downloadManagerResp.getTabellaAnagrafica()).thenReturn(tabellaAnagrafica);
        Mockito.when(cacheSQLite.nuovaAnagrafica(any())).thenReturn(false);
        GestoreAnagrafica gestoreAnagrafica = new GestoreAnagrafica();
        gestoreAnagrafica.richiediAnagrafica("nomeTabella", false);
    }
    /**
     - isAnagraficaCache: false
     - isNuovaAnagrafica: true
     */
    @Test
    void richiediAnagraficaTest2() throws MalformedRegistryException, RegistryNotFoundException {

        sqLiteMockedStatic.when(CacheSQLite::getInstance).thenReturn(cacheSQLite);
        downloadManagerMockedStatic.when(DownloadManager::getInstanceWithDefConfigAndDownloaderClient).thenReturn(downloadManager);
        Mockito.when(cacheSQLite.isAnagrafica(any())).thenReturn(false);
        //Mockito.when(cacheSQLite.getAnagrafica(any())).thenReturn(tabellaAnagrafica);
        Mockito.when(downloadManager.anagraficaDaManager("nomeTabella", true)).thenReturn(downloadManagerResp);
        Mockito.when(downloadManagerResp.getTabellaAnagrafica()).thenReturn(tabellaAnagrafica);
        Mockito.when(cacheSQLite.nuovaAnagrafica(any())).thenReturn(true);
        Mockito.when(downloadManagerResp.getDataUltimoAggiornamento()).thenReturn(date);
        Mockito.when(downloadManagerResp.getDataNuovoAggiornamento()).thenReturn(date);
        Mockito.when(cacheSQLite.nuovaDataAggiornamento(any(), any(), any())).thenReturn(Boolean.TRUE);
        GestoreAnagrafica gestoreAnagrafica = new GestoreAnagrafica();
        gestoreAnagrafica.richiediAnagrafica("nomeTabella", false);
    }

    /**
     - isAnagraficaCache: true
     - isDaAggiornare: false
     - isNuovaAnagrafica: false
     */
    @Test
    void richiediAnagraficaTest3() throws MalformedRegistryException, RegistryNotFoundException {

        sqLiteMockedStatic.when(CacheSQLite::getInstance).thenReturn(cacheSQLite);
        downloadManagerMockedStatic.when(DownloadManager::getInstanceWithDefConfigAndDownloaderClient).thenReturn(downloadManager);
        Mockito.when(cacheSQLite.isAnagrafica(any())).thenReturn(true);
        Mockito.when(cacheSQLite.isAnagraficaDaAggiornare(any())).thenReturn(false);
        Mockito.when(cacheSQLite.getAnagrafica(any())).thenReturn(tabellaAnagrafica);

        GestoreAnagrafica gestoreAnagrafica = new GestoreAnagrafica();
        gestoreAnagrafica.richiediAnagrafica("nomeTabella", false);
    }

    /**
     - isAnagraficaCache: true
     - isDaAggiornare: true
     - getIsAggiornato: true
     */
    @Test
    void richiediAnagraficaTest4() throws MalformedRegistryException, RegistryNotFoundException {

        sqLiteMockedStatic.when(CacheSQLite::getInstance).thenReturn(cacheSQLite);
        downloadManagerMockedStatic.when(DownloadManager::getInstanceWithDefConfigAndDownloaderClient).thenReturn(downloadManager);
        Mockito.when(cacheSQLite.isAnagrafica(any())).thenReturn(true);
        Mockito.when(cacheSQLite.isAnagraficaDaAggiornare(any())).thenReturn(true);
        Mockito.when(downloadManager.anagraficaDaManager(any())).thenReturn(downloadManagerResp);
        Mockito.when(downloadManagerResp.getIsAggiornato()).thenReturn(Boolean.TRUE);
        Mockito.when(downloadManagerResp.getDataUltimoAggiornamento()).thenReturn(date);
        Mockito.when(downloadManagerResp.getDataNuovoAggiornamento()).thenReturn(date);
        Mockito.when(cacheSQLite.getAnagrafica(any())).thenReturn(tabellaAnagrafica);

        GestoreAnagrafica gestoreAnagrafica = new GestoreAnagrafica();
        gestoreAnagrafica.richiediAnagrafica("nomeTabella", false);
    }

    /**
     - isAnagraficaCache: true
     - isDaAggiornare: true
     - getIsAggiornato: false
     */
    @Test
    void richiediAnagraficaTest5() throws MalformedRegistryException, RegistryNotFoundException {

        sqLiteMockedStatic.when(CacheSQLite::getInstance).thenReturn(cacheSQLite);
        downloadManagerMockedStatic.when(DownloadManager::getInstanceWithDefConfigAndDownloaderClient).thenReturn(downloadManager);
        Mockito.when(cacheSQLite.isAnagrafica(any())).thenReturn(true);
        Mockito.when(cacheSQLite.isAnagraficaDaAggiornare(any())).thenReturn(true);
        Mockito.when(downloadManager.anagraficaDaManager(any())).thenReturn(downloadManagerResp);
        Mockito.when(downloadManagerResp.getIsAggiornato()).thenReturn(Boolean.FALSE);
        Mockito.when(downloadManagerResp.getTabellaAnagrafica()).thenReturn(tabellaAnagrafica);
        Mockito.when(downloadManagerResp.getDataUltimoAggiornamento()).thenReturn(date);
        Mockito.when(downloadManagerResp.getDataNuovoAggiornamento()).thenReturn(date);
        Mockito.when(cacheSQLite.getAnagrafica(any())).thenReturn(tabellaAnagrafica);

        GestoreAnagrafica gestoreAnagrafica = new GestoreAnagrafica();
        gestoreAnagrafica.richiediAnagrafica("nomeTabella", false);
    }


    @Test
    void richiediAnagraficaTestConData() throws MalformedRegistryException, RegistryNotFoundException {
        sqLiteMockedStatic.when(CacheSQLite::getInstance).thenReturn(cacheSQLite);
        downloadManagerMockedStatic.when(DownloadManager::getInstanceWithDefConfigAndDownloaderClient).thenReturn(downloadManager);
        Mockito.when(cacheSQLite.isAnagrafica(any())).thenReturn(false);
        //Mockito.when(cacheSQLite.getAnagrafica(any())).thenReturn(tabellaAnagrafica);
        Mockito.when(downloadManager.anagraficaDaManager("nomeTabella", true)).thenReturn(downloadManagerResp);
        Mockito.when(downloadManagerResp.getTabellaAnagrafica()).thenReturn(tabellaAnagrafica);
        Mockito.when(cacheSQLite.nuovaAnagrafica(any())).thenReturn(false);
        GestoreAnagrafica gestoreAnagrafica = new GestoreAnagrafica();
        gestoreAnagrafica.richiediAnagrafica("nomeTabella", false, date);
    }

    @AfterEach
    void closeStaticMocks() {
        sqLiteMockedStatic.close();
        downloadManagerMockedStatic.close();
    }

}
