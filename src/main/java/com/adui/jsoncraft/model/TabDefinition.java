package com.adui.jsoncraft.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data model for ADUI Tab Definition
 * Represents a tab within a window containing fields
 */
public class TabDefinition {
    private String tabId;
    private String name;
    private String description;
    private int sequence;
    private int tabLevel;
    private boolean isReadOnly;
    private boolean isSingleRow;
    private String help;
    private List<FieldDefinition> fields;
    
    public TabDefinition() {
        this.fields = new ArrayList<>();
        this.sequence = 10;
        this.tabLevel = 0;
        this.isReadOnly = false;
        this.isSingleRow = false;
    }
    
    public TabDefinition(String tabId, String name) {
        this();
        this.tabId = tabId;
        this.name = name;
    }
    
    // Getters and Setters
    public String getTabId() { return tabId; }
    public void setTabId(String tabId) { this.tabId = tabId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getSequence() { return sequence; }
    public void setSequence(int sequence) { this.sequence = sequence; }
    
    public int getTabLevel() { return tabLevel; }
    public void setTabLevel(int tabLevel) { this.tabLevel = tabLevel; }
    
    public boolean isReadOnly() { return isReadOnly; }
    public void setReadOnly(boolean readOnly) { isReadOnly = readOnly; }
    
    public boolean isSingleRow() { return isSingleRow; }
    public void setSingleRow(boolean singleRow) { isSingleRow = singleRow; }
    
    public String getHelp() { return help; }
    public void setHelp(String help) { this.help = help; }
    
    public List<FieldDefinition> getFields() { return fields; }
    public void setFields(List<FieldDefinition> fields) { this.fields = fields; }
    
    // Utility Methods
    public void addField(FieldDefinition field) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        fields.add(field);
        updateFieldSequences();
    }
    
    public void removeField(FieldDefinition field) {
        if (fields != null) {
            fields.remove(field);
            updateFieldSequences();
        }
    }
    
    public FieldDefinition getField(String fieldId) {
        if (fields != null) {
            return fields.stream()
                .filter(field -> fieldId.equals(field.getFieldId()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
    
    public int getFieldCount() {
        return fields != null ? fields.size() : 0;
    }
    
    public void updateFieldSequences() {
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).setSequence((i + 1) * 10);
            }
        }
    }
    
    public void moveField(int fromIndex, int toIndex) {
        if (fields != null && fromIndex >= 0 && fromIndex < fields.size() && 
            toIndex >= 0 && toIndex < fields.size() && fromIndex != toIndex) {
            
            FieldDefinition field = fields.remove(fromIndex);
            fields.add(toIndex, field);
            updateFieldSequences();
        }
    }
    
    public boolean isValid() {
        return tabId != null && !tabId.trim().isEmpty() &&
               name != null && !name.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Tab[id=%s, name=%s, fields=%d]", 
            tabId, name, getFieldCount());
    }
}
