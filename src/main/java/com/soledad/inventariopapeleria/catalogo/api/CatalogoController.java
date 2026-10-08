package com.soledad.inventariopapeleria.catalogo.api;

import com.soledad.inventariopapeleria.catalogo.CatalogoExternoClient;
import com.soledad.inventariopapeleria.codigo.NormalizadorCodigo;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CatalogoExternoClient client;

    public CatalogoController(CatalogoExternoClient client) {
        this.client = client;
    }

    @GetMapping("/codigo/{codigo}")
    public SugerenciaProducto sugerir(@PathVariable String codigo) {
        String c = NormalizadorCodigo.normalizar(codigo);
        Optional<SugerenciaProducto> s = client.buscarPorCodigo(c);
        // RN-16: si no hay datos, devolver vacio; frontend debe revisar
        return s.orElse(new SugerenciaProducto(c, null, null, null));
    }
}