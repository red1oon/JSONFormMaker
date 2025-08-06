package com.adui.jsoncraft.model;

import java.util.HashMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; 
import java.util.Map;

/**
 * Data model for ADUI Field Definition
 * Represents a form field with its properties and configuration
 */
public class FieldDefinition {
    private String fieldId;
    private String name;
    private ComponentType componentType;
    private int sequence;
    private boolean isRequired;
    private boolean isReadOnly;
    private boolean isDisplayed;
    private String description;
    private String help;
    private String displayLogic;
    
    // Validation rules
    private ValidationRules validation;
    
    // UI configuration
    private Map<String, Object> ui;
    
    // Component-specific data
    private Map<String, Object> data;
    
    // Reference data for dropdowns/selections
    private ReferenceData reference;
    
    public FieldDefinition() {
        this.sequence = 10;
        this.isRequired = false;
        this.isReadOnly = false;
        this.isDisplayed = true;
        this.validation = new ValidationRules();
        this.ui = new HashMap<>();
        this.data = new HashMap<>();
    }
    
    public FieldDefinition(String fieldId, String name, ComponentType componentType) {
        this();
        this.fieldId = fieldId;
        this.name = name;
        this.componentType = componentType;
    }
    
    // Basic property getters and setters
    public String getFieldId() { 
        return fieldId; 
    }
    
    public void setFieldId(String fieldId) { 
        this.fieldId = fieldId; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public ComponentType getComponentType() { 
        return componentType; 
    }
    
    public void setComponentType(ComponentType componentType) { 
        this.componentType = componentType; 
    }
    
    public int getSequence() { 
        return sequence; 
    }
    
    public void setSequence(int sequence) { 
        this.sequence = sequence; 
    }
    
    public boolean isRequired() { 
        return isRequired; 
    }
    
    public void setRequired(boolean required) { 
        isRequired = required; 
    }
    
    public boolean isReadOnly() { 
        return isReadOnly; 
    }
    
    public void setReadOnly(boolean readOnly) { 
        isReadOnly = readOnly; 
    }
    
    public boolean isDisplayed() { 
        return isDisplayed; 
    }
    
    public void setDisplayed(boolean displayed) { 
        isDisplayed = displayed; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public String getHelp() { 
        return help; 
    }
    
    public void setHelp(String help) { 
        this.help = help; 
    }
    
    public String getDisplayLogic() { 
        return displayLogic; 
    }
    
    public void setDisplayLogic(String displayLogic) { 
        this.displayLogic = displayLogic; 
    }
    
    public ValidationRules getValidation() { 
        return validation; 
    }
    
    public void setValidation(ValidationRules validation) { 
        this.validation = validation; 
    }
    
    public Map<String, Object> getUi() { 
        return ui; 
    }
    
    public void setUi(Map<String, Object> ui) { 
        this.ui = ui; 
    }
    
    public Map<String, Object> getData() { 
        return data; 
    }
    
    public void setData(Map<String, Object> data) { 
        this.data = data; 
    }
    
    public ReferenceData getReference() { 
        return reference; 
    }
    
    public void setReference(ReferenceData reference) { 
        this.reference = reference; 
    }
    
    // Type-safe UI property methods
    
    /**
     * Get UI property as String (type-safe)
     * Handles Boolean, Integer, and other types by converting to String
     */
    public String getUiProperty(String key) {
        if (ui == null) return null;
        Object value = ui.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Get UI property as Boolean
     */
    public boolean getBooleanUiProperty(String key) {
        if (ui == null) return false;
        Object value = ui.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }
    
    /**
     * Get UI property as Integer with default value
     */
    public int getIntUiProperty(String key, int defaultValue) {
        if (ui == null) return defaultValue;
        Object value = ui.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Get UI property as Double with default value
     */
    public double getDoubleUiProperty(String key, double defaultValue) {
        if (ui == null) return defaultValue;
        Object value = ui.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Set UI property (accepts any type)
     */
    public void setUiProperty(String key, Object value) {
        if (ui == null) {
            ui = new HashMap<>();
        }
        ui.put(key, value);
    }
    
    /**
     * Remove UI property
     */
    public void removeUiProperty(String key) {
        if (ui != null) {
            ui.remove(key);
        }
    }
    
    /**
     * Check if UI property exists
     */
    public boolean hasUiProperty(String key) {
        return ui != null && ui.containsKey(key);
    }
    
    // Type-safe data property methods
    
    /**
     * Get data property as Object
     */
    public Object getDataProperty(String key) {
        if (data == null) return null;
        
        Object value = data.get(key);
        
        // Apply same JSON string detection as JsonParser
        if (value instanceof String && isJsonString((String) value)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode parsed = objectMapper.readTree((String) value);
                return convertJsonNodeToObject(parsed);
            } catch (Exception e) {
                // Keep as string if parsing fails
            }
        }
        
        return value;
    }

    /**
     * Helper method to detect JSON strings
     */
    private boolean isJsonString(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        str = str.trim();
        return (str.startsWith("{") && str.endsWith("}")) || 
               (str.startsWith("[") && str.endsWith("]"));
    }

    /**
     * Convert JsonNode to native Java objects
     */
    private Object convertJsonNodeToObject(JsonNode node) {
        if (node.isNull()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isDouble()) return node.asDouble();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(convertJsonNodeToObject(item));
            }
            return list;
        }
        if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(entry -> {
                map.put(entry.getKey(), convertJsonNodeToObject(entry.getValue()));
            });
            return map;
        }
        return node.toString();
    }
    
    /**
     * Get data property as String
     */
    public String getStringDataProperty(String key) {
        Object value = getDataProperty(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Get data property as Boolean
     */
    public boolean getBooleanDataProperty(String key) {
        Object value = getDataProperty(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }
    
    /**
     * Get data property as Integer with default value
     */
    public int getIntDataProperty(String key, int defaultValue) {
        Object value = getDataProperty(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Get data property as Double with default value
     */
    public double getDoubleDataProperty(String key, double defaultValue) {
        Object value = getDataProperty(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Set data property
     */
    public void setDataProperty(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }
    
    /**
     * Remove data property
     */
    public void removeDataProperty(String key) {
        if (data != null) {
            data.remove(key);
        }
    }
    
    /**
     * Check if data property exists
     */
    public boolean hasDataProperty(String key) {
        return data != null && data.containsKey(key);
    }
    
    // Validation and utility methods
    
    /**
     * Check if this field definition is valid
     */
    public boolean isValid() {
        return fieldId != null && !fieldId.trim().isEmpty() &&
               name != null && !name.trim().isEmpty() &&
               componentType != null;
    }
    
    /**
     * Create a copy of this field definition
     */
    public FieldDefinition copy() {
        FieldDefinition copy = new FieldDefinition();
        copy.fieldId = this.fieldId;
        copy.name = this.name;
        copy.componentType = this.componentType;
        copy.sequence = this.sequence;
        copy.isRequired = this.isRequired;
        copy.isReadOnly = this.isReadOnly;
        copy.isDisplayed = this.isDisplayed;
        copy.description = this.description;
        copy.help = this.help;
        copy.displayLogic = this.displayLogic;
        
        // Deep copy validation
        if (this.validation != null) {
            copy.validation = this.validation.copy();
        }
        
        // Deep copy maps
        if (this.ui != null) {
            copy.ui = new HashMap<>(this.ui);
        }
        
        if (this.data != null) {
            copy.data = new HashMap<>(this.data);
        }
        
        // Deep copy reference
        if (this.reference != null) {
            copy.reference = this.reference.copy();
        }
        
        return copy;
    }
    
    /**
     * Reset field to default state while preserving ID and type
     */
    public void reset() {
        this.name = componentType != null ? componentType.getJsonName() : "New Field";
        this.sequence = 10;
        this.isRequired = false;
        this.isReadOnly = false;
        this.isDisplayed = true;
        this.description = null;
        this.help = null;
        this.displayLogic = null;
        
        // Create new ValidationRules instead of calling reset()
        this.validation = new ValidationRules();
        
        if (this.ui == null) {
            this.ui = new HashMap<>();
        } else {
            this.ui.clear();
        }
        
        if (this.data == null) {
            this.data = new HashMap<>();
        } else {
            this.data.clear();
        }
        
        this.reference = null;
    }
    
    @Override
    public String toString() {
        return String.format("Field[id=%s, name=%s, type=%s]", 
            fieldId, name, componentType != null ? componentType.getJsonName() : "null");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FieldDefinition other = (FieldDefinition) obj;
        return fieldId != null ? fieldId.equals(other.fieldId) : other.fieldId == null;
    }
    
    @Override
    public int hashCode() {
        return fieldId != null ? fieldId.hashCode() : 0;
    }
}