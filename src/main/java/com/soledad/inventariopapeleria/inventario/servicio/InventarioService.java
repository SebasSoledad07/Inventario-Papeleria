package com.soledad.inventariopapeleria.inventario.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.inventario.dominio.MovimientoInventario;
import com.soledad.inventariopapeleria.inventario.dominio.TipoMovimiento;
import com.soledad.inventariopapeleria.inventario.repositorio.MovimientoInventarioRepository;
import com.soledad.inventariopapeleria.inventario.repositorio.StockRepository;
import com.soledad.inventariopapeleria.producto.dominio.Producto;
import com.soledad.inventariopapeleria.producto.repositorio.ProductoRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unica puerta para cambiar stock (RN-20). Todo cambio genera kardex.
 * Garantiza RN-19 (stock no negativo) via UPDATE ... WHERE stock >= :cantidad
 * o actualizacion directa controlada.
 */
@Service
public class InventarioService {

    private final StockRepository stockRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AuditoriaService auditoria;

    public InventarioService(StockRepository stockRepository,
                             ProductoRepository productoRepository,
                             MovimientoInventarioRepository movimientoRepository,
                             AuditoriaService auditoria) {
        this.stockRepository = stockRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.auditoria = auditoria;
    }

    @Transactional
    public void descontar(Long productoId, int cantidad, String referencia, Long usuarioId) {
        if (cantidad <= 0) {
            throw new ErrorNegocio("La cantidad a descontar debe ser mayor a cero.");
        }
        int filas = stockRepository.descontarStock(productoId, cantidad);
        if (filas == 0) {
            Producto p = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RecursoNoEncontrado("No existe el producto con id " + productoId + "."));
            throw new ErrorNegocio("Stock insuficiente para el producto '" + p.getNombre()
                    + "'. Disponible: " + p.getStock() + ", solicitado: " + cantidad + ".");
        }
        Producto actualizado = productoRepository.findById(productoId).orElseThrow();
        registrarMovimiento(actualizado, TipoMovimiento.SALIDA_VENTA, cantidad, referencia, null, usuarioId);
        auditoria.registrar(usuarioId, "DESCONTAR_STOCK", "Producto id " + productoId + ", cant " + cantidad);
    }

    @Transactional
    public void devolver(Long productoId, int cantidad, String referencia, Long usuarioId) {
        if (cantidad <= 0) {
            throw new ErrorNegocio("La cantidad a devolver debe ser mayor a cero.");
        }
        stockRepository.incrementarStock(productoId, cantidad);
        Producto actualizado = productoRepository.findById(productoId).orElseThrow();
        registrarMovimiento(actualizado, TipoMovimiento.DEVOLUCION_VENTA, cantidad, referencia, null, usuarioId);
        auditoria.registrar(usuarioId, "DEVOLVER_STOCK", "Producto id " + productoId + ", cant " + cantidad);
    }

    @Transactional
    public void ajustarPositivo(Long productoId, int cantidad, String motivo, Long usuarioId) {
        if (cantidad <= 0) {
            throw new ErrorNegocio("La cantidad debe ser mayor a cero.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ErrorNegocio("El motivo es obligatorio para el ajuste de inventario.");
        }
        stockRepository.incrementarStock(productoId, cantidad);
        Producto actualizado = productoRepository.findById(productoId).orElseThrow();
        registrarMovimiento(actualizado, TipoMovimiento.AJUSTE_POSITIVO, cantidad, null, motivo, usuarioId);
        auditoria.registrar(usuarioId, "AJUSTE_POSITIVO", "Producto id " + productoId + ", cant " + cantidad + ", motivo: " + motivo);
    }

    @Transactional
    public void ajustarNegativo(Long productoId, int cantidad, String motivo, Long usuarioId) {
        if (cantidad <= 0) {
            throw new ErrorNegocio("La cantidad debe ser mayor a cero.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ErrorNegocio("El motivo es obligatorio para el ajuste de inventario.");
        }
        int filas = stockRepository.descontarStock(productoId, cantidad);
        if (filas == 0) {
            Producto p = productoRepository.findById(productoId).orElseThrow();
            throw new ErrorNegocio("Stock insuficiente para el ajuste. Disponible: " + p.getStock() + ".");
        }
        Producto actualizado = productoRepository.findById(productoId).orElseThrow();
        registrarMovimiento(actualizado, TipoMovimiento.AJUSTE_NEGATIVO, cantidad, null, motivo, usuarioId);
        auditoria.registrar(usuarioId, "AJUSTE_NEGATIVO", "Producto id " + productoId + ", cant " + cantidad + ", motivo: " + motivo);
    }

    private void registrarMovimiento(Producto producto, TipoMovimiento tipo, int cantidad, String referencia, String motivo, Long usuarioId) {
        MovimientoInventario m = new MovimientoInventario();
        m.setProducto(producto);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setStockResultante(producto.getStock());
        m.setReferencia(referencia);
        m.setMotivo(motivo);
        m.setUsuarioId(usuarioId);
        m.setFecha(Instant.now());
        movimientoRepository.save(m);
    }
}