package com.adui.jsoncraft.canvas.refactored;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.events.FieldSelectionEvent;
import com.adui.jsoncraft.canvas.refactored.events.FormCanvasEventBus;
import com.adui.jsoncraft.canvas.refactored.events.FormChangeEvent;
import com.adui.jsoncraft.canvas.refactored.managers.FieldManager;
import com.adui.jsoncraft.canvas.refactored.managers.SelectionManager;
import com.adui.jsoncraft.model.ComponentType;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;

/**
 * FormCanvas Model - MVC Pattern Implementation
 * Manages form data state and business logic
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.FormCanvasModel
 */
public class FormCanvasModel {
    private static final Logger logger = LoggerFactory.getLogger(FormCanvasModel.class);
    
    // Model state
    private WindowDefinition currentWindow;
    private TabDefinition currentTab;
    private final List<ModelChangeListener> listeners;
    
    // Manager dependencies
    private final FormCanvasEventBus eventBus;
    private final FieldManager fieldManager;
    private final SelectionManager selectionManager;
    
    public FormCanvasModel() {
        this.listeners = new ArrayList<>();
        this.eventBus = FormCanvasEventBus.getInstance();
        this.fieldManager = new FieldManager();
        this.selectionManager = new SelectionManager();
        
        // Register for our own events to maintain state consistency
        registerEventListeners();
        
        logger.debug("FormCanvasModel initialized");
    }
    
    /**
     * Set current window
     */
    public void setCurrentWindow(WindowDefinition window) {
        WindowDefinition oldWindow = this.currentWindow;
        this.currentWindow = window;
        
        // Clear current tab if window changed
        if (oldWindow != window) {
            setCurrentTab(null);
        }
        
        // Select first tab if available
        if (window != null && window.getTabs() != null && !window.getTabs().isEmpty()) {
            setCurrentTab(window.getTabs().get(0));
        }
        
        // Fire event
        if (window != null) {
            eventBus.fire(window == oldWindow ? 
                FormChangeEvent.windowLoaded(window) : 
                FormChangeEvent.windowCreated(window));
        }
        
        // Notify listeners
        notifyListeners(ModelChangeEvent.Type.WINDOW_CHANGED, oldWindow, window);
        
        logger.debug("Current window set to: {}", 
            window != null ? window.getWindowId() : "null");
    }
    
    /**
     * Set current tab
     */
    public void setCurrentTab(TabDefinition tab) {
        // Validate tab belongs to current window
        if (tab != null && currentWindow != null) {
            if (!currentWindow.getTabs().contains(tab)) {
                logger.warn("Tab {} does not belong to current window {}", 
                    tab.getTabId(), currentWindow.getWindowId());
                return;
            }
        }
        
        TabDefinition oldTab = this.currentTab;
        this.currentTab = tab;
        
        // Clear selection when tab changes
        if (oldTab != tab) {
            selectionManager.clearSelection();
        }
        
        // Fire event
        if (tab != null && currentWindow != null) {
            eventBus.fire(FormChangeEvent.tabSelected(currentWindow, tab));
        }
        
        // Notify listeners
        notifyListeners(ModelChangeEvent.Type.TAB_CHANGED, oldTab, tab);
        
        logger.debug("Current tab set to: {}", 
            tab != null ? tab.getTabId() : "null");
    }
    
    /**
     * Create new window
     */
    public WindowDefinition createNewWindow() {
        WindowDefinition window = new WindowDefinition();
        window.setWindowId("NEW_WINDOW");
        window.setName("New Window");
        
        // Add default tab
        TabDefinition defaultTab = new TabDefinition();
        defaultTab.setTabId("TAB_001");
        defaultTab.setName("General");
        defaultTab.setSequence(10);
        
        window.addTab(defaultTab);
        
        setCurrentWindow(window);
        
        logger.debug("Created new window: {}", window.getWindowId());
        return window;
    }
    
    /**
     * Add new tab to current window
     */
    public TabDefinition addNewTab() {
        if (currentWindow == null) {
            logger.warn("Cannot add tab: no current window");
            return null;
        }
        
        TabDefinition tab = new TabDefinition();
        tab.setTabId("TAB_" + String.format("%03d", currentWindow.getTabCount() + 1));
        tab.setName("Tab " + (currentWindow.getTabCount() + 1));
        tab.setSequence((currentWindow.getTabCount() + 1) * 10);
        
        currentWindow.addTab(tab);
        
        // Fire event
        eventBus.fire(FormChangeEvent.tabAdded(currentWindow, tab));
        
        // Switch to new tab
        setCurrentTab(tab);
        
        logger.debug("Added new tab: {}", tab.getTabId());
        return tab;
    }
    
    /**
     * Add field to current tab
     */
    public FieldDefinition addField(ComponentType componentType) {
        if (currentWindow == null || currentTab == null) {
            logger.warn("Cannot add field: no current window or tab");
            return null;
        }
        
        return fieldManager.addField(currentWindow, currentTab, componentType);
    }
    
    /**
     * Remove field from current tab
     */
    public boolean removeField(FieldDefinition field) {
        if (currentWindow == null || currentTab == null || field == null) {
            logger.warn("Cannot remove field: invalid state or null field");
            return false;
        }
        
        // Clear selection if removing selected field
        if (selectionManager.isSelected(field)) {
            selectionManager.clearSelection();
        }
        
        return fieldManager.removeField(currentWindow, currentTab, field);
    }
    
    /**
     * Move field within current tab
     */
    public boolean moveField(int fromIndex, int toIndex) {
        if (currentWindow == null || currentTab == null) {
            logger.warn("Cannot move field: no current window or tab");
            return false;
        }
        
        return fieldManager.moveField(currentWindow, currentTab, fromIndex, toIndex);
    }
    
    /**
     * Register event listeners
     */
    private void registerEventListeners() {
        // Listen for selection events to maintain consistency
        eventBus.register(FieldSelectionEvent.class, this::handleFieldSelection);
    }
    
    /**
     * Handle field selection events
     */
    private void handleFieldSelection(FieldSelectionEvent event) {
        FieldDefinition selectedField = event.getSelectedField();
        notifyListeners(ModelChangeEvent.Type.FIELD_SELECTION_CHANGED, 
        	    event.getPreviousField(), selectedField);
    }
    
    /**
     * Add model change listener
     */
    public void addModelChangeListener(ModelChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove model change listener
     */
    public void removeModelChangeListener(ModelChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners of model changes
     */
    private void notifyListeners(ModelChangeEvent.Type type, Object oldValue, Object newValue) {
        ModelChangeEvent event = new ModelChangeEvent(type, oldValue, newValue);
        
        for (ModelChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onModelChanged(event);
            } catch (Exception e) {
                logger.error("Error in model change listener: {}", e.getMessage(), e);
            }
        }
    }
    
    // Getters
    public WindowDefinition getCurrentWindow() { return currentWindow; }
    public TabDefinition getCurrentTab() { return currentTab; }
    public FieldManager getFieldManager() { return fieldManager; }
    public SelectionManager getSelectionManager() { return selectionManager; }
    
    /**
     * Model change event
     */
    public static class ModelChangeEvent {
        public enum Type {
            WINDOW_CHANGED,
            TAB_CHANGED,
            FIELD_SELECTION_CHANGED
        }
        
        private final Type type;
        private final Object oldValue;
        private final Object newValue;
        private final long timestamp;
        
        public ModelChangeEvent(Type type, Object oldValue, Object newValue) {
            this.type = type;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Type getType() { return type; }
        public Object getOldValue() { return oldValue; }
        public Object getNewValue() { return newValue; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Model change listener interface
     */
    @FunctionalInterface
    public interface ModelChangeListener {
        void onModelChanged(ModelChangeEvent event);
    }
}
