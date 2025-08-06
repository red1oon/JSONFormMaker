package com.adui.jsoncraft.canvas.refactored.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.events.FieldSelectionEvent;
import com.adui.jsoncraft.canvas.refactored.events.FormCanvasEventBus;
import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Manages field selection state using Strategy pattern
 * Provides centralized selection logic with event notification
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.managers.SelectionManager
 */
public class SelectionManager {
    private static final Logger logger = LoggerFactory.getLogger(SelectionManager.class);
    
    private FieldDefinition selectedField;
    private final FormCanvasEventBus eventBus;
    
    public SelectionManager() {
        this.eventBus = FormCanvasEventBus.getInstance();
        logger.debug("SelectionManager initialized");
    }
    
    /**
     * Select a field
     */
    public void selectField(FieldDefinition field) {
        if (field == null) {
            clearSelection();
            return;
        }
        
        FieldDefinition previousField = this.selectedField;
        
        // Only change if different
        if (field.equals(previousField)) {
            logger.debug("Field already selected: {}", field.getFieldId());
            return;
        }
        
        this.selectedField = field;
        
        // Fire appropriate event
        if (previousField == null) {
            eventBus.fire(FieldSelectionEvent.selected(field));
        } else {
            eventBus.fire(FieldSelectionEvent.changed(field, previousField));
        }
        
        logger.debug("Field selected: {} (previous: {})", 
            field.getFieldId(), 
            previousField != null ? previousField.getFieldId() : "none");
    }
    
    /**
     * Clear selection
     */
    public void clearSelection() {
        if (selectedField != null) {
            FieldDefinition previousField = this.selectedField;
            this.selectedField = null;
            
            eventBus.fire(FieldSelectionEvent.deselected(previousField));
            logger.debug("Selection cleared (was: {})", previousField.getFieldId());
        }
    }
    
    /**
     * Get currently selected field
     */
    public FieldDefinition getSelectedField() {
        return selectedField;
    }
    
    /**
     * Check if a field is selected
     */
    public boolean hasSelection() {
        return selectedField != null;
    }
    
    /**
     * Check if specific field is selected
     */
    public boolean isSelected(FieldDefinition field) {
        return field != null && field.equals(selectedField);
    }
    
    /**
     * Selection strategy interface for extensibility
     */
    public interface SelectionStrategy {
        boolean canSelect(FieldDefinition field);
        void onFieldSelected(FieldDefinition field);
        void onSelectionCleared();
    }
    
    /**
     * Default selection strategy - allows all selections
     */
    public static class DefaultSelectionStrategy implements SelectionStrategy {
        @Override
        public boolean canSelect(FieldDefinition field) {
            return field != null;
        }
        
        @Override
        public void onFieldSelected(FieldDefinition field) {
            // Default: no special action
        }
        
        @Override
        public void onSelectionCleared() {
            // Default: no special action
        }
    }
    
    /**
     * Read-only selection strategy - prevents selection of read-only fields
     */
    public static class EditableOnlySelectionStrategy implements SelectionStrategy {
        @Override
        public boolean canSelect(FieldDefinition field) {
            return field != null && !field.isReadOnly();
        }
        
        @Override
        public void onFieldSelected(FieldDefinition field) {
            // Could add validation logic here
        }
        
        @Override
        public void onSelectionCleared() {
            // Default: no special action
        }
    }
}
