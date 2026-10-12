package com.soledad.inventariopapeleria.inventario.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.inventario.api.CompraDto;
import com.soledad.inventariopapeleria.inventario.api.CompraRequest;
import com.soledad.inventariopapeleria.inventario.api.DetalleCompraRequest;
import com.soledad.inventariopapeleria.inventario.dominio.Compra;
import com.soledad.inventariopapeleria.inventario.dominio.DetalleCompra;
import com.soledad.inventariopapeleria.inventario.dominio.MovimientoInventario;
import com.soledad.inventariopapeleria.inventario.dominio.TipoMovimiento;
import com.soledad.inventariopapeleria.inventario.repositorio.CompraRepository;
import com.soledad.inventariopapeleria.inventario.repositorio.DetalleCompraRepository;
import com.soledad.inventariopapeleria.inventario.repositorio.MovimientoInventarioRepository;
import com.soledad.inventariopapeleria.inventario.repositorio.StockRepository;
import com.soledad.inventariopapeleria.producto.dominio.Producto;
import com.soledad.inventariopapeleria.producto.repositorio.ProductoRepository;
import com.soledad.inventariopapeleria.proveedor.dominio.Proveedor;
import com.soledad.inventariopapeleria.proveedor.repositorio.ProveedorRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Entradas de mercancia (RF-14, RF-15). Junto a {@link InventarioService}
 * es la unica puerta que incrementa stock (RN-20): actualiza el costo
 * promedio ponderado (RN-23) y deja el kardex. Todo en una sola
 * transaccion (RNF-08).
 */
@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final StockRepository stockRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AuditoriaService auditoria;

    public CompraService(CompraRepository compraRepository,
                         DetalleCompraRepository detalleRepository,
                         ProductoRepository productoRepository,
                         ProveedorRepository proveedorRepository,
                         StockRepository stockRepository,
                         MovimientoInventarioRepository movimientoRepository,
                         AuditoriaService auditoria) {
        this.compraRepository = compraRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.stockRepository = stockRepository;
        this.movimientoRepository = movimientoRepository;
        this.auditoria = auditoria;
    }

    /**
     * Registra una compra: incrementa el stock de cada linea, recalcula el
     * costo promedio y registra los movimientos {@code ENTRADA_COMPRA}.
     * Si algo falla, no se guarda nada (atomics).
     */
    @Transactional
    public CompraDto registrar(CompraRequest request, Long usuarioId) {
        if (request.detalle() == null || request.detalle().isEmpty()) {
            throw new ErrorNegocio("La compra debe traer al menos un producto.");
        }
        Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No existe el proveedor con id " + request.proveedorId() + "."));
        String factura = request.numeroFactura().trim();
        if (compraRepository.existsByProveedorIdAndNumeroFactura(proveedor.getId(), factura)) {
            throw new ErrorNegocio("Ya existe una compra del proveedor con la factura '" + factura + "'.");
        }
        List<LineaPreparada> lineas = request.detalle().stream().map(this::prepararLinea).toList();

        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setNumeroFactura(factura);
        compra.setFecha(Instant.now());
        compra.setUsuarioId(usuarioId);
        compra.setObservacion(request.observacion());
        Compra guardada = compraRepository.save(compra);

        List<DetalleCompra> detalles = new ArrayList<>(lineas.size());
        for (LineaPreparada linea : lineas) {
            detalles.add(aplicarEntrada(guardada.getId(), factura, linea, usuarioId));
        }
        auditoria.registrar(usuarioId, "REGISTRAR_COMPRA",
                "Factura " + factura + ", proveedor " + proveedor.getId() + ", lineas " + detalles.size());
        return CompraDto.desde(guardada, detalles);
    }

    @Transactional(readOnly = true)
    public CompraDto consultar(Long compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la compra con id " + compraId + "."));
        return CompraDto.desde(compra, detalleRepository.findByCompraId(compraId));
    }

    private LineaPreparada prepararLinea(DetalleCompraRequest detalle) {
        if (detalle.cantidad() <= 0) {
            throw new ErrorNegocio("La cantidad debe ser mayor a cero.");
        }
        if (detalle.costoUnitario() == null || detalle.costoUnitario().signum() < 0) {
            throw new ErrorNegocio("El costo unitario no puede ser negativo.");
        }
        Producto producto = productoRepository.findById(detalle.productoId())
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No existe el producto con id " + detalle.productoId() + "."));
        return new LineaPreparada(producto, detalle.cantidad(), detalle.costoUnitario());
    }

    /**
     * RN-20 y RN-23: incrementa el stock, recalcula el costo promedio
     * ponderado con el stock y costo anteriores y registra el movimiento.
     */
    private DetalleCompra aplicarEntrada(Long compraId, String factura, LineaPreparada linea, Long usuarioId) {
        BigDecimal nuevoCosto = CalculadoraCostoPromedio.calcular(
                linea.producto().getStock(), linea.producto().getCosto(), linea.cantidad(), linea.costoUnitario());

        stockRepository.incrementarStock(linea.producto().getId(), linea.cantidad());

        Producto producto = productoRepository.findById(linea.producto().getId())
                .orElseThrow(() -> new RecursoNoEncontrado(
                        "No existe el producto con id " + linea.producto().getId() + "."));
        producto.setCosto(nuevoCosto);
        producto.setActualizadoEn(Instant.now());
        productoRepository.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipo(TipoMovimiento.ENTRADA_COMPRA);
        movimiento.setCantidad(linea.cantidad());
        movimiento.setStockResultante(producto.getStock());
        movimiento.setReferencia("Compra factura " + factura);
        movimiento.setUsuarioId(usuarioId);
        movimiento.setFecha(Instant.now());
        movimientoRepository.save(movimiento);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setCompra(compraRepository.findById(compraId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la compra con id " + compraId + ".")));
        detalle.setProducto(producto);
        detalle.setCantidad(linea.cantidad());
        detalle.setCostoUnitario(linea.costoUnitario());
        return detalleRepository.save(detalle);
    }

    private record LineaPreparada(Producto producto, int cantidad, BigDecimal costoUnitario) {
    }
}