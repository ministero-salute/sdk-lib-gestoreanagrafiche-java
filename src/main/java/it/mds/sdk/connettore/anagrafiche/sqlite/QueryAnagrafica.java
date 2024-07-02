/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.sqlite;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.mds.sdk.connettore.anagrafiche.tabella.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class QueryAnagrafica {

    private static final String ERRORE_CREAZIONE = "ERRORE CREAZIONE";
    private static final String ERRORE_ELIMINAZIONE = "ERRORE ELIMINAZIONE";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Cache<String, TabellaAnagrafica> cacheTabelle;
    private Cache<String, Boolean> cacheEsistenza;
    private Cache<String, Boolean> cacheTabellaScaduta;

    private static QueryAnagrafica instance;

    public static QueryAnagrafica getInstanceWithCache() {
        if (instance == null) {
            instance = new QueryAnagrafica();
        }
        return instance;
    }

    private QueryAnagrafica() {
        if (cacheTabelle == null) {
            cacheTabelle = Caffeine.newBuilder()
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .maximumSize(1000000)
                    .recordStats()
                    .build();
        }
        if (cacheEsistenza == null) {
            cacheEsistenza = Caffeine.newBuilder()
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .maximumSize(1000000)
                    .recordStats()
                    .build();
        }
        if (cacheTabellaScaduta == null) {
            cacheTabellaScaduta = Caffeine.newBuilder()
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .maximumSize(1000000)
                    .recordStats()
                    .build();
        }
    }

    /**
     * Cerca il {dato} nella tabella con nome {nomeTabella}, torna l'oggetto {TabellaAnagrafica} che contiene al suo
     * interno i dati trovati.
     *
     * @param nomeTabella nome della tabella nella quale effettuare la ricerca
     * @param dato        dato da cercare nella tabella
     * @return TabellaAnagrafica contenente i dati della query
     * @throws SQLException
     */
    public TabellaAnagrafica<RecordAnagrafica> selectDatoFromTabella(String nomeTabella, String dato) throws SQLException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        TabellaAnagrafica tabCache = cacheTabelle.getIfPresent(nomeTabella + dato);
        if (tabCache != null) {
            log.trace("cache tabelle stats {}", cacheTabelle.stats());
            stopWatch.stop();
            Stats.addTempo(nomeTabella, stopWatch.getTotalTimeNanos());
            return tabCache;
        }
        log.trace("cache tabelle stats {}", cacheTabelle.stats());
        String sql = "SELECT strftime('%Y-%m-%d %H:%M:%S',VALIDODA) AS VALIDODA,strftime('%Y-%m-%d %H:%M:%S',VALIDOA)" +
                " AS VALIDOA,DATO FROM " + nomeTabella + " WHERE DATO = ?";
        TabellaAnagrafica<RecordAnagrafica> tabellaAnagrafica = new TabellaAnagrafica();
        tabellaAnagrafica.setNome(nomeTabella);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        try (Connection conn = DbManager.getInstance().getConnection(); PreparedStatement stmt =
                conn.prepareStatement(sql)) {
            stmt.setString(1, dato);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                tabellaAnagrafica
                        .getRecordsAnagrafica()
                        .add(
                                new RecordAnagrafica(
                                        rs.getString("VALIDODA") != null ? LocalDateTime.parse(rs.getString("VALIDODA"), formatter) : null,
                                        rs.getString("VALIDOA") != null ? LocalDateTime.parse(rs.getString("VALIDOA"), formatter) : null,
                                        rs.getString("DATO")
                                )
                        );
            }
        }
        log.trace("Per la tabella anagrafica {} con dato {} trovati record: {}", nomeTabella, dato,
                tabellaAnagrafica.getRecordsAnagrafica());
        cacheTabelle.put(nomeTabella + dato, tabellaAnagrafica);
        stopWatch.stop();
        Stats.addTempo(nomeTabella, stopWatch.getTotalTimeNanos());
        return tabellaAnagrafica;
    }

    public boolean creaTabellaAnagrafica(String nomeTabella) throws SQLException {
        log.debug("{}.creaTabellaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        PreparedStatement stmt = null;
        try (Connection c = DbManager.getInstance().getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + nomeTabella +
                    " (VALIDODA, " +
                    " VALIDOA, " +
                    " DATO TEXT)";
            log.debug(sql);
            stmt = c.prepareStatement(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            return result == 0;
        } catch (Exception e) {
            log.error("{}.creaTabellaAnagrafica - nomeTabella[{}]", e.getClass().getName(), nomeTabella, e);
            return false;
        } finally {
            if (!Objects.isNull(stmt)) {
                stmt.close();
            }
        }
    }

    /**
     * Verifica che la tabella di anagrafica "nomeTabella" esista su DB SQLite
     *
     * @param nomeTabella
     * @return true se la tabella esiste
     */
    public boolean tabellaAnagraficaExists(String nomeTabella) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Boolean esistenzaDaCache = cacheEsistenza.getIfPresent(nomeTabella);
        if (esistenzaDaCache != null && esistenzaDaCache) {
            log.trace("cache esistenza stats {}", cacheEsistenza.stats());
            return true;
        }
        log.debug("{}.tabellaAnagraficaExists - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        try (Connection conn = DbManager.getInstance().getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, nomeTabella, null);
            rs.next();
            cacheEsistenza.put(nomeTabella, rs.getRow() > 0);
            stopWatch.stop();
            Stats.addTempo("esistenza", stopWatch.getTotalTimeNanos());
            return rs.getRow() > 0;
        } catch (SQLException e) {
            log.error("{}.tabellaAnagraficaExists - nomeTabella[{}]", e.getClass().getName(), nomeTabella, e);
        }
        return false;
    }

    /**
     * Cancella tabella di anagrafica "nomeTabella" su DB SQLite
     *
     * @param nomeTabella
     * @return true se la tabella è stata cancellata
     * @throws SQLException
     */
    public boolean cancellaTabella(String nomeTabella) throws SQLException {
        log.debug("{}.cancellaTabella - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        PreparedStatement stmt = null;
        try (Connection c = DbManager.getInstance().getConnection()) {
            String sql = "DROP TABLE IF  EXISTS " + nomeTabella;
            stmt = c.prepareStatement(sql);
            log.debug(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            return result == 0;
        } catch (Exception e) {
            log.error("{}.cancellaTabella - nomeTabella[{}]", e.getClass().getName(), nomeTabella, e);
            return false;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public boolean creaIndice(String nomeTabella) {
        String indiceSQL = "CREATE INDEX " + nomeTabella + "_DATO_IDX ON " + nomeTabella + " (DATO)";
        log.debug("Inizio creazione indice da script: {}", indiceSQL);
        try (Connection c = DbManager.getInstance().getConnection(); PreparedStatement stmt = c.prepareStatement(indiceSQL)) {
            int result = stmt.executeUpdate();
            return result == 0;
        } catch (Exception e) {
            log.error("Errore creazione indice su tabella {}", nomeTabella, e);
            return false;
        }
    }


    /**
     * Inserisce records di Anagrafica nella "tabellaAnagrafica"
     *
     * @param tabellaAnagrafica
     * @return il numero di record inseriti
     * @throws SQLException
     */
    public boolean aggiungiRecordTabellaAnagrafica(TabellaAnagrafica<RecordAnagrafica> tabellaAnagrafica) throws SQLException {
        log.debug("{}.aggiungiRecordTabellaAnagrafica - tabellaAnagrafica[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);
        Connection conn = DbManager.getInstance().getConnection();
        String nomeTabella = tabellaAnagrafica.getNome();
        List<RecordAnagrafica> listaRecord = tabellaAnagrafica.getRecordsAnagrafica();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        int recordInseriti = 0;
        String sql = "INSERT INTO " + nomeTabella + " VALUES(strftime('%Y-%m-%d %H:%M:%S',?),strftime('%Y-%m-%d %H:%M:%S',?),?)";
        log.debug(sql);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (RecordAnagrafica recordAnag : listaRecord) {
                pstmt.setString(1, (recordAnag.getValidoDa() != null ? (recordAnag.getValidoDa()).format(formatter) : null));
                pstmt.setString(2, (recordAnag.getValidoA() != null ? (recordAnag.getValidoA()).format(formatter) : null));
                pstmt.setString(3, recordAnag.getDato());
                pstmt.addBatch();
            }
            recordInseriti = pstmt.executeBatch().length;
            conn.commit();
        } catch (Exception e) {
            log.error("{}.aggiungiRecordTabellaAnagrafica - tabellaAnagrafica[{}]", e.getClass().getName(), tabellaAnagrafica, e);
            conn.close();
            return false;
        }

        conn.close();
        return recordInseriti == tabellaAnagrafica.getRecordsAnagrafica().size();
    }

    /**
     * Inserisce records di Anagrafica nella tabella dinamica per la BR3060
     *
     * @param tabellaAnagrafica
     * @return il numero di record inseriti
     * @throws SQLException
     */
    public boolean aggiungiRecordTabellaBR3060(TabellaAnagrafica<RecordBR3060> tabellaAnagrafica) throws SQLException {

        log.debug("{}.aggiungiRecordTabellaBR3060 - tabellaAnagrafica[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);

        Connection conn = DbManager.getInstance().getConnection();
        String nomeTabella = tabellaAnagrafica.getNome();
        List<RecordBR3060> listaRecord = tabellaAnagrafica.getRecordsAnagrafica();

        int recordInseriti = 0;
        String sql = "INSERT INTO " + nomeTabella + " VALUES(?, ?, ?, ?, ?, ? ,?, ?)";

        log.debug(sql);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (RecordBR3060 recordAnag : listaRecord) {

                pstmt.setInt(1, recordAnag.getIdRecord());
                pstmt.setString(2, recordAnag.getTipoTrasmissione());
                pstmt.setString(3, recordAnag.getCodRegione());
                pstmt.setString(4, recordAnag.getIdAssistito());
                pstmt.setString(5, recordAnag.getCodTipoFormulazione());
                pstmt.setString(6, recordAnag.getDataSomministrazione());
                pstmt.setString(7, recordAnag.getCodiceAntigene());
                pstmt.setInt(8, recordAnag.getDose() == null ? 0 : recordAnag.getDose());
                pstmt.addBatch();
            }
            recordInseriti = pstmt.executeBatch().length;
            conn.commit();

        } catch (Exception e) {
            log.error("{}.aggiungiRecordTabellaBR3060 - tabellaAnagrafica[{}]", e.getClass().getName(), tabellaAnagrafica, e);
            conn.close();
            return false;
        }
        conn.close();
        return recordInseriti == tabellaAnagrafica.getRecordsAnagrafica().size();
    }


    /**
     * Restituisce oggetto tabellaAnagrafica contenete tutti i record presenti in una tabella denominata "nomeTabella"
     *
     * @param nomeTabella Oggetto contenente il nome tabella e la lista di RecordAnagrafica
     * @return TabellaAnagrafica
     */
    public TabellaAnagrafica<RecordAnagrafica> selectAllTabellaAnagrafica(String nomeTabella) throws SQLException, ParseException {
        log.debug("{}.selectAllTabellaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella);
        String sql = "SELECT strftime('%Y-%m-%d %H:%M:%S',VALIDODA) AS VALIDODA,strftime('%Y-%m-%d %H:%M:%S',VALIDOA) AS VALIDOA,DATO FROM " + nomeTabella;
        TabellaAnagrafica<RecordAnagrafica> tabellaAnagrafica = new TabellaAnagrafica<>();
        tabellaAnagrafica.setNome(nomeTabella);
        PreparedStatement stmt = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        try (Connection conn = DbManager.getInstance().getConnection()) {
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                tabellaAnagrafica
                        .getRecordsAnagrafica()
                        .add(
                                new RecordAnagrafica(
                                        rs.getString("VALIDODA") != null ? LocalDateTime.parse(rs.getString("VALIDODA"), formatter) : null,
                                        rs.getString("VALIDOA") != null ? LocalDateTime.parse(rs.getString("VALIDOA"), formatter) : null,
                                        rs.getString("DATO")
                                )
                        );
            }
        } catch (SQLException e) {
            log.error("{}.selectAllTabellaAnagrafica - nomeTabella[{}] - BEGIN", this.getClass().getName(), nomeTabella, e);
        } finally {
            if (!Objects.isNull(stmt)) {
                stmt.close();
            }
        }
        return tabellaAnagrafica;
    }

    /**
     * @return la lista degli esiti scartati per la BR3060
     * @throws SQLException
     */
    public List<EsitoBR3060> selectFilterBR3060(TabellaAnagrafica<EsitoBR3060> tabellaAnagrafica) throws SQLException {

        log.debug("{}.selectFilterBR3060 - nomeTabella[{}] - BEGIN", this.getClass().getName(), "BR3060");

        String sql = "SELECT " +
                "(case when BR.TF = BR.IA then TRUE else FALSE end) AS RESULT," +
                "BR.ID_ASSISTITO, " +
                "BR.IDS " +
                " FROM( " +
                "SELECT ID_RECORD," +
                "COUNT(*) AS IA," +
                "COD_REGIONE," +
                "ID_ASSISTITO ," +
                "DATA_SOMMINISTRAZIONE," +
                "DOSE," +
                "TIPO_TRASMISSIONE," +
                "CAST(COD_TIPO_FORMULAZIONE AS INTEGER) as TF, " +
                "GROUP_CONCAT(ID_RECORD, '|') as ids " +
                "FROM " + tabellaAnagrafica.getNome() +
                " WHERE CAST(strftime('%s', DATA_SOMMINISTRAZIONE)  AS  integer) > CAST(strftime('%s', '2019-07-01')  AS  integer) " +
                //" WHERE CAST(DATA_SOMMINISTRAZIONE AS DATE) > CAST('2019-07-01' AS DATE) " +
                "GROUP BY COD_REGIONE , ID_ASSISTITO , DATA_SOMMINISTRAZIONE , DOSE , TIPO_TRASMISSIONE) " +
                "AS BR WHERE RESULT = 0 ORDER BY ID_RECORD ";

        PreparedStatement stmt = null;

        try (Connection conn = DbManager.getInstance().getConnection()) {
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                tabellaAnagrafica
                        .getRecordsAnagrafica()
                        .add(new EsitoBR3060(rs.getInt("RESULT"), rs.getString("ID_ASSISTITO"), rs.getString("IDS")));
            }
        } catch (SQLException e) {
            log.error("{}.selectFilterBR3060 - nomeTabella[{}] - BEGIN", this.getClass().getName(), "BR3060", e);
            throw new SQLException();
        } finally {
            if (!Objects.isNull(stmt)) {
                stmt.close();
            }
        }
        return tabellaAnagrafica.getRecordsAnagrafica();
    }

    /**
     *
     * @param tabellaAnagrafica
     * @return true se sono stati modificati i  record passati in input
     * @throws SQLException
     */
//    public boolean modificaRecordTabellaAnagrafica(TabellaAnagrafica tabellaAnagrafica) throws SQLException {
//        Connection conn = DbManager.getInstance().getConnection();
//        String nomeTabella = tabellaAnagrafica.getNome();
//        List<RecordAnagrafica> listaRecord = tabellaAnagrafica.getRecordsAnagrafica();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
//        int recordInseriti = 0;
//        String sql = "UPDATE " +nomeTabella+" SET VALIDODA = strftime('%Y-%m-%d %H:%M:%S',?) , VALIDOA =strftime('%Y-%m-%d %H:%M:%S',?)  WHERE DATO=?";
//        for(RecordAnagrafica recordAnag : listaRecord) {
//            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//                pstmt.setString(1, (recordAnag.getValidoDa()!=null ? (recordAnag.getValidoDa()).format(formatter) : null));
//                pstmt.setString(2, (recordAnag.getValidoA()!=null ? (recordAnag.getValidoA()).format(formatter) : null));
//                pstmt.setString(3, recordAnag.getDato());
//                int result = pstmt.executeUpdate();
//                recordInseriti+=result;
//            } catch (Exception e) {
//                log.error("ERRORE UPDATE TABELLA ANAGRAFICA - {}" + e.getClass().getName() + ": " + e.getMessage());
//                conn.close();
//                return false;
//            }
//        }
//        conn.close();
//        return recordInseriti==tabellaAnagrafica.getRecordsAnagrafica().size();
//    }

    /**
     * Cancella i record dentro alla tabella  "nomeTabella" su DB SQLite
     *
     * @param nomeTabella
     * @return true se i record sono stati cancellati è stata cancellata
     * @throws SQLException
     */
    public boolean cancellaRecordTabella(String nomeTabella) throws SQLException {
        PreparedStatement stmt = null;
        try (Connection c = DbManager.getInstance().getConnection()) {
            String sql = "DELETE FROM " + nomeTabella;
            stmt = c.prepareStatement(sql);
            log.debug(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            //TODO pensare a come gestire il ritorno
            return result > 0;
        } catch (Exception e) {
            log.error("ERRORE CANCELLAZIONE RECORD" + e.getClass().getName() + ": " + e.getMessage());
            return false;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }


    /**
     * Crea la tabella di appoggio che serve per verificare se la anagrafica è scaduta o meno
     *
     * @return boolean true se la tabella è stata creata
     * @throws SQLException
     */
    public boolean creaTabellaValiditaAnagrafica() throws SQLException {
        PreparedStatement stmt = null;
        try (Connection c = DbManager.getInstance().getConnection()) {
            log.info("Opened database successfully");
            String sql = "CREATE TABLE IF NOT EXISTS VALIDITA_ANAGRAFICHE" +
                    " (NOMETABELLA TEXT, " +
                    " LASTUPDATE DATETIME, " +
                    " NEXTUPDATE DATETIME)";
            log.debug(sql);
            stmt = c.prepareStatement(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            return result == 0;
        } catch (Exception e) {
            log.error(ERRORE_CREAZIONE + e.getClass().getName() + ": " + e.getMessage());
            return false;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Droppa la tabella di appoggio che serve per validare la BR3060
     *
     * @return boolean true se la tabella è stata droppata
     * @throws SQLException
     */
    public boolean dropTable(String nomeTabella) throws SQLException {
        PreparedStatement stmt = null;
        log.info("Inizio drop tabella {}", nomeTabella);
        try (Connection c = DbManager.getInstance().getConnection()) {
            log.info("Opened database successfully");
            String sql = "DROP TABLE IF EXISTS " + nomeTabella;
            log.debug(sql);
            stmt = c.prepareStatement(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            log.info("Fine drop tabella {}", nomeTabella);
            return result == 0;
        } catch (Exception e) {
            log.error(ERRORE_ELIMINAZIONE + e.getClass().getName() + ": " + e.getMessage());
            return false;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Crea la tabella di appoggio che serve per validare la BR3060
     *
     * @return boolean true se la tabella è stata creata
     * @throws SQLException
     */
    public boolean creaTabellaBR3060(String nomeTabella) throws SQLException {
        PreparedStatement stmt = null;
        log.info("Inizio creazione tabella {}", nomeTabella);
        try (Connection c = DbManager.getInstance().getConnection()) {
            log.info("Opened database successfully");
            String sql = "CREATE TABLE IF NOT EXISTS " + nomeTabella +
                    " (ID_RECORD INTEGER, " +
                    " TIPO_TRASMISSIONE TEXT, " +
                    " COD_REGIONE TEXT, " +
                    " ID_ASSISTITO TEXT, " +
                    " COD_TIPO_FORMULAZIONE TEXT, " +
                    " DATA_SOMMINISTRAZIONE TEXT, " +
                    " COD_ANTIGENE TEXT, " +
                    " DOSE INTEGER)";
            log.debug(sql);
            stmt = c.prepareStatement(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            log.info("Fine creazione tabella {}", nomeTabella);
            return result == 0;

        } catch (Exception e) {
            log.error(ERRORE_CREAZIONE + e.getClass().getName() + ": " + e.getMessage(), e);
            return false;
        } finally {
            if (!Objects.isNull(stmt)) {
                stmt.close();
            }
        }
    }

    public boolean aggiungiRecordTabellaValiditaAnagrafica(String nomeTabellaAnagrafica, Date lastUpdate, Date nextUpdate) throws SQLException {

        Connection conn = DbManager.getInstance().getConnection();
        String sql = "INSERT INTO VALIDITA_ANAGRAFICHE (NOMETABELLA,LASTUPDATE,NEXTUPDATE)  VALUES (?,strftime('%Y-%m-%d %H:%M:%S',?),strftime('%Y-%m-%d %H:%M:%S',?))";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String lstUpdateStr = sdf.format(lastUpdate);
        String nxtUpdateStr = sdf.format(nextUpdate);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeTabellaAnagrafica);
            pstmt.setString(2, lstUpdateStr);
            pstmt.setString(3, nxtUpdateStr);
            int result = pstmt.executeUpdate();
            return result == 1;
        } catch (Exception e) {
            log.error("ERRORE INSERT" + e);
            return false;
        } finally {
            conn.close();
        }
    }

    public boolean modificaRecordTabellaValiditaAnagrafica(String nomeTabellaAnagrafica, Date lastUpdate, Date nextUpdate) throws SQLException {
        Connection conn = DbManager.getInstance().getConnection();
        String sql = "UPDATE VALIDITA_ANAGRAFICHE set LASTUPDATE = strftime('%Y-%m-%d %H:%M:%S',?) , NEXTUPDATE = strftime('%Y-%m-%d %H:%M:%S',?) WHERE NOMETABELLA = ?";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String lstUpdateStr = sdf.format(lastUpdate);
        String nxtUpdateStr = sdf.format(nextUpdate);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lstUpdateStr);
            pstmt.setString(2, nxtUpdateStr);
            pstmt.setString(3, nomeTabellaAnagrafica);
            int result = pstmt.executeUpdate();
            if (result == 1) {
                cacheTabellaScaduta.put(nomeTabellaAnagrafica, true);
            }
            return result == 1;
        } catch (Exception e) {
            log.error("ERRORE UPDATE" + e);
            return false;
        } finally {
            conn.close();
        }
    }


    /**
     * Crea la tabella di appoggio che serve per verificare se la anagrafica è scaduta o meno
     *
     * @return boolean true se la tabella è stata creata
     * @throws SQLException
     */
    public boolean isAnagraficaValida(String nomeTabella, Date dataRichiesta) throws SQLException {
        Boolean cacheValidita = cacheTabellaScaduta.getIfPresent(nomeTabella);
        if (cacheValidita != null && cacheValidita) {
            return true;
        }
        PreparedStatement pstmt = null;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String dataRichiestaStr = sdf.format(dataRichiesta);

        try (Connection c = DbManager.getInstance().getConnection()) {
            String sql = "SELECT NOMETABELLA FROM VALIDITA_ANAGRAFICHE\n" +
                    "WHERE NOMETABELLA = ? \n" +
                    "AND strftime('%Y-%m-%d %H:%M:%S',LASTUPDATE) < strftime('%Y-%m-%d %H:%M:%S',?)\n" +
                    "AND strftime('%Y-%m-%d %H:%M:%S',NEXTUPDATE)  > strftime('%Y-%m-%d %H:%M:%S',?);";
            log.debug(sql);
            pstmt = c.prepareStatement(sql);
            pstmt.setString(1, nomeTabella);
            pstmt.setString(2, dataRichiestaStr);
            pstmt.setString(3, dataRichiestaStr);

            ResultSet result = pstmt.executeQuery();
            result.next();
            if (result.getRow() == 1) {
                cacheTabellaScaduta.put(nomeTabella, true);
            }
            return result.getRow() == 1;
        } catch (SQLException e) {
            log.error(ERRORE_CREAZIONE + " - class{} - message{}", e.getClass().getName(), e.getMessage());
            return false;
        } finally {
            if (!Objects.isNull(pstmt)) {
                pstmt.close();
            }
        }
    }

    /**
     * Verifica che la tabella di anafìgrafica sia presente all'interno della tabella di validita
     * @param nomeTabella
     * @return true se la tabella esiste all'interno di VALIDITA_ANAGRAFICHE
     * @throws SQLException
     */
//    public boolean isRecordValidaAnagrafica(String nomeTabella) throws SQLException {
//        PreparedStatement pstmt = null;
//
//        try (Connection c = DbManager.getInstance().getConnection()) {
//            log.info("Opened database successfully");
//            String sql = "SELECT NOMETABELLA FROM VALIDITA_ANAGRAFICHE\n" +
//                    "WHERE NOMETABELLA = ? \n";
//            log.debug(sql);
//            pstmt = c.prepareStatement(sql);
//            pstmt.setString(1, nomeTabella);
//
//            ResultSet result = pstmt.executeQuery();
//            result.next();
//            return result.getRow() ==1;
//        } catch (SQLException e) {
//            log.error(ERRORE_CREAZIONE + " - class{} - message{}", e.getClass().getName() , e.getMessage());
//            return false;
//        }finally {
//            if(pstmt!=null) {
//                pstmt.close();
//            }
//        }
//    }


    /**
     * Cancella i record dentro alla tabella  "nomeTabella" su DB SQLite
     *
     * @param nomeTabella
     * @return true se i record sono stati cancellati è stata cancellata
     * @throws SQLException
     */
    public boolean cancellaRecordFromTabellaValidita(String nomeTabella) throws SQLException {
        PreparedStatement stmt = null;
        try (Connection c = DbManager.getInstance().getConnection()) {
            String sql = "DELETE FROM VALIDITA_ANAGRAFICHE WHERE " + nomeTabella;
            stmt = c.prepareStatement(sql);
            log.debug(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            //TODO pensare a come gestire il ritorno
            return result > 0;
        } catch (Exception e) {
            log.error("ERRORE CANCELLAZIONE RECORD" + e.getClass().getName() + ": " + e.getMessage());
            return false;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
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
                sb.append("Query select su tabella : " + clazz + " tempo ns: " + tempo + " tempo sec: " + tempo / 1000000000 + "\n");
            }
            return sb.toString();
        }
    }

    /**
     * Inserisce records di Anagrafica nella tabella dinamica per CDM
     *
     * @param tabellaAnagrafica
     * @return il numero di record inseriti
     * @throws SQLException
     */
    public boolean aggiungiRecordTabellaCDM(TabellaAnagrafica<RecordCDM> tabellaAnagrafica) throws SQLException {

        log.debug("{}.aggiungiRecordTabellaCDM - tabellaAnagrafica[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);

        Connection conn = DbManager.getInstance().getConnection();
        String nomeTabella = tabellaAnagrafica.getNome();
        List<RecordCDM> listaRecord = tabellaAnagrafica.getRecordsAnagrafica();

        int recordInseriti = 0;
        String sql = "INSERT INTO " + nomeTabella + " VALUES(?, ?, ?, ?, ?)";

        log.debug(sql);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (RecordCDM recordAnag : listaRecord) {

                pstmt.setInt(1, recordAnag.getIdRecord());
                pstmt.setString(2, recordAnag.getCodiceAziendaSanitaria());
                pstmt.setString(3, recordAnag.getTipoStrutturaUtilizzatrice());
                pstmt.setString(4, recordAnag.getCodiceStrutturaUtilizzatrice());
                pstmt.setString(5, recordAnag.getCodiceUnitaOperativa());
                pstmt.addBatch();
            }
            recordInseriti = pstmt.executeBatch().length;
            conn.commit();

        } catch (Exception e) {
            log.error("{}.aggiungiRecordTabellaCDM - tabellaAnagrafica[{}]", e.getClass().getName(), tabellaAnagrafica, e);
            conn.close();
            return false;
        }
        conn.close();
        return recordInseriti == tabellaAnagrafica.getRecordsAnagrafica().size();
    }

    public boolean aggiungiRecordTabellaCT2(TabellaAnagrafica<RecordCDMCT2> tabellaAnagrafica) throws SQLException {

        log.debug("{}.aggiungiRecordTabellaCDM - tabellaAnagrafica[{}] - BEGIN", this.getClass().getName(), tabellaAnagrafica);

        Connection conn = DbManager.getInstance().getConnection();
        String nomeTabella = tabellaAnagrafica.getNome();
        List<RecordCDMCT2> listaRecord = tabellaAnagrafica.getRecordsAnagrafica();

        int recordInseriti = 0;
        String sql = "INSERT INTO " + nomeTabella + " VALUES(?, ?, ?, ?, ?, ?)";

        log.debug(sql);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (RecordCDMCT2 recordAnag : listaRecord) {

                pstmt.setInt(1, recordAnag.getIdRecord());
                pstmt.setString(2, recordAnag.getCodiceAziendaSanitaria());
                pstmt.setString(3, recordAnag.getCodiceContratto());
                pstmt.setString(4, recordAnag.getTipoDispositivoMedico());
                pstmt.setString(5, recordAnag.getCodiceDispositivoMedico());
                pstmt.setString(6, recordAnag.getProgressivoRiga());
                pstmt.addBatch();
            }
            recordInseriti = pstmt.executeBatch().length;
            conn.commit();

        } catch (Exception e) {
            log.error("{}.aggiungiRecordTabellaCT2 - tabellaAnagrafica[{}]", e.getClass().getName(), tabellaAnagrafica, e);
            conn.close();
            return false;
        }
        conn.close();
        return recordInseriti == tabellaAnagrafica.getRecordsAnagrafica().size();
    }

    /**
     * Crea la tabella di appoggio che serve per validare la BR3060
     *
     * @return boolean true se la tabella è stata creata
     * @throws SQLException
     */
    public boolean creaTabellaCDM(String nomeTabella) throws SQLException {
        PreparedStatement stmt = null;
        log.info("Inizio creazione tabella {}", nomeTabella);
        try (Connection c = DbManager.getInstance().getConnection()) {
            log.info("Opened database successfully");
            String sql = "CREATE TABLE IF NOT EXISTS " + nomeTabella +
                    " (ID_RECORD INTEGER, " +
                    " ASL TEXT, " +
                    " TIPO_STRUTTURA_MEDICO TEXT, " +
                    " CODICE_STRUTTURA_MEDICO TEXT, " +
                    " CODICE_OPERATIVA_CIG TEXT)";
            log.debug(sql);

            stmt = c.prepareStatement(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            log.info("Fine creazione tabella {}", nomeTabella);
            creaIndiceCDM(nomeTabella);
            log.info("Fine creazione Indice");
            return result == 0;

        } catch (Exception e) {
            log.error(ERRORE_CREAZIONE + e.getClass().getName() + ": " + e.getMessage(), e);
            return false;
        } finally {
            if (!Objects.isNull(stmt)) {
                stmt.close();
            }
        }
    }

    public boolean creaTabellaCT2(String nomeTabella) throws SQLException {
        PreparedStatement stmt = null;
        log.info("Inizio creazione tabella {}", nomeTabella);
        try (Connection c = DbManager.getInstance().getConnection()) {
            log.info("Opened database successfully");
            String sql = "CREATE TABLE IF NOT EXISTS " + nomeTabella +
                    " (ID_RECORD INTEGER, " +
                    " ASL TEXT, " +
                    " CODICE_CONTRATTO TEXT, " +
                    " TIPO_DISPOSITIVO_MEDICO TEXT, " +
                    " CODICE_DISPOSITIVO_MEDICO TEXT, " +
                    " NUMERO_PROGRESSIVO TEXT)";
            log.debug(sql);

            stmt = c.prepareStatement(sql);
            int result = stmt.executeUpdate();
            stmt.close();
            log.info("Fine creazione tabella {}", nomeTabella);
            creaIndiceCT2(nomeTabella);
            log.info("Fine creazione Indice");
            return result == 0;

        } catch (Exception e) {
            log.error(ERRORE_CREAZIONE + e.getClass().getName() + ": " + e.getMessage(), e);
            return false;
        } finally {
            if (!Objects.isNull(stmt)) {
                stmt.close();
            }
        }
    }

    public boolean creaIndiceCDM(String nomeTabella) {
        String indiceSQL = "create index idx_asl on " + nomeTabella + "(asl)";
        log.debug("Inizio creazione indice da script: {}", indiceSQL);
        try (Connection c = DbManager.getInstance().getConnection(); PreparedStatement stmt = c.prepareStatement(indiceSQL)) {
            int result = stmt.executeUpdate();
            return result == 0;
        } catch (Exception e) {
            log.error("Errore creazione indice su tabella {}", nomeTabella, e);
            return false;
        }
    }

    public boolean creaIndiceCT2(String nomeTabella) {
        String indiceSQL = "create index idx_ct2 on " + nomeTabella + "(asl,codice_contratto,tipo_dispositivo_medico,codice_dispositivo_medico)";
        log.debug("Inizio creazione indice da script: {}", indiceSQL);
        try (Connection c = DbManager.getInstance().getConnection(); PreparedStatement stmt = c.prepareStatement(indiceSQL)) {
            int result = stmt.executeUpdate();
            return result == 0;
        } catch (Exception e) {
            log.error("Errore creazione indice su tabella {}", nomeTabella, e);
            return false;
        }
    }

    public List<EsitoCDM> selectFilterCNS(TabellaAnagrafica<EsitoCDM> tabellaAnagrafica) throws SQLException {

        log.debug("{}.selectFilterCNS - nomeTabella[{}] - BEGIN", this.getClass().getName(), "CDM");

        String sql = "select asl,GROUP_CONCAT(ID_RECORD, '|') as ids from (\n" +
                "select asl,id_record from cdm m WHERE asl NOT IN (\n" +
                "select t.asl from CDM t where not exists (\n" +
                "      select a.asl from CDM a\n" +
                "\t\twhere a.asl = t.asl AND (\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000')\n" +
                "      )\n" +
                ")\n" +
                "UNION\n" +
                "select t.asl from CDM t where not exists (\n" +
                "      select a.asl from CDM a\n" +
                "\t\twhere a.asl = t.asl AND (\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000')\n" +
                "      )\n" +
                ")\n" +
                "UNION\n" +
                "select t.asl from CDM t where not exists (\n" +
                "      select a.asl from CDM a\n" +
                "\t\twhere a.asl = t.asl AND (\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO <> '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO <> '00000000' AND a.CODICE_OPERATIVA_CIG = '0000') OR\n" +
                "\t\t\t\t(a.TIPO_STRUTTURA_MEDICO = '00' AND a.CODICE_STRUTTURA_MEDICO = '00000000' AND a.CODICE_OPERATIVA_CIG <> '0000')\n" +
                "      )\n" +
                ")\n" +
                "))group by asl";

        PreparedStatement stmt = null;

        try (Connection conn = DbManager.getInstance().getConnection()) {
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                tabellaAnagrafica
                        .getRecordsAnagrafica()
                        .add(new EsitoCDM(rs.getString("ASL"), rs.getString("IDS")));
            }
        } catch (SQLException e) {
            log.error("{}.selectFilterCNS - nomeTabella[{}] - BEGIN", this.getClass().getName(), "CMD", e);
            throw new SQLException();
        } finally {
            if (!Objects.isNull(stmt)) {
                stmt.close();
            }
        }
        return tabellaAnagrafica.getRecordsAnagrafica();
    }

    /**
     * Crea la tabella di appoggio che serve per validare la BR95
     *
     * @return boolean true se la tabella è stata creata
     * @throws SQLException
     */

    public List<EsitoCDM> selectFilterCT2(TabellaAnagrafica<EsitoCDM> tabellaAnagrafica) throws SQLException {

        log.debug("{}.selectFilterCT2 - nomeTabella[{}] - BEGIN", this.getClass().getName(), "CT2");

        String sql = "select\n" +
                "\n" +
                "c.ASL,\n" +
                "\n" +
                "group_concat(c.ID_RECORD,\n" +
                "\n" +
                "'|') as ids\n" +
                "\n" +
                "from\n" +
                "\n" +
                "ct2 as c,\n" +
                "\n" +
                "(\n" +
                "\n" +
                "select\n" +
                "\n" +
                "ta.asl,\n" +
                "\n" +
                "ta.codice_contratto,\n" +
                "\n" +
                "ta.tipo_dispositivo_medico,\n" +
                "\n" +
                "ta.codice_dispositivo_medico\n" +
                "\n" +
                "from\n" +
                "\n" +
                "ct2 ta\n" +
                "\n" +
                "WHERE\n" +
                "\n" +
                "ta.NUMERO_PROGRESSIVO is null\n" +
                "\n" +
                "INTERSECT\n" +
                "\n" +
                "select\n" +
                "\n" +
                "tb.asl,\n" +
                "\n" +
                "tb.codice_contratto,\n" +
                "\n" +
                "tb.tipo_dispositivo_medico,\n" +
                "\n" +
                "tb.codice_dispositivo_medico\n" +
                "\n" +
                "from\n" +
                "\n" +
                "ct2 tb\n" +
                "\n" +
                "where\n" +
                "\n" +
                "tb.NUMERO_PROGRESSIVO is not null) as inter\n" +
                "\n" +
                "where\n" +
                "\n" +
                "c.asl = inter.asl\n" +
                "\n" +
                "and c.codice_contratto = inter.codice_contratto\n" +
                "\n" +
                "and c.tipo_dispositivo_medico = inter.tipo_dispositivo_medico\n" +
                "\n" +
                "and c.codice_dispositivo_medico = inter.codice_dispositivo_medico\n" +
                "group by c.ASL;";

        PreparedStatement stmt = null;


        try (Connection conn = DbManager.getInstance().getConnection()) {
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                tabellaAnagrafica
                        .getRecordsAnagrafica()
                        .add(new EsitoCDM(rs.getString("ASL"), rs.getString("IDS")));
            }
        } catch (SQLException e) {
            log.error("{}.selectFilterCT2 - nomeTabella[{}] - BEGIN", this.getClass().getName(), "CT2", e);
            throw new SQLException();
        } finally {
            if (!Objects.isNull(stmt)) {
                stmt.close();
            }
        }
        return tabellaAnagrafica.getRecordsAnagrafica();
    }
}
