/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.conf;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
@Getter
public class Configurazione {

    private static final String FILE_CONF ="configurazioni-anagrafiche.properties";

    ConfigurazioneDb configurazioneDb;
    ClientHost clientHost;
    ClientUsername clientUsername;
    ClientPassword clientPassword;
    Resilienza resilienza;
    Properties properties;

    public Configurazione() {this(leggiConfigurazioneEsterna());}

    public Configurazione(final Properties conf) {
        this.properties = conf;
        this.configurazioneDb = ConfigurazioneDb.builder()
                .withUrlSqLiteDb("jdbc:sqlite:"+conf.getProperty("sqlite.database.file.path", ""))
                .build();
        this.clientHost = ClientHost.builder()
                .withHost(conf.getProperty("client.host", ""))
                .build();
        this.clientUsername = ClientUsername.builder()
                .withUser(conf.getProperty("client.username", ""))
                .build();
        this.clientPassword = ClientPassword.builder()
                .withPass(conf.getProperty("client.password", ""))
                .build();
        this.resilienza = Resilienza.builder()
                .withOre(conf.getProperty("resilienza.ore", ""))
                .build();
    }


    @Value
    @Builder(setterPrefix = "with")
    public static class ConfigurazioneDb {
        String urlSqLiteDb;
    }
    @Value
    @Builder(setterPrefix = "with")
    public static class ClientHost {
        String host;
    }
    @Value
    @Builder(setterPrefix = "with")
    public static class ClientUsername{
        String user;
    }
    @Value
    @Builder(setterPrefix = "with")
    public static class ClientPassword {
        String pass;
    }
    @Value
    @Builder(setterPrefix = "with")
    public static class Resilienza {
        String ore;
    }

        private static Properties leggiConfigurazione(final String nomeFile) {
        final Properties prop = new Properties();
        try (final InputStream is = Configurazione.class.getClassLoader().getResourceAsStream(nomeFile)) {
            prop.load(is);
        } catch (IOException e) {
            log.error("Errore nella lettura delle configurazioni {}", nomeFile, e);
        }
        return prop;
    }
    private static Properties leggiConfigurazioneEsterna() {
        log.debug("{}.leggiConfigurazioneEsterna - BEGIN", Configurazione.class.getName());
        Properties properties = new Properties();
        try (InputStreamReader in = new InputStreamReader(new FileInputStream("/sdk/properties/" + FILE_CONF),
                StandardCharsets.UTF_8))  {
            properties.load(in);
        } catch (IOException e) {
            log.error("{}.leggiConfigurazioneEsterna", Configurazione.class.getName(), e);
            return leggiConfigurazione(FILE_CONF);
        }
        return properties;
    }



}
