/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.gestore.anagrafica;

import it.mds.sdk.anagrafiche.client.DownloaderClientImplementation;
import it.mds.sdk.connettore.anagrafiche.conf.Configurazione;
import it.mds.sdk.connettore.anagrafiche.tabella.RecordAnagrafica;
import it.mds.sdk.connettore.anagrafiche.tabella.TabellaAnagrafica;
import it.mds.sdk.anagrafiche.client.DownloaderClient;
import it.mds.sdk.anagrafiche.client.entities.Datum;
import it.mds.sdk.anagrafiche.client.entities.Registry;
import it.mds.sdk.anagrafiche.client.exceptions.MalformedRegistryException;
import it.mds.sdk.anagrafiche.client.exceptions.RegistryNotFoundException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
public class DownloadManager {

    private Configurazione conf;
    private static DownloadManager instance;
    private DownloaderClient downloaderClient;

    /*public DownloadManagerResp anagraficaDaManager(String tabella, Date data) {
        throw new UnsupportedOperationException("Non implementato");
    }*/

    /*public static DownloadManager getInstanceWithDefConfig(DownloaderClient downloaderClient) {
        if (instance == null) {
            instance = new DownloadManager(new Configurazione(), downloaderClient);
        }
        return instance;
    }*/
    public static DownloadManager getInstanceWithDefConfigAndDownloaderClient() {
        if (instance == null) {
            Configurazione conf = new Configurazione();
            instance = new DownloadManager(conf, getDownloaderClientWithConf(conf));
        }
        instance.setDownloaderClient(instance.getDownloaderClient());
        return instance;
    }
    /*public static DownloadManager getInstanceWithNoDefault(Configurazione conf, DownloaderClient downloaderClient) {
        if (instance == null) {
            instance = new DownloadManager(conf, downloaderClient);
        }
        return instance;
    }*/
    protected DownloadManager(Configurazione conf, DownloaderClient downloaderClient) {
        this.conf = conf;
        this.downloaderClient = downloaderClient;
    }

    public DownloadManagerResp anagraficaDaManager(String tabella) throws MalformedRegistryException, RegistryNotFoundException {
        return anagraficaDaManager(tabella, false);
    }

    public DownloadManagerResp anagraficaDaManager(String tabella, boolean force) throws MalformedRegistryException,
            RegistryNotFoundException {
        List<RecordAnagrafica> listaRecord = new ArrayList<>();
        Registry tabellaAnag = downloaderClient.retrieveRegistry(tabella, force);
        if(tabellaAnag == null) {
            log.error("Tabella anagrafica {} non ricevuta dal client " , tabella);
            throw new RegistryNotFoundException();
        }
        var isNew = tabellaAnag.isNew();
        if (isNew) {
            log.debug("La tabella {} ha isNew {} e deve essere aggiornata", tabella, isNew);
            List<Datum> data = tabellaAnag.getData().collect(Collectors.toList());
            for (Datum dato : data) {
                Date validoDa = dato.getValidFrom();
                Date validoA = dato.getValidTo();
                LocalDateTime validoDaTime = validoDa != null ? LocalDateTime.ofInstant(validoDa.toInstant(), ZoneId.systemDefault()) : null;
                LocalDateTime validoATime = validoA != null ? LocalDateTime.ofInstant(validoA.toInstant(), ZoneId.systemDefault()) : null;
                listaRecord.add(new RecordAnagrafica(validoDaTime, validoATime, dato.getValue()));
            }
        } else {
            log.debug("La tabella {} ha isNew {} e non deve essere aggiornata, verranno cambiati solo lastUpdate e " +
                    "nextUpdate", tabella, isNew);
        }
        TabellaAnagrafica anagrafica = new TabellaAnagrafica();
        anagrafica.setNome(tabella);
        anagrafica.setRecordsAnagrafica(listaRecord);

        return DownloadManagerResp.builder()
                .withIsAggiornato(!tabellaAnag.isNew())
                .withDataUltimoAggiornamento(tabellaAnag.getLastUpdate())
                .withDataNuovoAggiornamento(tabellaAnag.getNextUpdate())
                .withTabellaAnagrafica(anagrafica)
                .build();
    }

    protected static DownloaderClient getDownloaderClientWithConf(Configurazione conf) {
        DownloaderClient downloaderClient = DownloaderClientImplementation.instance();
        //downloaderClient.setMockUrl("/sdk/mock/");
        downloaderClient.setEndpointSoap(conf.getClientHost().getHost());
        downloaderClient.setWsseUsername(conf.getClientUsername().getUser());
        downloaderClient.setWssePassword(conf.getClientPassword().getPass());
        return downloaderClient;
    }
}
