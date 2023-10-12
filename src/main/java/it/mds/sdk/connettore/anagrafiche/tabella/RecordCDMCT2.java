package it.mds.sdk.connettore.anagrafiche.tabella;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordCDMCT2 {
    private int idRecord;
    private String codiceAziendaSanitaria;
    private String codiceContratto;
    private String tipoDispositivoMedico;
    private String codiceDispositivoMedico;
    private String progressivoRiga;

}