package com.soledad.inventariopapeleria.unitarias.inventario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.inventario.api.ConteoFisicoRequest;
import com.soledad.inventariopapeleria.inventario.api.LineaConteo;
import com.soledad.inventariopapeleria.inventario.servicio.ConteoFisicoService;
import com.soledad.inventariopapeleria.inventario.servicio.InventarioService;
import com.soledad.inventariopapeleria.producto.dominio.Producto;
import com.soledad.inventariopapeleria.producto.repositorio.ProductoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Conteo fisico (RF-18, RN-46): ajustes solo cuando hay diferencia, en un solo
 * lote y con el motivo obligatorio (RN-22).
 */
@ExtendWith(MockitoExtension.class)
class ConteoFisicoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioService inventario;
    @Mock private AuditoriaService auditoria;

    @InjectMocks private ConteoFisicoService servicio;

    @Test
    void generaAjustesSoloDondeHayDiferencia() {
        Producto p1 = producto(1L, 10);
        Producto p2 = producto(2L, 10);
        Producto p3 = mock(Producto.class);
        when(p3.getStock()).thenReturn(10);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(p2));
        when(productoRepository.findById(3L)).thenReturn(Optional.of(p3));

        var resultado = servicio.ejecutar(new ConteoFisicoRequest(List.of(
                new LineaConteo(1L, 12),   // sobra 2
                new LineaConteo(2L, 7),    // faltan 3
                new LineaConteo(3L, 10))), // sin diferencia
                25L);

        verify(inventario).ajustarPositivo(1L, 2, "conteo físico", 25L);
        verify(inventario).ajustarNegativo(2L, 3, "conteo físico", 25L);
        verify(inventario, never()).ajustarPositivo(3L, 0, "conteo físico", 25L);
        assertThat(resultado.ajustesAplicados()).isEqualTo(2);
        verify(auditoria).registrar(25L, "CONTEO_FISICO", "2 ajuste(s) aplicado(s)");
    }

    @Test
    void productoInexistenteFallaSinAjustarNada() {
        when(productoRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.ejecutar(
                new ConteoFisicoRequest(List.of(new LineaConteo(8L, 2))), 1L))
                .isInstanceOf(RecursoNoEncontrado.class);
        verify(inventario, never()).ajustarPositivo(anyLong(), anyInt(), anyString(), anyLong());
        verify(inventario, never()).ajustarNegativo(anyLong(), anyInt(), anyString(), anyLong());
    }

    private static Producto producto(Long id, Integer stock) {
        Producto p = mock(Producto.class);
        when(p.getId()).thenReturn(id);
        when(p.getStock()).thenReturn(stock);
        return p;
    }
}