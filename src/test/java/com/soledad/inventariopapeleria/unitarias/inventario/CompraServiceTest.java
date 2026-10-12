package com.soledad.inventariopapeleria.unitarias.inventario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.soledad.inventariopapeleria.inventario.servicio.CompraService;
import com.soledad.inventariopapeleria.producto.dominio.Producto;
import com.soledad.inventariopapeleria.producto.repositorio.ProductoRepository;
import com.soledad.inventariopapeleria.proveedor.dominio.Proveedor;
import com.soledad.inventariopapeleria.proveedor.repositorio.ProveedorRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CompraService (RF-14, RF-15): RN-23 costo promedio ponderado, RN-20 kardex,
 * transaccional y rechazo de facturas repetidas.
 */
@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock private CompraRepository compraRepository;
    @Mock private DetalleCompraRepository detalleRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private StockRepository stockRepository;
    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private AuditoriaService auditoria;

    @InjectMocks private CompraService servicio;

    @Test
    void registraCompraActualizaCostoPromedioYGeneraMovimiento() {
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Distribuidora escolar");
        Compra compra = new Compra();
        compra.setProveedor(proveedor);

        Producto inicial = producto(1L, 10, new BigDecimal("1000"));
        Producto actualizado = producto(1L, 15, new BigDecimal("1067"));

        // stock 10 a costo 1000; entran 5 a 1200 -> (10*1000 + 5*1200)/15 = 1066.66 -> 1067 (RN-23)
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(compraRepository.existsByProveedorIdAndNumeroFactura(any(), anyString())).thenReturn(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(inicial), Optional.of(actualizado));
        when(compraRepository.save(any(Compra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(compraRepository.findById(any())).thenReturn(Optional.of(compra));
        when(detalleRepository.save(any(DetalleCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        CompraDto dto = servicio.registrar(new CompraRequest(1L, "F-001", null,
                List.of(new DetalleCompraRequest(1L, 5, new BigDecimal("1200")))), 42L);

        verify(stockRepository).incrementarStock(1L, 5);

        ArgumentCaptor<Producto> productoCaptor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(productoCaptor.capture());
        assertThat(productoCaptor.getValue().getCosto()).isEqualByComparingTo("1067");

        ArgumentCaptor<MovimientoInventario> movCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(movCaptor.capture());
        assertThat(movCaptor.getValue().getTipo()).isEqualTo(TipoMovimiento.ENTRADA_COMPRA);
        assertThat(movCaptor.getValue().getCantidad()).isEqualTo(5);
        assertThat(movCaptor.getValue().getStockResultante()).isEqualTo(15);
        assertThat(movCaptor.getValue().getReferencia()).contains("F-001");

        assertThat(dto.detalle()).hasSize(1);
        assertThat(dto.detalle().get(0).costoUnitario()).isEqualByComparingTo("1200");
    }

    @Test
    void rechazaFacturaRepetidaDelMismoProveedor() {
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Distribuidora escolar");
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(compraRepository.existsByProveedorIdAndNumeroFactura(any(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(compra(1L, "F-001"), 1L))
                .isInstanceOf(ErrorNegocio.class)
                .hasMessageContaining("Ya existe");
        verify(compraRepository, never()).save(any());
        verify(stockRepository, never()).incrementarStock(anyLong(), anyInt());
    }

    @Test
    void rechazaProductoInexistenteSinTocarStock() {
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Distribuidora escolar");
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(compraRepository.existsByProveedorIdAndNumeroFactura(any(), anyString())).thenReturn(false);
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrar(
                new CompraRequest(1L, "F-002", null,
                        List.of(new DetalleCompraRequest(99L, 3, new BigDecimal("500")))), 1L))
                .isInstanceOf(RecursoNoEncontrado.class)
                .hasMessageContaining("No existe el producto");
        verify(compraRepository, never()).save(any());
        verify(stockRepository, never()).incrementarStock(anyLong(), anyInt());
    }

    @Test
    void rechazaCompraSinLineas() {
        assertThatThrownBy(() -> servicio.registrar(
                new CompraRequest(1L, "F-003", null, List.of()), 1L))
                .isInstanceOf(ErrorNegocio.class)
                .hasMessageContaining("al menos un producto");
    }

    private CompraRequest compra(Long proveedorId, String factura) {
        return new CompraRequest(proveedorId, factura, null,
                List.of(new DetalleCompraRequest(1L, 1, new BigDecimal("1000"))));
    }

    private static Producto producto(Long id, Integer stock) {
        Producto p = mock(Producto.class);
        when(p.getId()).thenReturn(id);
        when(p.getStock()).thenReturn(stock);
        return p;
    }

    private static Producto producto(Long id, Integer stock, BigDecimal costo) {
        Producto p = producto(id, stock);
        when(p.getCosto()).thenReturn(costo);
        return p;
    }
}