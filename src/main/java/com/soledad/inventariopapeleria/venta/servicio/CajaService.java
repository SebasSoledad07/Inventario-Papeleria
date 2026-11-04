package com.soledad.inventariopapeleria.venta.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.usuario.dominio.Usuario;
import com.soledad.inventariopapeleria.usuario.repositorio.UsuarioRepository;
import com.soledad.inventariopapeleria.venta.api.CajaDto;
import com.soledad.inventariopapeleria.venta.dominio.Caja;
import com.soledad.inventariopapeleria.venta.dominio.PagoVenta;
import com.soledad.inventariopapeleria.venta.repositorio.CajaRepository;
import com.soledad.inventariopapeleria.venta.repositorio.PagoVentaRepository;
import com.soledad.inventariopapeleria.venta.repositorio.VentaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CajaService {

    private final CajaRepository cajaRepository;
    private final VentaRepository ventaRepository;
    private final PagoVentaRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoria;

    public CajaService(CajaRepository cajaRepository,
                      VentaRepository ventaRepository,
                      PagoVentaRepository pagoRepository,
                      UsuarioRepository usuarioRepository,
                      AuditoriaService auditoria) {
        this.cajaRepository = cajaRepository;
        this.ventaRepository = ventaRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoria = auditoria;
    }

    @Transactional
    public CajaDto abrir(Long usuarioId, BigDecimal baseEfectivo) {
        if (baseEfectivo == null || baseEfectivo.signum() < 0) {
            throw new ErrorNegocio("La base inicial en efectivo no puede ser negativa.");
        }
        cajaRepository.findFirstByUsuarioIdAndEstadoOrderByAperturaDesc(usuarioId, "ABIERTA")
                .ifPresent(c -> {
                    throw new ErrorNegocio("El usuario ya tiene una caja abierta.");
                });
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe el usuario con id " + usuarioId + "."));
        Caja caja = new Caja();
        caja.setUsuario(usuario);
        caja.setEstado("ABIERTA");
        caja.setBaseEfectivo(baseEfectivo);
        caja.setApertura(Instant.now());
        Caja guardada = cajaRepository.save(caja);
        auditoria.registrar(usuarioId, "ABRIR_CAJA", "Caja id " + guardada.getId());
        return aDto(guardada);
    }

    @Transactional(readOnly = true)
    public CajaDto cajaAbierta(Long usuarioId) {
        Caja caja = cajaRepository.findFirstByUsuarioIdAndEstadoOrderByAperturaDesc(usuarioId, "ABIERTA")
                .orElseThrow(() -> new ErrorNegocio("El usuario no tiene una caja abierta."));
        return aDto(caja);
    }

    @Transactional(readOnly = true)
    public CajaDto consultar(Long cajaId) {
        Caja caja = cajaRepository.findById(cajaId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la caja con id " + cajaId + "."));
        return aDto(caja);
    }

    @Transactional
    public CajaDto cerrar(Long cajaId, BigDecimal efectivoContado, Long usuarioId) {
        if (efectivoContado == null || efectivoContado.signum() < 0) {
            throw new ErrorNegocio("El efectivo contado no puede ser negativo.");
        }
        Caja caja = cajaRepository.findById(cajaId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la caja con id " + cajaId + "."));
        if (!caja.getUsuario().getId().equals(usuarioId)) {
            throw new ErrorNegocio("La caja no pertenece al usuario.");
        }
        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new ErrorNegocio("La caja ya esta cerrada.");
        }
        BigDecimal esperado = calcularEfectivoEsperado(caja);
        BigDecimal diferencia = efectivoContado.subtract(esperado);
        caja.setEfectivoContado(efectivoContado);
        caja.setDiferencia(diferencia);
        caja.setCierre(Instant.now());
        caja.setEstado("CERRADA");
        Caja guardada = cajaRepository.save(caja);
        auditoria.registrar(usuarioId, "CERRAR_CAJA",
                "Caja id " + cajaId + ", esperado " + esperado + ", contado " + efectivoContado);
        return aDto(guardada);
    }

    private BigDecimal calcularEfectivoEsperado(Caja caja) {
        BigDecimal esperado = caja.getBaseEfectivo();
        BigDecimal totalCambio = BigDecimal.ZERO;
        for (var venta : ventaRepository.findByCajaId(caja.getId())) {
            if (!"CONFIRMADA".equals(venta.getEstado())) {
                continue;
            }
            totalCambio = totalCambio.add(venta.getCambio());
            for (PagoVenta pago : pagoRepository.findByVentaId(venta.getId())) {
                if ("EFECTIVO".equals(pago.getMedio())) {
                    esperado = esperado.add(pago.getValor());
                }
            }
        }
        return esperado.subtract(totalCambio);
    }

    private CajaDto aDto(Caja caja) {
        return new CajaDto(caja.getId(), caja.getUsuario().getId(), caja.getUsuario().getNombre(),
                caja.getEstado(), caja.getBaseEfectivo(), caja.getApertura(), caja.getCierre(),
                caja.getEfectivoContado(), caja.getDiferencia());
    }
}