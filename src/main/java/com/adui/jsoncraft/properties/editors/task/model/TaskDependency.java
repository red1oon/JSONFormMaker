package com.adui.jsoncraft.properties.editors.task.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Task Dependency - represents relationships between tasks
 */
public class TaskDependency {
    private String fromTaskId;
    private String toTaskId;
    private String type; // finish-to-start, start-to-start, finish-to-finish, start-to-finish
    private int lagDays;
    
    public TaskDependency(String fromTaskId, String toTaskId, String type, int lagDays) {
        this.fromTaskId = fromTaskId;
        this.toTaskId = toTaskId;
        this.type = type;
        this.lagDays = lagDays;
    }
    
    // Getters and setters
    public String getFromTaskId() { return fromTaskId; }
    public void setFromTaskId(String fromTaskId) { this.fromTaskId = fromTaskId; }
    
    public String getToTaskId() { return toTaskId; }
    public void setToTaskId(String toTaskId) { this.toTaskId = toTaskId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public int getLagDays() { return lagDays; }
    public void setLagDays(int lagDays) { this.lagDays = lagDays; }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fromTaskId", fromTaskId);
        map.put("toTaskId", toTaskId);
        map.put("type", type);
        map.put("lagDays", lagDays);
        return map;
    }
    
    public static TaskDependency fromMap(Map<String, Object> map) {
        return new TaskDependency(
            (String) map.get("fromTaskId"),
            (String) map.get("toTaskId"),
            (String) map.get("type"),
            ((Number) map.getOrDefault("lagDays", 0)).intValue()
        );
    }
}
