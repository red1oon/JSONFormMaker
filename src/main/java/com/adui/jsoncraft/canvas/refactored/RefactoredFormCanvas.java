package com.adui.jsoncraft.canvas.refactored;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.model.ComponentType;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;
import com.adui.jsoncraft.palette.ComponentPalette;

/**
 * Refactored FormCanvas - Drop-in Replacement for Original FormCanvas
 * Provides the same public API while using clean MVC architecture internally
 * 
 * @version 1.0 (Refactored)
 * @namespace com.adui.jsoncraft.canvas.refactored.RefactoredFormCanvas
 */
public class RefactoredFormCanvas extends JPanel implements ComponentPalette.ComponentPaletteListener {
    private static final Logger logger = LoggerFactory.getLogger(RefactoredFormCanvas.class);
    
    // MVC Controller handles all logic
    private final FormCanvasController controller;
    
    // Legacy listener support
    private final List<FormCanvasListener> listeners;
    
    public RefactoredFormCanvas() {
        this.controller = new FormCanvasController();
        this.listeners = new ArrayList<>();
        
        // Setup UI layout
        setupLayout();
        
        // Bridge legacy listeners to controller
        setupLegacyListenerBridge();
        
        logger.debug("RefactoredFormCanvas initialized as drop-in replacement");
    }
    
    /**
     * Setup layout with MVC view
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(controller.getView(), BorderLayout.CENTER);
    }
    
    /**
     * Bridge legacy listeners to controller
     */
    private void setupLegacyListenerBridge() {
        controller.addFormCanvasListener(new FormCanvasController.FormCanvasListener() {
            @Override
            public void windowChanged(WindowDefinition window) {
                notifyWindowChanged(window);
            }
            
            @Override
            public void tabChanged(TabDefinition tab) {
                notifyTabChanged(tab);
            }
            
            @Override
            public void fieldSelected(FieldDefinition field) {
                notifyFieldSelected(field);
            }
        });
        
        // ✅ NEW: Bridge FormCanvasView events to legacy listeners
        controller.getView().addViewEventListener(event -> {
            switch (event.getType()) {
                case WINDOW_PROPERTY_CHANGED:
                    WindowDefinition updatedWindow = (WindowDefinition) event.getData();
                    // ✅ This triggers ApplicationController.windowChanged()
                    notifyWindowChanged(updatedWindow);
                    logger.debug("✅ Window property change bridged to legacy listeners: {}", 
                        updatedWindow.getName());
                    break;
                    
                case TAB_PROPERTY_CHANGED:
                    TabDefinition updatedTab = (TabDefinition) event.getData();
                    // ✅ This triggers ApplicationController.tabChanged()  
                    notifyTabChanged(updatedTab);
                    logger.debug("✅ Tab property change bridged to legacy listeners: {}", 
                        updatedTab.getName());
                    break;
                    
                default:
                    // Other events handled by controller
                    break;
            }
        });
        
        logger.debug("✅ Legacy listener bridge setup with property change support");
    }
    
    // ===========================================
    // LEGACY API - BACKWARD COMPATIBILITY
    // ===========================================
    /**
     * Set current window (legacy API)
     */
    public void setCurrentWindow(WindowDefinition window) {
        controller.loadWindow(window);
    }
    /**
     * Create new window (legacy API)
     */
    public void createNewWindow() {
        controller.createNewWindow();
    }
    
    /**
     * Load window (legacy API)
     */
    public void loadWindow(WindowDefinition window) {
        controller.loadWindow(window);
    }
    
    /**
     * Get current window (legacy API)
     */
    public WindowDefinition getCurrentWindow() {
        return controller.getCurrentWindow();
    }
    
    /**
     * Get current tab (legacy API)
     */
    public TabDefinition getCurrentTab() {
        return controller.getCurrentTab();
    }
    
    /**
     * Get selected field (legacy API)
     */
    public FieldDefinition getSelectedField() {
        return controller.getSelectedField();
    }
    
    /**
     * Add form canvas listener (legacy API)
     */
    public void addFormCanvasListener(FormCanvasListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove form canvas listener (legacy API)
     */
    public void removeFormCanvasListener(FormCanvasListener listener) {
        listeners.remove(listener);
    }
    
    // ComponentPalette.ComponentPaletteListener implementation (delegate to controller)
    @Override
    public void componentSelected(ComponentType componentType) {
        controller.componentSelected(componentType);
    }
    
    // ===========================================
    // LEGACY EVENT NOTIFICATION
    // ===========================================
    
    private void notifyWindowChanged(WindowDefinition window) {
        for (FormCanvasListener listener : new ArrayList<>(listeners)) {
            try {
                listener.windowChanged(window);
            } catch (Exception e) {
                logger.error("Error in legacy window change listener: {}", e.getMessage(), e);
            }
        }
    }
    
    private void notifyTabChanged(TabDefinition tab) {
        for (FormCanvasListener listener : new ArrayList<>(listeners)) {
            try {
                listener.tabChanged(tab);
            } catch (Exception e) {
                logger.error("Error in legacy tab change listener: {}", e.getMessage(), e);
            }
        }
    }
    
    private void notifyFieldSelected(FieldDefinition field) {
        for (FormCanvasListener listener : new ArrayList<>(listeners)) {
            try {
                listener.fieldSelected(field);
            } catch (Exception e) {
                logger.error("Error in legacy field selection listener: {}", e.getMessage(), e);
            }
        }
    }
    
    // ===========================================
    // ENHANCED API - NEW FEATURES
    // ===========================================
    
    /**
     * Get the MVC controller for advanced access
     */
    public FormCanvasController getController() {
        return controller;
    }
    
    /**
     * Get the MVC model for advanced access
     */
    public FormCanvasModel getModel() {
        return controller.getModel();
    }
    
    /**
     * Get the MVC view for advanced access
     */
    public FormCanvasView getView() {
        return controller.getView();
    }
    
    /**
     * Get field manager for advanced field operations
     */
    public com.adui.jsoncraft.canvas.refactored.managers.FieldManager getFieldManager() {
        return controller.getFieldManager();
    }
    
    /**
     * Get selection manager for advanced selection control
     */
    public com.adui.jsoncraft.canvas.refactored.managers.SelectionManager getSelectionManager() {
        return controller.getSelectionManager();
    }
    
    /**
     * Legacy FormCanvas listener interface
     */
    public interface FormCanvasListener {
        void windowChanged(WindowDefinition window);
        void tabChanged(TabDefinition tab);
        void fieldSelected(FieldDefinition field);
    }
}
