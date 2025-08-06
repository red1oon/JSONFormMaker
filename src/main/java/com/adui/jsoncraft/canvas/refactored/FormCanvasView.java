package com.adui.jsoncraft.canvas.refactored;

import java.awt.BorderLayout;
import com.adui.jsoncraft.canvas.refactored.events.ModelUpdateEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.components.TabPanel;
import com.adui.jsoncraft.canvas.refactored.events.FieldSelectionEvent;
import com.adui.jsoncraft.canvas.refactored.events.FormCanvasEventBus;
import com.adui.jsoncraft.canvas.refactored.events.FormChangeEvent;
import com.adui.jsoncraft.canvas.refactored.managers.DragDropManager;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;

/**
 * FormCanvas View - MVC Pattern Implementation
 * Handles UI rendering and user interaction
 * 
 * @version 1.3 - ADDED: Tab Properties Editor (preserves all existing functionality)
 * @namespace com.adui.jsoncraft.canvas.refactored.FormCanvasView
 */
public class FormCanvasView extends JPanel {
    private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(FormCanvasView.class);
	private boolean loadingProperties = false;
    // UI Components
    private JTabbedPane tabPane;
    private JPanel windowPropertiesPanel;
    private JTextField windowIdField;
    private JTextField windowNameField;
    private JTextArea windowDescField;
    private DragDropManager dragDropManager;
    
    // NEW: Tab Properties Panel Components
    private JPanel tabPropertiesPanel;
    private JTextField tabIdField;
    private JTextField tabNameField;
    private JTextArea tabDescField;
    private JSpinner tabSequenceSpinner;
    
    // State
    private final Map<TabDefinition, TabPanel> tabPanels;
    private final FormCanvasEventBus eventBus;
    private TabPanel currentTabPanel;
    
    // Track current window for drag-drop operations (PRESERVED)
    private WindowDefinition currentWindow;
    
    // View event listeners
    private final java.util.List<ViewEventListener> viewListeners;
    
    public FormCanvasView(DragDropManager dragDropManager) {
        this.dragDropManager = dragDropManager;
        this.tabPanels = new HashMap<>();
        this.eventBus = FormCanvasEventBus.getInstance();
        this.viewListeners = new java.util.ArrayList<>();
        this.currentWindow = null;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        registerEventListeners();
        
        logger.debug("FormCanvasView initialized with tab properties editor");
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Form Designer"));
        
        // Create window properties panel (PRESERVED)
        createWindowPropertiesPanel();
        
        // NEW: Create tab properties panel
        createTabPropertiesPanel();
        
        // Create tab pane for form design (PRESERVED)
        tabPane = new JTabbedPane();
        tabPane.setTabPlacement(JTabbedPane.TOP);
        
        // Add "Add Tab" button (PRESERVED)
        JButton addTabButton = new JButton("+");
        addTabButton.setToolTipText("Add new tab");
        addTabButton.addActionListener(e -> notifyAddTabRequested());
        tabPane.addTab("", new JPanel());
        tabPane.setTabComponentAt(0, addTabButton);
        
        // Tab change listener (ENHANCED to load tab properties)
        tabPane.addChangeListener(e -> handleTabSelection());
    }
    
    /**
     * Create window properties panel (PRESERVED)
     */
    private void createWindowPropertiesPanel() {
        windowPropertiesPanel = new JPanel(new GridBagLayout());
        windowPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Window Properties"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Window ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        windowPropertiesPanel.add(new JLabel("Window ID:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        windowIdField = new JTextField();
        windowPropertiesPanel.add(windowIdField, gbc);
        
        // Window Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        windowPropertiesPanel.add(new JLabel("Window Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        windowNameField = new JTextField();
        windowPropertiesPanel.add(windowNameField, gbc);
        
        // Window Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        windowPropertiesPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        windowDescField = new JTextArea(3, 20);
        windowDescField.setLineWrap(true);
        windowDescField.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(windowDescField);
        windowPropertiesPanel.add(descScrollPane, gbc);
    }
    
    /**
     * NEW: Create tab properties panel
     */
    private void createTabPropertiesPanel() {
        tabPropertiesPanel = new JPanel(new GridBagLayout());
        tabPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Tab Properties"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Tab ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        tabPropertiesPanel.add(new JLabel("Tab ID:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tabIdField = new JTextField();
        tabPropertiesPanel.add(tabIdField, gbc);
        
        // Tab Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        tabPropertiesPanel.add(new JLabel("Tab Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tabNameField = new JTextField();
        tabPropertiesPanel.add(tabNameField, gbc);
        
        // Tab Sequence
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        tabPropertiesPanel.add(new JLabel("Sequence:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        tabSequenceSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 10));
        tabPropertiesPanel.add(tabSequenceSpinner, gbc);
        
        // Tab Description
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        tabPropertiesPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        tabDescField = new JTextArea(2, 20);
        tabDescField.setLineWrap(true);
        tabDescField.setWrapStyleWord(true);
        JScrollPane tabDescScrollPane = new JScrollPane(tabDescField);
        tabPropertiesPanel.add(tabDescScrollPane, gbc);
    }
    
    /**
     * Setup panel layout (UPDATED to include tab properties)
     */
    private void setupLayout() {
        // Create north panel to hold both property panels
        JPanel northPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Window properties on top
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.BOTH; 
        gbc.weightx = 1.0; gbc.weighty = 0.6;
        northPanel.add(windowPropertiesPanel, gbc);
        
        // Tab properties below window properties
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; 
        gbc.weightx = 1.0; gbc.weighty = 0.4;
        northPanel.add(tabPropertiesPanel, gbc);
        
        add(northPanel, BorderLayout.NORTH);
        add(tabPane, BorderLayout.CENTER);
    }
    
    /**
     * Setup event handlers (ENHANCED with tab property listeners)
     */
    private void setupEventHandlers() {
        // Window property change listeners (PRESERVED)
        windowIdField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
        });
        
        windowNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
        });
        
        windowDescField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { notifyWindowPropertyChanged(); }
        });
        
        // NEW: Tab property change listeners
        tabIdField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
        });
        
        tabNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
        });
        
        tabDescField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { notifyTabPropertyChanged(); }
        });
        
        tabSequenceSpinner.addChangeListener(e -> notifyTabPropertyChanged());
    }
    
    /**
     * Register for FormCanvas events (PRESERVED)
     */
    private void registerEventListeners() {
        // Listen for selection events to update UI
        eventBus.register(FieldSelectionEvent.class, this::handleFieldSelection);
        
        // Listen for form change events
        eventBus.register(FormChangeEvent.class, this::handleFormChange);
    }
    
    /**
     * Display window in the view (PRESERVED)
     */
    public void displayWindow(WindowDefinition window) {
        String windowName = (window != null) ? window.getName() : "null";
        logger.warn("🔍 DISPLAY WINDOW START: '{}'", windowName);
        
        // Suppress ALL property change events during file loading
        loadingProperties = true;
        logger.warn("  🚫 Event suppression ENABLED");
        
        try {
            // Clear existing tabs BEFORE setting new window
            logger.warn("  🧹 Clearing tabs...");
            clearTabs();
            
            // Set new window AFTER cleanup
            logger.warn("  🪟 Setting current window: '{}'", windowName);
            this.currentWindow = window;
            
            if (window == null) {
                logger.warn("  ❌ Window is null - clearing display");
                clearWindow();
                return;
            }
            
            // Update window properties WITHOUT triggering events
            logger.warn("  📝 Setting UI fields:");
            logger.warn("    - Window ID: '{}'", window.getWindowId());
            logger.warn("    - Window Name: '{}'", window.getName());
            logger.warn("    - Window Desc: '{}'", window.getDescription());
            
            windowIdField.setText(window.getWindowId() != null ? window.getWindowId() : "");
            windowNameField.setText(window.getName() != null ? window.getName() : "");
            windowDescField.setText(window.getDescription() != null ? window.getDescription() : "");
            
            logger.warn("  📝 UI fields set - Field value now: '{}'", windowNameField.getText());
            
            // Add tabs from window definition
            if (window.getTabs() != null) {
                logger.warn("  📋 Adding {} tabs", window.getTabs().size());
                for (TabDefinition tab : window.getTabs()) {
                    addTabToView(tab);
                }
            }
            
            // Select first tab if available
            if (tabPane.getTabCount() > 1) {
                logger.warn("  🎯 Selecting first tab");
                tabPane.setSelectedIndex(0);
            }
            
            logger.warn("  ✅ Display complete: {} with {} tabs", 
                window.getWindowId(), 
                window.getTabs() != null ? window.getTabs().size() : 0);
                
        } finally {
            // Re-enable events AFTER everything is loaded
            logger.warn("  🔓 Event suppression DISABLED");
            loadingProperties = false;
            
            // Force tab selection event AFTER loading is complete
            if (tabPane.getTabCount() > 1) {
                logger.warn("  🎯 Triggering tab selection");
                handleTabSelection();
            }
            logger.warn("🔍 DISPLAY WINDOW END");
        }
    }
    
    /**
     * Add tab to view (PRESERVED - drag-drop functionality intact)
     */
    public void addTabToView(TabDefinition tab) {
        TabPanel tabPanel = new TabPanel(tab);
        
        // Set both DragDropManager and WindowDefinition on TabPanel (PRESERVED)
        if (dragDropManager != null) {
            tabPanel.setDragDropManager(dragDropManager);
            
            if (currentWindow != null) {
                tabPanel.setWindowDefinition(currentWindow);
                logger.debug("DragDropManager and WindowDefinition set on TabPanel: {}", tab.getTabId());
            } else {
                logger.warn("CurrentWindow is null - drag-drop may not work for tab: {}", tab.getTabId());
            }
        } else {
            logger.warn("DragDropManager is null - drag-drop will not work for tab: {}", tab.getTabId());
        }
        
        tabPanels.put(tab, tabPanel);
        
        // Create scrollable wrapper (PRESERVED)
        JScrollPane scrollPane = new JScrollPane(tabPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Insert before the "+" button (PRESERVED)
        int insertIndex = tabPane.getTabCount() - 1;
     // Insert tab with custom component
        tabPane.insertTab(tab.getName(), null, scrollPane, "Tab: " + tab.getName(), insertIndex);

        // 🆕 Add custom tab component with delete button
        JPanel tabComponent = createTabComponent(tab);
        tabPane.setTabComponentAt(insertIndex, tabComponent);
        
        logger.debug("Added tab to view: {}", tab.getTabId());
    }
    /**
     * Create custom tab component with delete button
     */
    private JPanel createTabComponent(TabDefinition tab) {
        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.setOpaque(false);
        
        // Tab label
        JLabel tabLabel = new JLabel(tab.getName());
        tabLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        
        // Delete button (small X)
        JButton deleteButton = new JButton("×");
        deleteButton.setPreferredSize(new Dimension(16, 16));
        deleteButton.setFont(deleteButton.getFont().deriveFont(10f));
        deleteButton.setBorder(BorderFactory.createEmptyBorder());
        deleteButton.setOpaque(false);
        deleteButton.setToolTipText("Delete tab");
        deleteButton.addActionListener(e -> requestTabDeletion(tab));
        
        tabPanel.add(tabLabel, BorderLayout.CENTER);
        tabPanel.add(deleteButton, BorderLayout.EAST);
        
        return tabPanel;
    }
    /**
     * Request tab deletion with confirmation
     */
    private void requestTabDeletion(TabDefinition tab) {
        logger.debug("Tab deletion requested: {}", tab.getTabId());
        
        // 🛡️ Prevent deletion of last tab
        if (currentWindow != null && currentWindow.getTabCount() <= 1) {
            JOptionPane.showMessageDialog(this, 
                "Cannot delete the last remaining tab.", 
                "Delete Tab", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 🔔 Confirmation dialog
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete tab '" + tab.getName() + "'?\n" +
            "This action cannot be undone.",
            "Delete Tab",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            deleteTab(tab);
        }
    }

    /**
     * Delete tab with proper cleanup and state management
     */
    /**
     * Delete tab with direct manipulation (bypasses Controller)
     */
    private void deleteTab(TabDefinition tab) {
        boolean wasLoading = loadingProperties;
        loadingProperties = true;
        
        try {
            logger.warn("🗑️ STARTING DIRECT TAB DELETION: {}", tab.getTabId());
            logger.warn("  📊 Tab count before: {}", tabPane.getTabCount() - 1); // -1 for + button
            
            // Find which tab we're deleting
            int tabIndexToDelete = findTabIndex(tab);
            logger.warn("  🎯 Tab index to delete: {}", tabIndexToDelete);
            
            if (tabIndexToDelete < 0) {
                logger.warn("  ❌ Tab not found in UI, aborting");
                return;
            }
            
            // 🧹 Clean up TabPanel references
            TabPanel tabPanel = tabPanels.get(tab);
            boolean wasCurrentTab = (currentTabPanel == tabPanel);
            
            if (tabPanel != null) {
                logger.warn("  🧹 Cleaning up TabPanel references");
                tabPanel.setDragDropManager(null);
                tabPanel.setWindowDefinition(null);
                tabPanel.clearSelection();
            }
            
            // 🗑️ Remove from internal maps FIRST
            logger.warn("  📝 Removing from internal tracking");
            tabPanels.remove(tab);
            
            // 🗑️ Remove from model
            if (currentWindow != null) {
                logger.warn("  📝 Removing from WindowDefinition");
                currentWindow.removeTab(tab);
                logger.warn("  ✅ Removed from model. Remaining tabs: {}", currentWindow.getTabCount());
            }
            
            // 🖥️ Remove from UI - DIRECT manipulation
            logger.warn("  🖥️ Removing tab from UI at index: {}", tabIndexToDelete);
            tabPane.removeTabAt(tabIndexToDelete);
            logger.warn("  ✅ UI removal complete. Remaining tabs: {}", tabPane.getTabCount() - 1);
            
            // 🎯 Handle selection state
            if (wasCurrentTab) {
                logger.warn("  🔄 Updating current selection (was current tab)");
                currentTabPanel = null;
                clearTabProperties();
                
                // Select another tab if available
                if (tabPane.getTabCount() > 1) { // > 1 because of + button
                    int newIndex = Math.min(tabIndexToDelete, tabPane.getTabCount() - 2);
                    if (newIndex >= 0) {
                        logger.warn("  👆 Selecting new tab at index: {}", newIndex);
                        tabPane.setSelectedIndex(newIndex);
                    }
                }
            }
            
            // 🔥 CRITICAL: NO ViewEvent, NO Controller notification!
            // Just fire a simple FormChangeEvent for any listeners that need to know
            logger.warn("  📡 Firing FormChangeEvent.TAB_REMOVED (info only)");
            FormChangeEvent deleteEvent = new FormChangeEvent(
                FormChangeEvent.Type.TAB_REMOVED, 
                currentWindow, 
                tab, 
                null, 
                null, 
                null
            );
            eventBus.fire(deleteEvent);
            
            logger.warn("🎉 DIRECT TAB DELETION COMPLETE: {}", tab.getTabId());
            logger.warn("  📊 Final UI tab count: {}", tabPane.getTabCount() - 1);
            logger.warn("  📊 Final model tab count: {}", currentWindow != null ? currentWindow.getTabCount() : "no window");
            
        } catch (Exception e) {
            logger.error("💥 FAILED TO DELETE TAB: {}", e.getMessage(), e);
            
            // Try to restore state on error
            if (currentWindow != null && !currentWindow.getTabs().contains(tab)) {
                logger.warn("  🔄 Attempting to restore tab to model on error");
                currentWindow.addTab(tab);
            }
            
            JOptionPane.showMessageDialog(this,
                "Failed to delete tab: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            if (!wasLoading) {
                loadingProperties = false;
            }
        }
    }
    /**
     * Remove tab from view (PRESERVED)
     */
    public void removeTabFromView(TabDefinition tab) {
        TabPanel tabPanel = tabPanels.remove(tab);
        if (tabPanel != null) {
            int tabIndex = findTabIndex(tab);
            if (tabIndex >= 0) {
                tabPane.removeTabAt(tabIndex);
                logger.debug("Removed tab from view: {}", tab.getTabId());
            }
        }
    }
    
    /**
     * Select tab in view (PRESERVED)
     */
    public void selectTab(TabDefinition tab) {
        int tabIndex = findTabIndex(tab);
        if (tabIndex >= 0) {
            tabPane.setSelectedIndex(tabIndex);
        }
    }
    
    /**
     * Find tab index (PRESERVED)
     */
    private int findTabIndex(TabDefinition tab) {
        TabPanel tabPanel = tabPanels.get(tab);
        if (tabPanel != null) {
            for (int i = 0; i < tabPane.getTabCount() - 1; i++) { // -1 to exclude + button
                Component component = tabPane.getComponentAt(i);
                if (component instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) component;
                    if (scrollPane.getViewport().getView() == tabPanel) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * Clear all tabs (PRESERVED)
     */
    private void clearTabs() {
        // ✅ Clean up tab panel references before removal
        for (Map.Entry<TabDefinition, TabPanel> entry : tabPanels.entrySet()) {
            TabPanel tabPanel = entry.getValue();
            if (tabPanel != null) {
                tabPanel.setDragDropManager(null);
                tabPanel.setWindowDefinition(null);
                tabPanel.clearSelection();
            }
        }
        
        // Remove all tabs except the "+" button
        while (tabPane.getTabCount() > 1) {
            tabPane.removeTabAt(0);
        }
        tabPanels.clear();
        currentTabPanel = null;
        
        // ✅ Clear tab properties (respects loadingProperties flag)
        clearTabProperties();
    }
    
    /**
     * Clear window display (PRESERVED)
     */
    private void clearWindow() {
        // ✅ Note: loadingProperties should already be true when this is called
        // from displayWindow(), but ensure it for direct calls
        boolean wasLoading = loadingProperties;
        loadingProperties = true;
        
        try {
            windowIdField.setText("");
            windowNameField.setText("");
            windowDescField.setText("");
        } finally {
            // Only restore loading state if it wasn't already true
            if (!wasLoading) {
                loadingProperties = false;
            }
        }
        clearTabs();
    }
    
    /**
     * NEW: Clear tab properties
     */
    private void clearTabProperties() {
        // ✅ FIX: Respect existing loadingProperties state (like clearWindow does)
        boolean wasLoading = loadingProperties;
        loadingProperties = true;
        
        try {
            tabIdField.setText("");
            tabNameField.setText("");
            tabSequenceSpinner.setValue(10);
            tabDescField.setText("");
        } finally {
            // ✅ Only restore to false if it wasn't already true
            if (!wasLoading) {
                loadingProperties = false;
            }
        }
    }
    
    /**
     * Handle tab selection (ENHANCED to load tab properties)
     */
    private void handleTabSelection() {
        int selectedIndex = tabPane.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < tabPane.getTabCount() - 1) {
            Component component = tabPane.getComponentAt(selectedIndex);
            if (component instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) component;
                TabPanel tabPanel = (TabPanel) scrollPane.getViewport().getView();
                
                // ✅ Only update if this is a different tab panel
                if (currentTabPanel != tabPanel) {
                    currentTabPanel = tabPanel;
                    
                    // ✅ Ensure tab panel has correct references after file switch
                    if (dragDropManager != null && currentWindow != null) {
                        tabPanel.setDragDropManager(dragDropManager);
                        tabPanel.setWindowDefinition(currentWindow);
                    }
                    
                    loadTabProperties(tabPanel.getTabDefinition());
                    notifyTabSelected(tabPanel.getTabDefinition());
                }
            }
        } else {
            clearTabProperties();
            currentTabPanel = null;
        }
    }
    
    /**
     * NEW: Load tab properties into the form
     */
    private void loadTabProperties(TabDefinition tab) {
        if (tab != null) {
            boolean wasLoading = loadingProperties;  // ✅ Remember existing state
            loadingProperties = true;
            try {
                tabIdField.setText(tab.getTabId() != null ? tab.getTabId() : "");
                tabNameField.setText(tab.getName() != null ? tab.getName() : "");
                tabSequenceSpinner.setValue(tab.getSequence());
                tabDescField.setText(tab.getDescription() != null ? tab.getDescription() : "");
                logger.debug("Loaded tab properties for: {}", tab.getTabId());
            } finally {
                // ✅ Only restore to false if it wasn't already true
                if (!wasLoading) {
                    loadingProperties = false;
                }
            }
        } else {
            clearTabProperties();
        }
    }
    
    /**
     * Handle field selection events (PRESERVED)
     */
    private void handleFieldSelection(FieldSelectionEvent event) {
        // Update visual selection in current tab
        if (currentTabPanel != null) {
            if (event.getType() == FieldSelectionEvent.Type.SELECTED || 
                event.getType() == FieldSelectionEvent.Type.CHANGED) {
                currentTabPanel.selectField(event.getSelectedField());
            } else if (event.getType() == FieldSelectionEvent.Type.DESELECTED) {
                currentTabPanel.clearSelection();
            }
        }
    }
    
    /**
     * Handle form change events (PRESERVED)
     */
    private void handleFormChange(FormChangeEvent event) {
        switch (event.getType()) {
            case FIELD_ADDED:
                if (currentTabPanel != null && event.getField() != null) {
                    currentTabPanel.refreshFields();
                }
                break;
            case FIELD_REMOVED:
                if (currentTabPanel != null && event.getField() != null) {
                    currentTabPanel.refreshFields();
                }
                break;
            case FIELD_MOVED:
                if (currentTabPanel != null) {
                    currentTabPanel.refreshFields();
                }
                break;
            case TAB_ADDED:
                if (event.getTab() != null) {
                    addTabToView(event.getTab());
                    logger.debug("Tab added to view via event: {}", event.getTab().getTabId());
                }
                break;
            case TAB_REMOVED:
                if (event.getTab() != null) {
                    removeTabFromView(event.getTab());
                    logger.debug("Tab removed from view via event: {}", event.getTab().getTabId());
                }
                break;
            case TAB_SELECTED:
                if (event.getTab() != null) {
                    selectTab(event.getTab());
                    logger.debug("Tab selected via event: {}", event.getTab().getTabId());
                }
                break;
        }
    }
    
    // Notification methods (PRESERVED + NEW)
    private void notifyAddTabRequested() {
        notifyViewListeners(ViewEvent.Type.ADD_TAB_REQUESTED, null);
    }
    
    private void notifyTabSelected(TabDefinition tab) {
        notifyViewListeners(ViewEvent.Type.TAB_SELECTED, tab);
    }
    
    private void notifyWindowPropertyChanged() {
        // ✅ ENHANCED LOGGING - Track every property change
        String currentFieldValue = windowNameField.getText();
        String currentWindowName = (currentWindow != null) ? currentWindow.getName() : "null";
        
        logger.warn("🔍 WINDOW PROPERTY CHANGE DEBUG:");
        logger.warn("  📝 Field Value: '{}'", currentFieldValue);
        logger.warn("  🪟 Current Window Name: '{}'", currentWindowName);
        logger.warn("  🚫 Loading Properties: {}", loadingProperties);
        logger.warn("  📍 Stack Trace: {}", Thread.currentThread().getStackTrace()[2]);
        
        if (loadingProperties) {
            logger.warn("  ⏸️  SUPPRESSED - loadingProperties=true");
            return;
        }
        
        if (currentWindow != null) {
            String oldName = currentWindow.getName();
            
            // Update the actual window object
            currentWindow.setWindowId(windowIdField.getText());
            currentWindow.setName(windowNameField.getText());
            currentWindow.setDescription(windowDescField.getText());
            
            // Notify view listeners with the ACTUAL window
            notifyViewListeners(ViewEvent.Type.WINDOW_PROPERTY_CHANGED, currentWindow);
            
            logger.warn("  ✅ APPLIED CHANGE: '{}' -> '{}'", oldName, currentWindow.getName());
        } else {
            logger.warn("  ❌ NO CURRENT WINDOW - change ignored");
        }
    }
    
    /**
     * NEW: Notify tab property changes
     */
    private void notifyTabPropertyChanged() {
    	if (loadingProperties) {
            return;
        }
        if (currentTabPanel != null) {
        	TabDefinition currentTab = currentTabPanel.getTabDefinition(); 
            // Create updated tab data
            TabDefinition updatedTab = new TabDefinition();
            updatedTab.setTabId(tabIdField.getText());
            updatedTab.setName(tabNameField.getText());
            updatedTab.setDescription(tabDescField.getText());
            updatedTab.setSequence((Integer) tabSequenceSpinner.getValue());
            
            // Update the tab title in the UI immediately
            int selectedIndex = tabPane.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < tabPane.getTabCount() - 1) {
                String newTabName = updatedTab.getName();
                if (newTabName != null && !newTabName.trim().isEmpty()) {
                    tabPane.setTitleAt(selectedIndex, newTabName);
                }
            }
         // 🔄 Update custom tab component label if name changed
            if (selectedIndex >= 0 && selectedIndex < tabPane.getTabCount() - 1) {
                Component tabComponent = tabPane.getTabComponentAt(selectedIndex);
                if (tabComponent instanceof JPanel) {
                    JPanel panel = (JPanel) tabComponent;
                    Component[] components = panel.getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JLabel) {
                            ((JLabel) comp).setText(updatedTab.getName());
                            break;
                        }
                    }
                }
            }
            notifyViewListeners(ViewEvent.Type.TAB_PROPERTY_CHANGED, updatedTab);
            logger.debug("Tab property changed: {}", updatedTab.getName());
        }
    }
    
    private void notifyViewListeners(ViewEvent.Type type, Object data) {
        logger.warn("🚨 FIRING VIEW EVENT: {} with data: {}", type, 
                    data != null ? data.getClass().getSimpleName() : "null");
        logger.warn("📊 Registered listeners: {}", viewListeners.size());
        
        ViewEvent event = new ViewEvent(type, data);
        for (int i = 0; i < viewListeners.size(); i++) {
            ViewEventListener listener = viewListeners.get(i);
            try {
                logger.warn("  🎯 Notifying listener #{}: {}", i, listener.getClass().getSimpleName());
                listener.onViewEvent(event);
                logger.warn("  ✅ Listener #{} completed", i);
            } catch (Exception e) {
                logger.error("  ❌ Error in listener #{}: {}", i, e.getMessage(), e);
            }
        }
        logger.warn("🏁 VIEW EVENT COMPLETE: {}", type);
    }
    
    // View listener management (PRESERVED)
    public void addViewEventListener(ViewEventListener listener) {
        if (listener != null && !viewListeners.contains(listener)) {
            viewListeners.add(listener);
        }
    }
    
    public void removeViewEventListener(ViewEventListener listener) {
        viewListeners.remove(listener);
    }
    
    // Getters (PRESERVED)
    public TabPanel getCurrentTabPanel() { return currentTabPanel; }
    public Map<TabDefinition, TabPanel> getTabPanels() { return new HashMap<>(tabPanels); }
    public WindowDefinition getCurrentWindow() { return currentWindow; }
    
    /**
     * View event types (ENHANCED with TAB_PROPERTY_CHANGED)
     */
    public static class ViewEvent {
        public enum Type {
            ADD_TAB_REQUESTED,
            TAB_SELECTED,
            TAB_PROPERTY_CHANGED,  
            TAB_DELETE_REQUESTED, 
            WINDOW_PROPERTY_CHANGED
        }
        
        private final Type type;
        private final Object data;
        
        public ViewEvent(Type type, Object data) {
            this.type = type;
            this.data = data;
        }
        
        public Type getType() { return type; }
        public Object getData() { return data; }
    }
    
    /**
     * View event listener interface (PRESERVED)
     */
    @FunctionalInterface
    public interface ViewEventListener {
        void onViewEvent(ViewEvent event);
    }
}