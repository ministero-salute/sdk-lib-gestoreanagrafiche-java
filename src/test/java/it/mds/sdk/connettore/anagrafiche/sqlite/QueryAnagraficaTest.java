package it.mds.sdk.connettore.anagrafiche.sqlite;

import com.github.benmanes.caffeine.cache.Cache;
import it.mds.sdk.connettore.anagrafiche.gestore.anagrafica.GestoreAnagrafica;
import it.mds.sdk.connettore.anagrafiche.tabella.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QueryAnagraficaTest {

    private static final String FILE_TEST_DB = "src/test/resources/anagrafica.db";

    @Mock
    GestoreAnagrafica gestoreAnagrafica;
    MockedStatic<DbManager> dbManagerMockedStatic;
    private Cache<String, TabellaAnagrafica> cacheTabelle = Mockito.mock(Cache.class);
    private TabellaAnagrafica tabellaAnagrafica = Mockito.mock(TabellaAnagrafica.class);
    private Connection connection = Mockito.mock(Connection.class);

//    @BeforeEach
//    void init(){
//        dbManagerMockedStatic = mockStatic(DbManager.class);
//    }

    @Test
    @Order(1)
    void insertNuovaTabellaAnagrafica() throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        assertTrue(anagrafica.creaTabellaAnagrafica("tabellaTest"));
    }

    @Test
    @Order(3)
    void insertRecordTabellaAnagrafica() throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        TabellaAnagrafica tabella = new TabellaAnagrafica();
        tabella.setNome("tabellaTest");
        List<RecordAnagrafica> listaRecord = new ArrayList<>();
        LocalDateTime dateDA = LocalDateTime.of(LocalDate.of(2014, Month.APRIL, 6), LocalTime.of(8, 15, 00));
        LocalDateTime dateA = LocalDateTime.of(LocalDate.of(2099, Month.APRIL, 6), LocalTime.of(8, 15, 00));
        listaRecord.add(new RecordAnagrafica(dateDA, dateA, "VALORE2"));
        tabella.getRecordsAnagrafica().addAll(listaRecord);
        assertTrue(anagrafica.aggiungiRecordTabellaAnagrafica(tabella));
    }

    @Test
    @Order(2)
    void tabellaEsiste() throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        boolean esiste = anagrafica.tabellaAnagraficaExists("tabellaTest");
        System.out.println(esiste);
        assertTrue(esiste);

    }

    @Test
    @Order(4)
    void tabellaNonEsiste() throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        boolean esiste = anagrafica.tabellaAnagraficaExists("tabellaNonEsistenteTest");
        System.out.println(esiste);
        assertFalse(esiste);
    }

    @Test
    @Order(5)
    void getRecordTabellaAnagrafica() throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        TabellaAnagrafica tabellatest = anagrafica.selectAllTabellaAnagrafica("tabellaTest");
        assertFalse(tabellatest.getRecordsAnagrafica().isEmpty());
    }

    @Test
    @Order(6)
    void modificaTabellaAnagrafica() throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        TabellaAnagrafica tabella = new TabellaAnagrafica();
        tabella.setNome("tabellaTest");
        List<RecordAnagrafica> listaRecord = new ArrayList<>();
        LocalDateTime dateDA = LocalDateTime.of(LocalDate.of(2014, Month.APRIL, 6), LocalTime.of(8, 15, 00));
        LocalDateTime dateA = LocalDateTime.of(LocalDate.of(2099, Month.APRIL, 6), LocalTime.of(8, 15, 00));
        listaRecord.add(new RecordAnagrafica(dateDA, dateA, "VALORE3"));
        listaRecord.add(new RecordAnagrafica(dateDA, dateA, "VALORE4"));
        listaRecord.add(new RecordAnagrafica(dateDA, dateA, "VALORE5"));
        tabella.getRecordsAnagrafica().addAll(listaRecord);
        anagrafica.cancellaRecordTabella(tabella.getNome());
        assertTrue(anagrafica.aggiungiRecordTabellaAnagrafica(tabella));
    }

    @Test
    @Order(7)
    void insertNuovaTabellaValiditaAnagrafica() throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        assertTrue(anagrafica.creaTabellaValiditaAnagrafica());
    }

    @Test
    @Order(8)
    void insertRecordTabellaValiditaAnagrafica() throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String lastUpdate = "2022-04-20 08:00:00";
        String nextUpdate = "2022-04-25 08:00:00";

        Date lastUpdateDate = sdf.parse(lastUpdate);
        Date nextUpdateDate = sdf.parse(nextUpdate);
        assertTrue(anagrafica.aggiungiRecordTabellaValiditaAnagrafica("tabellaTest", lastUpdateDate, nextUpdateDate));
    }

    // TOFIX: Test da fixare
    //@Test
    @Order(9)
    void aggiornaRecordTabellaValiditaAnagrafica() throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String lastUpdate = "2022-04-20 08:00:00";
        String nextUpdate = "2023-04-25 08:00:00";

        Date lastUpdateDate = sdf.parse(lastUpdate);
        Date nextUpdateDate = sdf.parse(nextUpdate);
        assertTrue(anagrafica.modificaRecordTabellaValiditaAnagrafica("tabellaTest", lastUpdateDate, nextUpdateDate));
    }


    @Test
    @Order(11)
    void isValidTabellaValiditaAnagraficaOK() throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dataRichiesta = "2022-04-21 08:00:00";
        Date dataRichiestaDate = sdf.parse(dataRichiesta);
        assertTrue(anagrafica.isAnagraficaValida("tabellaTest", dataRichiestaDate));

    }

    @Test
    //@Disabled
    @Order(10)
    void isValidTabellaValiditaAnagraficaKO() throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dataRichiesta = "2030-05-25 08:00:00";
        Date dataRichiestaDate = sdf.parse(dataRichiesta);
        assertFalse(anagrafica.isAnagraficaValida("tabellaTest", dataRichiestaDate));

    }

    @Test
    @Order(12)
    void cancellaTabella() throws SQLException, ParseException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        //anagrafica.cancellaTabella("VALIDITA_ANAGRAFICHE");
        assertTrue(anagrafica.cancellaTabella("tabellaTest"));
    }

    @Test
    @Order(13)
    void selectDatoFromTabellaTest() throws SQLException {
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        when(cacheTabelle.getIfPresent(any())).thenReturn(tabellaAnagrafica);
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        doNothing().when(ps).setString(anyInt(), anyString());
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(Boolean.FALSE);

        anagrafica.selectDatoFromTabella("nomeTabella", "dato");
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(14)
    void creaIndiceTestOk() throws SQLException {
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
//        ResultSet rs = Mockito.mock(ResultSet.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
//        when(cacheTabelle.getIfPresent(any())).thenReturn(tabellaAnagrafica);
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        doNothing().when(ps).setString(anyInt(), anyString());
        when(ps.executeUpdate()).thenReturn(0);
        assertTrue(anagrafica.creaIndice("nomeTabella"));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(15)
    void creaIndiceTestKO() throws SQLException {
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        doNothing().when(ps).setString(anyInt(), anyString());
        when(ps.executeUpdate()).thenThrow(SQLException.class);
        assertFalse(anagrafica.creaIndice("nomeTabella"));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(16)
    void aggiungiRecordTabellaBR3060Test() throws SQLException {
        int[] uno = new int[1];
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(tabellaAnagrafica.getNome()).thenReturn("nomeTabella");
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(Collections.emptyList());
        doNothing().when(connection).commit();
        when(ps.executeBatch()).thenReturn(uno);
        RecordBR3060 obj = Mockito.mock(RecordBR3060.class);
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(List.of(obj));
        assertTrue(anagrafica.aggiungiRecordTabellaBR3060(tabellaAnagrafica));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(17)
    void aggiungiRecordTabellaBR3060TestKO() throws SQLException {
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(tabellaAnagrafica.getNome()).thenReturn("nomeTabella");
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(Collections.emptyList());
        //doThrow(Exception.class).when(connection).commit();
        when(ps.executeBatch()).thenThrow(SQLException.class);
        RecordBR3060 obj = Mockito.mock(RecordBR3060.class);
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(List.of(obj));
        assertFalse(anagrafica.aggiungiRecordTabellaBR3060(tabellaAnagrafica));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(18)
    void dropTable() throws SQLException {
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);
        doNothing().when(ps).close();
        assertTrue(anagrafica.dropTable("nomeTabella"));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(19)
    void dropTableKO() throws SQLException {
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(ps.executeUpdate()).thenThrow(SQLException.class);
        assertFalse(anagrafica.dropTable("nomeTabella"));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(20)
    void aggiungiRecordTabellaCDMTestKO() throws SQLException {
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(tabellaAnagrafica.getNome()).thenReturn("nomeTabella");
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(Collections.emptyList());
        //doThrow(Exception.class).when(connection).commit();
        when(ps.executeBatch()).thenThrow(SQLException.class);
        RecordCDM obj = Mockito.mock(RecordCDM.class);
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(List.of(obj));
        assertFalse(anagrafica.aggiungiRecordTabellaCDM(tabellaAnagrafica));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(21)
    void aggiungiRecordTabellaCDMTest() throws SQLException {
        int[] uno = new int[1];
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(tabellaAnagrafica.getNome()).thenReturn("nomeTabella");
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(Collections.emptyList());
        doNothing().when(connection).commit();
        when(ps.executeBatch()).thenReturn(uno);
        RecordCDM obj = Mockito.mock(RecordCDM.class);
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(List.of(obj));
        assertTrue(anagrafica.aggiungiRecordTabellaCDM(tabellaAnagrafica));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(22)
    void exceptionNuovaTabellaAnagrafica() throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        assertTrue(anagrafica.creaTabellaCDM("tabellaTest"));
    }
    @Test
    @Order(23)
    void insertNuovaTabellaAnagraficaB3060() throws SQLException {
        QueryAnagrafica anagrafica = QueryAnagrafica.getInstanceWithCache();
        assertTrue(anagrafica.creaTabellaBR3060("tabellaTest"));
    }

    @Test
    @Order(24)
    void aggiungiRecordTabellaCT2TestKO() throws SQLException {
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(tabellaAnagrafica.getNome()).thenReturn("nomeTabella");
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(Collections.emptyList());
        //doThrow(Exception.class).when(connection).commit();
        when(ps.executeBatch()).thenThrow(SQLException.class);
        RecordCDMCT2 obj = Mockito.mock(RecordCDMCT2.class);
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(List.of(obj));
        assertFalse(anagrafica.aggiungiRecordTabellaCT2(tabellaAnagrafica));
        dbManagerMockedStatic.close();
    }

    @Test
    @Order(25)
    void aggiungiRecordTabellaCT2Test() throws SQLException {
        int[] uno = new int[1];
        DbManager dbManager = Mockito.mock(DbManager.class);
        PreparedStatement ps = Mockito.mock(PreparedStatement.class);
        QueryAnagrafica anagrafica = spy(QueryAnagrafica.getInstanceWithCache());
        dbManagerMockedStatic = mockStatic(DbManager.class);
        dbManagerMockedStatic.when(DbManager::getInstance).thenReturn(dbManager);
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(ps);
        when(tabellaAnagrafica.getNome()).thenReturn("nomeTabella");
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(Collections.emptyList());
        doNothing().when(connection).commit();
        when(ps.executeBatch()).thenReturn(uno);
        RecordCDMCT2 obj = Mockito.mock(RecordCDMCT2.class);
        when(tabellaAnagrafica.getRecordsAnagrafica()).thenReturn(List.of(obj));
        assertTrue(anagrafica.aggiungiRecordTabellaCT2(tabellaAnagrafica));
        dbManagerMockedStatic.close();
    }

    @AfterAll
    static void deleteAnagraficaDb() {
        File file = new File(FILE_TEST_DB);
        file.delete();
    }

}