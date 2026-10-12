package com.soledad.inventariopapeleria.codigo.api;

import com.soledad.inventariopapeleria.codigo.GeneradorEtiqueta;
import com.soledad.inventariopapeleria.codigo.LectorCodigoFoto;
import com.soledad.inventariopapeleria.error.ErrorNegocio;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Codigos de barras (Fase 2): decodificacion de una foto enviada por parte del
 * navegador (RF-11) e impresion de etiquetas (RF-04).
 */
@RestController
@RequestMapping("/api/codigos")
public class CodigoController {

    /**
     * Recibe una imagen (foto o recorte) y devuelve el codigo de barras leido
     * con su formato (RF-11, RF-12).
     */
    @PostMapping(value = "/decodificar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LectorCodigoFoto.LecturaCodigo decodificar(@RequestParam("archivo") MultipartFile archivo) {
        return LectorCodigoFoto.decodificar(leerImagen(archivo))
                .orElseThrow(() -> new ErrorNegocio("No se pudo leer ningun codigo de barras de la imagen."));
    }

    /**
     * Genera la etiqueta imprimible con el codigo, el nombre y el precio del
     * producto (RF-04). Devuelve el PNG directamente.
     */
    @GetMapping(value = "/{codigo}/etiqueta", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> etiqueta(@PathVariable String codigo,
                                           @RequestParam(required = false) String nombre,
                                           @RequestParam(required = false) String precio) {
        byte[] png = GeneradorEtiqueta.generar(codigo, nombre, precio);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"etiqueta.png\"")
                .body(png);
    }

    private BufferedImage leerImagen(MultipartFile archivo) {
        try {
            BufferedImage imagen = ImageIO.read(archivo.getInputStream());
            if (imagen == null) {
                throw new ErrorNegocio("El archivo enviado no es una imagen valida.");
            }
            return imagen;
        } catch (IOException ex) {
            throw new ErrorNegocio("No se pudo leer el archivo enviado.", ex);
        }
    }
}