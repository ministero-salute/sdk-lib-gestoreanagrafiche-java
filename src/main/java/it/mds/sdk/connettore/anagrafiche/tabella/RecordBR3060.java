package it.mds.sdk.connettore.anagrafiche.tabella;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordBR3060 {
    private int idRecord;
    private String tipoTrasmissione;
    private String codRegione;
    private String idAssistito;
    private String codTipoFormulazione;
    private String dataSomministrazione;
    private String codiceAntigene;
    private Integer dose;

}