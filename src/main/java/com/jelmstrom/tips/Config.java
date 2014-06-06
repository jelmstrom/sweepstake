package com.jelmstrom.tips;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public enum Config {
    INSTANCE;

    private final Properties properties = new Properties();

    private Config() {
        String resource = "config.properties";
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(resource));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration: " + resource);
        }
    }

    public List<String> getGroupTeams(String group) {
        return Arrays.asList(properties.getProperty("teams." + group).split(","));
    }

}
