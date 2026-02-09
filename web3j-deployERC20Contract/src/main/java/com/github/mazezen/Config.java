package com.github.mazezen;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class Config {
    public static String getNetwork(String network) {
        Yaml yaml = new Yaml();
        InputStream resourceAsStream = Config.class.getClassLoader().getResourceAsStream("application.yaml");
        Map<String, Object> config = yaml.load(resourceAsStream);
        switch (network) {
            case "local" -> {
                return config.getOrDefault("LocalNetWorkNode", "").toString();
            }
            case "test" -> {
                return config.getOrDefault("TestNetWorkNode", "").toString();
            }
            case "main" -> {
                return config.getOrDefault("MainNetWorkNode", "").toString();
            }
            default -> {
                return "";
            }
        }
    }
}
