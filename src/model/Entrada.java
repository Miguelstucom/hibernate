package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name= "Entrada")
public class Entrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private String id;

    @Column(name = "instruccion")
    private String instruccion;

    public String getId() {
        return id;
    }

    public String getInstruccion() {
        return instruccion;
    }

    public void setInstruccion(String instruccion) {
        this.instruccion = instruccion;
    }
    
    @Override
    public String toString() {
        return "Entrada{" +
               "id=" + id +
               ", instruccion='" + instruccion + '\'' +
               '}';
    }

	public void setId(String string) {
		this.id = string;
		// TODO Auto-generated method stub
		
	}
}
