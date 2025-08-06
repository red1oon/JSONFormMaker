package com.adui.jsoncraft.properties.editors.task.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task Node - represents a single task in the project hierarchy
 * Fixed version with robust enum parsing
 */
public class TaskNode {
    private String id;
    private String name;
    private String description;
    private String parentId;
    private TaskStatus status;
    private TaskPriority priority;
    private String assignee;
    private Integer estimatedHours;
    private Integer actualHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private int completion;
    private String phase;
    private List<TaskNode> children;
    
    public TaskNode() {
        this.status = TaskStatus.NOT_STARTED;
        this.priority = TaskPriority.MEDIUM;
        this.completion = 0;
        this.children = new ArrayList<>();
    }
    
    public TaskNode(String id, String name, String parentId, TaskStatus status, 
                   TaskPriority priority, String phase) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.status = status != null ? status : TaskStatus.NOT_STARTED;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.phase = phase;
        this.completion = 0;
        this.children = new ArrayList<>();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    
    public Integer getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Integer estimatedHours) { this.estimatedHours = estimatedHours; }
    
    public Integer getActualHours() { return actualHours; }
    public void setActualHours(Integer actualHours) { this.actualHours = actualHours; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public int getCompletion() { return completion; }
    public void setCompletion(int completion) { this.completion = Math.max(0, Math.min(100, completion)); }
    
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    
    public List<TaskNode> getChildren() { return children; }
    public void setChildren(List<TaskNode> children) { this.children = children; }
    
    @Override
    public String toString() {
        return name + " (" + status + " - " + completion + "%)";
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("parentId", parentId);
        map.put("status", status.name()); // Use enum name, not display name
        map.put("priority", priority.name()); // Use enum name, not display name
        map.put("assignee", assignee);
        map.put("estimatedHours", estimatedHours);
        map.put("actualHours", actualHours);
        map.put("startDate", startDate != null ? startDate.toString() : null);
        map.put("endDate", endDate != null ? endDate.toString() : null);
        map.put("completion", completion);
        map.put("phase", phase);
        return map;
    }
    
    public static TaskNode fromMap(Map<String, Object> map) {
        TaskNode task = new TaskNode();
        task.setId((String) map.get("id"));
        task.setName((String) map.get("name"));
        task.setDescription((String) map.get("description"));
        task.setParentId((String) map.get("parentId"));
        
        // Fixed: Handle status parsing - convert display names to enum constants
        Object statusObj = map.get("status");
        if (statusObj instanceof String) {
            String statusStr = (String) statusObj;
            try {
                // First try direct enum name parsing
                task.setStatus(TaskStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                // If that fails, try mapping display names to enum constants
                switch (statusStr) {
                    case "Not Started":
                        task.setStatus(TaskStatus.NOT_STARTED);
                        break;
                    case "In Progress":
                        task.setStatus(TaskStatus.IN_PROGRESS);
                        break;
                    case "Completed":
                        task.setStatus(TaskStatus.COMPLETED);
                        break;
                    case "Blocked":
                        task.setStatus(TaskStatus.BLOCKED);
                        break;
                    case "On Hold":
                        task.setStatus(TaskStatus.ON_HOLD);
                        break;
                    default:
                        task.setStatus(TaskStatus.NOT_STARTED); // Safe default
                }
            }
        } else {
            task.setStatus(TaskStatus.NOT_STARTED);
        }
        
        // Fixed: Handle priority parsing - convert display names to enum constants
        Object priorityObj = map.get("priority");
        if (priorityObj instanceof String) {
            String priorityStr = (String) priorityObj;
            try {
                // First try direct enum name parsing
                task.setPriority(TaskPriority.valueOf(priorityStr));
            } catch (IllegalArgumentException e) {
                // If that fails, try mapping display names to enum constants
                switch (priorityStr) {
                    case "Low":
                        task.setPriority(TaskPriority.LOW);
                        break;
                    case "Medium":
                        task.setPriority(TaskPriority.MEDIUM);
                        break;
                    case "High":
                        task.setPriority(TaskPriority.HIGH);
                        break;
                    case "Critical":
                        task.setPriority(TaskPriority.CRITICAL);
                        break;
                    default:
                        task.setPriority(TaskPriority.MEDIUM); // Safe default
                }
            }
        } else {
            task.setPriority(TaskPriority.MEDIUM);
        }
        
        task.setAssignee((String) map.get("assignee"));
        task.setEstimatedHours((Integer) map.get("estimatedHours"));
        task.setActualHours((Integer) map.get("actualHours"));
        
        String startDateStr = (String) map.get("startDate");
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                task.setStartDate(LocalDate.parse(startDateStr));
            } catch (Exception e) {
                // Ignore invalid date formats
            }
        }
        
        String endDateStr = (String) map.get("endDate");
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                task.setEndDate(LocalDate.parse(endDateStr));
            } catch (Exception e) {
                // Ignore invalid date formats
            }
        }
        
        Object completionObj = map.get("completion");
        if (completionObj instanceof Number) {
            task.setCompletion(((Number) completionObj).intValue());
        }
        
        task.setPhase((String) map.get("phase"));
        
        return task;
    }
}