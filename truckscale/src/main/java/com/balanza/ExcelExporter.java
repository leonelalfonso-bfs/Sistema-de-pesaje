package com.balanza;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExporter {
    public static void exportToExcel(List<Map<String, Object>> weighings, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Pesadas");

        // Header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Fecha");
        headerRow.createCell(2).setCellValue("Patente Chasis");
        headerRow.createCell(3).setCellValue("Bruto");
        headerRow.createCell(4).setCellValue("Tara");
        headerRow.createCell(5).setCellValue("Neto");
        // Añade más columnas si necesitas

        // Data rows
        int rowNum = 1;
        for (Map<String, Object> weighing : weighings) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((int) weighing.get("id"));
            row.createCell(1).setCellValue((String) weighing.get("fecha"));
            row.createCell(2).setCellValue((String) weighing.get("patente_chasis"));
            row.createCell(3).setCellValue((double) weighing.get("bruto"));
            row.createCell(4).setCellValue((double) weighing.get("tara"));
            row.createCell(5).setCellValue((double) weighing.get("neto"));
            // Añade más
        }

        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        }
        workbook.close();
    }
}