package it.mds.sdk.connettore.anagrafiche.gestore.anagrafica;

import it.mds.sdk.connettore.anagrafiche.exception.AnagraficaException;
import it.mds.sdk.connettore.anagrafiche.sqlite.QueryAnagrafica;
import it.mds.sdk.connettore.anagrafiche.tabella.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Slf4j
public class CacheSQLite implements AnagraficaCache {

    private static CacheSQLite instance;

    public static CacheSQLite getInstance() {
        if (instance == null) {
            instance = new CacheSQLite();
        }
        return instance;
    }

    @Override
    public Boolean isAnagrafica(String nomeTabella) {
        log.debug("{}.isAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.tabellaAnagraficaExists(nomeTabella);
    }

    @Override
    public Boolean isAnagraficaDaAggiornare(String nomeTabella) {
        log.debug("{}.isAnagraficaDaAggiornare - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        try {
            return !anagrafica.isAnagraficaValida(nomeTabella, new Date());
        } catch (SQLException e) {
            log.error("{}.isAnagraficaDaAggiornare -  nomeCampo[{}] - Error: {}",
                    e.getClass().getName(), nomeTabella, e.getMessage(), e);
            throw new AnagraficaException("Operazione nuovaAnagrafica non riuscita");
        }
    }

    @Override
    public Boolean aggiornaAnagrafica(TabellaAnagrafica tabellaAnagrafica) {
        log.debug("{}.aggiornaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);
        //la tabella che contiene i valori di anagrafiche non ha PKey(non è stata definita sul client delle anagrafiche).
        //quindi possiamo solo cancellare i record e reinserirli dato che non possiamo fare un update
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        try {
            anagrafica.cancellaRecordTabella(tabellaAnagrafica.getNome());
            return anagrafica.aggiungiRecordTabellaAnagrafica(tabellaAnagrafica);
        } catch (SQLException e) {
            log.error("{}.aggiornaAnagrafica -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), tabellaAnagrafica, e.getMessage(), e);
            throw new AnagraficaException("Operazione aggiornaAnagrafica non riuscita");
        }
    }

    @Override
    public Boolean nuovaAnagrafica(TabellaAnagrafica tabellaAnagrafica) {
        log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        try {
            if (!anagrafica.tabellaAnagraficaExists("VALIDITA_ANAGRAFICHE")) {
                anagrafica.creaTabellaValiditaAnagrafica();
            }
            anagrafica.aggiungiRecordTabellaValiditaAnagrafica(tabellaAnagrafica.getNome(), new Date(), new Date());
            if (anagrafica.creaTabellaAnagrafica(tabellaAnagrafica.getNome())) {
                boolean recordAggiuntiOk = anagrafica.aggiungiRecordTabellaAnagrafica(tabellaAnagrafica);
                if (!recordAggiuntiOk) {
                    anagrafica.cancellaRecordFromTabellaValidita(tabellaAnagrafica.getNome());
                    anagrafica.cancellaTabella(tabellaAnagrafica.getNome());
                    return false;
                }
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            log.error("{}.nuovaAnagrafica -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), tabellaAnagrafica, e.getMessage(), e, e);
            throw new AnagraficaException("Operazione nuovaAnagrafica non riuscita", e);
        }
    }

    public Boolean dropTableBR3060(String nomeTabella) throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.dropTable(nomeTabella);
    }

    public Boolean creaTabellaBR3060(String nomeTabella) throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.creaTabellaBR3060(nomeTabella);
    }

    public List<EsitoBR3060> selectFilterBR3060(TabellaAnagrafica<EsitoBR3060> tabellaAnagrafica) throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.selectFilterBR3060(tabellaAnagrafica);
    }


    public Boolean addRecordBR3060(TabellaAnagrafica<RecordBR3060> tabellaAnagrafica) {

        log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();

        try {

            boolean recordAggiuntiOk = anagrafica.aggiungiRecordTabellaBR3060(tabellaAnagrafica);

            if (!recordAggiuntiOk) {
                log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - NESSUN RECORD AGGIUNTO ALLA TABELLA BR3060", this.getClass().getName(), tabellaAnagrafica);
                return false;
            }
            log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - FINE", this.getClass().getName(), tabellaAnagrafica);
            return true;

        } catch (SQLException e) {
            log.error("{}.nuovaAnagrafica -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), tabellaAnagrafica, e.getMessage(), e);
            throw new AnagraficaException("Operazione nuovaAnagrafica non riuscita", e);
        }
    }

    boolean creaIndice(String nomeTabella) {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.creaIndice(nomeTabella);
    }

    @Override
    public Boolean cancellaAnagrafica(String nomeTabella) {
        log.debug("{}.cancellaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        try {
            return anagrafica.cancellaTabella(nomeTabella);
        } catch (SQLException e) {
            log.error("{}.cancellaAnagrafica -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), nomeTabella, e.getMessage(), e);
            throw new AnagraficaException("Operazione cancellaAnagrafica non riuscita");
        }
    }

    @Override
    public Boolean nuovaDataAggiornamento(String nomeTabella, Date ultimaDataAggiornamento, Date nuovaDataAggiornamento) {
        log.debug("{}.nuovaDataAggiornamento - nomeTabella[{}] - ultimaDataAggiornamento[{}] - nuovaDataAggiornamento[{}]- BEGIN",
                this.getClass().getName(), nomeTabella, ultimaDataAggiornamento, nuovaDataAggiornamento);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        try {
            return anagrafica.modificaRecordTabellaValiditaAnagrafica(nomeTabella, ultimaDataAggiornamento, nuovaDataAggiornamento);
        } catch (SQLException e) {
            log.error("{}.nuovaDataAggiornamento -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), nomeTabella, e.getMessage(), e);
            throw new AnagraficaException("Operazione nuovaDataAggiornamento non riuscita");
        }
    }

    @Override
    public TabellaAnagrafica getAnagrafica(String nomeTabella) {
        log.debug("{}.getAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        try {
            return anagrafica.selectAllTabellaAnagrafica(nomeTabella);
        } catch (SQLException | ParseException e) {
            log.error("{}.getAnagrafica -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), nomeTabella, e.getMessage(), e);
            throw new AnagraficaException("Operazione GetAnagrafica non riuscita");
        }
    }

    public TabellaAnagrafica getAnagrafica(String nomeTabella, String valoreDaEstrarre) {
        log.debug("{}.getAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        try {
            return anagrafica.selectDatoFromTabella(nomeTabella, valoreDaEstrarre);
        } catch (SQLException e) {
            log.error("{}.getAnagrafica -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), nomeTabella, e.getMessage(), e);
            throw new AnagraficaException("Operazione GetAnagrafica non riuscita");
        }
    }

    // Gestione cross Record per CDM
    public Boolean dropTableCDM(String nomeTabella) throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.dropTable(nomeTabella);
    }

    public Boolean dropTableCT2(String nomeTabella) throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.dropTable(nomeTabella);
    }

    public Boolean creaTabellaCDM(String nomeTabella) throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.creaTabellaCDM(nomeTabella);
    }

    public Boolean creaTabellaCT2(String nomeTabella) throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.creaTabellaCT2(nomeTabella);
    }

    public List<EsitoCDM> selectFilterCNS(TabellaAnagrafica<EsitoCDM> tabellaAnagrafica) throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.selectFilterCNS(tabellaAnagrafica);
    }


    public Boolean addRecordCDM(TabellaAnagrafica<RecordCDM> tabellaAnagrafica) {

        log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();

        try {

            boolean recordAggiuntiOk = anagrafica.aggiungiRecordTabellaCDM(tabellaAnagrafica);

            if (!recordAggiuntiOk) {
                log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - NESSUN RECORD AGGIUNTO ALLA TABELLA CT2", this.getClass().getName(), tabellaAnagrafica);
                return false;
            }
            log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - FINE", this.getClass().getName(), tabellaAnagrafica);
            return true;

        } catch (SQLException e) {
            log.error("{}.nuovaAnagrafica -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), tabellaAnagrafica, e.getMessage(), e);
            throw new AnagraficaException("Operazione nuovaAnagrafica non riuscita", e);
        }
    }

    public Boolean addRecordCT2(TabellaAnagrafica<RecordCDMCT2> tabellaAnagrafica) {

        log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();

        try {

            boolean recordAggiuntiOk = anagrafica.aggiungiRecordTabellaCT2(tabellaAnagrafica);

            if (!recordAggiuntiOk) {
                log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - NESSUN RECORD AGGIUNTO ALLA TABELLA CT2", this.getClass().getName(), tabellaAnagrafica);
                return false;
            }
            log.debug("{}.nuovaAnagrafica - nomeTabella[{}] - FINE", this.getClass().getName(), tabellaAnagrafica);
            return true;

        } catch (SQLException e) {
            log.error("{}.nuovaAnagrafica -  nomeTabella[{}] -  Error: {}",
                    e.getClass().getName(), tabellaAnagrafica, e.getMessage(), e);
            throw new AnagraficaException("Operazione nuovaAnagrafica non riuscita", e);
        }
    }

    // Gestione cross Record per CT2

    public List<EsitoCDM> selectFilterCT2(TabellaAnagrafica<EsitoCDM> tabellaAnagrafica) throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        return anagrafica.selectFilterCT2(tabellaAnagrafica);
    }
}
