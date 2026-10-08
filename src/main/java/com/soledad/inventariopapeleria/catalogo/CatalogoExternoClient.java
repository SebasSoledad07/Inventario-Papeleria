package com.soledad.inventariopapeleria.catalogo;

import com.soledad.inventariopapeleria.catalogo.api.SugerenciaProducto;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class CatalogoExternoClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogoExternoClient.class);
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public Optional<SugerenciaProducto> buscarPorCodigo(String codigo) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(BASE_URL)
                    .pathSegment("product", codigo + ".json")
                    .build().toUri();
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != HttpStatus.OK.value()) {
                log.debug("Catalogo externo devolvio {} para codigo {}", resp.statusCode(), codigo);
                return Optional.empty();
            }
            // Parse minimal: asumimos formato simple; para version inicial devolvemos vacio si no parseable
            String body = resp.body();
            if (body == null || !body.contains("\"status\":1")) {
                return Optional.empty();
            }
            return Optional.empty(); // minimal; implementar parse completo si necesario mas adelante
        } catch (IOException | InterruptedException e) {
            log.warn("Falla al consultar catalogo externo para codigo {}: {}", codigo, e.getMessage());
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}