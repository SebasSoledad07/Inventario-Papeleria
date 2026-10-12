package com.soledad.inventariopapeleria.codigo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Genera la etiqueta imprimible de un producto (RF-04): codigo de barras con
 * el nombre y el precio. El codigo se normaliza (RN-14); si tiene 8 digitos se
 * imprime EAN-8, 13 EAN-13 y cualquier otra longitud Code 128.
 */
public final class GeneradorEtiqueta {

    private static final int ANCHO_BARRA_EAN13 = 300;
    private static final int ANCHO_BARRA_CODE128 = 400;
    private static final int ANCHO_MINIMO = 480;
    private static final int ALTO_BARRA = 90;
    private static final int ALTO_TEXTO = 110;

    private GeneradorEtiqueta() {
    }

    /**
     * Compone la etiqueta en PNG y la devuelve como bytes. {@code nombre} y
     * {@code precio} son opcionales y se dibujan centrados bajo el codigo.
     *
     * @throws IllegalArgumentException si el codigo es nulo o vacio
     */
    public static byte[] generar(String codigo, String nombre, String precio) {
        String codigoNormalizado = NormalizadorCodigo.normalizar(codigo);
        if (codigoNormalizado.isEmpty()) {
            throw new IllegalArgumentException("El codigo no puede estar vacio.");
        }
        BarcodeFormat formato = formatoDe(codigoNormalizado);
        BitMatrix matriz;
        try {
            matriz = new MultiFormatWriter().encode(codigoNormalizado, formato, anchoBarra(formato), ALTO_BARRA);
        } catch (WriterException ex) {
            throw new IllegalArgumentException("No se pudo dibujar el codigo " + codigoNormalizado + ".", ex);
        }
        return comprimirPng(componer(matriz, codigoNormalizado, nombre, precio));
    }

    private static BufferedImage componer(BitMatrix matriz, String codigo, String nombre, String precio) {
        int ancho = Math.max(ANCHO_MINIMO, matriz.getWidth());
        int alto = matriz.getHeight() + ALTO_TEXTO;
        BufferedImage etiqueta = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = etiqueta.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ancho, alto);
        g.drawImage(MatrixToImageWriter.toBufferedImage(matriz), (ancho - matriz.getWidth()) / 2, 0, null);
        g.setColor(Color.BLACK);
        if (nombre != null && !nombre.isBlank()) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            centrar(g, acortar(nombre), ancho, 60);
        }
        if (precio != null && !precio.isBlank()) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 38));
            centrar(g, precio, ancho, 100);
        }
        if ((nombre == null || nombre.isBlank()) && (precio == null || precio.isBlank())) {
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
            centrar(g, acortar(codigo), ancho, 100);
        }
        g.dispose();
        return etiqueta;
    }

    private static void centrar(Graphics2D g, String texto, int ancho, int y) {
        FontMetrics metricas = g.getFontMetrics();
        g.drawString(texto, (ancho - metricas.stringWidth(texto)) / 2, y);
    }

    private static String acortar(String texto) {
        return texto.length() <= 40 ? texto : texto.substring(0, 38) + "...";
    }

    private static BarcodeFormat formatoDe(String codigo) {
        if (codigo.length() == 8) {
            return BarcodeFormat.EAN_8;
        }
        if (codigo.length() == 13) {
            return BarcodeFormat.EAN_13;
        }
        return BarcodeFormat.CODE_128;
    }

    private static int anchoBarra(BarcodeFormat formato) {
        return formato == BarcodeFormat.CODE_128 ? ANCHO_BARRA_CODE128 : ANCHO_BARRA_EAN13;
    }

    private static byte[] comprimirPng(BufferedImage imagen) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            ImageIO.write(imagen, "png", salida);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar la imagen de la etiqueta.", ex);
        }
        return salida.toByteArray();
    }
}