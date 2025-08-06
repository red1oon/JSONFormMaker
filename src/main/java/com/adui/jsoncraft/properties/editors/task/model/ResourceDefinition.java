package com.adui.jsoncraft.properties.editors.task.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Resource Definition - represents people, equipment, or materials
 */
public class ResourceDefinition {
    private String id;
    private String name;
    private String type; // person, equipment, material
    private double rate; // cost per hour
    private int availability; // percentage
    private String skills;
    
    public ResourceDefinition(String id, String name, String type, double rate, int availability) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rate = rate;
        this.availability = availability;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    
    public int getAvailability() { return availability; }
    public void setAvailability(int availability) { this.availability = availability; }
    
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("type", type);
        map.put("rate", rate);
        map.put("availability", availability);
        map.put("skills", skills);
        return map;
    }
    
    public static ResourceDefinition fromMap(Map<String, Object> map) {
        ResourceDefinition resource = new ResourceDefinition(
            (String) map.get("id"),
            (String) map.get("name"),
            (String) map.get("type"),
            ((Number) map.getOrDefault("rate", 0.0)).doubleValue(),
            ((Number) map.getOrDefault("availability", 100)).intValue()
        );
        resource.setSkills((String) map.get("skills"));
        return resource;
    }
}
