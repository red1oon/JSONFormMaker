package com.adui.jsoncraft.canvas.refactored.events;

import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;

/**
 * Event fired when form structure changes
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.events.FormChangeEvent
 */
public class FormChangeEvent {
    
    public enum Type {
        // Window events
        WINDOW_CREATED,
        WINDOW_LOADED,
        WINDOW_PROPERTY_CHANGED,
        
        // Tab events
        TAB_ADDED,
        TAB_REMOVED,
        TAB_SELECTED,
        TAB_PROPERTY_CHANGED,
        
        // Field events
        FIELD_ADDED,
        FIELD_REMOVED,
        FIELD_MOVED,
        FIELD_PROPERTY_CHANGED
    }
    
    private final Type type;
    private final WindowDefinition window;
    private final TabDefinition tab;
    private final FieldDefinition field;
    private final Object oldValue;
    private final Object newValue;
    private final long timestamp;
    
    /**
     * Create form change event
     */
    public FormChangeEvent(Type type, WindowDefinition window, TabDefinition tab, 
                          FieldDefinition field, Object oldValue, Object newValue) {
        this.type = type;
        this.window = window;
        this.tab = tab;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Factory methods for common events
    public static FormChangeEvent windowCreated(WindowDefinition window) {
        return new FormChangeEvent(Type.WINDOW_CREATED, window, null, null, null, null);
    }
    
    public static FormChangeEvent windowLoaded(WindowDefinition window) {
        return new FormChangeEvent(Type.WINDOW_LOADED, window, null, null, null, null);
    }
    
    public static FormChangeEvent tabAdded(WindowDefinition window, TabDefinition tab) {
        return new FormChangeEvent(Type.TAB_ADDED, window, tab, null, null, null);
    }
    
    public static FormChangeEvent tabSelected(WindowDefinition window, TabDefinition tab) {
        return new FormChangeEvent(Type.TAB_SELECTED, window, tab, null, null, null);
    }
    
    public static FormChangeEvent fieldAdded(WindowDefinition window, TabDefinition tab, FieldDefinition field) {
        return new FormChangeEvent(Type.FIELD_ADDED, window, tab, field, null, null);
    }
    
    public static FormChangeEvent fieldRemoved(WindowDefinition window, TabDefinition tab, FieldDefinition field) {
        return new FormChangeEvent(Type.FIELD_REMOVED, window, tab, field, null, null);
    }
    
    public static FormChangeEvent fieldMoved(WindowDefinition window, TabDefinition tab, FieldDefinition field) {
        return new FormChangeEvent(Type.FIELD_MOVED, window, tab, field, null, null);
    }
    
    public static FormChangeEvent tabPropertyChanged(WindowDefinition window, TabDefinition tab, Object oldValue, Object newValue) {
        return new FormChangeEvent(Type.TAB_PROPERTY_CHANGED, window, tab, null, oldValue, newValue);
    }
    
    // Getters
    public Type getType() { return type; }
    public WindowDefinition getWindow() { return window; }
    public TabDefinition getTab() { return tab; }
    public FieldDefinition getField() { return field; }
    public Object getOldValue() { return oldValue; }
    public Object getNewValue() { return newValue; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("FormChangeEvent[type=%s, window=%s, tab=%s, field=%s]",
            type,
            window != null ? window.getWindowId() : "null",
            tab != null ? tab.getTabId() : "null",
            field != null ? field.getFieldId() : "null");
    }
}
