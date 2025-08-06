package com.adui.jsoncraft.properties.editors.task.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.adui.jsoncraft.properties.editors.task.model.TaskNode;
import com.adui.jsoncraft.properties.editors.task.model.TaskPriority;
import com.adui.jsoncraft.properties.editors.task.model.TaskStatus;

/**
 * Custom renderer for task tree nodes with status and priority indicators
 */
public class TaskNodeRenderer extends DefaultTreeCellRenderer {
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
            boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof TaskNode) {
                TaskNode task = (TaskNode) userObject;
                
                // Set icon based on status
                Icon icon = getIconForStatus(task.getStatus());
                setIcon(icon);
                
                // Set text with priority and completion
                String text = task.getName();
                if (task.getCompletion() > 0) {
                    text += " (" + task.getCompletion() + "%)";
                }
                setText(text);
                
                // Set text color based on priority
                Color textColor = getColorForPriority(task.getPriority());
                setForeground(sel ? Color.WHITE : textColor);
            }
        }
        
        return this;
    }
    
    private Icon getIconForStatus(TaskStatus status) {
        // Create simple colored circles for status
        return new StatusIcon(getColorForStatus(status));
    }
    
    private Color getColorForStatus(TaskStatus status) {
        switch (status) {
            case NOT_STARTED: return Color.LIGHT_GRAY;
            case IN_PROGRESS: return Color.BLUE;
            case COMPLETED: return Color.GREEN;
            case BLOCKED: return Color.RED;
            case ON_HOLD: return Color.ORANGE;
            default: return Color.LIGHT_GRAY;
        }
    }
    
    private Color getColorForPriority(TaskPriority priority) {
        switch (priority) {
            case LOW: return Color.BLACK;
            case MEDIUM: return Color.BLUE;
            case HIGH: return Color.ORANGE;
            case CRITICAL: return Color.RED;
            default: return Color.BLACK;
        }
    }
    
    /**
     * Simple icon for task status
     */
    private static class StatusIcon implements Icon {
        private final Color color;
        
        public StatusIcon(Color color) {
            this.color = color;
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, getIconWidth(), getIconHeight());
            g2.setColor(Color.DARK_GRAY);
            g2.drawOval(x, y, getIconWidth(), getIconHeight());
            g2.dispose();
        }
        
        @Override
        public int getIconWidth() { return 12; }
        
        @Override
        public int getIconHeight() { return 12; }
    }
}
