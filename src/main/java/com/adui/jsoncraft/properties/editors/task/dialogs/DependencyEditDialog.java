package com.adui.jsoncraft.properties.editors.task.dialogs;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.adui.jsoncraft.properties.editors.task.model.TaskDependency;
import com.adui.jsoncraft.properties.editors.task.model.TaskNode;

/**
 * Dialog for editing task dependencies
 */
public class DependencyEditDialog extends JDialog {
    private TaskDependency dependency;
    private boolean confirmed = false;
    
    private JComboBox<TaskNode> fromTaskCombo;
    private JComboBox<TaskNode> toTaskCombo;
    private JComboBox<String> typeCombo;
    private JSpinner lagDaysSpinner;
    
    public DependencyEditDialog(Frame parent, TaskDependency dependency, List<TaskNode> allTasks) {
        super(parent, dependency == null ? "Add Dependency" : "Edit Dependency", true);
        this.dependency = dependency != null ? dependency : new TaskDependency("", "", "finish-to-start", 0);
        
        initializeComponents(allTasks);
        setupLayout();
        setupEventHandlers();
        loadDependencyData();
        
        setSize(400, 200);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents(List<TaskNode> allTasks) {
        fromTaskCombo = new JComboBox<>(allTasks.toArray(new TaskNode[0]));
        fromTaskCombo.setRenderer(new TaskComboRenderer());
        
        toTaskCombo = new JComboBox<>(allTasks.toArray(new TaskNode[0]));
        toTaskCombo.setRenderer(new TaskComboRenderer());
        
        typeCombo = new JComboBox<>(new String[]{
            "finish-to-start", "start-to-start", "finish-to-finish", "start-to-finish"
        });
        
        lagDaysSpinner = new JSpinner(new SpinnerNumberModel(0, -365, 365, 1));
    }
    
    private void setupLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("From Task:"), gbc);
        gbc.gridx = 1;
        add(fromTaskCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("To Task:"), gbc);
        gbc.gridx = 1;
        add(toTaskCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Lag Days:"), gbc);
        gbc.gridx = 1;
        add(lagDaysSpinner, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            if (validateAndSave()) {
                confirmed = true;
                setVisible(false);
            }
        });
        
        cancelButton.addActionListener(e -> setVisible(false));
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }
    
    private void setupEventHandlers() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void loadDependencyData() {
        if (dependency != null) {
            // Find and select tasks
            for (int i = 0; i < fromTaskCombo.getItemCount(); i++) {
                TaskNode task = fromTaskCombo.getItemAt(i);
                if (task.getId().equals(dependency.getFromTaskId())) {
                    fromTaskCombo.setSelectedIndex(i);
                    break;
                }
            }
            
            for (int i = 0; i < toTaskCombo.getItemCount(); i++) {
                TaskNode task = toTaskCombo.getItemAt(i);
                if (task.getId().equals(dependency.getToTaskId())) {
                    toTaskCombo.setSelectedIndex(i);
                    break;
                }
            }
            
            typeCombo.setSelectedItem(dependency.getType());
            lagDaysSpinner.setValue(dependency.getLagDays());
        }
    }
    
    private boolean validateAndSave() {
        TaskNode fromTask = (TaskNode) fromTaskCombo.getSelectedItem();
        TaskNode toTask = (TaskNode) toTaskCombo.getSelectedItem();
        
        if (fromTask == null || toTask == null) {
            JOptionPane.showMessageDialog(this, "Both tasks must be selected", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (fromTask.getId().equals(toTask.getId())) {
            JOptionPane.showMessageDialog(this, "Task cannot depend on itself", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        dependency.setFromTaskId(fromTask.getId());
        dependency.setToTaskId(toTask.getId());
        dependency.setType((String) typeCombo.getSelectedItem());
        dependency.setLagDays((Integer) lagDaysSpinner.getValue());
        
        return true;
    }
    
    public TaskDependency getDependency() {
        return dependency;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Custom renderer for task combo boxes
     */
    private static class TaskComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof TaskNode) {
                TaskNode task = (TaskNode) value;
                setText(task.getName() + " (" + task.getId() + ")");
            }
            
            return this;
        }
    }
}
