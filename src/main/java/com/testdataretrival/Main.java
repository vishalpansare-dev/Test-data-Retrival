package com.testdataretrival;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            config.load(fis);
            logger.info("Loaded config.properties successfully.");
        } catch (IOException e) {
            logger.severe("Error loading config.properties: " + e.getMessage());
            System.err.println("Error loading config.properties: " + e.getMessage());
            return;
        }

        String inputFile = config.getProperty("input.file");
        String inputFormat = config.getProperty("input.file.format");
        List<Map<String, String>> inputData = new ArrayList<>();
        try {
            if (inputFile != null && !inputFile.isEmpty()) {
                logger.info("Reading input file: " + inputFile + " as format: " + inputFormat);
                if ("csv".equalsIgnoreCase(inputFormat)) {
                    inputData = CSVUtils.readCSV(inputFile);
                } else if ("excel".equalsIgnoreCase(inputFormat)) {
                    inputData = ExcelUtils.readExcel(inputFile);
                } else if ("json".equalsIgnoreCase(inputFormat)) {
                    inputData = CSVUtils.readJSON(inputFile);
                }
                logger.info("Loaded " + inputData.size() + " rows from input file.");
            }
        } catch (Exception e) {
            logger.severe("Error reading input file: " + e.getMessage());
            System.err.println("Error reading input file: " + e.getMessage());
            return;
        }

        List<Map<String, Object>> results = new ArrayList<>();
        if (inputData.isEmpty()) {
            // No input file, single API call
            try {
                logger.info("No input file. Making a single API call.");
                Map<String, Object> result = APIUtils.callAPI(config, Collections.emptyMap());
                results.add(result);
            } catch (Exception e) {
                logger.severe("API call failed: " + e.getMessage());
                System.err.println("API call failed: " + e.getMessage());
            }
        } else {
            for (Map<String, String> row : inputData) {
                try {
                    logger.info("Making API call for row: " + row);
                    Map<String, Object> result = APIUtils.callAPI(config, row);
                    results.add(result);
                } catch (Exception e) {
                    logger.severe("API call failed for row: " + row + ", error: " + e.getMessage());
                    System.err.println("API call failed for row: " + row + ", error: " + e.getMessage());
                }
            }
        }

        // Write output
        String outputFile = config.getProperty("output.file");
        String outputFormat = outputFile != null && outputFile.endsWith(".json") ? "json" : outputFile != null && (outputFile.endsWith(".xlsx") || outputFile.endsWith(".xls")) ? "excel" : "csv";
        try {
            logger.info("Writing results to output file: " + outputFile + " as format: " + outputFormat);
            if ("csv".equalsIgnoreCase(outputFormat)) {
                CSVUtils.writeCSV(outputFile, results);
            } else if ("excel".equalsIgnoreCase(outputFormat)) {
                ExcelUtils.writeExcel(outputFile, results);
            } else if ("json".equalsIgnoreCase(outputFormat)) {
                CSVUtils.writeJSON(outputFile, results);
            }
            logger.info("Output file written successfully.");
        } catch (Exception e) {
            logger.severe("Error writing output file: " + e.getMessage());
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }
}
