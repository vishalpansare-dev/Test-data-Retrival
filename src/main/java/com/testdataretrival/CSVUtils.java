package com.testdataretrival;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class CSVUtils {
    private static final Logger logger = Logger.getLogger(CSVUtils.class.getName());
    public static List<Map<String, String>> readCSV(String filePath) throws IOException {
        logger.info("Reading CSV file: " + filePath);
        List<Map<String, String>> data = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] headers = null;
            try {
                headers = reader.readNext();
                if (headers == null) return data;
                String[] row;
                while ((row = reader.readNext()) != null) {
                    Map<String, String> map = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        map.put(headers[i], i < row.length ? row[i] : "");
                    }
                    data.add(map);
                }
            } catch (CsvValidationException e) {
                logger.severe("CSV validation error: " + e.getMessage());
                throw new IOException("CSV validation error: " + e.getMessage(), e);
            }
        }
        logger.info("Read " + data.size() + " rows from CSV file.");
        return data;
    }

    public static void writeCSV(String filePath, List<Map<String, Object>> data) throws IOException {
        logger.info("Writing CSV file: " + filePath);
        if (data.isEmpty()) return;
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            Set<String> headers = new LinkedHashSet<>(data.get(0).keySet());
            writer.writeNext(headers.toArray(new String[0]));
            for (Map<String, Object> row : data) {
                String[] vals = headers.stream().map(h -> {
                    Object v = row.get(h);
                    if (v instanceof List) {
                        List<?> list = (List<?>) v;
                        if (list.size() == 1) return String.valueOf(list.get(0));
                        return String.join(",", list.stream().map(String::valueOf).toArray(String[]::new));
                    }
                    return v == null ? "" : v.toString();
                }).toArray(String[]::new);
                writer.writeNext(vals);
            }
        }
        logger.info("CSV file written successfully: " + filePath);
    }

    public static List<Map<String, String>> readJSON(String filePath) throws IOException {
        logger.info("Reading JSON file: " + filePath);
        ObjectMapper mapper = new ObjectMapper();
        List<?> rawList = mapper.readValue(new File(filePath), List.class);
        List<Map<String, String>> list = new ArrayList<>();
        for (Object obj : rawList) {
            if (obj instanceof Map) {
                Map<?, ?> rawMap = (Map<?, ?>) obj;
                Map<String, String> map = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
                list.add(map);
            }
        }
        logger.info("Read " + list.size() + " rows from JSON file.");
        return list;
    }

    public static void writeJSON(String filePath, List<Map<String, Object>> data) throws IOException {
        logger.info("Writing JSON file: " + filePath);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), data);
        logger.info("JSON file written successfully: " + filePath);
    }
}
