package it.mds.sdk.connettore.anagrafiche.tabella;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class TabellaAnagrafica<T> {
	private String nome;
	private List<T> recordsAnagrafica;
	
	public TabellaAnagrafica(String nome, List<T> recordsAnagrafica) {
		super();
		this.nome = nome;
		this.recordsAnagrafica = recordsAnagrafica;
	}

	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public List<T> getRecordsAnagrafica() {
		return (recordsAnagrafica == null ? recordsAnagrafica=new ArrayList<>() : recordsAnagrafica);
		
	}
	public void setRecordsAnagrafica(List<T> recordsAnagrafica) {
		this.recordsAnagrafica = recordsAnagrafica;
	}

}
