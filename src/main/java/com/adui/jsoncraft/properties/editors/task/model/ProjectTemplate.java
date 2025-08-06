package com.adui.jsoncraft.properties.editors.task.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Project Template - represents reusable project structures
 */
public class ProjectTemplate {
    private String name;
    private String description;
    private List<TaskNode> tasks;
    private List<TaskDependency> dependencies;
    
    public ProjectTemplate(String name, String description, List<TaskNode> tasks, List<TaskDependency> dependencies) {
        this.name = name;
        this.description = description;
        this.tasks = new ArrayList<>(tasks);
        this.dependencies = new ArrayList<>(dependencies);
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<TaskNode> getTasks() { return new ArrayList<>(tasks); }
    public void setTasks(List<TaskNode> tasks) { this.tasks = new ArrayList<>(tasks); }
    
    public List<TaskDependency> getDependencies() { return new ArrayList<>(dependencies); }
    public void setDependencies(List<TaskDependency> dependencies) { this.dependencies = new ArrayList<>(dependencies); }
    
    @Override
    public String toString() {
        return name;
    }
}
