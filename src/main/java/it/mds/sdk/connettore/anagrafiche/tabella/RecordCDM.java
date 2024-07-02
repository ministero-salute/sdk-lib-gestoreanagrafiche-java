package it.mds.sdk.connettore.anagrafiche.tabella;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordCDM {
    private int idRecord;
    private String codiceAziendaSanitaria;
    private String tipoStrutturaUtilizzatrice;
    private String codiceStrutturaUtilizzatrice;
    private String codiceUnitaOperativa;

}