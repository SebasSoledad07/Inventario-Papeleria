package com.soledad.inventariopapeleria.codigo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lector de codigos de barras desde una imagen (fotografia, RF-11). Soportados
 * EAN-13, EAN-8, UPC-A, UPC-E, Code 128 y Code 39 (RF-12). Si no encuentra
 * ningun codigo devuelve {@link Optional#empty()} para que el frontend decida
 * como informar al usuario.
 */
public final class LectorCodigoFoto {

    private static final List<BarcodeFormat> FORMATOS_SOPORTADOS = List.of(
            BarcodeFormat.EAN_13, BarcodeFormat.EAN_8, BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E, BarcodeFormat.CODE_128, BarcodeFormat.CODE_39);

    private LectorCodigoFoto() {
    }

    /**
     * Intenta decodificar un codigo de barras de la imagen. Devuelve el valor
     * leido y el formato detectado, o vacio si no se encuentra ningun codigo
     * soportado (RF-12).
     */
    public static Optional<LecturaCodigo> decodificar(BufferedImage imagen) {
        if (imagen == null) {
            return Optional.empty();
        }
        Map<DecodeHintType, Object> pistas = new EnumMap<>(DecodeHintType.class);
        pistas.put(DecodeHintType.POSSIBLE_FORMATS, FORMATOS_SOPORTADOS);
        pistas.put(DecodeHintType.TRY_HARDER, true);
        BinaryBitmap mapaBits = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(imagen)));
        try {
            Result resultado = new MultiFormatReader().decode(mapaBits, pistas);
            return Optional.of(new LecturaCodigo(resultado.getText(), resultado.getBarcodeFormat()));
        } catch (ReaderException ex) {
            return Optional.empty();
        }
    }

    /** Codigo leido y el formato en el que viene (RF-12). */
    public record LecturaCodigo(String codigo, BarcodeFormat formato) {
    }
}