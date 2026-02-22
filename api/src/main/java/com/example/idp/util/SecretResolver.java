package com.example.idp.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SecretResolver {
    private final Environment environment;

    public SecretResolver(Environment environment) {
        this.environment = environment;
    }

    public String resolve(String value, String envName) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        String filePath = environment.getProperty(envName + "_FILE");
        if (filePath == null || filePath.isBlank()) {
            return value;
        }
        try {
            return Files.readString(Path.of(filePath), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read secret file for " + envName, e);
        }
    }
}
