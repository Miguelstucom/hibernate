package model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import utils.TipoVid;

@Entity
@Table(name= "vid")
public class Vid {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = true)
    private int id;
    
    @Column(name = "tipo_vid", nullable = true)
    private TipoVid vid;
    
    @Column(name = "cantidad", nullable = true)
    private int cantidad;
    
    @Column(name = "price", nullable = true, precision = 10, scale = 2)
    private BigDecimal price;
    
    public Vid() {}
    
    public Vid(TipoVid vid, int cantidad, BigDecimal price) {
        this.vid = vid;
        this.cantidad = cantidad;
        this.price = price;
    }
    
    public int getId() {
        return this.id;
    }
    
    public TipoVid getVid() {
        return vid;
    }
    
    public int getCantidad() {
        return cantidad;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    @Override
    public String toString() {
        return "Vid [vid=" + (vid.equals("0") ? "blanca" : "negra") + ", cantidad=" + cantidad + ", price=" + price + "]";
    }
}


