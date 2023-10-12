package it.mds.sdk.connettore.anagrafiche.gestore.anagrafica;

import it.mds.sdk.connettore.anagrafiche.exception.AnagraficaException;
import it.mds.sdk.connettore.anagrafiche.sqlite.QueryAnagrafica;
import it.mds.sdk.connettore.anagrafiche.tabella.TabellaAnagrafica;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CacheSQLiteTest {

    MockedStatic<QueryAnagrafica> queryAnagraficaMockedStatic;
    MockedStatic<CacheSQLite> cacheSQLiteMockedStatic;
    QueryAnagrafica queryAnagrafica = Mockito.mock(QueryAnagrafica.class);
    TabellaAnagrafica tabellaAnagrafica = Mockito.mock(TabellaAnagrafica.class);
    CacheSQLite cacheSQLite = new CacheSQLite();

    Date date = Mockito.mock(Date.class);

    @BeforeEach
    void init() {
        queryAnagraficaMockedStatic = mockStatic(QueryAnagrafica.class);
        cacheSQLiteMockedStatic = mockStatic(CacheSQLite.class);
    }

    @Test
    void getInstanceTest() {
        queryAnagraficaMockedStatic.when(CacheSQLite::getInstance).thenReturn(cacheSQLite);
        CacheSQLite.getInstance();
    }

    @Test
    void isAnagraficaTest() {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.tabellaAnagraficaExists(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.isAnagrafica("nomeTabella"));
    }

    @Test
    void isAnagraficaDaAggiornareTestOk() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.isAnagraficaValida(any(), any())).thenReturn(Boolean.FALSE);
        Assertions.assertTrue(cacheSQLite.isAnagraficaDaAggiornare("nomeTabella"));
    }

    @Test
    void isAnagraficaDaAggiornareTestKO() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.isAnagraficaValida(any(), any())).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.isAnagraficaDaAggiornare("nomeTabella"));
    }

    @Test
    void aggiornaAnagraficaTestOk() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.cancellaRecordTabella(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaAnagrafica(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.aggiornaAnagrafica(tabellaAnagrafica));
    }

    @Test
    void aggiornaAnagraficaTestKO() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.cancellaRecordTabella(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaAnagrafica(any())).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.aggiornaAnagrafica(tabellaAnagrafica));
    }

    /**
     * tabellaAnagraficaExists() true
     * creaTabellaAnagrafica() true
     * aggiungiRecordTabellaAnagrafica() true
     *
     * @throws SQLException
     */
    @Test
    void nuovaAnagraficaTestOk() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.tabellaAnagraficaExists(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.creaTabellaValiditaAnagrafica()).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaValiditaAnagrafica(any(), any(), any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.creaTabellaAnagrafica(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaAnagrafica(tabellaAnagrafica)).thenReturn(Boolean.TRUE);

        Assertions.assertTrue(cacheSQLite.nuovaAnagrafica(tabellaAnagrafica));
    }

    /**
     * tabellaAnagraficaExists() true
     * creaTabellaAnagrafica() true
     * aggiungiRecordTabellaAnagrafica() false
     *
     * @throws SQLException
     */
    @Test
    void nuovaAnagraficaTestOk2() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.tabellaAnagraficaExists(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.creaTabellaValiditaAnagrafica()).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaValiditaAnagrafica(any(), any(), any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.creaTabellaAnagrafica(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaAnagrafica(tabellaAnagrafica)).thenReturn(Boolean.FALSE);
        Mockito.when(queryAnagrafica.cancellaRecordFromTabellaValidita(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.cancellaTabella(any())).thenReturn(Boolean.TRUE);

        Assertions.assertFalse(cacheSQLite.nuovaAnagrafica(tabellaAnagrafica));
    }

    /**
     * tabellaAnagraficaExists() true
     * creaTabellaAnagrafica() false
     *
     * @throws SQLException
     */
    @Test
    void nuovaAnagraficaTestOk3() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.tabellaAnagraficaExists(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.creaTabellaValiditaAnagrafica()).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaValiditaAnagrafica(any(), any(), any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.creaTabellaAnagrafica(any())).thenReturn(Boolean.FALSE);

        Assertions.assertFalse(cacheSQLite.nuovaAnagrafica(tabellaAnagrafica));
    }

    /**
     * tabellaAnagraficaExists() false
     * creaTabellaAnagrafica() true
     * aggiungiRecordTabellaAnagrafica() false
     *
     * @throws SQLException
     */
    @Test
    void nuovaAnagraficaTestOk4() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.tabellaAnagraficaExists(any())).thenReturn(Boolean.FALSE);
        Mockito.when(queryAnagrafica.creaTabellaValiditaAnagrafica()).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.creaTabellaValiditaAnagrafica()).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaValiditaAnagrafica(any(), any(), any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.creaTabellaAnagrafica(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaAnagrafica(tabellaAnagrafica)).thenReturn(Boolean.FALSE);
        Mockito.when(queryAnagrafica.cancellaRecordFromTabellaValidita(any())).thenReturn(Boolean.TRUE);
        Mockito.when(queryAnagrafica.cancellaTabella(any())).thenReturn(Boolean.TRUE);

        Assertions.assertFalse(cacheSQLite.nuovaAnagrafica(tabellaAnagrafica));
    }

    /**
     * tabellaAnagraficaExists() false
     *
     * @throws SQLException
     */
    @Test
    void nuovaAnagraficaTestKO() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.tabellaAnagraficaExists(any())).thenReturn(Boolean.FALSE);
        Mockito.when(queryAnagrafica.creaTabellaValiditaAnagrafica()).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.nuovaAnagrafica(tabellaAnagrafica));
    }

    @Test
    void dropTableBR3060Test() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.dropTable(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.dropTableBR3060("nomeTabella"));
    }


    @Test
    void dropTableCDMTest() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.dropTable(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.dropTableCDM("nomeTabella"));
    }

    @Test
    void dropTableCT2Test() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.dropTable(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.dropTableCT2("nomeTabella"));
    }

    @Test
    void creaTableBR3060Test() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.creaTabellaBR3060(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.creaTabellaBR3060("nomeTabella"));
    }

    @Test
    void creaTableCDMTest() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.creaTabellaCDM(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.creaTabellaCDM("nomeTabella"));
    }

    @Test
    void creaTableCT2Test() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.creaTabellaCT2(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.creaTabellaCT2("nomeTabella"));
    }

    @Test
    void selectFilterBR3060Test() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.selectFilterBR3060(any())).thenReturn(Collections.emptyList());
        Assertions.assertEquals(Collections.emptyList(), cacheSQLite.selectFilterBR3060(tabellaAnagrafica));
    }

    @Test
    void selectFilterCNSTest() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.selectFilterCNS(any())).thenReturn(Collections.emptyList());
        Assertions.assertEquals(Collections.emptyList(), cacheSQLite.selectFilterCNS(tabellaAnagrafica));
    }

    @Test
    void selectFilterCT2Test() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.selectFilterCT2(any())).thenReturn(Collections.emptyList());
        Assertions.assertEquals(Collections.emptyList(), cacheSQLite.selectFilterCT2(tabellaAnagrafica));
    }

    /**
     * @throws SQLException recordAggiuntiOk: true
     */
    @Test
    void addRecordBR3060TestOk() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaBR3060(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.addRecordBR3060(tabellaAnagrafica));
    }

    @Test
    void addRecordCDMTestOk() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaCDM(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.addRecordCDM(tabellaAnagrafica));
    }

    @Test
    void addRecordCT2TestOk() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaCT2(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.addRecordCT2(tabellaAnagrafica));
    }

    /**
     * @throws SQLException recordAggiuntiOk: false
     */
    @Test
    void addRecordBR3060TestOk2() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaBR3060(any())).thenReturn(Boolean.FALSE);
        Assertions.assertFalse(cacheSQLite.addRecordBR3060(tabellaAnagrafica));
    }

    /**
     * @throws SQLException
     */
    @Test
    void addRecordBR3060TestKO() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaBR3060(any())).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.addRecordBR3060(tabellaAnagrafica));
    }

    @Test
    void addRecordCDMException() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaCDM(any())).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.addRecordCDM(tabellaAnagrafica));
    }

    @Test
    void addRecordCT2Exception() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.aggiungiRecordTabellaCT2(any())).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.addRecordCT2(tabellaAnagrafica));
    }

    @Test
    void creaIndiceTest() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.creaIndice(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.creaIndice("nomeTabella"));
    }

    @Test
    void cancellaAnagraficaTestOk() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.cancellaTabella(any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.cancellaAnagrafica("nomeTabella"));
    }

    @Test
    void cancellaAnagraficaTestKO() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.cancellaTabella(any())).thenThrow(SQLException.class);
        //Assertions.assertTrue(cacheSQLite.cancellaAnagrafica("nomeTabella"));
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.cancellaAnagrafica("nomeTabella"));
    }

    @Test
    void nuovaDataAggiornamentoOk() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.modificaRecordTabellaValiditaAnagrafica(any(), any(), any())).thenReturn(Boolean.TRUE);
        Assertions.assertTrue(cacheSQLite.nuovaDataAggiornamento("nomeTabella", date, date));
    }

    @Test
    void nuovaDataAggiornamentoKO() throws SQLException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.modificaRecordTabellaValiditaAnagrafica(any(), any(), any())).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.nuovaDataAggiornamento("nomeTabella", date, date));
    }

    @Test
    void getAnagraficaOk() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.selectAllTabellaAnagrafica(any())).thenReturn(tabellaAnagrafica);
        Assertions.assertEquals(tabellaAnagrafica, cacheSQLite.getAnagrafica("nomeTabella"));
    }

    @Test
    void getAnagraficaKO() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.selectAllTabellaAnagrafica(any())).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.getAnagrafica("nomeTabella"));
    }

    @Test
    void getAnagraficaValoreDaEstrarreOk() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.selectDatoFromTabella(any(), any())).thenReturn(tabellaAnagrafica);
        Assertions.assertEquals(tabellaAnagrafica, cacheSQLite.getAnagrafica("nomeTabella", "valore"));
    }

    @Test
    void getAnagraficaValoreDaEstrarreKO() throws SQLException, ParseException {
        queryAnagraficaMockedStatic.when(QueryAnagrafica::getInstanceWithCache).thenReturn(queryAnagrafica);
        Mockito.when(queryAnagrafica.selectDatoFromTabella(any(), any())).thenThrow(SQLException.class);
        Assertions.assertThrows(AnagraficaException.class, () -> cacheSQLite.getAnagrafica("nomeTabella", "valore"));
    }

    @AfterEach
    void closeStaticMocks() {
        queryAnagraficaMockedStatic.close();
        cacheSQLiteMockedStatic.close();
    }

}
