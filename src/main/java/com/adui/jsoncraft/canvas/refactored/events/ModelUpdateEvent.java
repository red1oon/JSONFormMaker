package com.adui.jsoncraft.canvas.refactored.events;

import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;

/**
 * Event fired when model objects are updated (extends existing event system)
 * Specifically for property changes that need to be persisted
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.events.ModelUpdateEvent
 */
public class ModelUpdateEvent {
    
    public enum Type {
        // Window property updates
        WINDOW_ID_CHANGED,
        WINDOW_NAME_CHANGED,
        WINDOW_DESCRIPTION_CHANGED,
        WINDOW_TYPE_CHANGED,
        
        // Tab property updates
        TAB_ID_CHANGED,
        TAB_NAME_CHANGED,
        TAB_DESCRIPTION_CHANGED,
        TAB_SEQUENCE_CHANGED,
        
        // Field property updates
        FIELD_ID_CHANGED,
        FIELD_NAME_CHANGED,
        FIELD_DESCRIPTION_CHANGED,
        FIELD_COMPONENT_TYPE_CHANGED,
        FIELD_VALIDATION_CHANGED,
        FIELD_UI_PROPERTIES_CHANGED,
        FIELD_DATA_PROPERTIES_CHANGED,
        
        // Model state changes
        MODEL_LOADED,
        MODEL_RESET,
        PERSISTENCE_REQUIRED
    }
    
    private final Type type;
    private final WindowDefinition window;
    private final TabDefinition tab;
    private final FieldDefinition field;
    private final String propertyName;
    private final Object oldValue;
    private final Object newValue;
    private final long timestamp;
    
    /**
     * Create model update event
     */
    public ModelUpdateEvent(Type type, WindowDefinition window, TabDefinition tab, 
                           FieldDefinition field, String propertyName, Object oldValue, Object newValue) {
        this.type = type;
        this.window = window;
        this.tab = tab;
        this.field = field;
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Factory methods for common model updates
    public static ModelUpdateEvent windowPropertyChanged(WindowDefinition window, String propertyName, 
                                                       Object oldValue, Object newValue) {
        Type type = getWindowUpdateType(propertyName);
        return new ModelUpdateEvent(type, window, null, null, propertyName, oldValue, newValue);
    }
    
    public static ModelUpdateEvent tabPropertyChanged(TabDefinition tab, String propertyName, 
                                                    Object oldValue, Object newValue) {
        Type type = getTabUpdateType(propertyName);
        return new ModelUpdateEvent(type, null, tab, null, propertyName, oldValue, newValue);
    }
    
    public static ModelUpdateEvent fieldPropertyChanged(FieldDefinition field, String propertyName, 
                                                      Object oldValue, Object newValue) {
        Type type = getFieldUpdateType(propertyName);
        return new ModelUpdateEvent(type, null, null, field, propertyName, oldValue, newValue);
    }
    
    public static ModelUpdateEvent modelLoaded(WindowDefinition window) {
        return new ModelUpdateEvent(Type.MODEL_LOADED, window, null, null, null, null, null);
    }
    
    public static ModelUpdateEvent persistenceRequired() {
        return new ModelUpdateEvent(Type.PERSISTENCE_REQUIRED, null, null, null, null, null, null);
    }
    
    /**
     * Determine update type from property name
     */
    private static Type getWindowUpdateType(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "windowid": return Type.WINDOW_ID_CHANGED;
            case "name": return Type.WINDOW_NAME_CHANGED;
            case "description": return Type.WINDOW_DESCRIPTION_CHANGED;
            case "windowtype": return Type.WINDOW_TYPE_CHANGED;
            default: return Type.PERSISTENCE_REQUIRED;
        }
    }
    
    private static Type getTabUpdateType(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "tabid": return Type.TAB_ID_CHANGED;
            case "name": return Type.TAB_NAME_CHANGED;
            case "description": return Type.TAB_DESCRIPTION_CHANGED;
            case "sequence": return Type.TAB_SEQUENCE_CHANGED;
            default: return Type.PERSISTENCE_REQUIRED;
        }
    }
    
    private static Type getFieldUpdateType(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "fieldid": return Type.FIELD_ID_CHANGED;
            case "name": return Type.FIELD_NAME_CHANGED;
            case "description": return Type.FIELD_DESCRIPTION_CHANGED;
            case "componenttype": return Type.FIELD_COMPONENT_TYPE_CHANGED;
            case "validation": return Type.FIELD_VALIDATION_CHANGED;
            case "ui": return Type.FIELD_UI_PROPERTIES_CHANGED;
            case "data": return Type.FIELD_DATA_PROPERTIES_CHANGED;
            default: return Type.PERSISTENCE_REQUIRED;
        }
    }
    
    // Getters
    public Type getType() { return type; }
    public WindowDefinition getWindow() { return window; }
    public TabDefinition getTab() { return tab; }
    public FieldDefinition getField() { return field; }
    public String getPropertyName() { return propertyName; }
    public Object getOldValue() { return oldValue; }
    public Object getNewValue() { return newValue; }
    public long getTimestamp() { return timestamp; }
    
    /**
     * Check if this update requires persistence
     */
    public boolean requiresPersistence() {
        return type != Type.MODEL_LOADED; // All changes except loading require persistence
    }
    
    /**
     * Get description of the change
     */
    public String getChangeDescription() {
        if (propertyName != null) {
            return String.format("Property '%s' changed from '%s' to '%s'", 
                propertyName, oldValue, newValue);
        }
        return type.toString();
    }
    
    @Override
    public String toString() {
        return String.format("ModelUpdateEvent[type=%s, property=%s, old=%s, new=%s]", 
            type, propertyName, oldValue, newValue);
    }
}
