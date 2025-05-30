package com.testdataretrival;

import io.restassured.specification.RequestSpecification;
import java.util.Properties;
import java.util.Base64;
import java.util.logging.Logger;

public class Auth {
    private static final Logger logger = Logger.getLogger(Auth.class.getName());
    public static void applyAuth(RequestSpecification req, Properties config) {
        if (!"yes".equalsIgnoreCase(config.getProperty("auth.required", "no"))) return;
        String type = config.getProperty("auth.type", "").toLowerCase();
        logger.info("Applying authentication type: " + type);
        switch (type) {
            case "basic":
                String user = config.getProperty("auth.username", "");
                String pass = config.getProperty("auth.password", "");
                logger.info("Using basic auth for user: " + user);
                String encoded = Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
                req.header("Authorization", "Basic " + encoded);
                break;
            case "header":
                String headerName = config.getProperty("auth.header.name", "Authorization");
                String headerValue = config.getProperty("auth.header.value", "");
                logger.info("Using header auth: " + headerName);
                req.header(headerName, headerValue);
                break;
            case "oauth":
                // OAuth 2.0 Bearer Token
                String token = config.getProperty("auth.oauth.token", "");
                logger.info("Using OAuth2 Bearer token auth");
                req.header("Authorization", "Bearer " + token);
                break;
            case "cookie":
                String cookieName = config.getProperty("auth.cookie.name", "");
                String cookieValue = config.getProperty("auth.cookie.value", "");
                logger.info("Using Cookie auth: " + cookieName);
                req.cookie(cookieName, cookieValue);
                break;
            // Add OAuth, Cookies, etc. as needed
            default:
                logger.warning("No or unsupported auth type specified.");
                break;
        }
    }
}
