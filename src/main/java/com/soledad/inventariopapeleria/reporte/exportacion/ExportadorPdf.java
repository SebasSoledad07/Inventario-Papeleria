package com.soledad.inventariopapeleria.reporte.exportacion;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

public class ExportadorPdf {

    public static byte[] exportar(String titulo, List<Map<String, Object>> filas) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph(titulo));
            if (!filas.isEmpty()) {
                Map<String, Object> first = filas.get(0);
                PdfPTable table = new PdfPTable(first.keySet().size());
                for (String key : first.keySet()) {
                    table.addCell(new PdfPCell(new Paragraph(key)));
                }
                for (Map<String, Object> fila : filas) {
                    for (Object val : fila.values()) {
                        table.addCell(val == null ? "" : val.toString());
                    }
                }
                doc.add(table);
            }
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }
}