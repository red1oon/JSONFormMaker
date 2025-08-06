package com.adui.jsoncraft.utils;

import com.adui.jsoncraft.json.JsonGenerator;
import com.adui.jsoncraft.json.JsonGenerator.JsonGenerationException;
import com.adui.jsoncraft.json.JsonParser;
import com.adui.jsoncraft.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * File Manager for JSONFormMaker
 * Handles file operations, sample creation, and template management
 * 
 * @version 1.1 - Enhanced with JsonParser support and correct ComponentTypes
 */
public class FileManager {
    private static final Logger logger = LoggerFactory.getLogger(FileManager.class);
    
    private static final String FILE_EXTENSION = ".adui-json";
    private static final String SAMPLES_DIRECTORY = "samples";
    
    private final JsonGenerator jsonGenerator;
    private final JsonParser jsonParser;
    private JFileChooser fileChooser;
    private final List<File> recentFiles;
    
    public FileManager() throws JsonGenerationException {
        this.jsonGenerator = new JsonGenerator(true, false);
        this.jsonParser = new JsonParser();
        this.recentFiles = new ArrayList<>();
        initializeFileChooser();
        createSampleFiles();
    }
    
    private void initializeFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "ADUI JSON Files (*.adui-json)", "adui-json"));
        
        // Set default directory to Documents/JSON
        Path defaultDir = getDefaultJsonDirectory();
        fileChooser.setCurrentDirectory(defaultDir.toFile());
    }
    
    /**
     * Get default JSON directory (ADDED - fixes missing method error)
     */
    public Path getDefaultJsonDirectory() {
        try {
            return ConfigManager.getInstance().getDefaultJsonDirectory();
        } catch (Exception e) {
            // Fallback if ConfigManager has issues
            String userHome = System.getProperty("user.home");
            return Paths.get(userHome, "Documents", "JSON");
        }
    }
    
    /**
     * Save WindowDefinition to file
     */
    public boolean saveToFile(WindowDefinition window, File file) {
        try {
            String json = jsonGenerator.generateJson(window);
            
            // Ensure file has correct extension
            if (!file.getName().endsWith(FILE_EXTENSION)) {
                file = new File(file.getParent(), file.getName() + FILE_EXTENSION);
            }
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
            
            addToRecentFiles(file);
            logger.info("Saved window definition to: {}", file.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to save file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Show save dialog and save file
     */
    public File showSaveDialog(JFrame parent, WindowDefinition window) {
        // Set suggested filename
        if (window.getWindowId() != null) {
            fileChooser.setSelectedFile(new File(window.getWindowId() + FILE_EXTENSION));
        }
        
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            if (saveToFile(window, file)) {
                return file;
            } else {
                JOptionPane.showMessageDialog(parent, 
                    "Failed to save file: " + file.getName(),
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        return null;
    }
    
    /**
     * Show open dialog and load file
     */
    public WindowDefinition showOpenDialog(JFrame parent) {
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            WindowDefinition window = loadFromFile(file);
            if (window != null) {
                addToRecentFiles(file);
            }
            return window;
        }
        
        return null;
    }
    
    /**
     * Load WindowDefinition from file - ENHANCED with JsonParser
     */
    public WindowDefinition loadFromFile(File file) {
        try {
            // Use JsonParser to load the file
            WindowDefinition window = jsonParser.parseFile(file);
            
            logger.info("Loaded window definition from: {}", file.getAbsolutePath());
            return window;
            
        } catch (JsonParser.JsonParseException e) {
            logger.error("Failed to parse JSON file: {}", file.getAbsolutePath(), e);
            
            // Show user-friendly error dialog
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                    "Failed to parse JSON file:\n" + e.getMessage(), 
                    "Parse Error", JOptionPane.ERROR_MESSAGE);
            });
            return null;
            
        } catch (Exception e) {
            logger.error("Failed to load file: {}", file.getAbsolutePath(), e);
            
            // Show user-friendly error dialog
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                    "Failed to load file:\n" + e.getMessage(), 
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            });
            return null;
        }
    }
    
    /**
     * Create sample files in Documents/JSON directory
     * @throws JsonGenerationException 
     */
    public void createSampleFiles() throws JsonGenerationException {
        try {
            Path jsonDir = getDefaultJsonDirectory();
            Path samplesDir = jsonDir.resolve(SAMPLES_DIRECTORY);
            
            if (!Files.exists(samplesDir)) {
                Files.createDirectories(samplesDir);
                logger.info("Created samples directory: {}", samplesDir);
            }
            
            // Create equipment inspection sample
            createEquipmentInspectionSample(samplesDir);
            
            // Create safety checklist sample
            createSafetyChecklistSample(samplesDir);
            
            // Create task management sample
            createTaskManagementSample(samplesDir);
            
            logger.info("Sample files created in: {}", samplesDir);
            
        } catch (IOException e) {
            logger.warn("Failed to create sample files", e);
        }
    }
    
    private void createEquipmentInspectionSample(Path samplesDir) throws IOException, JsonGenerationException {
        WindowDefinition window = new WindowDefinition();
        window.setWindowId("EQUIPMENT_INSPECTION");
        window.setName("Equipment Inspection Form");
        window.setDescription("Daily equipment inspection checklist");
        
        // Create basic info tab
        TabDefinition basicTab = new TabDefinition();
        basicTab.setTabId("TAB_BASIC");
        basicTab.setName("Basic Info");
        basicTab.setSequence(10);
        
        // Add sample fields - CORRECTED ComponentTypes
        FieldDefinition equipIdField = new FieldDefinition();
        equipIdField.setFieldId("EQUIPMENT_ID");
        equipIdField.setName("Equipment ID");
        equipIdField.setComponentType(ComponentType.TEXT_FIELD);
        equipIdField.setSequence(10);
        equipIdField.setRequired(true);
        basicTab.addField(equipIdField);
        
        FieldDefinition locationField = new FieldDefinition();
        locationField.setFieldId("LOCATION");
        locationField.setName("Location");
        locationField.setComponentType(ComponentType.TEXT_FIELD);
        locationField.setSequence(20);
        basicTab.addField(locationField);
        
        window.addTab(basicTab);
        
        // Save sample file
        String json = jsonGenerator.generateJson(window);
        Files.write(samplesDir.resolve("equipment_inspection.adui-json"), json.getBytes());
    }
    
    private void createSafetyChecklistSample(Path samplesDir) throws IOException, JsonGenerationException {
        WindowDefinition window = new WindowDefinition();
        window.setWindowId("SAFETY_CHECKLIST");
        window.setName("Safety Checklist");
        window.setDescription("Workplace safety inspection form");
        
        TabDefinition safetyTab = new TabDefinition();
        safetyTab.setTabId("TAB_SAFETY");
        safetyTab.setName("Safety Items");
        safetyTab.setSequence(10);
        
        // Add sample safety fields - CORRECTED ComponentTypes
        FieldDefinition emergencyExitField = new FieldDefinition();
        emergencyExitField.setFieldId("EMERGENCY_EXIT_CLEAR");
        emergencyExitField.setName("Emergency exits clear?");
        emergencyExitField.setComponentType(ComponentType.YES_NO_FIELD);
        emergencyExitField.setSequence(10);
        emergencyExitField.setRequired(true);
        safetyTab.addField(emergencyExitField);
        
        FieldDefinition firstAidField = new FieldDefinition();
        firstAidField.setFieldId("FIRST_AID_STOCKED");
        firstAidField.setName("First aid kit stocked?");
        firstAidField.setComponentType(ComponentType.YES_NO_FIELD);
        firstAidField.setSequence(20);
        firstAidField.setRequired(true);
        safetyTab.addField(firstAidField);
        
        window.addTab(safetyTab);
        
        String json = jsonGenerator.generateJson(window);
        Files.write(samplesDir.resolve("safety_checklist.adui-json"), json.getBytes());
    }
    
    private void createTaskManagementSample(Path samplesDir) throws IOException, JsonGenerationException {
        WindowDefinition window = new WindowDefinition();
        window.setWindowId("TASK_MANAGEMENT");
        window.setName("Task Management");
        window.setDescription("Project task tracking form");
        
        TabDefinition taskTab = new TabDefinition();
        taskTab.setTabId("TAB_TASK");
        taskTab.setName("Task Details");
        taskTab.setSequence(10);
        
        // Add task fields - CORRECTED ComponentTypes
        FieldDefinition taskNameField = new FieldDefinition();
        taskNameField.setFieldId("TASK_NAME");
        taskNameField.setName("Task Name");
        taskNameField.setComponentType(ComponentType.TEXT_FIELD);
        taskNameField.setSequence(10);
        taskNameField.setRequired(true);
        taskTab.addField(taskNameField);
        
        FieldDefinition priorityField = new FieldDefinition();
        priorityField.setFieldId("PRIORITY");
        priorityField.setName("Priority");
        priorityField.setComponentType(ComponentType.SELECT_FIELD);
        priorityField.setSequence(20);
        taskTab.addField(priorityField);
        
        FieldDefinition descriptionField = new FieldDefinition();
        descriptionField.setFieldId("DESCRIPTION");
        descriptionField.setName("Description");
        descriptionField.setComponentType(ComponentType.TEXT_AREA_FIELD);
        descriptionField.setSequence(30);
        taskTab.addField(descriptionField);
        
        window.addTab(taskTab);
        
        String json = jsonGenerator.generateJson(window);
        Files.write(samplesDir.resolve("task_management.adui-json"), json.getBytes());
    }
    
    /**
     * Recent files management
     */
    public void addToRecentFiles(File file) {
        recentFiles.remove(file); // Remove if already exists
        recentFiles.add(0, file); // Add to beginning
        
        // Limit to 10 recent files
        while (recentFiles.size() > 10) {
            recentFiles.remove(recentFiles.size() - 1);
        }
    }
    
    public List<File> getRecentFiles() {
        return new ArrayList<>(recentFiles);
    }
    
    /**
     * Show file dialog for generic file selection (ADDED - for compatibility)
     */
    public File showFileDialog(JFrame parent) {
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    /**
     * Get file chooser for advanced operations
     */
    public JFileChooser getFileChooser() {
        return fileChooser;
    }
}