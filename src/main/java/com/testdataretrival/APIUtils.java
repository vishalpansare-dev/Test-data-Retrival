package com.testdataretrival;

import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class APIUtils {
    private static final Logger logger = Logger.getLogger(APIUtils.class.getName());

    public static Map<String, Object> callAPI(Properties config, Map<String, String> row) throws Exception {
        String url = substituteParams(config.getProperty("base.url"), row);
        String method = config.getProperty("request.method", "GET").toUpperCase();
        logger.info("Preparing API call: " + method + " " + url);
        String requestBodyFile = config.getProperty("request.body.file");
        String headersFile = config.getProperty("request.headers.file");
        Map<String, String> headers = loadHeaders(headersFile);
        RequestSpecification req = RestAssured.given();
        if (headers != null) {
            logger.info("Loaded headers: " + headers);
            req.headers(headers);
        }
        if (requestBodyFile != null && !requestBodyFile.isEmpty()) {
            String body = new String(Files.readAllBytes(Paths.get(requestBodyFile)));
            logger.info("Loaded request body from file: " + requestBodyFile);
            logger.info("Request body from file: " + body);
            req.body(body);
        }
        Auth.applyAuth(req, config);
        Response response;
        switch (method) {
            case "POST": response = req.post(url); break;
            case "PUT": response = req.put(url); break;
            case "DELETE": response = req.delete(url); break;
            case "PATCH": response = req.patch(url); break;
            default: response = req.get(url); break;
        }
        logger.info("API response status: " + response.statusLine());
        logger.info("API response : " + response.getBody().asPrettyString());
        if (response.statusCode() >= 400) throw new IOException("API error: " + response.statusLine());
        return extractFields(response, config.getProperty("data.retrieval.jsonpath"));
    }

    public static String substituteParams(String url, Map<String, String> row) {
        if (row == null) return url;
        for (Map.Entry<String, String> entry : row.entrySet()) {
            url = url.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return url;
    }

    public static Map<String, String> loadHeaders(String headersFile) throws IOException {
        if (headersFile == null || headersFile.isEmpty()) return null;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(headersFile)) {
            props.load(fis);
        }
        Map<String, String> map = new HashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }
        return map;
    }

    public static Map<String, Object> extractFields(Response response, String jsonPaths) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (jsonPaths == null || jsonPaths.isEmpty()) {
            result.put("response", response.asString());
            return result;
        }
        String responseString = response.asString();
        String[] paths = jsonPaths.split(",");
        for (String path : paths) {
            String key = path.trim();
            Object value;
            try {
                value = JsonPath.read(responseString, key);
                logger.info("Extracted value for '" + key + "': " + value);
            } catch (Exception e) {
                logger.warning("Error extracting JSONPath '" + key + "': " + e.getMessage());
                value = "";
            }
            // Flatten if value is a List
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                if (list.size() == 1) {
                    result.put(key, list.get(0));
                } else {
                    result.put(key, String.join(",", list.stream().map(String::valueOf).toArray(String[]::new)));
                }
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
}
