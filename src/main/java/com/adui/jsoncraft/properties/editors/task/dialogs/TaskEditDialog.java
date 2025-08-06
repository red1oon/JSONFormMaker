package com.adui.jsoncraft.properties.editors.task.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.adui.jsoncraft.properties.editors.task.model.TaskNode;
import com.adui.jsoncraft.properties.editors.task.model.TaskPriority;
import com.adui.jsoncraft.properties.editors.task.model.TaskStatus;

/**
 * Dialog for editing task properties
 */
public class TaskEditDialog extends JDialog {
    private TaskNode task;
    private TaskNode originalTask;
    private boolean confirmed = false;
    
    // Form fields
    private JTextField idField;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JComboBox<TaskStatus> statusCombo;
    private JComboBox<TaskPriority> priorityCombo;
    private JTextField assigneeField;
    private JSpinner estimatedHoursSpinner;
    private JSpinner actualHoursSpinner;
    private JFormattedTextField startDateField;
    private JFormattedTextField endDateField;
    private JSlider completionSlider;
    private JTextField phaseField;
    
    public TaskEditDialog(Frame parent, TaskNode task) {
        super(parent, task == null ? "Add Task" : "Edit Task", true);
        this.originalTask = task;
        this.task = task != null ? copyTask(task) : createNewTask();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadTaskData();
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }
    

    // ALSO ADD this improved initializeComponents() method:
    private void initializeComponents() {
        // Components will be created in setupLayout() for better organization
    }
    
 // REPLACE the setupLayout() method in TaskEditDialog.java with this:

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 0: ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        idField = new JTextField(20);
        formPanel.add(idField, gbc);
        
        // Row 1: Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(30);
        formPanel.add(nameField, gbc);
        
        // Row 2: Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.3;
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(300, 60));
        formPanel.add(descScroll, gbc);
        
        // Row 3: Status and Priority (side by side)
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        statusCombo = new JComboBox<>(TaskStatus.values());
        statusCombo.setPreferredSize(new Dimension(150, 25));
        formPanel.add(statusCombo, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("  Priority:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        priorityCombo = new JComboBox<>(TaskPriority.values());
        priorityCombo.setPreferredSize(new Dimension(150, 25));
        formPanel.add(priorityCombo, gbc);
        
        // Row 4: Assignee
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Assignee:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        assigneeField = new JTextField(25);
        formPanel.add(assigneeField, gbc);
        
        // Row 5: Estimated and Actual Hours (side by side)
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Estimated Hours:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        estimatedHoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        estimatedHoursSpinner.setPreferredSize(new Dimension(100, 25));
        formPanel.add(estimatedHoursSpinner, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("  Actual Hours:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        actualHoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        actualHoursSpinner.setPreferredSize(new Dimension(100, 25));
        formPanel.add(actualHoursSpinner, gbc);
        
        // Row 6: Start and End Date (side by side)
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        startDateField = new JFormattedTextField();
        startDateField.setPreferredSize(new Dimension(120, 25));
        startDateField.setToolTipText("Format: YYYY-MM-DD");
        formPanel.add(startDateField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("  End Date:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        endDateField = new JFormattedTextField();
        endDateField.setPreferredSize(new Dimension(120, 25));
        endDateField.setToolTipText("Format: YYYY-MM-DD");
        formPanel.add(endDateField, gbc);
        
        // Row 7: Completion %
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Completion %:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        completionSlider = new JSlider(0, 100, 0);
        completionSlider.setMajorTickSpacing(25);
        completionSlider.setMinorTickSpacing(5);
        completionSlider.setPaintTicks(true);
        completionSlider.setPaintLabels(true);
        completionSlider.setPreferredSize(new Dimension(300, 50));
        
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(completionSlider, BorderLayout.CENTER);
        JLabel completionLabel = new JLabel("0%");
        completionSlider.addChangeListener(e -> completionLabel.setText(completionSlider.getValue() + "%"));
        sliderPanel.add(completionLabel, BorderLayout.EAST);
        formPanel.add(sliderPanel, gbc);
        
        // Row 8: Phase
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Phase:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        phaseField = new JTextField(25);
        formPanel.add(phaseField, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(80, 30));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(80, 30));
        
        okButton.addActionListener(e -> {
            if (validateAndSave()) {
                confirmed = true;
                setVisible(false);
            }
        });
        
        cancelButton.addActionListener(e -> setVisible(false));
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Size the dialog properly
        setSize(500, 450);
        setLocationRelativeTo(getParent());
    }
    
    private void setupEventHandlers() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Auto-generate ID if adding new task
        if (originalTask == null) {
            nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { updateId(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { updateId(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { updateId(); }
                
                private void updateId() {
                    if (idField.getText().isEmpty()) {
                        String name = nameField.getText().trim();
                        if (!name.isEmpty()) {
                            String id = name.toUpperCase().replaceAll("\\s+", "_");
                            if (id.length() > 10) {
                                id = id.substring(0, 10);
                            }
                            idField.setText(id);
                        }
                    }
                }
            });
        }
    }
    
    private void loadTaskData() {
        if (task != null) {
            idField.setText(task.getId() != null ? task.getId() : "");
            nameField.setText(task.getName() != null ? task.getName() : "");
            descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");
            statusCombo.setSelectedItem(task.getStatus());
            priorityCombo.setSelectedItem(task.getPriority());
            assigneeField.setText(task.getAssignee() != null ? task.getAssignee() : "");
            
            if (task.getEstimatedHours() != null) {
                estimatedHoursSpinner.setValue(task.getEstimatedHours());
            }
            if (task.getActualHours() != null) {
                actualHoursSpinner.setValue(task.getActualHours());
            }
            
            if (task.getStartDate() != null) {
                startDateField.setText(task.getStartDate().toString());
            }
            if (task.getEndDate() != null) {
                endDateField.setText(task.getEndDate().toString());
            }
            
            completionSlider.setValue(task.getCompletion());
            phaseField.setText(task.getPhase() != null ? task.getPhase() : "");
        }
        
        // Disable ID field if editing existing task
        idField.setEnabled(originalTask == null);
    }
    
    private boolean validateAndSave() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Update task with form data
        task.setId(id);
        task.setName(name);
        task.setDescription(descriptionArea.getText().trim());
        task.setStatus((TaskStatus) statusCombo.getSelectedItem());
        task.setPriority((TaskPriority) priorityCombo.getSelectedItem());
        task.setAssignee(assigneeField.getText().trim());
        task.setEstimatedHours((Integer) estimatedHoursSpinner.getValue());
        task.setActualHours((Integer) actualHoursSpinner.getValue());
        
        // Parse dates
        try {
            String startDateText = startDateField.getText().trim();
            if (!startDateText.isEmpty()) {
                task.setStartDate(LocalDate.parse(startDateText));
            }
            
            String endDateText = endDateField.getText().trim();
            if (!endDateText.isEmpty()) {
                task.setEndDate(LocalDate.parse(endDateText));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        task.setCompletion(completionSlider.getValue());
        task.setPhase(phaseField.getText().trim());
        
        return true;
    }
    
    private TaskNode copyTask(TaskNode original) {
        TaskNode copy = new TaskNode(original.getId(), original.getName(), 
            original.getParentId(), original.getStatus(), original.getPriority(), original.getPhase());
        copy.setDescription(original.getDescription());
        copy.setAssignee(original.getAssignee());
        copy.setEstimatedHours(original.getEstimatedHours());
        copy.setActualHours(original.getActualHours());
        copy.setStartDate(original.getStartDate());
        copy.setEndDate(original.getEndDate());
        copy.setCompletion(original.getCompletion());
        return copy;
    }
    
    private TaskNode createNewTask() {
        return new TaskNode("", "", null, TaskStatus.NOT_STARTED, TaskPriority.MEDIUM, "");
    }
    
    public TaskNode getTask() {
        return task;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
