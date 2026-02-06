package com.github.mazezen.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class YamlConfig {

    public static String getMainNetWorkNode(String network) {
        InputStream inputStream = YamlConfig.class.getClassLoader().getResourceAsStream("application.yaml");
        Yaml yaml = new Yaml();

        Map<String, Object> yamlConfig = yaml.load(inputStream);
        switch (network) {
            case "local" -> {
                return yamlConfig.getOrDefault("LocalNetWorkNode", "").toString();
            }
            case "test" -> {
                return yamlConfig.getOrDefault("TestNetWorkNode", "").toString();
            }
            case "main" -> {
                return yamlConfig.getOrDefault("MainNetWorkNode", "").toString();
            }
            default -> {
                return "";
            }
        }
    }
}
