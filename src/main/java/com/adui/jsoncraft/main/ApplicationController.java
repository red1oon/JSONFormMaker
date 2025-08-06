package com.adui.jsoncraft.main;

import java.awt.Dimension;
import com.adui.jsoncraft.json.JsonParser; 
import java.awt.Font;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.FormCanvas;
import com.adui.jsoncraft.canvas.refactored.RefactoredFormCanvas;
import com.adui.jsoncraft.json.JsonGenerator;
import com.adui.jsoncraft.json.JsonGenerator.JsonGenerationException;
import com.adui.jsoncraft.model.ComponentType;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;
import com.adui.jsoncraft.palette.ComponentPalette;
import com.adui.jsoncraft.properties.PropertyInspector;
import com.adui.jsoncraft.utils.FileManager;
import com.adui.jsoncraft.validation.ValidationEngine;

/**
 * Application Controller for JSONFormMaker
 * Coordinates between UI components and manages application state
 */
public class ApplicationController implements 
    FormCanvas.FormCanvasListener,
    PropertyInspector.PropertyChangeListener,
    ComponentPalette.ComponentPaletteListener {
	// Persistence enhancements
	private JsonParser jsonParser; 
    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    
    // UI Components
    private MainWindow mainWindow;
    private RefactoredFormCanvas formCanvas;
    private PropertyInspector propertyInspector;
    private ComponentPalette componentPalette;
    
    // Core services
    private FileManager fileManager;
    private JsonGenerator jsonGenerator;
    private ValidationEngine validationEngine;
    
    // Current state
    private WindowDefinition currentWindow;
    private File currentFile;
    private boolean hasUnsavedChanges;
    
    public ApplicationController(MainWindow mainWindow) throws JsonGenerationException {
        this.mainWindow = mainWindow;
        this.fileManager = new FileManager();
        this.jsonGenerator = new JsonGenerator();
        this.validationEngine = new ValidationEngine();
        this.hasUnsavedChanges = false;
     // Initialize persistence enhancements
        this.jsonParser = new JsonParser(); 
 
        initializeComponents();
        setupEventHandlers();
        
        logger.info("Application controller initialized");
    }
    
    private void initializeComponents() {
        // Create UI components
    	this.formCanvas = new RefactoredFormCanvas(); 
        propertyInspector = new PropertyInspector();
        componentPalette = new ComponentPalette();
        
        // Replace placeholder panels in main window
        replaceMainWindowPanels();
    }
    
    private void replaceMainWindowPanels() {
        // Replace the placeholder panels in MainWindow with actual components
        // This would require MainWindow to expose methods to replace panels
        // For now, log that we would replace them
        logger.debug("Would replace main window panels with actual components");
        
        // In a real implementation, MainWindow would have methods like:
        // mainWindow.setPalettePanel(componentPalette);
        // mainWindow.setCanvasPanel(formCanvas);
        // mainWindow.setPropertiesPanel(propertyInspector);
    }
    
    private void setupEventHandlers() {
        // Form canvas events
    	// Cast to the correct listener interface
    	formCanvas.addFormCanvasListener(new RefactoredFormCanvas.FormCanvasListener() {
    	    @Override
    	    public void windowChanged(WindowDefinition window) {
    	        ApplicationController.this.windowChanged(window); // Delegate to existing methods
    	    }
    	    
    	    @Override
    	    public void tabChanged(TabDefinition tab) {
    	        ApplicationController.this.tabChanged(tab); // Delegate to existing methods  
    	    }
    	    
    	    @Override
    	    public void fieldSelected(FieldDefinition field) {
        // Update property inspector with selected field (handles null properly)
        propertyInspector.setSelectedField(field);

        if (field == null) {
            mainWindow.updateStatus("No field selected");
            logger.debug("Field selection cleared");
        } else {
            mainWindow.updateStatus("Field selected: " + field.getName());
            logger.debug("Field selected: {}", field.getFieldId());
        }
    	    }
    	});
        
        // Property inspector events
        propertyInspector.addPropertyChangeListener(this);
        
        // Component palette events
        componentPalette.addComponentPaletteListener(this);
        componentPalette.addComponentPaletteListener(formCanvas);
    }
    
    // File operations
    public void newWindow() {
        if (hasUnsavedChanges && !confirmDiscardChanges()) {
            return;
        }
        
        formCanvas.createNewWindow();
        currentFile = null;
        hasUnsavedChanges = false;
        updateWindowTitle();
        
        mainWindow.updateStatus("New window created");
        mainWindow.updateFileStatus("New Window");
        mainWindow.updateValidationStatus(true, "Valid");
        
        logger.info("Created new window");
    }
    
    public void openFile() {
        if (hasUnsavedChanges && !confirmDiscardChanges()) {
            return;
        }
        
        File selectedFile = fileManager.showFileDialog(mainWindow);
        if (selectedFile != null) {
            try {
                WindowDefinition window = jsonParser.parseFile(selectedFile);
                if (window != null) {
                    currentWindow = window;
                    formCanvas.setCurrentWindow(window);  // ✅ Correct method name
                    propertyInspector.setCurrentWindow(window);
                    currentFile = selectedFile;  // ✅ Now we have the actual file
                    hasUnsavedChanges = false;
                    updateWindowTitle();
                    
                    mainWindow.updateStatus("File opened successfully");
                    mainWindow.updateFileStatus(selectedFile.getName());
                    validateCurrentWindow();
                    
                    logger.info("Opened window: {}", window.getWindowId());
                }
            } catch (JsonParser.JsonParseException e) {
                logger.error("Failed to parse JSON file", e);
                mainWindow.updateStatus("Error: " + e.getMessage());
                JOptionPane.showMessageDialog(mainWindow, 
                    "Failed to open file: " + e.getMessage(), 
                    "Parse Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Handle case where fileManager.showOpenDialog returns WindowDefinition directly
            // (Keep existing logic as fallback)
            WindowDefinition window = fileManager.showOpenDialog(mainWindow);
            if (window != null) {
            	 currentWindow = window;
                 formCanvas.setCurrentWindow(window);
                 propertyInspector.setCurrentWindow(window);
                 currentFile = null; // TODO: Get actual file from fileManager
                 hasUnsavedChanges = false;
                 updateWindowTitle();
                 
                 mainWindow.updateStatus("File opened successfully");
                 mainWindow.updateFileStatus(window.getName());
                 validateCurrentWindow();
                 
                 logger.info("Opened window: {}", window.getWindowId());
            }
        } 
    }
    
    public void saveFile() {
        if (currentFile != null) {
            saveToFile(currentFile);
        } else {
            saveAsFile();
        }
    }
    
    public void saveAsFile() {
        WindowDefinition window = formCanvas.getCurrentWindow();
        if (window != null) {
            File file = fileManager.showSaveDialog(mainWindow, window);
            if (file != null) {
                currentFile = file;
                hasUnsavedChanges = false;
                updateWindowTitle();
                fileManager.addToRecentFiles(file);
                
                mainWindow.updateStatus("File saved successfully");
                mainWindow.updateFileStatus(file.getName());
                
                logger.info("Saved window to: {}", file.getName());
            }
        }
    }
    
    private void saveToFile(File file) {
        WindowDefinition window = formCanvas.getCurrentWindow();
        if (window != null) {
            try {
                // Generate JSON using existing JsonGenerator
                String jsonContent = jsonGenerator.generateJson(window);
                
                // Write to file
                java.nio.file.Files.write(file.toPath(), jsonContent.getBytes("UTF-8"));
                
                // Update state
                hasUnsavedChanges = false;
                updateWindowTitle();
                
                mainWindow.updateStatus("File saved successfully");
                mainWindow.updateFileStatus(file.getName());
                
                logger.info("Saved window to: {}", file.getName());
                
            } catch (Exception e) {
                logger.error("Failed to save file", e);
                mainWindow.updateStatus("Error: Failed to save file");
                JOptionPane.showMessageDialog(mainWindow, 
                    "Failed to save file: " + e.getMessage(), 
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void validateJson() {
        WindowDefinition window = formCanvas.getCurrentWindow();
        if (window != null) {
            ValidationEngine.ValidationResult result = validationEngine.validateWindow(window);
            
            String summary = result.getSummary();
            boolean isValid = result.isValid();
            
            mainWindow.updateValidationStatus(isValid, summary);
            
            if (!result.getErrors().isEmpty() || !result.getWarnings().isEmpty()) {
                showValidationResults(result);
            } else {
                mainWindow.updateStatus("Validation successful - no issues found");
            }
            
            logger.info("Validation result: {}", summary);
        }
    }
    
    private void showValidationResults(ValidationEngine.ValidationResult result) {
        StringBuilder message = new StringBuilder();
        
        if (!result.getErrors().isEmpty()) {
            message.append("Errors:\n");
            for (ValidationEngine.ValidationError error : result.getErrors()) {
                message.append("• ").append(error.getMessage()).append("\n");
            }
        }
        
        if (!result.getWarnings().isEmpty()) {
            if (message.length() > 0) message.append("\n");
            message.append("Warnings:\n");
            for (ValidationEngine.ValidationWarning warning : result.getWarnings()) {
                message.append("• ").append(warning.getMessage()).append("\n");
            }
        }
        
        int messageType = result.isValid() ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE;
        String title = result.isValid() ? "Validation Warnings" : "Validation Errors";
        
        JOptionPane.showMessageDialog(mainWindow, message.toString(), title, messageType);
    }
    
    public void previewForm() {
        WindowDefinition window = formCanvas.getCurrentWindow();
        if (window != null) {
            try {
                String json = jsonGenerator.generateJson(window);
                showJsonPreview(json);
                mainWindow.updateStatus("Generated JSON preview");
            } catch (Exception e) {
                logger.error("Failed to generate JSON preview", e);
                mainWindow.updateStatus("Failed to generate preview");
                JOptionPane.showMessageDialog(mainWindow,
                    "Failed to generate JSON preview: " + e.getMessage(),
                    "Preview Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showJsonPreview(String json) {
        JTextArea textArea = new JTextArea(json);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JDialog dialog = new JDialog(mainWindow, "JSON Preview", true);
        dialog.add(scrollPane);
        dialog.pack();
        dialog.setLocationRelativeTo(mainWindow);
        dialog.setVisible(true);
    }
    
    public void exportJson() {
        WindowDefinition window = formCanvas.getCurrentWindow();
        if (window != null) {
            // Validate before export
            ValidationEngine.ValidationResult result = validationEngine.validateWindow(window);
            if (!result.isValid()) {
                int choice = JOptionPane.showConfirmDialog(mainWindow,
                    "Window has validation errors. Export anyway?",
                    "Validation Errors",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Export (same as save for now)
            saveAsFile();
        }
    }
    
    private void validateCurrentWindow() {
        WindowDefinition window = formCanvas.getCurrentWindow();
        if (window != null) {
            ValidationEngine.ValidationResult result = validationEngine.validateWindow(window);
            String summary = result.getSummary();
            boolean isValid = result.isValid();
            mainWindow.updateValidationStatus(isValid, summary);
        }
    }
    
    private boolean confirmDiscardChanges() {
        if (!hasUnsavedChanges) return true;
        
        int result = JOptionPane.showConfirmDialog(mainWindow,
            "You have unsaved changes. Discard them?",
            "Unsaved Changes",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        return result == JOptionPane.YES_OPTION;
    }
    
    private void updateWindowTitle() {
        String title = "JSONFormMaker - ADUI JSON Craft Studio";
        
        if (currentFile != null) {
            title += " - " + currentFile.getName();
        } else if (currentWindow != null) {
            title += " - " + currentWindow.getName();
        }
        
        if (hasUnsavedChanges) {
            title += " *";
        }
        
        mainWindow.setTitle(title);
    }
    
    // FormCanvas.FormCanvasListener implementation
    @Override
    public void windowChanged(WindowDefinition window) {
        this.currentWindow = window;
        propertyInspector.setCurrentWindow(window);
        hasUnsavedChanges = true;
        updateWindowTitle();
        validateCurrentWindow();
        
        mainWindow.updateStatus("Window modified");
    }
    
    @Override
    public void tabChanged(TabDefinition tab) {
        mainWindow.updateStatus("Tab changed: " + tab.getName());
    }
    
    @Override
    public void fieldSelected(FieldDefinition field) {
        // Update property inspector with selected field (handles null properly)
        propertyInspector.setSelectedField(field);

        if (field == null) {
            mainWindow.updateStatus("No field selected");
            logger.debug("Field selection cleared");
        } else {
            mainWindow.updateStatus("Field selected: " + field.getName());
            logger.debug("Field selected: {}", field.getFieldId());
        }
    }
    
    // PropertyInspector.PropertyChangeListener implementation
    @Override
    public void propertyChanged(FieldDefinition field) {
        hasUnsavedChanges = true;
        updateWindowTitle();
        validateCurrentWindow();
        
        mainWindow.updateStatus("Field properties updated");
    }
    
    // ComponentPalette.ComponentPaletteListener implementation
    @Override
    public void componentSelected(ComponentType type) {
        mainWindow.updateStatus("Component selected: " + type.getJsonName());
    }
    
    // Getters for main window to access
    public RefactoredFormCanvas getFormCanvas() { return formCanvas; }
    public PropertyInspector getPropertyInspector() { return propertyInspector; }
    public ComponentPalette getComponentPalette() { return componentPalette; }
}
