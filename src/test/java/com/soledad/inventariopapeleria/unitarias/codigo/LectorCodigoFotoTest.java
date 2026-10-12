package com.soledad.inventariopapeleria.unitarias.codigo;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.UPCEReader;
import com.soledad.inventariopapeleria.codigo.LectorCodigoFoto;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * RF-11: decodificacion de codigos desde una imagen.
 * RF-12: formatos soportados EAN-13, EAN-8, UPC-A, UPC-E, Code 128 y Code 39.
 */
class LectorCodigoFotoTest {

    @ParameterizedTest
    @MethodSource("codigosValidos")
    void decodificaCadaFormatoSoportado(String codigo, BarcodeFormat formato) throws Exception {
        BufferedImage imagen = dibujar(codigo, formato);
        var lectura = LectorCodigoFoto.decodificar(imagen);
        assertThat(lectura).isPresent();
        assertThat(lectura.get().codigo()).isEqualTo(codigo);
        assertThat(lectura.get().formato()).isEqualTo(formato);
    }

    static Stream<Arguments> codigosValidos() {
        return Stream.of(
                Arguments.of("7701234567897", BarcodeFormat.EAN_13),
                Arguments.of("96385074", BarcodeFormat.EAN_8),
                Arguments.of("036000291452", BarcodeFormat.UPC_A),
                Arguments.of(upceConDigito("0421000"), BarcodeFormat.UPC_E),
                Arguments.of("LAPIZ-770", BarcodeFormat.CODE_128),
                Arguments.of("REG-42", BarcodeFormat.CODE_39));
    }

    @Test
    void imagenSinCodigoDevuelveVacio() {
        BufferedImage enBlanco = new BufferedImage(300, 100, BufferedImage.TYPE_INT_RGB);
        assertThat(LectorCodigoFoto.decodificar(enBlanco)).isEmpty();
    }

    @Test
    void imagenNullDevuelveVacio() {
        assertThat(LectorCodigoFoto.decodificar(null)).isEmpty();
    }

    private static BufferedImage dibujar(String codigo, BarcodeFormat formato) throws Exception {
        BitMatrix matriz = new MultiFormatWriter().encode(codigo, formato, 320, 80);
        return MatrixToImageWriter.toBufferedImage(matriz);
    }

    private static String upceConDigito(String base7) {
        // La conversion UPC-E -> UPC-A la hace ZXing; el digito de control se
        // calcula con la regla estandar de UPC-A sobre los 11 primeros digitos.
        String upca = UPCEReader.convertUPCEtoUPCA(base7 + "0");
        int suma = 0;
        for (int i = 0; i < 11; i++) {
            int d = Character.getNumericValue(upca.charAt(i));
            suma += (i % 2 == 0) ? d * 3 : d;
        }
        return base7 + Character.forDigit((10 - (suma % 10)) % 10, 10);
    }
}