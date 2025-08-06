package com.adui.jsoncraft.canvas.refactored;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.components.FieldVisualizer;
import com.adui.jsoncraft.canvas.refactored.events.FieldSelectionEvent;
import com.adui.jsoncraft.canvas.refactored.events.FormCanvasEventBus;
import com.adui.jsoncraft.canvas.refactored.events.FormChangeEvent;
import com.adui.jsoncraft.canvas.refactored.managers.DragDropManager;
import com.adui.jsoncraft.canvas.refactored.managers.FieldManager;
import com.adui.jsoncraft.canvas.refactored.managers.SelectionManager;
import com.adui.jsoncraft.model.ComponentType;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;
import com.adui.jsoncraft.palette.ComponentPalette;

/**
 * FormCanvas Controller - MVC Pattern Implementation
 * Coordinates between Model, View, and business logic
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.FormCanvasController
 */
public class FormCanvasController implements ComponentPalette.ComponentPaletteListener {
    private static final Logger logger = LoggerFactory.getLogger(FormCanvasController.class);
    
    // MVC Components
    private final FormCanvasModel model;
    private final FormCanvasView view;
    
    // Managers
    private final FieldManager fieldManager;
    private final SelectionManager selectionManager;
    private final DragDropManager dragDropManager;
    
    // Event system
    private final FormCanvasEventBus eventBus;
    
    // Legacy listener support
    private final List<FormCanvasListener> legacyListeners;
    
    public FormCanvasController() {
        // Initialize MVC components
        this.model = new FormCanvasModel();
        
        // Get managers from model
        this.fieldManager = model.getFieldManager();
        this.selectionManager = model.getSelectionManager();
        this.dragDropManager = new DragDropManager(fieldManager);
        this.view = new FormCanvasView(dragDropManager);
        
        // Get event bus
        this.eventBus = FormCanvasEventBus.getInstance();
        
        // Initialize legacy support
        this.legacyListeners = new ArrayList<>();
        
        // Wire up components
        setupConnections();
        
        logger.debug("FormCanvasController initialized");
    }
    
    /**
     * Setup connections between MVC components
     */
    private void setupConnections() {
        // View -> Controller event handling
        view.addViewEventListener(this::handleViewEvent);
        
        // Model -> View updates
        model.addModelChangeListener(this::handleModelChange);
        
        // Enhanced event handling
        registerEventListeners();
        
        // Initialize with a new window
        createNewWindow();
    }
    
    /**
     * Register for FormCanvas events
     */
    private void registerEventListeners() {
        // Field deletion events (NEW ENHANCEMENT)
        eventBus.register(FieldVisualizer.FieldDeletionEvent.class, this::handleFieldDeletion);
        
        // Field copy events (NEW ENHANCEMENT) 
        eventBus.register(FieldVisualizer.FieldCopyEvent.class, this::handleFieldCopy);
        
        // Selection events for legacy listener support
        eventBus.register(FieldSelectionEvent.class, this::handleSelectionForLegacy);
        
        // Form change events for legacy listener support
        eventBus.register(FormChangeEvent.class, this::handleFormChangeForLegacy);
    }
    
    /**
     * Handle view events
     */
    private void handleViewEvent(FormCanvasView.ViewEvent event) {
        switch (event.getType()) {
            case ADD_TAB_REQUESTED:
                addNewTab();
                break;
                
            case TAB_SELECTED:
                if (event.getData() instanceof TabDefinition) {
                    model.setCurrentTab((TabDefinition) event.getData());
                }
                break;
                
            case WINDOW_PROPERTY_CHANGED:
                if (event.getData() instanceof WindowDefinition) {
                    updateWindowProperties((WindowDefinition) event.getData());
                }
                break;
            case TAB_PROPERTY_CHANGED:
                if (event.getData() instanceof TabDefinition) {
                    updateTabProperties((TabDefinition) event.getData());
                }
                break;
        }
    }
    
    /**
     * Update tab properties
     */
    private void updateTabProperties(TabDefinition updatedTab) {
        TabDefinition currentTab = model.getCurrentTab();
        if (currentTab != null) {
            // Update the actual tab properties
            currentTab.setTabId(updatedTab.getTabId());
            currentTab.setName(updatedTab.getName());
            currentTab.setDescription(updatedTab.getDescription());
            currentTab.setSequence(updatedTab.getSequence());
            
            // Notify of change via event bus
            eventBus.fire(FormChangeEvent.tabPropertyChanged(model.getCurrentWindow(), currentTab, null, null));
            
            logger.debug("Updated tab properties for: {}", currentTab.getTabId());
        }
    }
    
    /**
     * Handle model changes
     */
    private void handleModelChange(FormCanvasModel.ModelChangeEvent event) {
        switch (event.getType()) {
            case WINDOW_CHANGED:
                if (event.getNewValue() instanceof WindowDefinition) {
                    view.displayWindow((WindowDefinition) event.getNewValue());
                }
                break;
                
            case TAB_CHANGED:
                if (event.getNewValue() instanceof TabDefinition) {
                    view.selectTab((TabDefinition) event.getNewValue());
                }
                break;
                
            case FIELD_SELECTION_CHANGED:
                // View will handle this via event bus
                break;
        }
    }
    
    /**
     * Handle field deletion (NEW ENHANCEMENT)
     */
    private void handleFieldDeletion(FieldVisualizer.FieldDeletionEvent event) {
        FieldDefinition field = event.getField();
        boolean removed = model.removeField(field);
        
        if (removed) {
            logger.info("Field deleted: {}", field.getFieldId());
            JOptionPane.showMessageDialog(view, 
                "Field '" + field.getName() + "' has been deleted.", 
                "Field Deleted", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            logger.warn("Failed to delete field: {}", field.getFieldId());
            JOptionPane.showMessageDialog(view, 
                "Failed to delete field '" + field.getName() + "'.", 
                "Delete Failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handle field copy (NEW ENHANCEMENT)
     */
    private void handleFieldCopy(FieldVisualizer.FieldCopyEvent event) {
        FieldDefinition field = event.getField();
        WindowDefinition currentWindow = model.getCurrentWindow();
        TabDefinition currentTab = model.getCurrentTab();
        
        if (currentWindow != null && currentTab != null) {
            FieldDefinition copy = fieldManager.copyField(currentWindow, currentTab, currentTab, field);
            if (copy != null) {
                logger.info("Field copied: {} -> {}", field.getFieldId(), copy.getFieldId());
                
                // Select the new field
                selectionManager.selectField(copy);
                
                JOptionPane.showMessageDialog(view, 
                    "Field '" + field.getName() + "' has been copied as '" + copy.getName() + "'.", 
                    "Field Copied", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Handle selection events for legacy listener support
     */
    private void handleSelectionForLegacy(FieldSelectionEvent event) {
        FieldDefinition selectedField = event.getSelectedField();
        for (FormCanvasListener listener : legacyListeners) {
            try {
                listener.fieldSelected(selectedField);
            } catch (Exception e) {
                logger.error("Error in legacy field selection listener: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Handle form change events for legacy listener support
     */
    private void handleFormChangeForLegacy(FormChangeEvent event) {
        for (FormCanvasListener listener : legacyListeners) {
            try {
                switch (event.getType()) {
                    case WINDOW_CREATED:
                    case WINDOW_LOADED:
                        listener.windowChanged(event.getWindow());
                        break;
                    case TAB_SELECTED:
                        listener.tabChanged(event.getTab());
                        break;
                }
            } catch (Exception e) {
                logger.error("Error in legacy form change listener: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Create new window
     */
    public WindowDefinition createNewWindow() {
        return model.createNewWindow();
    }
    
    /**
     * Load window
     */
    public void loadWindow(WindowDefinition window) {
        model.setCurrentWindow(window);
    }
    
    /**
     * Add new tab
     */
    public TabDefinition addNewTab() {
        return model.addNewTab();
    }
    
    /**
     * Update window properties
     */
    private void updateWindowProperties(WindowDefinition updatedWindow) {
        WindowDefinition currentWindow = model.getCurrentWindow();
        if (currentWindow != null) {
            currentWindow.setWindowId(updatedWindow.getWindowId());
            currentWindow.setName(updatedWindow.getName());
            currentWindow.setDescription(updatedWindow.getDescription());
            
            // Notify of change
            eventBus.fire(FormChangeEvent.windowLoaded(currentWindow));
        }
    }
    
    // ComponentPalette.ComponentPaletteListener implementation
    @Override
    public void componentSelected(ComponentType componentType) {
        // Add field of selected type to current tab
        FieldDefinition field = model.addField(componentType);
        if (field != null) {
            // Select the newly added field
            selectionManager.selectField(field);
            logger.debug("Added and selected new field: {} of type {}", 
                field.getFieldId(), componentType.getJsonName());
        }
    }
    
    // Legacy FormCanvas API support for backward compatibility
    
    /**
     * Get current window (legacy API)
     */
    public WindowDefinition getCurrentWindow() {
        return model.getCurrentWindow();
    }
    
    /**
     * Get current tab (legacy API)
     */
    public TabDefinition getCurrentTab() {
        return model.getCurrentTab();
    }
    
    /**
     * Get selected field (legacy API)
     */
    public FieldDefinition getSelectedField() {
        return selectionManager.getSelectedField();
    }
    
    /**
     * Add form canvas listener (legacy API)
     */
    public void addFormCanvasListener(FormCanvasListener listener) {
        if (listener != null && !legacyListeners.contains(listener)) {
            legacyListeners.add(listener);
        }
    }
    
    /**
     * Remove form canvas listener (legacy API)
     */
    public void removeFormCanvasListener(FormCanvasListener listener) {
        legacyListeners.remove(listener);
    }
    
    // Getters for components
    public FormCanvasModel getModel() { return model; }
    public FormCanvasView getView() { return view; }
    public FieldManager getFieldManager() { return fieldManager; }
    public SelectionManager getSelectionManager() { return selectionManager; }
    public DragDropManager getDragDropManager() { return dragDropManager; }
    
    /**
     * Legacy FormCanvas listener interface for backward compatibility
     */
    public interface FormCanvasListener {
        void windowChanged(WindowDefinition window);
        void tabChanged(TabDefinition tab);
        void fieldSelected(FieldDefinition field);
    }
}
