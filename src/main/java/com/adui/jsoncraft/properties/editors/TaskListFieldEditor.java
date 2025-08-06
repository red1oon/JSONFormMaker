package com.adui.jsoncraft.properties.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.properties.editors.task.dialogs.DependencyEditDialog;
import com.adui.jsoncraft.properties.editors.task.dialogs.ResourceEditDialog;
import com.adui.jsoncraft.properties.editors.task.dialogs.TaskEditDialog;
// Import all supporting classes from the task package
import com.adui.jsoncraft.properties.editors.task.model.ProjectTemplate;
import com.adui.jsoncraft.properties.editors.task.model.ResourceDefinition;
import com.adui.jsoncraft.properties.editors.task.model.TaskDependency;
import com.adui.jsoncraft.properties.editors.task.model.TaskNode;
import com.adui.jsoncraft.properties.editors.task.model.TaskPriority;
import com.adui.jsoncraft.properties.editors.task.model.TaskStatus;
import com.adui.jsoncraft.properties.editors.task.ui.TaskNodeRenderer;
import com.adui.jsoncraft.properties.editors.task.ui.TaskNodeTransferHandler;
import com.adui.jsoncraft.properties.editors.task.ui.TaskTreeModel;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Property editor for TaskListField - Project management with hierarchical tasks,
 * dependencies, resources, and templates
 * 
 * Version: 1.5.0 - COMPLETE IMPLEMENTATION WITH FOCUS PERSISTENCE FIX
 * Namespace: com.adui.jsoncraft.properties.editors.TaskListFieldEditor
 */
public class TaskListFieldEditor extends ComponentDataEditor {
    private static final Logger logger = LoggerFactory.getLogger(TaskListFieldEditor.class);
    
    // Task Structure components
    private JTree taskTree;
    private TaskTreeModel taskTreeModel;
    private JButton addTaskButton;
    private JButton editTaskButton;
    private JButton deleteTaskButton;
    private JButton addSubtaskButton;
    
    // Dependencies components
    private JTable dependencyTable;
    private DefaultTableModel dependencyTableModel;
    private JButton addDepButton;
    private JButton editDepButton;
    private JButton deleteDepButton;
    
    // Resources components
    private JTable resourceTable;
    private DefaultTableModel resourceTableModel;
    private JButton addResourceButton;
    private JButton editResourceButton;
    private JButton deleteResourceButton;
    
    // Templates components
    private JList<ProjectTemplate> templateList;
    private DefaultListModel<ProjectTemplate> templateListModel;
    private JButton applyTemplateButton;
    private JButton saveAsTemplateButton;
    private JTextArea templateDescArea;
    
    // Settings components
    private JComboBox<String> defaultStatusCombo;
    private JComboBox<String> defaultPriorityCombo;
    private JCheckBox autoCalculateProgressCheckbox;
    private JCheckBox showProgressBarsCheckbox;
    
    // Data Models
    private List<TaskNode> allTasks;
    private List<TaskDependency> dependencies;
    private List<ResourceDefinition> resources;
    private Map<String, ProjectTemplate> templates;
    
    public TaskListFieldEditor(FieldDefinition field) {
        super(field);
        logger.debug("Created TaskListFieldEditor for field: {}", field.getFieldId());
    }
    
    @Override
    protected void initializeComponents() {
        // Initialize data structures
        allTasks = new ArrayList<>();
        dependencies = new ArrayList<>();
        resources = new ArrayList<>();
        templates = createDefaultTemplates();
        
        // Task Structure components
        taskTreeModel = new TaskTreeModel();
        taskTree = new JTree(taskTreeModel);
        taskTree.setEditable(false);
        taskTree.setDragEnabled(true);
        taskTree.setDropMode(DropMode.ON_OR_INSERT);
        taskTree.setTransferHandler(new TaskNodeTransferHandler());
        taskTree.setCellRenderer(new TaskNodeRenderer());
        taskTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // Task buttons
        addTaskButton = new JButton("Add Root Task");
        addSubtaskButton = new JButton("Add Subtask");
        editTaskButton = new JButton("Edit Task");
        deleteTaskButton = new JButton("Delete Task");
        
        // Dependencies table
        String[] depColumns = {"From Task", "To Task", "Type", "Lag Days"};
        dependencyTableModel = new DefaultTableModel(depColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dependencyTable = new JTable(dependencyTableModel);
        dependencyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Dependencies buttons
        addDepButton = new JButton("Add Dependency");
        editDepButton = new JButton("Edit Dependency");
        deleteDepButton = new JButton("Delete Dependency");
        
        // Resources table
        String[] resColumns = {"ID", "Name", "Type", "Rate", "Availability %", "Skills"};
        resourceTableModel = new DefaultTableModel(resColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resourceTable = new JTable(resourceTableModel);
        resourceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Resources buttons
        addResourceButton = new JButton("Add Resource");
        editResourceButton = new JButton("Edit Resource");
        deleteResourceButton = new JButton("Delete Resource");
        
        // Templates components
        templateListModel = new DefaultListModel<>();
        templateList = new JList<>(templateListModel);
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateDescArea = new JTextArea(4, 20);
        templateDescArea.setEditable(false);
        templateDescArea.setLineWrap(true);
        templateDescArea.setWrapStyleWord(true);
        
        applyTemplateButton = new JButton("Apply Template");
        saveAsTemplateButton = new JButton("Save as Template");
        
        // Settings components
        defaultStatusCombo = new JComboBox<>(new String[]{
            "not_started", "in_progress", "completed", "on_hold", "cancelled"
        });
        defaultPriorityCombo = new JComboBox<>(new String[]{
            "low", "medium", "high", "critical"
        });
        autoCalculateProgressCheckbox = new JCheckBox("Auto-calculate Progress");
        showProgressBarsCheckbox = new JCheckBox("Show Progress Bars");
        
        // Setup event handlers AFTER all components are created
        setupEventHandlers();
        
        // Load templates into list
        for (ProjectTemplate template : templates.values()) {
            templateListModel.addElement(template);
        }
        
        // Initialize data displays
        refreshTaskTree();
        refreshDependencyTable();
        refreshResourceTable();
        updateTaskButtonStates();
    }
    
    private void setupEventHandlers() {
        // Task structure events - KEY FIX: Add notifyPropertyChanged() calls
        addTaskButton.addActionListener(e -> {
            addRootTask();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        addSubtaskButton.addActionListener(e -> {
            addSubtask();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        editTaskButton.addActionListener(e -> {
            editSelectedTask();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        deleteTaskButton.addActionListener(e -> {
            deleteSelectedTask();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        taskTree.addTreeSelectionListener(e -> updateTaskButtonStates());
        
        // Dependencies events - KEY FIX: Add notifyPropertyChanged() calls
        addDepButton.addActionListener(e -> {
            addDependency();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        editDepButton.addActionListener(e -> {
            editDependency();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        deleteDepButton.addActionListener(e -> {
            deleteDependency();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        // Dependency table selection listener
        dependencyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = dependencyTable.getSelectedRow() >= 0;
                editDepButton.setEnabled(hasSelection);
                deleteDepButton.setEnabled(hasSelection);
            }
        });
        
        // Resources events - KEY FIX: Add notifyPropertyChanged() calls
        addResourceButton.addActionListener(e -> {
            addResource();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        editResourceButton.addActionListener(e -> {
            editResource();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        deleteResourceButton.addActionListener(e -> {
            deleteResource();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        // Resource table selection listener
        resourceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = resourceTable.getSelectedRow() >= 0;
                editResourceButton.setEnabled(hasSelection);
                deleteResourceButton.setEnabled(hasSelection);
            }
        });
        
        // Templates events - KEY FIX: Add notifyPropertyChanged() calls
        templateList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ProjectTemplate selected = templateList.getSelectedValue();
                if (selected != null) {
                    templateDescArea.setText(selected.getDescription());
                    applyTemplateButton.setEnabled(true);
                } else {
                    templateDescArea.setText("");
                    applyTemplateButton.setEnabled(false);
                }
            }
        });
        
        applyTemplateButton.addActionListener(e -> {
            applySelectedTemplate();
            notifyPropertyChanged(); // ← CRITICAL: Save data immediately
        });
        
        saveAsTemplateButton.addActionListener(e -> {
            saveAsTemplate();
            // Note: Don't notify here as this doesn't change field data
        });
        
        // Settings events - KEY FIX: Add notifyPropertyChanged() calls
        defaultStatusCombo.addActionListener(e -> notifyPropertyChanged());
        defaultPriorityCombo.addActionListener(e -> notifyPropertyChanged());
        autoCalculateProgressCheckbox.addActionListener(e -> notifyPropertyChanged());
        showProgressBarsCheckbox.addActionListener(e -> notifyPropertyChanged());
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        // Note: Individual tabs will have their own titles, no need for overall border title
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Task Structure tab
        tabbedPane.addTab("Tasks", createTaskStructurePanel());
        
        // Dependencies tab
        tabbedPane.addTab("Dependencies", createDependenciesPanel());
        
        // Resources tab
        tabbedPane.addTab("Resources", createResourcesPanel());
        
        // Templates tab
        tabbedPane.addTab("Templates", createTemplatesPanel());
        
        // Settings tab
        tabbedPane.addTab("Settings", createSettingsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createTaskStructurePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Task tree
        JScrollPane treeScrollPane = new JScrollPane(taskTree);
        treeScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addTaskButton);
        buttonPanel.add(addSubtaskButton);
        buttonPanel.add(editTaskButton);
        buttonPanel.add(deleteTaskButton);
        
        panel.add(treeScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createDependenciesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Dependencies table
        JScrollPane tableScrollPane = new JScrollPane(dependencyTable);
        tableScrollPane.setPreferredSize(new Dimension(400, 200));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addDepButton);
        buttonPanel.add(editDepButton);
        buttonPanel.add(deleteDepButton);
        
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createResourcesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Resources table
        JScrollPane tableScrollPane = new JScrollPane(resourceTable);
        tableScrollPane.setPreferredSize(new Dimension(400, 200));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addResourceButton);
        buttonPanel.add(editResourceButton);
        buttonPanel.add(deleteResourceButton);
        
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTemplatesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Split pane for list and description
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left panel - template list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Available Templates"));
        JScrollPane listScrollPane = new JScrollPane(templateList);
        listScrollPane.setPreferredSize(new Dimension(200, 250));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);
        
        // Right panel - description and buttons
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Template Description"));
        
        JScrollPane descScrollPane = new JScrollPane(templateDescArea);
        rightPanel.add(descScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(applyTemplateButton);
        buttonPanel.add(saveAsTemplateButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(200);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Default Status
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Default Status:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(defaultStatusCombo, gbc);
        
        // Default Priority
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Default Priority:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(defaultPriorityCombo, gbc);
        
        // Checkboxes
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(autoCalculateProgressCheckbox, gbc);
        
        gbc.gridy = 3;
        panel.add(showProgressBarsCheckbox, gbc);
        
        // Add vertical glue
        gbc.gridy = 4; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), gbc);
        
        return panel;
    }
    
    @Override
    protected void loadFieldData() {
        if (field == null) {
            logger.warn("Field is null during loadFieldData()");
            return;
        }
        
        try {
            // Create new lists instead of clearing existing ones to avoid TreeModel reference issues
            List<TaskNode> newTasks = new ArrayList<>();
            List<TaskDependency> newDependencies = new ArrayList<>();
            List<ResourceDefinition> newResources = new ArrayList<>();
            
            // Load tasks with better error handling
            Object tasksData = field.getDataProperty("tasks");
            logger.info("Tasks data type: {}", tasksData != null ? tasksData.getClass().getName() : "null");

            if (tasksData != null) {
                List<?> tasksList = null;
                
                if (tasksData instanceof List) {
                    // Already a List - use directly
                    tasksList = (List<?>) tasksData;
                    logger.info("Tasks data is already a List with {} items", tasksList.size());
                } else if (tasksData instanceof String) {
                    // JSON string - need to parse it
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        tasksList = mapper.readValue((String) tasksData, List.class);
                        logger.info("Parsed JSON string to List with {} items", tasksList.size());
                    } catch (Exception e) {
                        logger.error("Failed to parse tasks JSON string: {}", tasksData, e);
                    }
                } else {
                    logger.warn("Unexpected tasks data type: {}", tasksData.getClass().getName());
                }
                
                if (tasksList != null) {
                    for (Object taskObj : tasksList) {
                        if (taskObj instanceof Map) {
                            try {
                                TaskNode task = TaskNode.fromMap((Map<String, Object>) taskObj);
                                newTasks.add(task);
                                logger.debug("Loaded task: {}", task.getName());
                            } catch (Exception e) {
                                logger.warn("Failed to load task: " + taskObj, e);
                            }
                        }
                    }
                }
            }

            // Apply same fix for dependencies:
            Object dependenciesData = field.getDataProperty("dependencies");
            if (dependenciesData != null) {
                List<?> depsList = null;
                
                if (dependenciesData instanceof List) {
                    depsList = (List<?>) dependenciesData;
                } else if (dependenciesData instanceof String) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        depsList = mapper.readValue((String) dependenciesData, List.class);
                    } catch (Exception e) {
                        logger.error("Failed to parse dependencies JSON string", e);
                    }
                }
                
                if (depsList != null) {
                    for (Object depObj : depsList) {
                        if (depObj instanceof Map) {
                            try {
                                TaskDependency dep = TaskDependency.fromMap((Map<String, Object>) depObj);
                                newDependencies.add(dep);
                            } catch (Exception e) {
                                logger.warn("Failed to load dependency: " + depObj, e);
                            }
                        }
                    }
                }
            }

            // Apply same fix for resources:
            Object resourcesData = field.getDataProperty("resources");
            if (resourcesData != null) {
                List<?> resList = null;
                
                if (resourcesData instanceof List) {
                    resList = (List<?>) resourcesData;
                } else if (resourcesData instanceof String) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        resList = mapper.readValue((String) resourcesData, List.class);
                    } catch (Exception e) {
                        logger.error("Failed to parse resources JSON string", e);
                    }
                }
                
                if (resList != null) {
                    for (Object resObj : resList) {
                        if (resObj instanceof Map) {
                            try {
                                ResourceDefinition res = ResourceDefinition.fromMap((Map<String, Object>) resObj);
                                newResources.add(res);
                            } catch (Exception e) {
                                logger.warn("Failed to load resource: " + resObj, e);
                            }
                        }
                    }
                }
            }
            
            // Replace the lists atomically - this prevents TreeModel reference issues
            this.allTasks = newTasks;
            this.dependencies = newDependencies;
            this.resources = newResources;
            
            // Load settings
            Object settingsData = field.getDataProperty("settings");
            if (settingsData instanceof Map) {
                Map<String, Object> settings = (Map<String, Object>) settingsData;
                
                String defaultStatus = (String) settings.get("defaultStatus");
                if (defaultStatus != null) {
                    defaultStatusCombo.setSelectedItem(defaultStatus);
                }
                
                String defaultPriority = (String) settings.get("defaultPriority");
                if (defaultPriority != null) {
                    defaultPriorityCombo.setSelectedItem(defaultPriority);
                }
                
                Boolean autoCalc = (Boolean) settings.get("autoCalculateProgress");
                if (autoCalc != null) {
                    autoCalculateProgressCheckbox.setSelected(autoCalc);
                }
                
                Boolean showProgress = (Boolean) settings.get("showProgressBars");
                if (showProgress != null) {
                    showProgressBarsCheckbox.setSelected(showProgress);
                }
            }
            
            // Refresh UI on EDT
            SwingUtilities.invokeLater(() -> {
                refreshTaskTree();
                refreshDependencyTable();
                refreshResourceTable();
                updateTaskButtonStates();
            });
            
            logger.debug("Loaded field data: {} tasks, {} dependencies, {} resources", 
                        allTasks.size(), dependencies.size(), resources.size());
            logger.info("=== JSON PARSER DEBUG ===");
            logger.info("Field ID: {}", field.getFieldId());
            logger.info("Field Data Map: {}", field.getData());
            logger.info("Tasks Data Raw: {}", field.getDataProperty("tasks"));
            logger.info("Dependencies Data Raw: {}", field.getDataProperty("dependencies"));
            logger.info("Resources Data Raw: {}", field.getDataProperty("resources"));
            logger.info("========================");            
        } catch (Exception e) {
            logger.error("Error loading field data", e);
        }
    }
    
    @Override
    protected void saveFieldData() {
        if (field == null) {
            logger.warn("Field is null during saveFieldData()");
            return;
        }
        
        try {
            // Save tasks
            List<Map<String, Object>> taskMaps = new ArrayList<>();
            for (TaskNode task : allTasks) {
                taskMaps.add(task.toMap());
            }
            field.setDataProperty("tasks", taskMaps);
            
            // Save dependencies
            List<Map<String, Object>> depMaps = new ArrayList<>();
            for (TaskDependency dep : dependencies) {
                depMaps.add(dep.toMap());
            }
            field.setDataProperty("dependencies", depMaps);
            
            // Save resources
            List<Map<String, Object>> resMaps = new ArrayList<>();
            for (ResourceDefinition res : resources) {
                resMaps.add(res.toMap());
            }
            field.setDataProperty("resources", resMaps);
            
            // Save settings
            Map<String, Object> settings = new HashMap<>();
            settings.put("defaultStatus", defaultStatusCombo.getSelectedItem());
            settings.put("defaultPriority", defaultPriorityCombo.getSelectedItem());
            settings.put("autoCalculateProgress", autoCalculateProgressCheckbox.isSelected());
            settings.put("showProgressBars", showProgressBarsCheckbox.isSelected());
            field.setDataProperty("settings", settings);
            
            logger.debug("Saved field data for field: {}", field.getFieldId());
            
        } catch (Exception e) {
            logger.error("Error saving field data", e);
        }
    }
    
    @Override
    public boolean validateInput() {
        // Validate all tasks have unique IDs
        Set<String> taskIds = new HashSet<>();
        for (TaskNode task : allTasks) {
            if (taskIds.contains(task.getId())) {
                JOptionPane.showMessageDialog(this, 
                    "Duplicate task ID found: " + task.getId(), 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            taskIds.add(task.getId());
        }
        
        // Validate dependencies reference valid tasks
        for (TaskDependency dep : dependencies) {
            if (!taskIds.contains(dep.getFromTaskId()) || !taskIds.contains(dep.getToTaskId())) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid dependency: references non-existent task", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    // ===========================================
    // TASK STRUCTURE METHODS
    // ===========================================
    
    private void addRootTask() {
        TaskEditDialog dialog = new TaskEditDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            TaskNode newTask = dialog.getTask();
            allTasks.add(newTask);
            refreshTaskTree();
            refreshDependencyTable(); // Update dependency table to reflect new task
        }
    }
    
    private void addSubtask() {
        TreePath selectedPath = taskTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a parent task first.", 
                "No Parent Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        if (!(selectedNode.getUserObject() instanceof TaskNode)) {
            JOptionPane.showMessageDialog(this, 
                "Please select a valid task to add subtask to.", 
                "Invalid Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        TaskNode parentTask = (TaskNode) selectedNode.getUserObject();
        TaskEditDialog dialog = new TaskEditDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            TaskNode newTask = dialog.getTask();
            newTask.setParentId(parentTask.getId());
            allTasks.add(newTask);
            refreshTaskTree();
            refreshDependencyTable();
        }
    }
    
    private void editSelectedTask() {
        TreePath selectedPath = taskTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a task to edit.", 
                "No Task Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        if (!(selectedNode.getUserObject() instanceof TaskNode)) {
            return;
        }
        
        TaskNode taskToEdit = (TaskNode) selectedNode.getUserObject();
        TaskEditDialog dialog = new TaskEditDialog((Frame) SwingUtilities.getWindowAncestor(this), taskToEdit);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            // Update the task in the list
            TaskNode updatedTask = dialog.getTask();
            for (int i = 0; i < allTasks.size(); i++) {
                if (allTasks.get(i).getId().equals(taskToEdit.getId())) {
                    allTasks.set(i, updatedTask);
                    break;
                }
            }
            refreshTaskTree();
            refreshDependencyTable();
        }
    }
    
    private void deleteSelectedTask() {
        TreePath selectedPath = taskTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a task to delete.", 
                "No Task Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        if (!(selectedNode.getUserObject() instanceof TaskNode)) {
            return;
        }
        
        TaskNode taskToDelete = (TaskNode) selectedNode.getUserObject();
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete task '" + taskToDelete.getName() + "'?\n" +
            "This will also delete all subtasks and related dependencies.", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            // Remove task and all its subtasks
            allTasks.removeIf(task -> 
                task.getId().equals(taskToDelete.getId()) || 
                taskToDelete.getId().equals(task.getParentId()));
            
            // Remove related dependencies
            dependencies.removeIf(dep -> 
                dep.getFromTaskId().equals(taskToDelete.getId()) || 
                dep.getToTaskId().equals(taskToDelete.getId()));
            
            refreshTaskTree();
            refreshDependencyTable();
        }
    }
    
    private void refreshTaskTree() {
        try {
            if (taskTreeModel != null && taskTree != null) {
                logger.debug("Refreshing task tree with {} tasks", allTasks.size());
                
                // Pass a copy of the tasks to prevent reference issues
                taskTreeModel.setTasks(new ArrayList<>(allTasks));
                
                // Expand all nodes
                SwingUtilities.invokeLater(() -> {
                    try {
                        for (int i = 0; i < taskTree.getRowCount(); i++) {
                            taskTree.expandRow(i);
                        }
                        logger.debug("Task tree refresh completed, {} rows visible", taskTree.getRowCount());
                    } catch (Exception e) {
                        logger.warn("Error expanding tree rows", e);
                    }
                });
            }
        } catch (Exception e) {
            logger.warn("Error refreshing task tree", e);
        }
    }
    
    private void updateTaskButtonStates() {
        TreePath selectedPath = taskTree.getSelectionPath();
        boolean hasSelection = selectedPath != null && 
            selectedPath.getLastPathComponent() instanceof DefaultMutableTreeNode &&
            ((DefaultMutableTreeNode) selectedPath.getLastPathComponent()).getUserObject() instanceof TaskNode;
        
        editTaskButton.setEnabled(hasSelection);
        deleteTaskButton.setEnabled(hasSelection);
        addSubtaskButton.setEnabled(hasSelection);
    }
    
    // ===========================================
    // DEPENDENCY METHODS
    // ===========================================
    
    private void addDependency() {
        if (allTasks.size() < 2) {
            JOptionPane.showMessageDialog(this, 
                "You need at least 2 tasks to create a dependency.", 
                "Insufficient Tasks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        DependencyEditDialog dialog = new DependencyEditDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), null, allTasks);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            TaskDependency newDep = dialog.getDependency();
            dependencies.add(newDep);
            refreshDependencyTable();
        }
    }
    
    private void editDependency() {
        int selectedRow = dependencyTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a dependency to edit.", 
                "No Dependency Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        TaskDependency depToEdit = dependencies.get(selectedRow);
        DependencyEditDialog dialog = new DependencyEditDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), depToEdit, allTasks);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            TaskDependency updatedDep = dialog.getDependency();
            dependencies.set(selectedRow, updatedDep);
            refreshDependencyTable();
        }
    }
    
    private void deleteDependency() {
        int selectedRow = dependencyTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a dependency to delete.", 
                "No Dependency Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this dependency?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            dependencies.remove(selectedRow);
            refreshDependencyTable();
        }
    }
    
    private void refreshDependencyTable() {
        dependencyTableModel.setRowCount(0);
        
        for (TaskDependency dep : dependencies) {
            // Find task names for display
            String fromTaskName = findTaskName(dep.getFromTaskId());
            String toTaskName = findTaskName(dep.getToTaskId());
            
            Object[] row = {
                fromTaskName + " (" + dep.getFromTaskId() + ")",
                toTaskName + " (" + dep.getToTaskId() + ")",
                dep.getType(),
                dep.getLagDays()
            };
            dependencyTableModel.addRow(row);
        }
        
        // Update button states
        boolean hasSelection = dependencyTable.getSelectedRow() >= 0;
        editDepButton.setEnabled(hasSelection);
        deleteDepButton.setEnabled(hasSelection);
    }
    
    private String findTaskName(String taskId) {
        for (TaskNode task : allTasks) {
            if (task.getId().equals(taskId)) {
                return task.getName();
            }
        }
        return "Unknown";
    }
    
    // ===========================================
    // RESOURCE METHODS
    // ===========================================
    
    private void addResource() {
        ResourceEditDialog dialog = new ResourceEditDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ResourceDefinition newResource = dialog.getResource();
            resources.add(newResource);
            refreshResourceTable();
        }
    }
    
    private void editResource() {
        int selectedRow = resourceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a resource to edit.", 
                "No Resource Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        ResourceDefinition resToEdit = resources.get(selectedRow);
        ResourceEditDialog dialog = new ResourceEditDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), resToEdit);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ResourceDefinition updatedResource = dialog.getResource();
            resources.set(selectedRow, updatedResource);
            refreshResourceTable();
        }
    }
    
    private void deleteResource() {
        int selectedRow = resourceTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a resource to delete.", 
                "No Resource Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this resource?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            resources.remove(selectedRow);
            refreshResourceTable();
        }
    }
    
    private void refreshResourceTable() {
        resourceTableModel.setRowCount(0);
        
        for (ResourceDefinition resource : resources) {
            Object[] row = {
                resource.getId(),
                resource.getName(),
                resource.getType(),
                resource.getRate(),
                resource.getAvailability() + "%",
                resource.getSkills()
            };
            resourceTableModel.addRow(row);
        }
        
        // Update button states
        boolean hasSelection = resourceTable.getSelectedRow() >= 0;
        editResourceButton.setEnabled(hasSelection);
        deleteResourceButton.setEnabled(hasSelection);
    }
    
    // ===========================================
    // TEMPLATE METHODS
    // ===========================================
    
    private void applySelectedTemplate() {
        ProjectTemplate selected = templateList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a template to apply.", 
                "No Template Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Applying this template will replace all current tasks and dependencies.\n" +
            "Do you want to continue?", 
            "Confirm Template Application", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            // Clear existing data
            allTasks.clear();
            dependencies.clear();
            
            // Apply template
            allTasks.addAll(selected.getTasks());
            dependencies.addAll(selected.getDependencies());
            
            // Refresh displays
            refreshTaskTree();
            refreshDependencyTable();
        }
    }
    
    private void saveAsTemplate() {
        if (allTasks.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No tasks to save as template.", 
                "No Tasks", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String name = JOptionPane.showInputDialog(this, 
            "Enter template name:", 
            "Save Template", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            String description = JOptionPane.showInputDialog(this, 
                "Enter template description (optional):", 
                "Template Description", 
                JOptionPane.PLAIN_MESSAGE);
            
            ProjectTemplate template = new ProjectTemplate(
                name.trim(), 
                description != null ? description.trim() : "", 
                new ArrayList<>(allTasks), 
                new ArrayList<>(dependencies)
            );
            
            templates.put(template.getName(), template);
            
            // Update template list
            templateListModel.clear();
            for (ProjectTemplate tmpl : templates.values()) {
                templateListModel.addElement(tmpl);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Template saved successfully!", 
                "Template Saved", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private Map<String, ProjectTemplate> createDefaultTemplates() {
        Map<String, ProjectTemplate> templateMap = new HashMap<>();
        
        // Software Development Template
        List<TaskNode> devTasks = Arrays.asList(
            new TaskNode("DEV001", "Requirements Analysis", null, TaskStatus.NOT_STARTED, TaskPriority.HIGH, "Planning"),
            new TaskNode("DEV002", "System Design", null, TaskStatus.NOT_STARTED, TaskPriority.HIGH, "Design"),
            new TaskNode("DEV003", "Implementation", null, TaskStatus.NOT_STARTED, TaskPriority.MEDIUM, "Development"),
            new TaskNode("DEV004", "Testing", null, TaskStatus.NOT_STARTED, TaskPriority.MEDIUM, "Testing"),
            new TaskNode("DEV005", "Deployment", null, TaskStatus.NOT_STARTED, TaskPriority.LOW, "Deployment")
        );
        
        List<TaskDependency> devDeps = Arrays.asList(
            new TaskDependency("DEV001", "DEV002", "finish-to-start", 1),
            new TaskDependency("DEV002", "DEV003", "finish-to-start", 2),
            new TaskDependency("DEV003", "DEV004", "finish-to-start", 0),
            new TaskDependency("DEV004", "DEV005", "finish-to-start", 1)
        );
        
        ProjectTemplate devTemplate = new ProjectTemplate("Software Development", 
            "Standard software development lifecycle with requirements, design, development, testing, and deployment phases", 
            devTasks, devDeps);
        templateMap.put(devTemplate.getName(), devTemplate);
        
        // Construction Project Template
        List<TaskNode> constructionTasks = Arrays.asList(
            new TaskNode("CONST001", "Site Preparation", null, TaskStatus.NOT_STARTED, TaskPriority.HIGH, "Planning"),
            new TaskNode("CONST002", "Foundation", null, TaskStatus.NOT_STARTED, TaskPriority.HIGH, "Construction"),
            new TaskNode("CONST003", "Framing", null, TaskStatus.NOT_STARTED, TaskPriority.MEDIUM, "Construction"),
            new TaskNode("CONST004", "Electrical & Plumbing", null, TaskStatus.NOT_STARTED, TaskPriority.MEDIUM, "Systems"),
            new TaskNode("CONST005", "Finishing", null, TaskStatus.NOT_STARTED, TaskPriority.LOW, "Completion")
        );
        
        List<TaskDependency> constructionDeps = Arrays.asList(
            new TaskDependency("CONST001", "CONST002", "finish-to-start", 1),
            new TaskDependency("CONST002", "CONST003", "finish-to-start", 2),
            new TaskDependency("CONST003", "CONST004", "finish-to-start", 0),
            new TaskDependency("CONST004", "CONST005", "finish-to-start", 3)
        );
        
        ProjectTemplate constructionTemplate = new ProjectTemplate("Construction Project", 
            "Standard construction project phases from site preparation to completion", 
            constructionTasks, constructionDeps);
        templateMap.put(constructionTemplate.getName(), constructionTemplate);
        
        return templateMap;
    }
    
    // Helper method to notify parent of property changes
    @Override
    protected void notifyPropertyChanged() {
        saveFieldData();
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChanged(field);
        }
        logger.debug("Property changed notification sent for field: {}", field.getFieldId());
    }
}