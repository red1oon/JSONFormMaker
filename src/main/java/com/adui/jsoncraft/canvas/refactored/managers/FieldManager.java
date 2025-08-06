package com.adui.jsoncraft.canvas.refactored.managers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.events.FormCanvasEventBus;
import com.adui.jsoncraft.canvas.refactored.events.FormChangeEvent;
import com.adui.jsoncraft.model.ComponentType;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;

/**
 * Manages field operations using Strategy pattern
 * Provides centralized field CRUD operations with event notification
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.managers.FieldManager
 */
public class FieldManager {
    private static final Logger logger = LoggerFactory.getLogger(FieldManager.class);
    
    private final FormCanvasEventBus eventBus;
    private FieldCreationStrategy creationStrategy;
    
    public FieldManager() {
        this.eventBus = FormCanvasEventBus.getInstance();
        this.creationStrategy = new DefaultFieldCreationStrategy();
        logger.debug("FieldManager initialized");
    }
    
    /**
     * Add a new field to the specified tab
     */
    public FieldDefinition addField(WindowDefinition window, TabDefinition tab, ComponentType componentType) {
        if (window == null || tab == null || componentType == null) {
            logger.warn("Cannot add field: null window, tab, or component type");
            return null;
        }
        
        // Create field using strategy
        FieldDefinition field = creationStrategy.createField(componentType, tab.getFields().size());
        
        // Add to tab
        tab.addField(field);
        
        // Fire event
        eventBus.fire(FormChangeEvent.fieldAdded(window, tab, field));
        
        logger.debug("Added field {} to tab {}", field.getFieldId(), tab.getTabId());
        return field;
    }
    
    /**
     * Remove a field from its tab
     */
    public boolean removeField(WindowDefinition window, TabDefinition tab, FieldDefinition field) {
        if (window == null || tab == null || field == null) {
            logger.warn("Cannot remove field: null parameters");
            return false;
        }
        
        boolean removed = tab.getFields().remove(field);
        if (removed) {
            // Update sequences
            updateFieldSequences(tab);
            
            // Fire event
            eventBus.fire(FormChangeEvent.fieldRemoved(window, tab, field));
            
            logger.debug("Removed field {} from tab {}", field.getFieldId(), tab.getTabId());
        }
        
        return removed;
    }
    
    /**
     * Move a field within the same tab
     */
    public boolean moveField(WindowDefinition window, TabDefinition tab, int fromIndex, int toIndex) {
        if (window == null || tab == null) {
            logger.warn("Cannot move field: null window or tab");
            return false;
        }
        
        List<FieldDefinition> fields = tab.getFields();
        if (fromIndex < 0 || fromIndex >= fields.size() || toIndex < 0 || toIndex > fields.size()) {
            logger.warn("Cannot move field: invalid indices {} -> {}", fromIndex, toIndex);
            return false;
        }
        
        if (fromIndex == toIndex) {
            return true; // No move needed
        }
        
        // Perform the move
        FieldDefinition field = fields.remove(fromIndex);
        int insertIndex = toIndex > fromIndex ? toIndex - 1 : toIndex;
        fields.add(insertIndex, field);
        
        // Update sequences
        updateFieldSequences(tab);
        
        // Fire event
        eventBus.fire(FormChangeEvent.fieldMoved(window, tab, field));
        
        logger.debug("Moved field {} from {} to {} in tab {}", 
            field.getFieldId(), fromIndex, insertIndex, tab.getTabId());
        
        return true;
    }
    
    /**
     * Copy a field within the same tab or to another tab
     */
    public FieldDefinition copyField(WindowDefinition window, TabDefinition sourceTab, 
                                   TabDefinition targetTab, FieldDefinition field) {
        if (window == null || sourceTab == null || targetTab == null || field == null) {
            logger.warn("Cannot copy field: null parameters");
            return null;
        }
        
        // Create copy with new ID
        FieldDefinition copy = field.copy();
        copy.setFieldId(generateUniqueFieldId(window, field.getFieldId()));
        copy.setSequence((targetTab.getFields().size() + 1) * 10);
        
        // Add to target tab
        targetTab.addField(copy);
        
        // Fire event
        eventBus.fire(FormChangeEvent.fieldAdded(window, targetTab, copy));
        
        logger.debug("Copied field {} to {} in tab {}", 
            field.getFieldId(), copy.getFieldId(), targetTab.getTabId());
        
        return copy;
    }
    
    /**
     * Find a field by ID in the window
     */
    public FieldDefinition findField(WindowDefinition window, String fieldId) {
        if (window == null || fieldId == null) {
            return null;
        }
        
        for (TabDefinition tab : window.getTabs()) {
            FieldDefinition field = tab.getField(fieldId);
            if (field != null) {
                return field;
            }
        }
        
        return null;
    }
    
    /**
     * Get all fields in the window
     */
    public List<FieldDefinition> getAllFields(WindowDefinition window) {
        List<FieldDefinition> allFields = new ArrayList<>();
        
        if (window != null && window.getTabs() != null) {
            for (TabDefinition tab : window.getTabs()) {
                if (tab.getFields() != null) {
                    allFields.addAll(tab.getFields());
                }
            }
        }
        
        return allFields;
    }
    
    /**
     * Update field sequences to maintain order
     */
    private void updateFieldSequences(TabDefinition tab) {
        if (tab != null && tab.getFields() != null) {
            List<FieldDefinition> fields = tab.getFields();
            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).setSequence((i + 1) * 10);
            }
        }
    }
    
    /**
     * Generate unique field ID
     */
    private String generateUniqueFieldId(WindowDefinition window, String baseId) {
        String newId = baseId;
        int counter = 1;
        
        while (findField(window, newId) != null) {
            newId = baseId + "_" + counter;
            counter++;
        }
        
        return newId;
    }
    
    /**
     * Set field creation strategy
     */
    public void setCreationStrategy(FieldCreationStrategy strategy) {
        this.creationStrategy = strategy != null ? strategy : new DefaultFieldCreationStrategy();
    }
    
    /**
     * Field creation strategy interface
     */
    public interface FieldCreationStrategy {
        FieldDefinition createField(ComponentType componentType, int position);
    }
    
    /**
     * Default field creation strategy
     */
    public static class DefaultFieldCreationStrategy implements FieldCreationStrategy {
        
        @Override
        public FieldDefinition createField(ComponentType componentType, int position) {
            String fieldId = generateFieldId(componentType, position);
            String fieldName = componentType.getJsonName();
            
            FieldDefinition field = new FieldDefinition(fieldId, fieldName, componentType);
            field.setSequence((position + 1) * 10);
            
            // Set component-specific defaults
            switch (componentType) {
                case TEXT_FIELD:
                    field.setUiProperty("placeholder", "Enter text...");
                    break;
                case NUMBER_FIELD:
                    field.getValidation().setMin(0);
                    break;
                case EMAIL_FIELD:
                    field.setUiProperty("placeholder", "user@example.com");
                    field.getValidation().setPattern("^[A-Za-z0-9+_.-]+@(.+)$");
                    break;
                case DATE_FIELD:
                    field.setUiProperty("format", "yyyy-MM-dd");
                    break;
                case YES_NO_FIELD:
                    field.setUiProperty("displayStyle", "toggle");
                    break;
                // Add more component-specific defaults as needed
            }
            
            return field;
        }
        
        private String generateFieldId(ComponentType componentType, int position) {
            String prefix = componentType.name().toUpperCase();
            return prefix + "_" + String.format("%03d", position + 1);
        }
    }
}
