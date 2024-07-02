/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.gestore.anagrafica;

import it.mds.sdk.connettore.anagrafiche.tabella.TabellaAnagrafica;
import it.mds.sdk.anagrafiche.client.exceptions.MalformedRegistryException;
import it.mds.sdk.anagrafiche.client.exceptions.RegistryNotFoundException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class GestoreAnagrafica {

	CacheSQLite cacheSQLite;
	DownloadManager downloadManager;

	/**
	 * Restituisce l'intera anagrafica con all'interno tutti i record trovati.
     * Consulta la cache prima di effettuare l'eventuale aggiornamento con le tabelle del ministero.
	 * @param nomeTabella stringa che rappresenta il nome della tabella per cui si richiede anagrafica
	 * @param forzatura parametro booleano, se true scarica l’anagrafica indipendentemente dal fatto che il dato in cache sia valido o meno   .
	 * @return oggetto di tipo TabellaAnagrafica   {@link TabellaAnagrafica}
	 */
    //@Deprecated(forRemoval = true)
	public TabellaAnagrafica richiediAnagrafica(String nomeTabella, boolean forzatura) throws MalformedRegistryException, RegistryNotFoundException {
        cacheSQLite = CacheSQLite.getInstance();
        log.debug("non trovata nella cache memoria la tabella {}", nomeTabella);
        downloadManager = DownloadManager.getInstanceWithDefConfigAndDownloaderClient();
		TabellaAnagrafica tabellaAnagrafica;
		boolean isAngraficaCache = cacheSQLite.isAnagrafica(nomeTabella);
		if(isAngraficaCache){
			log.info("Anagrafica in cache {} " ,nomeTabella);
			boolean isDaAggiornare =cacheSQLite.isAnagraficaDaAggiornare(nomeTabella);
			if(isDaAggiornare){
				log.info("Anagrafica {} in cache da aggiornare ",nomeTabella);
				DownloadManagerResp response =downloadManager.anagraficaDaManager(nomeTabella);
				if(Boolean.TRUE.equals(response.getIsAggiornato())){
					log.info("Client risponde che è aggiornata. LastUpdate {} , nextUpdate {} ",response.getDataUltimoAggiornamento(),response.getDataNuovoAggiornamento());
					if(response.getDataUltimoAggiornamento()!=null && response.getDataNuovoAggiornamento()!=null) {
						cacheSQLite.nuovaDataAggiornamento(nomeTabella, response.getDataUltimoAggiornamento(), response.getDataNuovoAggiornamento());
					}
					tabellaAnagrafica = cacheSQLite.getAnagrafica(nomeTabella);
				}else{
					log.info("Aggiorno l'anagrafica {}. LastUpdate {} , nextUpdate {}",nomeTabella,response.getDataUltimoAggiornamento(),response.getDataNuovoAggiornamento());
					tabellaAnagrafica = response.getTabellaAnagrafica();
					if(response.getDataUltimoAggiornamento()!=null && response.getDataNuovoAggiornamento()!=null) {
						cacheSQLite.nuovaDataAggiornamento(nomeTabella, response.getDataUltimoAggiornamento(), response.getDataNuovoAggiornamento());
					}
					cacheSQLite.aggiornaAnagrafica(tabellaAnagrafica);
				}
			}else{
				log.info("Anagrafica {} aggiornata quindi rispondo la mia in cache",nomeTabella);
				tabellaAnagrafica = cacheSQLite.getAnagrafica(nomeTabella);
			}
		}else{
			log.info("non ho anagrafica {} in cache",nomeTabella);
			DownloadManagerResp response =downloadManager.anagraficaDaManager(nomeTabella,true);
			tabellaAnagrafica = response.getTabellaAnagrafica();
			boolean anagrOk =cacheSQLite.nuovaAnagrafica(tabellaAnagrafica);
			if(anagrOk) {
				log.info("LastUpdate {} , nextUpdate {} ",response.getDataUltimoAggiornamento(),response.getDataNuovoAggiornamento());
				if(response.getDataUltimoAggiornamento()!=null && response.getDataNuovoAggiornamento()!=null)
                {
					cacheSQLite.nuovaDataAggiornamento(
                            nomeTabella,
                            response.getDataUltimoAggiornamento(),
                            response.getDataNuovoAggiornamento()
                    );
				}
			}

		}
		return tabellaAnagrafica;
	}
    public TabellaAnagrafica richiediAnagrafica(String nomeTabella, String valoreDaEstrarre, boolean forzatura) throws MalformedRegistryException, RegistryNotFoundException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        cacheSQLite = CacheSQLite.getInstance();
        downloadManager = DownloadManager.getInstanceWithDefConfigAndDownloaderClient();
        stopWatch1.stop();
        Stats.addTempo("init", stopWatch1.getTotalTimeNanos());
        TabellaAnagrafica tabellaAnagrafica;
        boolean isAngraficaCache = cacheSQLite.isAnagrafica(nomeTabella);
        if(isAngraficaCache){
            log.debug("Anagrafica in cache {}, viene verificato aggiornamento" ,nomeTabella);
            boolean isDaAggiornare =cacheSQLite.isAnagraficaDaAggiornare(nomeTabella);
            if(isDaAggiornare) {
                log.debug("Anagrafica {} in cache da aggiornare con quella del ministero",nomeTabella);
                DownloadManagerResp response = null;
                int ore = Integer.parseInt(downloadManager.getConf().getResilienza().getOre());
                try {
                    response = downloadManager.anagraficaDaManager(nomeTabella);
                } catch (MalformedRegistryException | RegistryNotFoundException e) {
                    log.warn("Impossibile ottenere l'anagrafica {} dal client, viene utilizzata quella in cache per " +
                            "altre {} ore", nomeTabella, ore, e);
                    cacheSQLite.nuovaDataAggiornamento(nomeTabella, new Date(), addHoursToNow(ore));
                    tabellaAnagrafica = cacheSQLite.getAnagrafica(nomeTabella, valoreDaEstrarre);
                    stopWatch.stop();
                    Stats.addTempo(nomeTabella, stopWatch.getTotalTimeNanos());
                    return tabellaAnagrafica;
                }
                if(Boolean.TRUE.equals(response.getIsAggiornato())){
                    log.info("Client risponde che anagrafica {} è aggiornata. LastUpdate {} , nextUpdate {} ",
                            nomeTabella, response.getDataUltimoAggiornamento(),response.getDataNuovoAggiornamento());
                    if(response.getDataUltimoAggiornamento()!=null && response.getDataNuovoAggiornamento()!=null) {
                        cacheSQLite.nuovaDataAggiornamento(nomeTabella, response.getDataUltimoAggiornamento(), response.getDataNuovoAggiornamento());
                    }
                    tabellaAnagrafica = cacheSQLite.getAnagrafica(nomeTabella, valoreDaEstrarre);
                }else{
                    log.info("Aggiorno l'anagrafica {}. LastUpdate {} , nextUpdate {}",nomeTabella,response.getDataUltimoAggiornamento(),response.getDataNuovoAggiornamento());
                    tabellaAnagrafica = response.getTabellaAnagrafica();
                    if(response.getDataUltimoAggiornamento()!=null && response.getDataNuovoAggiornamento()!=null) {
                        cacheSQLite.nuovaDataAggiornamento(nomeTabella, response.getDataUltimoAggiornamento(), response.getDataNuovoAggiornamento());
                    }
                    cacheSQLite.aggiornaAnagrafica(tabellaAnagrafica);
                }
            }else{
                log.debug("Anagrafica {} aggiornata quindi rispondo la mia in cache",nomeTabella);
                tabellaAnagrafica = cacheSQLite.getAnagrafica(nomeTabella, valoreDaEstrarre);
            }
        }else{
            log.info("non ho anagrafica {} in cache",nomeTabella);
            DownloadManagerResp response =downloadManager.anagraficaDaManager(nomeTabella,true);
            tabellaAnagrafica = response.getTabellaAnagrafica();
            boolean anagrOk =cacheSQLite.nuovaAnagrafica(tabellaAnagrafica);
            boolean indiceOk = cacheSQLite.creaIndice(nomeTabella);
            if(anagrOk && indiceOk) {
                log.info("LastUpdate {} , nextUpdate {} per tabella {}",response.getDataUltimoAggiornamento(),
                        response.getDataNuovoAggiornamento(), nomeTabella);
                if(response.getDataUltimoAggiornamento()!=null && response.getDataNuovoAggiornamento()!=null) {
                    cacheSQLite.nuovaDataAggiornamento(nomeTabella, response.getDataUltimoAggiornamento(), response.getDataNuovoAggiornamento());
                }
            }

        }
        stopWatch.stop();
        Stats.addTempo(nomeTabella, stopWatch.getTotalTimeNanos());
        return tabellaAnagrafica;
    }

	public TabellaAnagrafica richiediAnagrafica(String nomeTabella, boolean forzatura, Date dataRichiesta) throws MalformedRegistryException, RegistryNotFoundException {
		return richiediAnagrafica(nomeTabella,forzatura);
	}

    private Date addHoursToNow(int ore) {
        Date dataAttuale = new Date();
        return Date.from(dataAttuale.toInstant().plus(Duration.ofHours(ore)));
    }

    private static class Stats {

        private static Map<String, Long> tempoRegole = new HashMap<>();
        private static int esecuzioni = 0;

        static void addTempo(String regola, long tempoNano) {
            Long tempo = tempoRegole.get(regola);
            esecuzioni++;
            if (tempo == null) {
                tempoRegole.put(regola, tempoNano);
            } else {
                tempoRegole.put(regola, tempo + tempoNano);
            }
            if (esecuzioni % 1000000 == 0) {
                log.info("esecuzione {}, tempistiche \n{}", esecuzioni, scriviTempi());
            }
        }

        static String scriviTempi() {
            StringBuilder sb = new StringBuilder();
            for (String clazz : tempoRegole.keySet()) {
                long tempo = tempoRegole.get(clazz);
                sb.append("Tabella : ").append(clazz).append(" tempo ns: ").append(tempo).append(" tempo sec: ").append(tempo / 1000000000).append("\n");
            }
            return sb.toString();
        }
    }

}




