package com.soledad.inventariopapeleria.venta.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Consecutivo de ventas (RN-31): sin secuencia, actualizado con UPDATE...RETURNING.
 */
@Entity
@Table(name = "consecutivos")
public class Consecutivo {

    @Id
    private String nombre;

    private Integer ultimo;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getUltimo() { return ultimo; }
    public void setUltimo(Integer ultimo) { this.ultimo = ultimo; }
}