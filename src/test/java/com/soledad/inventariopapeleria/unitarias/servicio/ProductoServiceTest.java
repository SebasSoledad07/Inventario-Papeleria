package com.soledad.inventariopapeleria.unitarias.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.producto.api.ProductoRequest;
import com.soledad.inventariopapeleria.producto.dominio.Categoria;
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
}