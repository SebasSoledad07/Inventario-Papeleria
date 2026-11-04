package com.soledad.inventariopapeleria.reporte.exportacion;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportadorExcel {

    public static byte[] exportar(String titulo, List<Map<String, Object>> filas) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(titulo.length() > 30 ? titulo.substring(0, 30) : titulo);
            if (!filas.isEmpty()) {
                Map<String, Object> first = filas.get(0);
                Row header = sheet.createRow(0);
                int c = 0;
                for (String key : first.keySet()) {
                    header.createCell(c++).setCellValue(key);
                }
                int r = 1;
                for (Map<String, Object> fila : filas) {
                    Row row = sheet.createRow(r++);
                    c = 0;
                    for (Object val : fila.values()) {
                        row.createCell(c++).setCellValue(val == null ? "" : val.toString());
                    }
                }
                for (int i = 0; i < first.keySet().size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel: " + e.getMessage(), e);
        }
    }
}