package com.soledad.inventariopapeleria.producto.servicio;

import com.soledad.inventariopapeleria.auditoria.servicio.AuditoriaService;
import com.soledad.inventariopapeleria.codigo.GeneradorCodigoInterno;
import com.soledad.inventariopapeleria.codigo.NormalizadorCodigo;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import com.soledad.inventariopapeleria.error.RecursoNoEncontrado;
import com.soledad.inventariopapeleria.producto.api.ProductoDto;
import com.soledad.inventariopapeleria.producto.api.ProductoRequest;
import com.soledad.inventariopapeleria.producto.dominio.HistorialPrecios;
import com.soledad.inventariopapeleria.producto.dominio.Producto;
import com.soledad.inventariopapeleria.producto.dominio.TarifaIva;
import com.soledad.inventariopapeleria.producto.dominio.Unidad;
import com.soledad.inventariopapeleria.producto.repositorio.CategoriaRepository;
import com.soledad.inventariopapeleria.producto.repositorio.HistorialPreciosRepository;
import com.soledad.inventariopapeleria.producto.repositorio.ProductoRepository;
import com.soledad.inventariopapeleria.proveedor.repositorio.ProveedorRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final HistorialPreciosRepository historialRepository;
    private final AuditoriaService auditoria;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           ProveedorRepository proveedorRepository,
                           HistorialPreciosRepository historialRepository,
                           AuditoriaService auditoria) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedorRepository = proveedorRepository;
        this.historialRepository = historialRepository;
        this.auditoria = auditoria;
    }

    @Transactional
    public ProductoDto crear(ProductoRequest req, Long actorId) {
        String codigo = resolverCodigo(req.codigoBarras());
        if (productoRepository.existsByCodigoBarras(codigo)) {
            throw new ErrorNegocio("Ya existe un producto con el codigo de barras '" + codigo + "'.");
        }
        Producto p = new Producto();
        p.setCodigoBarras(codigo);
        p.setNombre(req.nombre().trim());
        p.setMarca(req.marca() != null ? req.marca().trim() : null);
        p.setCategoria(categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la categoria con id " + req.categoriaId() + ".")));
        if (req.proveedorId() != null) {
            p.setProveedor(proveedorRepository.findById(req.proveedorId())
                    .orElseThrow(() -> new RecursoNoEncontrado("No existe el proveedor con id " + req.proveedorId() + ".")));
        }
        p.setUnidad(Unidad.valueOf(req.unidad().trim().toUpperCase()));
        p.setTarifaIva(TarifaIva.desde(req.tarifaIva()));
        p.setPrecioVenta(req.precioVenta());
        p.setCosto(req.costo());
        p.setStock(req.stockInicial() != null ? req.stockInicial() : 0);
        p.setStockMinimo(req.stockMinimo());
        p.setActivo(true);
        Instant ahora = Instant.now();
        p.setCreadoEn(ahora);
        p.setActualizadoEn(ahora);
        Producto guardado = productoRepository.save(p);
        auditoria.registrar(actorId, "CREAR_PRODUCTO", "Producto: " + guardado.getCodigoBarras());
        return ProductoDto.desde(guardado);
    }

    @Transactional(readOnly = true)
    public ProductoDto buscarPorCodigo(String codigo) {
        String c = NormalizadorCodigo.normalizar(codigo);
        return productoRepository.findByCodigoBarras(c).map(ProductoDto::desde)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe un producto con el codigo '" + c + "'."));
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> buscarPorNombre(String texto) {
        return productoRepository.buscarPorNombreSimilar(texto).stream().map(ProductoDto::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> listarActivos() {
        return productoRepository.findAllByActivoTrueOrderByNombreAsc().stream().map(ProductoDto::desde).toList();
    }

    @Transactional
    public ProductoDto actualizarPrecio(Long id, BigDecimal nuevoPrecio, Long actorId) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe el producto con id " + id + "."));
        BigDecimal anterior = p.getPrecioVenta();
        if (anterior.compareTo(nuevoPrecio) != 0) {
            p.setPrecioVenta(nuevoPrecio);
            p.setActualizadoEn(Instant.now());
            HistorialPrecios h = new HistorialPrecios();
            h.setProducto(p);
            h.setPrecioAnterior(anterior);
            h.setPrecioNuevo(nuevoPrecio);
            h.setUsuarioId(actorId);
            h.setFecha(Instant.now());
            historialRepository.save(h);
            auditoria.registrar(actorId, "ACTUALIZAR_PRECIO", "Producto id " + id);
        }
        return ProductoDto.desde(p);
    }

    private String resolverCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            // RN-15: generar codigo interno EAN-13
            return GeneradorCodigoInterno.generar(String.valueOf(System.currentTimeMillis()));
        }
        String c = NormalizadorCodigo.normalizar(codigo);
        if (c.length() != 13 && c.length() != 12 && c.length() != 8) {
            // Si no cumple formato tipico, tratarlo como interno? pero normalizar
        }
        return c;
    }
}