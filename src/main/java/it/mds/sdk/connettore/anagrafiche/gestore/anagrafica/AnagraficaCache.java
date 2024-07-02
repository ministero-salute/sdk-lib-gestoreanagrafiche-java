/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.gestore.anagrafica;

import it.mds.sdk.connettore.anagrafiche.tabella.TabellaAnagrafica;

import java.util.Date;

public interface AnagraficaCache {

    Boolean isAnagrafica(String nomeTabella);
    Boolean isAnagraficaDaAggiornare(String nomeTabella);
    Boolean aggiornaAnagrafica(TabellaAnagrafica tabellaAnagrafica);
    Boolean nuovaAnagrafica(TabellaAnagrafica tabellaAnagrafica);
    Boolean cancellaAnagrafica(String nomeTabella);
    Boolean nuovaDataAggiornamento(String nomeTabella, Date ultimaDataAggiornamento, Date nuovaDataAggiornamento);
    TabellaAnagrafica getAnagrafica(String nomeTabella);

}
