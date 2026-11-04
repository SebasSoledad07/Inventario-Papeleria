package com.soledad.inventariopapeleria.venta.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.cliente.dominio.Cliente;
import com.soledad.inventariopapeleria.cliente.repositorio.ClienteRepository;
import com.soledad.inventariopapeleria.configuracion.servicio.ConfiguracionService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.inventario.servicio.InventarioService;
import com.soledad.inventariopapeleria.producto.dominio.Producto;
import com.soledad.inventariopapeleria.producto.dominio.TarifaIva;
import com.soledad.inventariopapeleria.producto.dominio.Unidad;
import com.soledad.inventariopapeleria.producto.repositorio.ProductoRepository;
import com.soledad.inventariopapeleria.usuario.dominio.Rol;
import com.soledad.inventariopapeleria.usuario.dominio.Usuario;
import com.soledad.inventariopapeleria.usuario.repositorio.UsuarioRepository;
import com.soledad.inventariopapeleria.venta.api.AnulacionRequest;
import com.soledad.inventariopapeleria.venta.api.ItemVentaRequest;
import com.soledad.inventariopapeleria.venta.api.PagoRequest;
import com.soledad.inventariopapeleria.venta.api.VentaDto;
import com.soledad.inventariopapeleria.venta.api.VentaRequest;
import com.soledad.inventariopapeleria.venta.dominio.Caja;
import com.soledad.inventariopapeleria.venta.dominio.DetalleVenta;
import com.soledad.inventariopapeleria.venta.dominio.MedioPago;
import com.soledad.inventariopapeleria.venta.dominio.PagoVenta;
import com.soledad.inventariopapeleria.venta.dominio.Venta;
import com.soledad.inventariopapeleria.venta.repositorio.CajaRepository;
import com.soledad.inventariopapeleria.venta.repositorio.DetalleVentaRepository;
import com.soledad.inventariopapeleria.venta.repositorio.PagoVentaRepository;
import com.soledad.inventariopapeleria.venta.repositorio.VentaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleRepository;
    private final PagoVentaRepository pagoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CajaRepository cajaRepository;
    private final InventarioService inventario;
    private final ConsecutivoService consecutivoService;
    private final ConfiguracionService configuracion;
    private final AuditoriaService auditoria;

    public VentaService(VentaRepository ventaRepository,
                       DetalleVentaRepository detalleRepository,
                       PagoVentaRepository pagoRepository,
                       ProductoRepository productoRepository,
                       ClienteRepository clienteRepository,
                       UsuarioRepository usuarioRepository,
                       CajaRepository cajaRepository,
                       InventarioService inventario,
                       ConsecutivoService consecutivoService,
                       ConfiguracionService configuracion,
                       AuditoriaService auditoria) {
        this.ventaRepository = ventaRepository;
        this.detalleRepository = detalleRepository;
        this.pagoRepository = pagoRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.cajaRepository = cajaRepository;
        this.inventario = inventario;
        this.consecutivoService = consecutivoService;
        this.configuracion = configuracion;
        this.auditoria = auditoria;
    }

    @Transactional
    public VentaDto confirmar(VentaRequest request, Long actorId) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new ErrorNegocio("La venta debe traer al menos un producto.");
        }
        if (request.pagos() == null || request.pagos().isEmpty()) {
            throw new ErrorNegocio("La venta debe traer al menos un pago.");
        }
        Usuario cajero = usuarioRepository.findById(actorId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe el usuario con id " + actorId + "."));

        Caja caja;
        if (request.cajaId() != null) {
            caja = cajaRepository.findById(request.cajaId())
                    .orElseThrow(() -> new RecursoNoEncontrado("No existe la caja con id " + request.cajaId() + "."));
            if (!caja.getUsuario().getId().equals(actorId)) {
                throw new ErrorNegocio("La caja no pertenece al cajero.");
            }
            if (!"ABIERTA".equals(caja.getEstado())) {
                throw new ErrorNegocio("La caja esta cerrada.");
            }
        } else {
            caja = cajaRepository.findFirstByUsuarioIdAndEstadoOrderByAperturaDesc(actorId, "ABIERTA")
                    .orElseThrow(() -> new ErrorNegocio("El cajero no tiene una caja abierta."));
        }

        List<LineaPreparada> lineas = new ArrayList<>();
        for (ItemVentaRequest item : request.items()) {
            if (item.cantidad() <= 0) {
                throw new ErrorNegocio("La cantidad debe ser mayor a cero.");
            }
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new RecursoNoEncontrado("No existe el producto con id " + item.productoId() + "."));
            if (!producto.isActivo()) {
                throw new ErrorNegocio("El producto '" + producto.getNombre() + "' esta inactivo.");
            }
            if (item.descuento() != null && item.descuento().signum() < 0) {
                throw new ErrorNegocio("El descuento no puede ser negativo.");
            }
            lineas.add(new LineaPreparada(producto, item.cantidad(), item.descuento()));
        }

        BigDecimal totalPagos = BigDecimal.ZERO;
        BigDecimal totalNoEfectivo = BigDecimal.ZERO;
        BigDecimal totalEfectivo = BigDecimal.ZERO;
        for (PagoRequest pago : request.pagos()) {
            MedioPago medio;
            try {
                medio = MedioPago.desde(pago.medio());
            } catch (IllegalArgumentException e) {
                throw new ErrorNegocio("Medio de pago no valido: " + pago.medio() + ".");
            }
            if (pago.valor() == null || pago.valor().signum() <= 0) {
                throw new ErrorNegocio("El valor del pago debe ser mayor a cero.");
            }
            totalPagos = totalPagos.add(pago.valor());
            if (medio == MedioPago.EFECTIVO) {
                totalEfectivo = totalEfectivo.add(pago.valor());
            } else {
                totalNoEfectivo = totalNoEfectivo.add(pago.valor());
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineaPreparada l : lineas) {
            subtotal = subtotal.add(l.producto.getPrecioVenta().multiply(BigDecimal.valueOf(l.cantidad)));
        }
        BigDecimal descuentoLineas = BigDecimal.ZERO;
        for (LineaPreparada l : lineas) {
            if (l.descuento != null) {
                descuentoLineas = descuentoLineas.add(l.descuento);
            }
        }
        BigDecimal descuentoGlobal = request.descuentoTotal() != null ? request.descuentoTotal() : BigDecimal.ZERO;
        if (descuentoGlobal.signum() < 0) {
            throw new ErrorNegocio("El descuento total no puede ser negativo.");
        }
        BigDecimal descuentoTotal = descuentoLineas.add(descuentoGlobal);

        BigDecimal limitePct = limiteDescuento();
        BigDecimal limiteValor = subtotal.multiply(limitePct).divide(BigDecimal.valueOf(100));
        if (descuentoTotal.compareTo(limiteValor) > 0) {
            if (request.descuentoAutorizadoPor() == null) {
                throw new ErrorNegocio("El descuento supera el " + limitePct + "% y requiere autorizacion de un administrador.");
            }
            Usuario autorizador = usuarioRepository.findById(request.descuentoAutorizadoPor())
                    .orElseThrow(() -> new RecursoNoEncontrado("No existe el usuario autorizador."));
            if (autorizador.getRol() != Rol.ADMIN || !autorizador.isActivo()) {
                throw new ErrorNegocio("El descuento debe ser autorizado por un administrador activo.");
            }
        }

        BigDecimal[] shares = prorratear(descuentoGlobal, lineas, subtotal);

        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal ivaTotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < lineas.size(); i++) {
            LineaPreparada l = lineas.get(i);
            BigDecimal precioLinea = l.producto.getPrecioVenta().multiply(BigDecimal.valueOf(l.cantidad));
            BigDecimal descuentoLinea = (l.descuento != null ? l.descuento : BigDecimal.ZERO).add(shares[i]);
            BigDecimal neto = precioLinea.subtract(descuentoLinea);
            if (neto.signum() < 0) {
                throw new ErrorNegocio("El descuento no puede superar el valor de la linea.");
            }
            TarifaIva tarifa = l.producto.getTarifaIva();
            CalculadoraVenta.Desglose desglose = CalculadoraVenta.calcularIvaIncluido(neto, tarifa.porcentaje());
            DetalleVenta d = new DetalleVenta();
            d.setProducto(l.producto);
            d.setCantidad(l.cantidad);
            d.setPrecioUnitario(l.producto.getPrecioVenta());
            d.setBase(desglose.base());
            d.setIva(desglose.iva());
            d.setDescuento(descuentoLinea);
            d.setTotalLinea(desglose.base().add(desglose.iva()));
            detalles.add(d);
            ivaTotal = ivaTotal.add(desglose.iva());
            total = total.add(d.getTotalLinea());
        }

        if (totalNoEfectivo.compareTo(total) > 0) {
            throw new ErrorNegocio("Los pagos con tarjeta/transferencia no pueden superar el total.");
        }
        if (totalPagos.compareTo(total) < 0) {
            throw new ErrorNegocio("La suma de los pagos es menor al total de la venta.");
        }
        BigDecimal cambio = totalPagos.subtract(total);
        if (cambio.compareTo(totalEfectivo) > 0) {
            cambio = totalEfectivo.subtract(total.subtract(totalNoEfectivo));
            if (cambio.signum() < 0) {
                cambio = BigDecimal.ZERO;
            }
        }

        for (LineaPreparada l : lineas) {
            if (l.producto.getUnidad() == Unidad.SERVICIO) {
                continue;
            }
            if (l.producto.getStock() < l.cantidad) {
                throw new ErrorNegocio("Stock insuficiente para '" + l.producto.getNombre()
                        + "'. Disponible: " + l.producto.getStock() + ", solicitado: " + l.cantidad + ".");
            }
        }

        Integer consecutivo = consecutivoService.siguienteVenta();

        Cliente cliente = null;
        if (request.clienteId() != null) {
            cliente = clienteRepository.findById(request.clienteId())
                    .orElseThrow(() -> new RecursoNoEncontrado("No existe el cliente con id " + request.clienteId() + "."));
        }

        Venta venta = new Venta();
        venta.setConsecutivo(consecutivo);
        venta.setCaja(caja);
        venta.setUsuario(cajero);
        venta.setCliente(cliente);
        venta.setFecha(Instant.now());
        venta.setEstado("CONFIRMADA");
        venta.setSubtotal(subtotal);
        venta.setDescuentoTotal(descuentoTotal);
        venta.setIvaTotal(ivaTotal);
        venta.setTotal(total);
        venta.setCambio(cambio);
        venta.setDescuentoAutorizadoPor(request.descuentoAutorizadoPor());
        Venta guardada = ventaRepository.save(venta);

        for (DetalleVenta d : detalles) {
            d.setVenta(guardada);
            detalleRepository.save(d);
        }
        for (PagoRequest p : request.pagos()) {
            PagoVenta pago = new PagoVenta();
            pago.setVenta(guardada);
            pago.setMedio(p.medio().trim().toUpperCase());
            pago.setValor(p.valor());
            pagoRepository.save(pago);
        }

        String referencia = "Venta #" + consecutivo;
        for (LineaPreparada l : lineas) {
            if (l.producto.getUnidad() == Unidad.SERVICIO) {
                continue;
            }
            inventario.descontar(l.producto.getId(), l.cantidad, referencia, actorId);
        }

        auditoria.registrar(actorId, "CONFIRMAR_VENTA", "Venta #" + consecutivo + ", total " + total);
        return VentaDto.desde(guardada, detalleRepository.findByVentaId(guardada.getId()),
                pagoRepository.findByVentaId(guardada.getId()));
    }

    @Transactional
    public VentaDto anular(Long ventaId, AnulacionRequest request, Long actorId) {
        Usuario actor = usuarioRepository.findById(actorId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe el usuario con id " + actorId + "."));
        if (actor.getRol() != Rol.ADMIN) {
            throw new ErrorNegocio("Solo un administrador puede anular ventas.");
        }
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la venta con id " + ventaId + "."));
        if ("ANULADA".equals(venta.getEstado())) {
            throw new ErrorNegocio("La venta ya esta anulada.");
        }
        // PENDIENTE-DECISION-2: solo se anula si la caja esta abierta
        if (!"ABIERTA".equals(venta.getCaja().getEstado())) {
            throw new ErrorNegocio("No se puede anular una venta cuya caja ya esta cerrada.");
        }
        String motivo = request.motivo().trim();
        String referencia = "Anulacion venta #" + venta.getConsecutivo();
        for (DetalleVenta d : detalleRepository.findByVentaId(venta.getId())) {
            if (d.getProducto().getUnidad() == Unidad.SERVICIO) {
                continue;
            }
            inventario.devolver(d.getProducto().getId(), d.getCantidad(), referencia, actorId);
        }
        venta.setEstado("ANULADA");
        venta.setMotivoAnulacion(motivo);
        Venta guardada = ventaRepository.save(venta);
        auditoria.registrar(actorId, "ANULAR_VENTA", "Venta #" + venta.getConsecutivo() + ", motivo: " + motivo);
        return VentaDto.desde(guardada, detalleRepository.findByVentaId(guardada.getId()),
                pagoRepository.findByVentaId(guardada.getId()));
    }

    @Transactional(readOnly = true)
    public VentaDto consultar(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la venta con id " + ventaId + "."));
        return VentaDto.desde(venta, detalleRepository.findByVentaId(venta.getId()),
                pagoRepository.findByVentaId(venta.getId()));
    }

    @Transactional(readOnly = true)
    public List<VentaDto> buscar(Instant desde, Instant hasta, Long cajeroId, Long clienteId, Integer consecutivo) {
        return ventaRepository.buscar(desde, hasta, cajeroId, clienteId, consecutivo).stream()
                .map(v -> VentaDto.desde(v, detalleRepository.findByVentaId(v.getId()),
                        pagoRepository.findByVentaId(v.getId())))
                .toList();
    }

    private BigDecimal limiteDescuento() {
        String valor = configuracion.listar().stream()
                .filter(c -> "limite_descuento".equals(c.clave()))
                .map(c -> c.valor())
                .findFirst()
                .orElse("10");
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            return BigDecimal.TEN;
        }
    }

    // PENDIENTE-DECISION-1: prorrateo del descuento global entre lineas
    private BigDecimal[] prorratear(BigDecimal descuentoGlobal, List<LineaPreparada> lineas, BigDecimal subtotal) {
        BigDecimal[] shares = new BigDecimal[lineas.size()];
        for (int i = 0; i < shares.length; i++) {
            shares[i] = BigDecimal.ZERO;
        }
        if (descuentoGlobal == null || descuentoGlobal.signum() <= 0 || subtotal.signum() <= 0) {
            return shares;
        }
        BigDecimal acumulado = BigDecimal.ZERO;
        for (int i = 0; i < lineas.size(); i++) {
            LineaPreparada l = lineas.get(i);
            BigDecimal precioLinea = l.producto.getPrecioVenta().multiply(BigDecimal.valueOf(l.cantidad));
            if (i == lineas.size() - 1) {
                shares[i] = descuentoGlobal.subtract(acumulado);
            } else {
                BigDecimal share = descuentoGlobal.multiply(precioLinea).divide(subtotal, 0, java.math.RoundingMode.HALF_UP);
                shares[i] = share;
                acumulado = acumulado.add(share);
            }
        }
        return shares;
    }

    private record LineaPreparada(Producto producto, int cantidad, BigDecimal descuento) {
    }
}