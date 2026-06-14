package com.kwesou.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;

public class ConfigManager {
    private final Properties properties = new Properties();
    private File configFile;

    public void resolveConfig(Path configDir, Logger LOGGER) {
        try {
            this.configFile = configDir.resolve("Press2Hold.properties").toFile();

            if(configFile.createNewFile()) {
                LOGGER.info("created new config file at \"{}\"", configFile.getAbsolutePath());
                properties.setProperty("DisplayType", "Chat");
                save();
            }
            else
                LOGGER.info("config file already existed at \"{}\"", configFile.getAbsolutePath());

            loadIntoProperties(configFile);
            if (!properties.containsKey("DisplayType")) {
                properties.setProperty("DisplayType", "Chat");
                save();
            }
        } catch (IOException e) {
            LOGGER.error("failed to load config", e);
        }
    }

    private void loadIntoProperties(File configFile) throws IOException{
        try (FileInputStream inputStream = new FileInputStream(configFile)){
            properties.load(inputStream);
        }
    }

    private void save() throws IOException {
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            properties.store(out, "press2hold configuration");
        }
    }

    public String getDisplayType() {
        return properties.getProperty("DisplayType", "Chat");
    }

    public void setDisplayType(String displayType) throws IOException {
        properties.setProperty("DisplayType", displayType);
        save();
    }
}
