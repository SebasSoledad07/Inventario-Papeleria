package com.soledad.inventariopapeleria.unitarias.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.producto.api.ProductoRequest;
import com.soledad.inventariopapeleria.producto.dominio.Categoria;
import com.soledad.inventariopapeleria.producto.dominio.HistorialPrecios;
import com.soledad.inventariopapeleria.producto.dominio.Producto;
import com.soledad.inventariopapeleria.producto.dominio.TarifaIva;
import com.soledad.inventariopapeleria.producto.dominio.Unidad;
import com.soledad.inventariopapeleria.producto.repositorio.CategoriaRepository;
import com.soledad.inventariopapeleria.producto.repositorio.HistorialPreciosRepository;
import com.soledad.inventariopapeleria.producto.repositorio.ProductoRepository;
import com.soledad.inventariopapeleria.producto.servicio.ProductoService;
import com.soledad.inventariopapeleria.proveedor.repositorio.ProveedorRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private HistorialPreciosRepository historialRepository;
    @Mock private AuditoriaService auditoria;

    @InjectMocks private ProductoService servicio;

    @Test
    void creaProductoSinCodigoGeneraInternoEan13() {
        Categoria c = new Categoria();
        c.setNombre("Utiles");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(c));
        when(productoRepository.existsByCodigoBarras(any(String.class))).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new ProductoRequest(null, "Lapiz", "Faber", 1L, null,
                Unidad.UNIDAD.name(), TarifaIva.IVA_19.valor(),
                new BigDecimal("1000"), new BigDecimal("700"), 10, 5);
        var dto = servicio.crear(req, 1L);
        assertThat(dto.codigoBarras()).isNotNull();
        assertThat(dto.codigoBarras().length()).isEqualTo(13);
        assertThat(dto.codigoBarras()).startsWith("2");
    }

    @Test
    void actualizarRegistraHistorialDePrecioYNoTocaElStock() {
        Producto p = productoActivo();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria("Utiles")));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new ProductoRequest(null, "Lapiz HB", "Faber", 2L, null,
                Unidad.UNIDAD.name(), TarifaIva.IVA_19.valor(),
                new BigDecimal("1200"), new BigDecimal("700"), 999, 3);
        var dto = servicio.actualizar(1L, req, 5L);

        assertThat(dto.nombre()).isEqualTo("Lapiz HB");
        assertThat(dto.precioVenta()).isEqualByComparingTo("1200");
        // RN-20: el stock solo lo modifica inventario.
        assertThat(dto.stock()).isEqualTo(10);
        ArgumentCaptor<HistorialPrecios> captor = ArgumentCaptor.forClass(HistorialPrecios.class);
        verify(historialRepository).save(captor.capture());
        assertThat(captor.getValue().getPrecioAnterior()).isEqualByComparingTo("1000");
        assertThat(captor.getValue().getPrecioNuevo()).isEqualByComparingTo("1200");
        assertThat(captor.getValue().getUsuarioId()).isEqualTo(5L);
    }

    @Test
    void actualizarSinCambioDePrecioNoRegistraHistorial() {
        Producto p = productoActivo();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria("Utiles")));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new ProductoRequest(null, "Lapiz", "Faber", 2L, null,
                Unidad.UNIDAD.name(), TarifaIva.IVA_19.valor(),
                new BigDecimal("1000"), new BigDecimal("700"), 0, 3);
        servicio.actualizar(1L, req, 5L);

        verify(historialRepository, never()).save(any());
    }

    @Test
    void actualizarConCodigoDeOtroProductoLanzaError() {
        Producto p = productoActivo();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.existsByCodigoBarras("7777777777777")).thenReturn(true);

        var req = new ProductoRequest("7777777777777", "Lapiz", "Faber", 2L, null,
                Unidad.UNIDAD.name(), TarifaIva.IVA_19.valor(),
                new BigDecimal("1000"), new BigDecimal("700"), 0, 3);

        assertThatThrownBy(() -> servicio.actualizar(1L, req, 5L))
                .isInstanceOf(ErrorNegocio.class);
    }

    @Test
    void inactivarMarcaElProductoInactivo() {
        Producto p = productoActivo();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = servicio.inactivar(1L, 5L);

        assertThat(dto.activo()).isFalse();
        verify(auditoria).registrar(5L, "INACTIVAR_PRODUCTO", "Producto id 1");
    }

    @Test
    void activarMarcaElProductoActivo() {
        Producto p = productoActivo();
        p.setActivo(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = servicio.activar(1L, 5L);

        assertThat(dto.activo()).isTrue();
        verify(auditoria).registrar(5L, "ACTIVAR_PRODUCTO", "Producto id 1");
    }

    private Producto productoActivo() {
        Producto p = new Producto();
        p.setCodigoBarras("2000000000001");
        p.setNombre("Lapiz");
        p.setPrecioVenta(new BigDecimal("1000"));
        p.setCosto(new BigDecimal("700"));
        p.setStock(10);
        p.setStockMinimo(5);
        p.setActivo(true);
        p.setUnidad(Unidad.UNIDAD);
        p.setTarifaIva(TarifaIva.IVA_19);
        return p;
    }

    private Categoria categoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        return c;
    }
}