package com.adui.jsoncraft.properties.editors.task.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.adui.jsoncraft.properties.editors.task.model.TaskNode;

/**
 * Custom tree model for task hierarchy management
 */
public class TaskTreeModel extends DefaultTreeModel {
    private DefaultMutableTreeNode root;
    private List<TaskNode> allTasks;
    
    public TaskTreeModel() {
        super(new DefaultMutableTreeNode("Project Tasks"));
        this.root = (DefaultMutableTreeNode) getRoot();
        this.allTasks = new ArrayList<>();
    }
    
    public void setTasks(List<TaskNode> tasks) {
        this.allTasks = tasks;
        reload();
    }
    
    public void reload() {
        root.removeAllChildren();
        
        // Build tree structure
        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();
        
        // Create nodes for all tasks
        for (TaskNode task : allTasks) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(task);
            nodeMap.put(task.getId(), node);
        }
        
        // Build hierarchy
        for (TaskNode task : allTasks) {
            DefaultMutableTreeNode node = nodeMap.get(task.getId());
            
            if (task.getParentId() == null) {
                // Root level task
                root.add(node);
            } else {
                // Child task - find parent
                DefaultMutableTreeNode parentNode = nodeMap.get(task.getParentId());
                if (parentNode != null) {
                    parentNode.add(node);
                } else {
                    // Parent not found, add to root
                    root.add(node);
                }
            }
        }
        
        super.reload();
    }
}
