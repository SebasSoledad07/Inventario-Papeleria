package com.soledad.inventariopapeleria.unitarias.codigo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.soledad.inventariopapeleria.codigo.GeneradorEtiqueta;
import com.soledad.inventariopapeleria.codigo.LectorCodigoFoto;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * RF-04: etiqueta imprimible con codigo, nombre y precio. El codigo de la
 * etiqueta debe poder volver a leerse con el lector de foto.
 */
class GeneradorEtiquetaTest {

    @Test
    void generaPngConCodigoLegible() throws Exception {
        byte[] png = GeneradorEtiqueta.generar("7701234567897", "Lapiz HB", "$ 1.200");

        assertThat(esPng(png)).isTrue();
        BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(png));
        var lectura = LectorCodigoFoto.decodificar(imagen);
        assertThat(lectura).isPresent();
        assertThat(lectura.get().codigo()).isEqualTo("7701234567897");
    }

    @Test
    void sinNombreNiPrecioDibujaElCodigo() {
        byte[] png = GeneradorEtiqueta.generar("96385074", null, null);
        assertThat(esPng(png)).isTrue();
    }

    @Test
    void codigoVacioLanzaExcepcion() {
        assertThatThrownBy(() -> GeneradorEtiqueta.generar("   ", "Lapiz", "$ 1.200"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private boolean esPng(byte[] bytes) {
        return bytes.length > 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G';
    }
}