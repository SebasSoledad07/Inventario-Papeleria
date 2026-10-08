package com.soledad.inventariopapeleria.producto.repositorio;

import com.soledad.inventariopapeleria.producto.dominio.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    @Query(value = "SELECT * FROM productos p WHERE activo "
            + "AND (lower(f_unaccent(p.nombre)) LIKE '%' || lower(f_unaccent(:texto)) || '%' "
            + "OR lower(f_unaccent(p.nombre)) % lower(f_unaccent(:texto))) "
            + "ORDER BY similarity(lower(f_unaccent(p.nombre)), lower(f_unaccent(:texto))) DESC "
            + "LIMIT 20", nativeQuery = true)
    List<Producto> buscarPorNombreSimilar(@Param("texto") String texto);

    List<Producto> findAllByActivoTrueOrderByNombreAsc();
}