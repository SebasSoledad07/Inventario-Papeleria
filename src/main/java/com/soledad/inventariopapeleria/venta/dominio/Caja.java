package com.soledad.inventariopapeleria.venta.dominio;

import com.soledad.inventariopapeleria.usuario.dominio.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Caja (RN-30, RN-40, RN-41, RN-42).
 */
@Entity
@Table(name = "cajas")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 10)
    private String estado = "ABIERTA";

    @Column(name = "base_efectivo", nullable = false)
    private BigDecimal baseEfectivo;

    @Column(nullable = false)
    private Instant apertura;

    @Column
    private Instant cierre;

    @Column(name = "efectivo_contado")
    private BigDecimal efectivoContado;

    @Column
    private BigDecimal diferencia;

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getBaseEfectivo() { return baseEfectivo; }
    public void setBaseEfectivo(BigDecimal baseEfectivo) { this.baseEfectivo = baseEfectivo; }
    public Instant getApertura() { return apertura; }
    public void setApertura(Instant apertura) { this.apertura = apertura; }
    public Instant getCierre() { return cierre; }
    public void setCierre(Instant cierre) { this.cierre = cierre; }
    public BigDecimal getEfectivoContado() { return efectivoContado; }
    public void setEfectivoContado(BigDecimal efectivoContado) { this.efectivoContado = efectivoContado; }
    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
}