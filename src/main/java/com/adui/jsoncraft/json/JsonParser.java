package com.adui.jsoncraft.json;

import com.adui.jsoncraft.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON Parser for ADUI JSON files
 * Loads JSON files back into model objects
 * 
 * @version 1.1 - Fixed for existing model classes
 * @namespace com.adui.jsoncraft.json.JsonParser
 */
public class JsonParser {
    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
    
    private final ObjectMapper objectMapper;
    
    public JsonParser() {
        this.objectMapper = new ObjectMapper();
        logger.debug("JsonParser initialized");
    }
    
    /**
     * Parse JSON file into WindowDefinition
     */
    public WindowDefinition parseFile(File file) throws JsonParseException {
        try {
            logger.info("Parsing JSON file: {}", file.getAbsolutePath());
            
            JsonNode rootNode = objectMapper.readTree(file);
            return parseWindowDefinition(rootNode);
            
        } catch (IOException e) {
            throw new JsonParseException("Failed to read JSON file: " + file.getName(), e);
        } catch (Exception e) {
            throw new JsonParseException("Failed to parse JSON content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse JSON string into WindowDefinition
     */
    public WindowDefinition parseString(String jsonContent) throws JsonParseException {
        try {
            logger.debug("Parsing JSON string content");
            
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            return parseWindowDefinition(rootNode);
            
        } catch (IOException e) {
            throw new JsonParseException("Failed to parse JSON string", e);
        } catch (Exception e) {
            throw new JsonParseException("Failed to parse JSON content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse root window definition
     */
    private WindowDefinition parseWindowDefinition(JsonNode rootNode) throws JsonParseException {
        validateRequiredFields(rootNode, "windowId", "name");
        
        WindowDefinition window = new WindowDefinition();
        window.setWindowId(getStringValue(rootNode, "windowId"));
        window.setName(getStringValue(rootNode, "name"));
        window.setDescription(getStringValue(rootNode, "description"));
        window.setWindowType(getStringValue(rootNode, "windowType", "Transaction"));
        window.setHelp(getStringValue(rootNode, "help"));
        
        // Parse metadata
        if (rootNode.has("metadata")) {
            window.setMetadata(parseMetadata(rootNode.get("metadata")));
        }
        
        // Parse tabs
        if (rootNode.has("tabs")) {
            JsonNode tabsNode = rootNode.get("tabs");
            if (tabsNode.isArray()) {
                List<TabDefinition> tabs = new ArrayList<>();
                for (JsonNode tabNode : tabsNode) {
                    tabs.add(parseTabDefinition(tabNode));
                }
                window.setTabs(tabs);
            }
        }
        
        logger.debug("Parsed window: {} with {} tabs", window.getWindowId(), 
            window.getTabs() != null ? window.getTabs().size() : 0);
        
        return window;
    }
    
    /**
     * Parse tab definition
     */
    private TabDefinition parseTabDefinition(JsonNode tabNode) throws JsonParseException {
        validateRequiredFields(tabNode, "tabId", "name");
        
        TabDefinition tab = new TabDefinition();
        tab.setTabId(getStringValue(tabNode, "tabId"));
        tab.setName(getStringValue(tabNode, "name"));
        tab.setDescription(getStringValue(tabNode, "description"));
        tab.setSequence(getIntValue(tabNode, "sequence", 10));
        tab.setTabLevel(getIntValue(tabNode, "tabLevel", 0));
        tab.setReadOnly(getBooleanValue(tabNode, "isReadOnly", false));
        tab.setSingleRow(getBooleanValue(tabNode, "isSingleRow", false));
        tab.setHelp(getStringValue(tabNode, "help"));
        
        // Parse fields
        if (tabNode.has("fields")) {
            JsonNode fieldsNode = tabNode.get("fields");
            if (fieldsNode.isArray()) {
                List<FieldDefinition> fields = new ArrayList<>();
                for (JsonNode fieldNode : fieldsNode) {
                    fields.add(parseFieldDefinition(fieldNode));
                }
                tab.setFields(fields);
            }
        }
        
        logger.debug("Parsed tab: {} with {} fields", tab.getTabId(), 
            tab.getFields() != null ? tab.getFields().size() : 0);
        
        return tab;
    }
    
    /**
     * Parse field definition
     */
    private FieldDefinition parseFieldDefinition(JsonNode fieldNode) throws JsonParseException {
        validateRequiredFields(fieldNode, "fieldId", "name", "component");
        
        FieldDefinition field = new FieldDefinition();
        field.setFieldId(getStringValue(fieldNode, "fieldId"));
        field.setName(getStringValue(fieldNode, "name"));
        field.setSequence(getIntValue(fieldNode, "sequence", 10));
        field.setReadOnly(getBooleanValue(fieldNode, "isReadOnly", false));
        field.setDisplayed(getBooleanValue(fieldNode, "isDisplayed", true));
        field.setDescription(getStringValue(fieldNode, "description"));
        field.setHelp(getStringValue(fieldNode, "help"));
        field.setDisplayLogic(getStringValue(fieldNode, "displayLogic"));
        
        // Parse component type
        String componentTypeName = getStringValue(fieldNode, "component");
        ComponentType componentType = ComponentType.fromJsonName(componentTypeName);
        if (componentType == null) {
            logger.warn("Unknown component type: {}, defaulting to TextField", componentTypeName);
            componentType = ComponentType.TEXT_FIELD; // Fallback to TEXT_FIELD
        }
        field.setComponentType(componentType);
        
        // Parse validation rules - SIMPLIFIED to avoid missing methods
        if (fieldNode.has("validation")) {
            field.setValidation(parseValidationRules(fieldNode.get("validation")));
        }
        
        // Parse UI properties
        if (fieldNode.has("ui")) {
            field.setUi(parseObjectMap(fieldNode.get("ui")));
        }
        
        // Parse component data
        if (fieldNode.has("data")) {
            field.setData(parseObjectMap(fieldNode.get("data")));
        }
        
        // Parse reference data - SIMPLIFIED to avoid missing methods
        if (fieldNode.has("reference")) {
            field.setReference(parseReferenceData(fieldNode.get("reference")));
        }
        
        logger.debug("Parsed field: {} ({})", field.getFieldId(), field.getComponentType().getJsonName());
        
        return field;
    }
    
    /**
     * Parse validation rules - SIMPLIFIED for existing ValidationRules class
     */
    private ValidationRules parseValidationRules(JsonNode validationNode) {
        ValidationRules validation = new ValidationRules();
        validation.setRequired(getBooleanValue(validationNode, "required", false));
        
        // Only set values if they exist in JSON
        if (validationNode.has("minLength")) {
            validation.setMinLength(getIntValue(validationNode, "minLength", 0));
        }
        if (validationNode.has("maxLength")) {
            validation.setMaxLength(getIntValue(validationNode, "maxLength", 0));
        }
        if (validationNode.has("min")) {
            validation.setMin(getDoubleValue(validationNode, "min", 0.0));
        }
        if (validationNode.has("max")) {
            validation.setMax(getDoubleValue(validationNode, "max", 0.0));
        }
        if (validationNode.has("pattern")) {
            validation.setPattern(getStringValue(validationNode, "pattern"));
        }

        return validation;
    }
    
    /**
     * Parse reference data - SIMPLIFIED for existing ReferenceData class
     */
    private ReferenceData parseReferenceData(JsonNode referenceNode) {
        ReferenceData reference = new ReferenceData();
        
        // Skip setter methods that may not exist
        // reference.setType(), setQuery(), setKeyField(), setDisplayField() may not exist
        
        // Parse values array (this should work)
        if (referenceNode.has("values")) {
            JsonNode valuesNode = referenceNode.get("values");
            if (valuesNode.isArray()) {
                List<ReferenceData.ReferenceValue> values = new ArrayList<>();
                for (JsonNode valueNode : valuesNode) {
                    ReferenceData.ReferenceValue refValue = new ReferenceData.ReferenceValue();
                    refValue.setKey(getStringValue(valueNode, "key"));
                    refValue.setDisplay(getStringValue(valueNode, "display")); 
                    refValue.setColor(getStringValue(valueNode, "color")); 
                    values.add(refValue);
                }
                reference.setValues(values);
            }
        }
        
        return reference;
    }
    
    /**
     * Parse metadata into map
     */
    private Map<String, Object> parseMetadata(JsonNode metadataNode) {
        Map<String, Object> metadata = new HashMap<>();
        metadataNode.fields().forEachRemaining(entry -> {
            metadata.put(entry.getKey(), getNodeValue(entry.getValue()));
        });
        return metadata;
    }
    
    /**
     * Parse JSON object into Map
     */
    private Map<String, Object> parseObjectMap(JsonNode objectNode) {
        Map<String, Object> map = new HashMap<>();
        objectNode.fields().forEachRemaining(entry -> {
        	String key = entry.getKey();
        	Object value = getNodeValue(entry.getValue());

        	// Detect JSON strings and parse them
        	if (value instanceof String && isJsonString((String) value)) {
        	    try {
        	        JsonNode parsed = objectMapper.readTree((String) value);
        	        value = getNodeValue(parsed);
        	    } catch (Exception e) {
        	        // Keep as string if parsing fails
        	    }
        	}

        	map.put(key, value);
        });
        return map;
    }
    private boolean isJsonString(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        str = str.trim();
        return (str.startsWith("{") && str.endsWith("}")) || 
               (str.startsWith("[") && str.endsWith("]"));
    }
    // Utility methods for safe value extraction
    private String getStringValue(JsonNode node, String fieldName) {
        return getStringValue(node, fieldName, null);
    }
    
    private String getStringValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : defaultValue;
    }
    
    private int getIntValue(JsonNode node, String fieldName, int defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asInt(defaultValue) : defaultValue;
    }
    
    private double getDoubleValue(JsonNode node, String fieldName, double defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asDouble(defaultValue) : defaultValue;
    }
    
    private boolean getBooleanValue(JsonNode node, String fieldName, boolean defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asBoolean(defaultValue) : defaultValue;
    }
    
    /**
     * Fixed getNodeValue method for JsonParser.java
     * Version: 1.2 - Native object conversion fix
     * Namespace: com.adui.jsoncraft.json.JsonParser
     * 
     * Replace the existing getNodeValue method with this implementation
     */
    private Object getNodeValue(JsonNode node) {
        if (node.isNull()) return null;
        if (node.isTextual()) return node.asText();
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isDouble()) return node.asDouble();
        if (node.isBoolean()) return node.asBoolean();
        
        // Convert complex structures to native Java objects
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(getNodeValue(item)); // Recursive conversion
            }
            return list;
        }
        
        if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(entry -> {
                map.put(entry.getKey(), getNodeValue(entry.getValue())); // Recursive conversion
            });
            return map;
        }
        
        // Fallback for any other node types
        return node.toString();
    }
    
    private void validateRequiredFields(JsonNode node, String... fieldNames) throws JsonParseException {
        for (String fieldName : fieldNames) {
            if (!node.has(fieldName) || node.get(fieldName).isNull()) {
                throw new JsonParseException("Required field missing: " + fieldName);
            }
        }
    }
    
    /**
     * Custom exception for JSON parsing errors
     */
    public static class JsonParseException extends Exception {
        private static final long serialVersionUID = 1L;

		public JsonParseException(String message) {
            super(message);
        }
        
        public JsonParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}