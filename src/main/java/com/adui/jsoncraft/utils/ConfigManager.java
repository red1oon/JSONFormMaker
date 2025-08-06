package com.adui.jsoncraft.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration Manager for JSONFormMaker
 * Handles application settings, preferences, and file locations
 * 
 * Version: 1.0.0
 * Namespace: com.adui.jsoncraft.utils.ConfigManager
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "jsonformmaker.properties";
    private static final String CONFIG_DIR = ".jsonformmaker";
    
    private static ConfigManager instance;
    private Properties properties;
    private Path configPath;
    private Path configDir;
    
    private ConfigManager() {
        this.properties = new Properties();
        this.configDir = Paths.get(System.getProperty("user.home"), CONFIG_DIR);
        this.configPath = configDir.resolve(CONFIG_FILE);
    }
    
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    public void initialize() {
        loadDefaultProperties();
        ensureConfigDirectory();
        loadConfigFile();
        ensureDefaultDirectories();
        logger.info("ConfigManager initialized with config at: {}", configPath);
    }
    
    private void loadDefaultProperties() {
        // UI Defaults
        properties.setProperty("ui.theme", "FlatLaf");
        properties.setProperty("ui.defaultWidth", "1200");
        properties.setProperty("ui.defaultHeight", "800");
        properties.setProperty("ui.minWidth", "1000");
        properties.setProperty("ui.minHeight", "600");
        
        // Panel Sizes
        properties.setProperty("palette.width", "280");
        properties.setProperty("properties.width", "350");
        properties.setProperty("console.height", "150");
        
        // Window State
        properties.setProperty("window.width", "1200");
        properties.setProperty("window.height", "800");
        properties.setProperty("window.maximized", "false");
        
        // File Settings
        String userHome = System.getProperty("user.home");
        properties.setProperty("file.defaultLocation", Paths.get(userHome, "Documents", "JSONFormMaker").toString());
        properties.setProperty("file.autoSave", "true");
        properties.setProperty("file.autoSaveInterval", "120");  // seconds
        properties.setProperty("file.backupEnabled", "true");
        properties.setProperty("file.maxRecentFiles", "10");
        
        // Editor Settings
        properties.setProperty("editor.fontSize", "12");
        properties.setProperty("editor.fontFamily", "SansSerif");
        properties.setProperty("editor.tabSize", "4");
        properties.setProperty("editor.showLineNumbers", "true");
        properties.setProperty("editor.wordWrap", "true");
        
        // Validation Settings
        properties.setProperty("validation.realtime", "true");
        properties.setProperty("validation.showWarnings", "true");
        properties.setProperty("validation.strictMode", "false");
        
        // Application Settings
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("app.checkUpdates", "true");
        properties.setProperty("app.sendUsageStats", "false");
        
        logger.debug("Default properties loaded");
    }
    
    private void ensureConfigDirectory() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                logger.info("Created config directory: {}", configDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create config directory: {}", configDir, e);
        }
    }
    
    private void loadConfigFile() {
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
                logger.info("Configuration loaded from: {}", configPath);
            } catch (IOException e) {
                logger.error("Failed to load configuration from: {}", configPath, e);
            }
        } else {
            logger.info("Configuration file not found, using defaults: {}", configPath);
        }
    }
    
    private void ensureDefaultDirectories() {
        try {
            // Create default file location
            String defaultLocation = getProperty("file.defaultLocation");
            Path defaultPath = Paths.get(defaultLocation);
            if (!Files.exists(defaultPath)) {
                Files.createDirectories(defaultPath);
                logger.info("Created default file directory: {}", defaultPath);
            }
            
            // Create samples directory
            Path samplesPath = defaultPath.resolve("samples");
            if (!Files.exists(samplesPath)) {
                Files.createDirectories(samplesPath);
                logger.info("Created samples directory: {}", samplesPath);
            }
            
            // Create templates directory
            Path templatesPath = defaultPath.resolve("templates");
            if (!Files.exists(templatesPath)) {
                Files.createDirectories(templatesPath);
                logger.info("Created templates directory: {}", templatesPath);
            }
            
        } catch (IOException e) {
            logger.error("Failed to create default directories", e);
        }
    }
    
    // String Property Methods
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public void setProperty(String key, String value) {
        if (value != null) {
            properties.setProperty(key, value);
        } else {
            properties.remove(key);
        }
    }
    
    // Integer Property Methods
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for property {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    public void setIntProperty(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
    }
    
    // Boolean Property Methods
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    public void setBooleanProperty(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }
    
    // Double Property Methods
    public double getDoubleProperty(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid double value for property {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    public void setDoubleProperty(String key, double value) {
        properties.setProperty(key, String.valueOf(value));
    }
    
    // Long Property Methods
    public long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid long value for property {}: {}", key, value);
            }
        }
        return defaultValue;
    }
    
    public void setLongProperty(String key, long value) {
        properties.setProperty(key, String.valueOf(value));
    }
    
    // Configuration Management
    public void saveConfiguration() {
        try {
            ensureConfigDirectory();
            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "JSONFormMaker Configuration");
                logger.info("Configuration saved to: {}", configPath);
            }
        } catch (IOException e) {
            logger.error("Failed to save configuration to: {}", configPath, e);
        }
    }
    
    public void resetToDefaults() {
        properties.clear();
        loadDefaultProperties();
        logger.info("Configuration reset to defaults");
    }
    
    public void reloadConfiguration() {
        properties.clear();
        loadDefaultProperties();
        loadConfigFile();
        logger.info("Configuration reloaded");
    }
    
    // Property Management
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    public void removeProperty(String key) {
        properties.remove(key);
    }
    
    public java.util.Set<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }
    
    // Utility Methods
    public Path getConfigPath() {
        return configPath;
    }
    
    public Path getConfigDirectory() {
        return configDir;
    }
    
    public Path getDefaultFileLocation() {
        return Paths.get(getProperty("file.defaultLocation"));
    }
    
    public Path getDefaultJsonDirectory() {
        return getDefaultFileLocation();
    }
    
    public void setDefaultJsonDirectory(Path directory) {
        setProperty("file.defaultLocation", directory.toString());
    }
    
    public Path getSamplesDirectory() {
        return getDefaultFileLocation().resolve("samples");
    }
    
    public Path getTemplatesDirectory() {
        return getDefaultFileLocation().resolve("templates");
    }
    
    // Recent Files Management
    public void addRecentFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return;
        
        // Get current recent files
        String[] recentFiles = getRecentFiles();
        int maxRecentFiles = getIntProperty("file.maxRecentFiles", 10);
        
        // Create new list with the new file at the top
        StringBuilder recentList = new StringBuilder();
        recentList.append(filePath);
        
        int count = 1;
        for (String file : recentFiles) {
            if (!file.equals(filePath) && count < maxRecentFiles) {
                recentList.append(";").append(file);
                count++;
            }
        }
        
        setProperty("file.recentFiles", recentList.toString());
    }
    
    public String[] getRecentFiles() {
        String recentFiles = getProperty("file.recentFiles", "");
        if (recentFiles.isEmpty()) {
            return new String[0];
        }
        return recentFiles.split(";");
    }
    
    public void clearRecentFiles() {
        removeProperty("file.recentFiles");
    }
    
    // Window State Management
    public void saveWindowState(int width, int height, boolean maximized) {
        setIntProperty("window.width", width);
        setIntProperty("window.height", height);
        setBooleanProperty("window.maximized", maximized);
    }
    
    public int getWindowWidth() {
        return getIntProperty("window.width", 1200);
    }
    
    public int getWindowHeight() {
        return getIntProperty("window.height", 800);
    }
    
    public boolean isWindowMaximized() {
        return getBooleanProperty("window.maximized", false);
    }
    
    // Panel State Management
    public void savePanelSizes(int paletteWidth, int propertiesWidth, int consoleHeight) {
        setIntProperty("palette.width", paletteWidth);
        setIntProperty("properties.width", propertiesWidth);
        setIntProperty("console.height", consoleHeight);
    }
    
    public int getPaletteWidth() {
        return getIntProperty("palette.width", 280);
    }
    
    public int getPropertiesWidth() {
        return getIntProperty("properties.width", 350);
    }
    
    public int getConsoleHeight() {
        return getIntProperty("console.height", 150);
    }
    
    // Theme and UI Settings
    public String getTheme() {
        return getProperty("ui.theme", "FlatLaf");
    }
    
    public void setTheme(String theme) {
        setProperty("ui.theme", theme);
    }
    
    public int getFontSize() {
        return getIntProperty("editor.fontSize", 12);
    }
    
    public void setFontSize(int fontSize) {
        setIntProperty("editor.fontSize", fontSize);
    }
    
    public String getFontFamily() {
        return getProperty("editor.fontFamily", "SansSerif");
    }
    
    public void setFontFamily(String fontFamily) {
        setProperty("editor.fontFamily", fontFamily);
    }
    
    // Validation Settings
    public boolean isRealtimeValidation() {
        return getBooleanProperty("validation.realtime", true);
    }
    
    public void setRealtimeValidation(boolean enabled) {
        setBooleanProperty("validation.realtime", enabled);
    }
    
    public boolean showValidationWarnings() {
        return getBooleanProperty("validation.showWarnings", true);
    }
    
    public void setShowValidationWarnings(boolean enabled) {
        setBooleanProperty("validation.showWarnings", enabled);
    }
    
    // Auto-save Settings
    public boolean isAutoSaveEnabled() {
        return getBooleanProperty("file.autoSave", true);
    }
    
    public void setAutoSaveEnabled(boolean enabled) {
        setBooleanProperty("file.autoSave", enabled);
    }
    
    public int getAutoSaveInterval() {
        return getIntProperty("file.autoSaveInterval", 120);
    }
    
    public void setAutoSaveInterval(int seconds) {
        setIntProperty("file.autoSaveInterval", seconds);
    }
    
    // Debug and Logging
    public void dumpConfiguration() {
        logger.info("=== Configuration Dump ===");
        for (String key : properties.stringPropertyNames()) {
            logger.info("{} = {}", key, properties.getProperty(key));
        }
        logger.info("=== End Configuration ===");
    }
    
    @Override
    public String toString() {
        return String.format("ConfigManager{configPath=%s, propertyCount=%d}", 
                           configPath, properties.size());
    }
}