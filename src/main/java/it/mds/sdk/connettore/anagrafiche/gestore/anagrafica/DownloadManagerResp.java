/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.connettore.anagrafiche.gestore.anagrafica;

import it.mds.sdk.connettore.anagrafiche.tabella.TabellaAnagrafica;
import lombok.Builder;
import lombok.Data;

import java.util.Date;


@Data
public class DownloadManagerResp {

    private Boolean isAggiornato;
    private TabellaAnagrafica tabellaAnagrafica;
    private Date dataNuovoAggiornamento;
    private Date dataUltimoAggiornamento;

    @Builder(setterPrefix = "with")
    public DownloadManagerResp(Boolean isAggiornato, TabellaAnagrafica tabellaAnagrafica, Date dataNuovoAggiornamento, Date dataUltimoAggiornamento) {
        this.isAggiornato = isAggiornato;
        this.tabellaAnagrafica = tabellaAnagrafica;
        this.dataNuovoAggiornamento = dataNuovoAggiornamento;
        this.dataUltimoAggiornamento = dataUltimoAggiornamento;
    }
}
