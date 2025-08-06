package com.adui.jsoncraft.properties.editors.task.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Task List Settings - configuration for task list behavior
 */
public class TaskListSettings {
    private boolean showProgressBars;
    private boolean autoCalculateProgress;
    private String defaultPriority;
    private String defaultStatus;
    
    public TaskListSettings() {
        this.showProgressBars = true;
        this.autoCalculateProgress = true;
        this.defaultPriority = "medium";
        this.defaultStatus = "not_started";
    }
    
    // Getters and setters
    public boolean isShowProgressBars() { return showProgressBars; }
    public void setShowProgressBars(boolean showProgressBars) { this.showProgressBars = showProgressBars; }
    
    public boolean isAutoCalculateProgress() { return autoCalculateProgress; }
    public void setAutoCalculateProgress(boolean autoCalculateProgress) { this.autoCalculateProgress = autoCalculateProgress; }
    
    public String getDefaultPriority() { return defaultPriority; }
    public void setDefaultPriority(String defaultPriority) { this.defaultPriority = defaultPriority; }
    
    public String getDefaultStatus() { return defaultStatus; }
    public void setDefaultStatus(String defaultStatus) { this.defaultStatus = defaultStatus; }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("showProgressBars", showProgressBars);
        map.put("autoCalculateProgress", autoCalculateProgress);
        map.put("defaultPriority", defaultPriority);
        map.put("defaultStatus", defaultStatus);
        return map;
    }
}
