package com.testdataretrival;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class ExcelUtils {
    private static final Logger logger = Logger.getLogger(ExcelUtils.class.getName());
    public static List<Map<String, String>> readExcel(String filePath) throws IOException {
        logger.info("Reading Excel file: " + filePath);
        List<Map<String, String>> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) return data;
            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) headers.add(cell.getStringCellValue());
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    map.put(headers.get(i), getCellValue(cell));
                }
                data.add(map);
            }
        }
        logger.info("Read " + data.size() + " rows from Excel file.");
        return data;
    }

    public static void writeExcel(String filePath, List<Map<String, Object>> data) throws IOException {
        logger.info("Writing Excel file: " + filePath);
        if (data.isEmpty()) return;
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Results");
            Set<String> headers = data.get(0).keySet();
            Row headerRow = sheet.createRow(0);
            int col = 0;
            for (String h : headers) headerRow.createCell(col++).setCellValue(h);
            int rowIdx = 1;
            for (Map<String, Object> row : data) {
                Row excelRow = sheet.createRow(rowIdx++);
                int c = 0;
                for (String h : headers) {
                    Object val = row.get(h);
                    excelRow.createCell(c++).setCellValue(val == null ? "" : val.toString());
                }
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
        logger.info("Excel file written successfully: " + filePath);
    }

    private static String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            case BLANK: return "";
            default: return "";
        }
    }
}
