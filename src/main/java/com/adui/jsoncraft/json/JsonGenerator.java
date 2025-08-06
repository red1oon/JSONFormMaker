package com.adui.jsoncraft.json;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.ReferenceData;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.ValidationRules;
import com.adui.jsoncraft.model.WindowDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON Generator for ADUI Window Definitions
 * Converts WindowDefinition objects to valid ADUI JSON format
 * Based on ADUI JSON Programming Guide v2.0
 */
public class JsonGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JsonGenerator.class);
    
    private final ObjectMapper objectMapper;
    private final boolean prettyPrint;
    private final boolean includeComments;
    
    public JsonGenerator() {
        this(true, false);
    }
    
    public JsonGenerator(boolean prettyPrint, boolean includeComments) {
        this.objectMapper = new ObjectMapper();
        this.prettyPrint = prettyPrint;
        this.includeComments = includeComments;
    }
    
    /**
     * Generate JSON string from WindowDefinition
     */
    public String generateJson(WindowDefinition window) throws JsonGenerationException {
        try {
            ObjectNode rootNode = generateJsonNode(window);
            
            if (prettyPrint) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            } else {
                return objectMapper.writeValueAsString(rootNode);
            }
            
        } catch (Exception e) {
            logger.error("Failed to generate JSON for window: {}", window.getWindowId(), e);
            throw new JsonGenerationException("Failed to generate JSON", e);
        }
    }
    
    /**
     * Generate JsonNode from WindowDefinition
     */
    public ObjectNode generateJsonNode(WindowDefinition window) throws JsonGenerationException {
        if (window == null) {
            throw new JsonGenerationException("WindowDefinition cannot be null");
        }
        
        ObjectNode rootNode = objectMapper.createObjectNode();
        
        // Window properties
        rootNode.put("windowId", window.getWindowId());
        rootNode.put("name", window.getName());
        
        if (window.getDescription() != null) {
            rootNode.put("description", window.getDescription());
        }
        
        rootNode.put("windowType", window.getWindowType());
        
        if (window.getHelp() != null) {
            rootNode.put("help", window.getHelp());
        }
        
        // Generate tabs array
        ArrayNode tabsArray = generateTabsArray(window.getTabs());
        rootNode.set("tabs", tabsArray);
        
        // Generate metadata
        ObjectNode metadataNode = generateMetadata(window.getMetadata());
        rootNode.set("metadata", metadataNode);
        
        return rootNode;
    }
    
    /**
     * Generate tabs array
     */
    private ArrayNode generateTabsArray(java.util.List<TabDefinition> tabs) throws JsonGenerationException {
        ArrayNode tabsArray = objectMapper.createArrayNode();
        
        if (tabs != null) {
            for (TabDefinition tab : tabs) {
                ObjectNode tabNode = generateTabNode(tab);
                tabsArray.add(tabNode);
            }
        }
        
        return tabsArray;
    }
    
    /**
     * Generate individual tab node
     */
    private ObjectNode generateTabNode(TabDefinition tab) throws JsonGenerationException {
        ObjectNode tabNode = objectMapper.createObjectNode();
        
        tabNode.put("tabId", tab.getTabId());
        tabNode.put("name", tab.getName());
        
        if (tab.getDescription() != null) {
            tabNode.put("description", tab.getDescription());
        }
        
        tabNode.put("sequence", tab.getSequence());
        
        if (tab.getTabLevel() != 0) {
            tabNode.put("tabLevel", tab.getTabLevel());
        }
        
        if (tab.isReadOnly()) {
            tabNode.put("isReadOnly", true);
        }
        
        if (tab.isSingleRow()) {
            tabNode.put("isSingleRow", true);
        }
        
        if (tab.getHelp() != null) {
            tabNode.put("help", tab.getHelp());
        }
        
        // Generate fields array
        ArrayNode fieldsArray = generateFieldsArray(tab.getFields());
        tabNode.set("fields", fieldsArray);
        
        return tabNode;
    }
    
    /**
     * Generate fields array
     */
    private ArrayNode generateFieldsArray(java.util.List<FieldDefinition> fields) throws JsonGenerationException {
        ArrayNode fieldsArray = objectMapper.createArrayNode();
        
        if (fields != null) {
            for (FieldDefinition field : fields) {
                ObjectNode fieldNode = generateFieldNode(field);
                fieldsArray.add(fieldNode);
            }
        }
        
        return fieldsArray;
    }
    
    /**
     * Generate individual field node
     */
    private ObjectNode generateFieldNode(FieldDefinition field) throws JsonGenerationException {
        ObjectNode fieldNode = objectMapper.createObjectNode();
        
        fieldNode.put("fieldId", field.getFieldId());
        fieldNode.put("name", field.getName());
        fieldNode.put("component", field.getComponentType().getJsonName());
        fieldNode.put("sequence", field.getSequence());
        
        if (field.isReadOnly()) {
            fieldNode.put("isReadOnly", true);
        }
        
        if (!field.isDisplayed()) {
            fieldNode.put("isDisplayed", false);
        }
        
        if (field.getDescription() != null) {
            fieldNode.put("description", field.getDescription());
        }
        
        if (field.getHelp() != null) {
            fieldNode.put("help", field.getHelp());
        }
        
        if (field.getDisplayLogic() != null) {
            fieldNode.put("displayLogic", field.getDisplayLogic());
        }
        
        // Add validation rules
        if (field.getValidation() != null) {
            ObjectNode validationNode = generateValidationNode(field.getValidation());
            if (validationNode.size() > 0) {
                fieldNode.set("validation", validationNode);
            }
        }
        
        // Add UI configuration
        if (field.getUi() != null && !field.getUi().isEmpty()) {
            ObjectNode uiNode = generateUiNode(field.getUi());
            fieldNode.set("ui", uiNode);
        }
        
        // Add component data
        if (field.getData() != null && !field.getData().isEmpty()) {
            ObjectNode dataNode = generateDataNode(field.getData());
            fieldNode.set("data", dataNode);
        }
        
        // Add reference data
        if (field.getReference() != null) {
            ObjectNode referenceNode = generateReferenceNode(field.getReference());
            fieldNode.set("reference", referenceNode);
        }
        
        return fieldNode;
    }
    
    /**
     * Generate validation node
     */
    private ObjectNode generateValidationNode(ValidationRules validation) {
        ObjectNode validationNode = objectMapper.createObjectNode();
        
        if (validation.isRequired()) {
            validationNode.put("required", true);
        }
        
        if (validation.getRequiredWhen() != null) {
            validationNode.put("requiredWhen", validation.getRequiredWhen());
        }
        
     // ✅ FIXED CODE - only output non-null, non-zero values
        if (validation.getMin() != null) {
            validationNode.put("min", validation.getMin().doubleValue());
        }
        if (validation.getMax() != null && validation.getMax().doubleValue() != 0) {
            validationNode.put("max", validation.getMax().doubleValue());
        }
        if (validation.getMinLength() != null && validation.getMinLength() != 0) {
            validationNode.put("minLength", validation.getMinLength());
        }
        if (validation.getMaxLength() != null && validation.getMaxLength() != 0) {
            validationNode.put("maxLength", validation.getMaxLength());
        }
        
        if (validation.getMinDate() != null) {
            validationNode.put("minDate", validation.getMinDate());
        }
        
        if (validation.getMaxDate() != null) {
            validationNode.put("maxDate", validation.getMaxDate());
        }
        
        if (validation.getPattern() != null) {
            validationNode.put("pattern", validation.getPattern());
        }
        
        if (validation.getDecimalPlaces() != null) {
            validationNode.put("decimalPlaces", validation.getDecimalPlaces());
        }
        
        if (validation.getMinSelections() != null) {
            validationNode.put("minSelections", validation.getMinSelections());
        }
        
        if (validation.getMaxSelections() != null) {
            validationNode.put("maxSelections", validation.getMaxSelections());
        }
        
        if (validation.getMaxFiles() != null) {
            validationNode.put("maxFiles", validation.getMaxFiles());
        }
        
        if (validation.getMaxPhotos() != null) {
            validationNode.put("maxPhotos", validation.getMaxPhotos());
        }
        
        return validationNode;
    }
    
    /**
     * Generate UI configuration node
     */
    private ObjectNode generateUiNode(Map<String, Object> ui) {
        ObjectNode uiNode = objectMapper.createObjectNode();
        
        for (Map.Entry<String, Object> entry : ui.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                uiNode.put(key, (String) value);
            } else if (value instanceof Boolean) {
                uiNode.put(key, (Boolean) value);
            } else if (value instanceof Integer) {
                uiNode.put(key, (Integer) value);
            } else if (value instanceof Double) {
                uiNode.put(key, (Double) value);
            } else {
                // Convert complex objects to JSON
                JsonNode valueNode = objectMapper.valueToTree(value);
                uiNode.set(key, valueNode);
            }
        }
        
        return uiNode;
    }
    
    /**
     * Generate component data node
     */
    private ObjectNode generateDataNode(Map<String, Object> data) {
        ObjectNode dataNode = objectMapper.createObjectNode();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Convert to JSON node
            JsonNode valueNode = objectMapper.valueToTree(value);
            dataNode.set(key, valueNode);
        }
        
        return dataNode;
    }
    
    /**
     * Generate reference data node
     */
    private ObjectNode generateReferenceNode(ReferenceData reference) {
        ObjectNode referenceNode = objectMapper.createObjectNode();
        
        if (reference.getId() != null) {
            referenceNode.put("id", reference.getId());
        }
        
        if (reference.getName() != null) {
            referenceNode.put("name", reference.getName());
        } 
        
        
        if (reference.isAllowCustomValues()) {
            referenceNode.put("allowCustomValues", true);
        }
        
        // Generate values array
        if (reference.getValues() != null && !reference.getValues().isEmpty()) {
            ArrayNode valuesArray = objectMapper.createArrayNode();
            
            for (ReferenceData.ReferenceValue value : reference.getValues()) {
                ObjectNode valueNode = objectMapper.createObjectNode();
                valueNode.put("key", value.getKey());
                valueNode.put("display", value.getDisplay()); 
                 
                
                if (value.getColor() != null) {
                    valueNode.put("color", value.getColor());
                }
                 
                valuesArray.add(valueNode);
            }
            
            referenceNode.set("values", valuesArray);
        }
        
        return referenceNode;
    }
    
    /**
     * Generate metadata node
     */
    private ObjectNode generateMetadata(Map<String, Object> metadata) {
        ObjectNode metadataNode = objectMapper.createObjectNode();
        
        // Add standard metadata
        metadataNode.put("version", "2.0");
        metadataNode.put("source", "JSONFormMaker");
        metadataNode.put("lastModified", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Add custom metadata
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                JsonNode valueNode = objectMapper.valueToTree(entry.getValue());
                metadataNode.set(entry.getKey(), valueNode);
            }
        }
        
        return metadataNode;
    }
    
    /**
     * Custom exception for JSON generation errors
     */
    public static class JsonGenerationException extends Exception {
        public JsonGenerationException(String message) {
            super(message);
        }
        
        public JsonGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
