package com.adui.jsoncraft.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for reference data used in selection components
 * Represents dropdown options, radio button choices, etc.
 */
public class ReferenceData {
    private String id;
    private String name;
    private String validationType;
    private boolean allowCustomValues;
    private List<ReferenceValue> values;
    
    public ReferenceData() {
        this.values = new ArrayList<>();
        this.validationType = "LIST";
        this.allowCustomValues = false;
    }
    
    public ReferenceData(String id, String name) {
        this();
        this.id = id;
        this.name = name;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getValidationType() { return validationType; }
    public void setValidationType(String validationType) { this.validationType = validationType; }
    
    public boolean isAllowCustomValues() { return allowCustomValues; }
    public void setAllowCustomValues(boolean allowCustomValues) { this.allowCustomValues = allowCustomValues; }
    
    public List<ReferenceValue> getValues() { return values; }
    public void setValues(List<ReferenceValue> values) { this.values = values; }
    
    // Utility Methods
    public void addValue(ReferenceValue value) {
        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(value);
    }
    
    public void removeValue(ReferenceValue value) {
        if (values != null) {
            values.remove(value);
        }
    }
    
    public ReferenceValue getValue(String key) {
        if (values != null) {
            return values.stream()
                .filter(value -> key.equals(value.getKey()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
    
    public int getValueCount() {
        return values != null ? values.size() : 0;
    }
    
    public ReferenceData copy() {
        ReferenceData copy = new ReferenceData();
        copy.id = this.id;
        copy.name = this.name;
        copy.validationType = this.validationType;
        copy.allowCustomValues = this.allowCustomValues;
        
        if (this.values != null) {
            copy.values = new ArrayList<>();
            for (ReferenceValue value : this.values) {
                copy.values.add(value.copy());
            }
        }
        
        return copy;
    }
    
    /**
     * Inner class representing a single reference value
     */
    public static class ReferenceValue {
        private String key;
        private String display;
        private String description;
        private String color;
        private String icon;
        private int sortOrder;
        
        public ReferenceValue() {
            this.sortOrder = 10;
        }
        
        public ReferenceValue(String key, String display) {
            this();
            this.key = key;
            this.display = display;
        }
        
        // Getters and Setters
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        
        public String getDisplay() { return display; }
        public void setDisplay(String display) { this.display = display; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public int getSortOrder() { return sortOrder; }
        public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
        
        public ReferenceValue copy() {
            ReferenceValue copy = new ReferenceValue();
            copy.key = this.key;
            copy.display = this.display;
            copy.description = this.description;
            copy.color = this.color;
            copy.icon = this.icon;
            copy.sortOrder = this.sortOrder;
            return copy;
        }
        
        @Override
        public String toString() {
            return String.format("ReferenceValue[key=%s, display=%s]", key, display);
        }
    }
}
