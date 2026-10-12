package com.soledad.inventariopapeleria.inventario.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.inventario.api.ConteoFisicoRequest;
import com.soledad.inventariopapeleria.inventario.api.ConteoFisicoResultado;
import com.soledad.inventariopapeleria.inventario.api.LineaConteo;
import com.soledad.inventariopapeleria.producto.dominio.Producto;
import com.soledad.inventariopapeleria.producto.repositorio.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Conteo fisico (RF-18, D-3): el estado vive en el navegador y al terminar
 * se genera un ajuste por cada diferencia entre lo contado y el stock, todo
 * en una sola transaccion. Motivo obligatorio (RN-22).
 */
@Service
public class ConteoFisicoService {

    private static final String MOTIVO = "conteo físico";

    private final ProductoRepository productoRepository;
    private final InventarioService inventario;
    private final AuditoriaService auditoria;

    public ConteoFisicoService(ProductoRepository productoRepository,
                               InventarioService inventario,
                               AuditoriaService auditoria) {
        this.productoRepository = productoRepository;
        this.inventario = inventario;
        this.auditoria = auditoria;
    }

    /**
     * Aplica los ajustes de inventario por las diferencias del conteo.
     *
     * @return numero de ajustes generados
     */
    @Transactional
    public ConteoFisicoResultado ejecutar(ConteoFisicoRequest request, Long usuarioId) {
        int ajustes = 0;
        for (LineaConteo linea : request.lineas()) {
            Producto producto = productoRepository.findById(linea.productoId())
                    .orElseThrow(() -> new RecursoNoEncontrado(
                            "No existe el producto con id " + linea.productoId() + "."));
            int diferencia = linea.cantidadContada() - producto.getStock();
            if (diferencia > 0) {
                inventario.ajustarPositivo(producto.getId(), diferencia, MOTIVO, usuarioId);
                ajustes++;
            } else if (diferencia < 0) {
                inventario.ajustarNegativo(producto.getId(), -diferencia, MOTIVO, usuarioId);
                ajustes++;
            }
        }
        auditoria.registrar(usuarioId, "CONTEO_FISICO", ajustes + " ajuste(s) aplicado(s)");
        return new ConteoFisicoResultado(ajustes);
    }
}