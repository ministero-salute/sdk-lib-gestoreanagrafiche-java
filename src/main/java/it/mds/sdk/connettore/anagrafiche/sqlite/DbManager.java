/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.sqlite;

import it.mds.sdk.connettore.anagrafiche.conf.Configurazione;
import lombok.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbManager {

    private final Configurazione config;

    private DbManager() {
        this.config = new Configurazione();
    }

    public static DbManager getInstance() {
        return DBManagerHolder.INSTANCE;
    }

    private static class DBManagerHolder {
        private static final DbManager INSTANCE = new DbManager();
    }

    public  Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getConfigurazioneDb().getUrlSqLiteDb());
    }


}

