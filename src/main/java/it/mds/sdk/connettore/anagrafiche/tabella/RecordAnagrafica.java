package it.mds.sdk.connettore.anagrafiche.tabella;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
public class RecordAnagrafica {

    private LocalDateTime validoDa;
    private LocalDateTime validoA;
    private String dato;

    public RecordAnagrafica(LocalDateTime validoDa, LocalDateTime validoA, String dato) {
        super();
        this.validoDa = validoDa;
        this.validoA = validoA;
        this.dato = dato;
    }

    public LocalDateTime getValidoDa() {
        return validoDa;
    }

    public LocalDateTime getValidoA() {
        return validoA;
    }

    public String getDato() {
        return dato;
    }

    public void setDato(String dato) {
        this.dato = dato;
    }

}