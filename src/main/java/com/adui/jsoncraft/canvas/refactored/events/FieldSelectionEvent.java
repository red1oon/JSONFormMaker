package com.adui.jsoncraft.canvas.refactored.events;

import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Event fired when field selection changes in FormCanvas
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.events.FieldSelectionEvent
 */
public class FieldSelectionEvent {
    
    public enum Type {
        SELECTED,   // Field was selected
        DESELECTED, // Field was deselected
        CHANGED     // Selection changed from one field to another
    }
    
    private final Type type;
    private final FieldDefinition selectedField;
    private final FieldDefinition previousField;
    private final long timestamp;
    
    /**
     * Create field selection event
     */
    public FieldSelectionEvent(Type type, FieldDefinition selectedField, FieldDefinition previousField) {
        this.type = type;
        this.selectedField = selectedField;
        this.previousField = previousField;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Create field selected event
     */
    public static FieldSelectionEvent selected(FieldDefinition field) {
        return new FieldSelectionEvent(Type.SELECTED, field, null);
    }
    
    /**
     * Create field deselected event
     */
    public static FieldSelectionEvent deselected(FieldDefinition field) {
        return new FieldSelectionEvent(Type.DESELECTED, null, field);
    }
    
    /**
     * Create field changed event
     */
    public static FieldSelectionEvent changed(FieldDefinition newField, FieldDefinition previousField) {
        return new FieldSelectionEvent(Type.CHANGED, newField, previousField);
    }
    
    // Getters
    public Type getType() { return type; }
    public FieldDefinition getSelectedField() { return selectedField; }
    public FieldDefinition getPreviousField() { return previousField; }
    public long getTimestamp() { return timestamp; }
    
    public boolean hasSelectedField() { return selectedField != null; }
    public boolean hasPreviousField() { return previousField != null; }
    
    @Override
    public String toString() {
        return String.format("FieldSelectionEvent[type=%s, selected=%s, previous=%s]",
            type,
            selectedField != null ? selectedField.getFieldId() : "null",
            previousField != null ? previousField.getFieldId() : "null");
    }
}
