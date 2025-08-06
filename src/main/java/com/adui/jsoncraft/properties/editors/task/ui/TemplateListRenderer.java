package com.adui.jsoncraft.properties.editors.task.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.adui.jsoncraft.properties.editors.task.model.ProjectTemplate;

/**
 * Custom renderer for template list
 */
public class TemplateListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof ProjectTemplate) {
            ProjectTemplate template = (ProjectTemplate) value;
            setText(template.getName());
            setToolTipText(template.getDescription());
        }
        
        return this;
    }
}
