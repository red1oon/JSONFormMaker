package com.adui.jsoncraft.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data model for ADUI Window Definition
 * Represents the complete structure of a mobile form
 */
public class WindowDefinition {
    private String windowId;
    private String name;
    private String description;
    private String windowType;
    private String help;
    private List<TabDefinition> tabs;
    private Map<String, Object> metadata;
    
    public WindowDefinition() {
        this.tabs = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.windowType = "Transaction";
    }
    
    public WindowDefinition(String windowId, String name) {
        this();
        this.windowId = windowId;
        this.name = name;
    }
    
    // Getters and Setters
    public String getWindowId() { return windowId; }
    public void setWindowId(String windowId) { this.windowId = windowId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getWindowType() { return windowType; }
    public void setWindowType(String windowType) { this.windowType = windowType; }
    
    public String getHelp() { return help; }
    public void setHelp(String help) { this.help = help; }
    
    public List<TabDefinition> getTabs() { return tabs; }
    public void setTabs(List<TabDefinition> tabs) { this.tabs = tabs; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    // Utility Methods
    public void addTab(TabDefinition tab) {
        if (tabs == null) {
            tabs = new ArrayList<>();
        }
        tabs.add(tab);
    }
    
    public void removeTab(TabDefinition tab) {
        if (tabs != null) {
            tabs.remove(tab);
        }
    }
    
    public TabDefinition getTab(String tabId) {
        if (tabs != null) {
            return tabs.stream()
                .filter(tab -> tabId.equals(tab.getTabId()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
    
    public int getTabCount() {
        return tabs != null ? tabs.size() : 0;
    }
    
    public int getFieldCount() {
        if (tabs == null) return 0;
        return tabs.stream()
            .mapToInt(tab -> tab.getFieldCount())
            .sum();
    }
    
    public boolean isValid() {
        return windowId != null && !windowId.trim().isEmpty() &&
               name != null && !name.trim().isEmpty() &&
               tabs != null && !tabs.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Window[id=%s, name=%s, tabs=%d, fields=%d]", 
            windowId, name, getTabCount(), getFieldCount());
    }
}
