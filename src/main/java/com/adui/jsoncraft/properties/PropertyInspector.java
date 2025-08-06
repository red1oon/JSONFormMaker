package com.adui.jsoncraft.properties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.WindowDefinition;
import com.adui.jsoncraft.properties.editors.ComponentDataEditor;

/**
 * Property Inspector for JSONFormMaker
 * Hierarchical property editing panel
 * 
 * Version: 1.1.0 - FIXED: Removed duplicate title and validation panel
 * Namespace: com.adui.jsoncraft.properties.PropertyInspector
 */
public class PropertyInspector extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(PropertyInspector.class);
    
    private FieldDefinition currentField;
    private WindowDefinition currentWindow;
    private List<PropertyChangeListener> listeners;
    private boolean loadingProperties = false;  // Flag to prevent event loops during programmatic updates
    
    // Property panels
    private JPanel generalPanel;
    private JPanel uiPanel;
    private JPanel componentDataPanel;  
    private ComponentDataEditor currentComponentEditor;
    
    // General property fields
    private JTextField fieldIdField;
    private JTextField fieldNameField;
    private JLabel componentTypeLabel;
    private JSpinner sequenceSpinner;
    private JCheckBox requiredCheckBox;
    private JCheckBox readOnlyCheckBox;
    private JTextArea helpTextArea;
    private JTextArea descriptionArea;
    
    // UI property fields
    private JTextField placeholderField;
    private JCheckBox showIconsCheckBox;
    private JTextField helpTextUiField;
    
    public PropertyInspector() {
        this.listeners = new ArrayList<>();
        initializeComponents();
        setupLayout();
        clearProperties();
        
        logger.debug("Property inspector initialized");
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        // FIXED: Removed duplicate title border
        // setBorder(BorderFactory.createTitledBorder("Properties"));
        
        createGeneralPanel();
        createUiPanel();
        createComponentDataPanel();
    }
    
    private void createGeneralPanel() {
        generalPanel = new JPanel(new GridBagLayout());
        generalPanel.setBorder(new TitledBorder("General Properties"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        
        // Field ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        generalPanel.add(new JLabel("Field ID:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fieldIdField = new JTextField();
        fieldIdField.addActionListener(e -> updateCurrentField());
        generalPanel.add(fieldIdField, gbc);
        
        // Field Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        generalPanel.add(new JLabel("Field Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fieldNameField = new JTextField();
        fieldNameField.addActionListener(e -> updateCurrentField());
        generalPanel.add(fieldNameField, gbc);
        
        // Component Type (read-only)
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        generalPanel.add(new JLabel("Component Type:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        componentTypeLabel = new JLabel();
        componentTypeLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        generalPanel.add(componentTypeLabel, gbc);
        
        // Sequence
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        generalPanel.add(new JLabel("Sequence:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        sequenceSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 9999, 10));
        sequenceSpinner.addChangeListener(e -> updateCurrentField());
        generalPanel.add(sequenceSpinner, gbc);
        
        // Required and Read-only checkboxes side by side
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        requiredCheckBox = new JCheckBox("Required Field");
        requiredCheckBox.addActionListener(e -> updateCurrentField());
        generalPanel.add(requiredCheckBox, gbc);
        
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        readOnlyCheckBox = new JCheckBox("Read Only");
        readOnlyCheckBox.addActionListener(e -> updateCurrentField());
        generalPanel.add(readOnlyCheckBox, gbc);
        
        // Help Text (reduced from 3 to 2 rows)
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        generalPanel.add(new JLabel("Help Text:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.2;
        helpTextArea = new JTextArea(2, 20);
        helpTextArea.setLineWrap(true);
        helpTextArea.setWrapStyleWord(true);
        JScrollPane helpScroll = new JScrollPane(helpTextArea);

        // Add change listeners for help text area
        helpTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCurrentField(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCurrentField(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCurrentField(); }
        });
        generalPanel.add(helpScroll, gbc);
        
        // Description (reduced from 3 to 1 row)
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        generalPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.1;
        descriptionArea = new JTextArea(1, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);

        // Add change listeners for description area
        descriptionArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCurrentField(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCurrentField(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCurrentField(); }
        });
        generalPanel.add(descScroll, gbc);
    }
    
    private void createUiPanel() {
        uiPanel = new JPanel(new GridBagLayout());
        uiPanel.setBorder(new TitledBorder("UI Configuration"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        
        // Placeholder
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        uiPanel.add(new JLabel("Placeholder:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        placeholderField = new JTextField();
        placeholderField.addActionListener(e -> updateCurrentField());
        uiPanel.add(placeholderField, gbc);
        
        // Show Icons
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        showIconsCheckBox = new JCheckBox("Show Icons");
        showIconsCheckBox.addActionListener(e -> updateCurrentField());
        uiPanel.add(showIconsCheckBox, gbc);
        
        // Help Text (UI)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        uiPanel.add(new JLabel("UI Help Text:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        helpTextUiField = new JTextField();
        helpTextUiField.addActionListener(e -> updateCurrentField());
        uiPanel.add(helpTextUiField, gbc);
    }
    
    private void createComponentDataPanel() {
        componentDataPanel = new JPanel(new BorderLayout());
        componentDataPanel.setBorder(new TitledBorder("Component Data"));
        componentDataPanel.setPreferredSize(new Dimension(0, 150));
    }
    
    private void setupLayout() {
        // Create scrollable panel for all property panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        mainPanel.add(generalPanel);
        mainPanel.add(Box.createVerticalStrut(3));
        // FIXED: Removed validation panel completely
        // mainPanel.add(validationPanel);
        // mainPanel.add(Box.createVerticalStrut(3));
        mainPanel.add(uiPanel);
        mainPanel.add(Box.createVerticalStrut(3));
        mainPanel.add(componentDataPanel);
        mainPanel.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> updateCurrentField());
        buttonPanel.add(applyButton);
        
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> loadFieldProperties());
        buttonPanel.add(resetButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void setSelectedField(FieldDefinition field) {
        this.currentField = field;
        loadFieldProperties();
        updateComponentDataPanel();
        logger.debug("Selected field: {}", field != null ? field.getFieldId() : "null");
    }
    
    private void updateComponentDataPanel() {
        // Remove existing component editor
        if (currentComponentEditor != null) {
            componentDataPanel.remove(currentComponentEditor);
            currentComponentEditor = null;
        }
        
        // Clear the panel
        componentDataPanel.removeAll();
        
        if (currentField != null && currentField.getComponentType() != null) {
            try {
                // Create component-specific editor using factory
                currentComponentEditor = PropertyEditorFactory.createEditor(
                    currentField.getComponentType(), currentField);
                
                if (currentComponentEditor != null) {
                    // Add property change listener
                	currentComponentEditor.addPropertyChangeListener(
                		    (ComponentDataEditor.PropertyChangeListener) field -> notifyPropertyChanged());
                    
                    // Add to panel
                    componentDataPanel.add(currentComponentEditor, BorderLayout.CENTER);
                    
                    logger.debug("Created component editor for: {}", currentField.getComponentType());
                } else {
                    addPlaceholderLabel("No specific editor for " + currentField.getComponentType().getJsonName());
                }
            } catch (Exception e) {
                logger.warn("Failed to create component editor", e);
                addPlaceholderLabel("Error loading component editor");
            }
        } else {
            addPlaceholderLabel("Select a field to edit component properties");
        }
        
        // Refresh the panel
        componentDataPanel.revalidate();
        componentDataPanel.repaint();
    }

    private void addPlaceholderLabel(String text) {
        JLabel placeholder = new JLabel(text, SwingConstants.CENTER);
        placeholder.setForeground(Color.GRAY);
        componentDataPanel.add(placeholder, BorderLayout.CENTER);
    }
    
    public void setCurrentWindow(WindowDefinition window) {
        this.currentWindow = window;
    }
    
    private void loadFieldProperties() {
        boolean wasLoading = loadingProperties;
        loadingProperties = true;

        try {
            if (currentField == null) {
                clearProperties();
                return;
            }
            
            // Load general properties
            fieldIdField.setText(currentField.getFieldId());
            fieldNameField.setText(currentField.getName());
            componentTypeLabel.setText(currentField.getComponentType().getJsonName());
            sequenceSpinner.setValue(currentField.getSequence());
            requiredCheckBox.setSelected(currentField.isRequired());
            readOnlyCheckBox.setSelected(currentField.isReadOnly());
            helpTextArea.setText(currentField.getHelp() != null ? currentField.getHelp() : "");
            descriptionArea.setText(currentField.getDescription() != null ? currentField.getDescription() : "");
            
            // Load UI properties
            placeholderField.setText(currentField.getUiProperty("placeholder"));
            showIconsCheckBox.setSelected(currentField.getBooleanUiProperty("showIcons"));
            helpTextUiField.setText(currentField.getUiProperty("helpText"));
            
            // Enable all fields
            setFieldsEnabled(true);
        } finally {
            if (!wasLoading) {
                loadingProperties = false;
            }
        }
    }
    
    private void clearProperties() {
        boolean wasLoading = loadingProperties;
        loadingProperties = true;

        try {
            // Clear all fields
            fieldIdField.setText("");
            fieldNameField.setText("");
            componentTypeLabel.setText("");
            sequenceSpinner.setValue(10);
            requiredCheckBox.setSelected(false);
            readOnlyCheckBox.setSelected(false);
            helpTextArea.setText("");
            descriptionArea.setText("");
            
            placeholderField.setText("");
            showIconsCheckBox.setSelected(false);
            helpTextUiField.setText("");
            
            // Disable all fields
            setFieldsEnabled(false);
        } finally {
            if (!wasLoading) {
                loadingProperties = false;
            }
        }
    }
    
    private void setFieldsEnabled(boolean enabled) {
        fieldIdField.setEnabled(enabled);
        fieldNameField.setEnabled(enabled);
        sequenceSpinner.setEnabled(enabled);
        requiredCheckBox.setEnabled(enabled);
        readOnlyCheckBox.setEnabled(enabled);
        helpTextArea.setEnabled(enabled);
        descriptionArea.setEnabled(enabled);
        
        placeholderField.setEnabled(enabled);
        showIconsCheckBox.setEnabled(enabled);
        helpTextUiField.setEnabled(enabled);
    }
    
    private void updateCurrentField() {
        if (loadingProperties) return;  // Skip updates during programmatic loading

        if (currentField == null) return;
        
        try {
            // Update general properties
            currentField.setFieldId(fieldIdField.getText().trim());
            currentField.setName(fieldNameField.getText().trim());
            currentField.setSequence((Integer) sequenceSpinner.getValue());
            currentField.setRequired(requiredCheckBox.isSelected());
            currentField.setReadOnly(readOnlyCheckBox.isSelected());
            // Set help text (convert empty to null)
            String helpText = helpTextArea.getText().trim();
            currentField.setHelp(helpText.isEmpty() ? null : helpText);

            // Set description (convert empty to null)
            String descriptionText = descriptionArea.getText().trim();
            currentField.setDescription(descriptionText.isEmpty() ? null : descriptionText);
            
            // Update UI properties
            currentField.setUiProperty("placeholder", placeholderField.getText().trim());
            currentField.setUiProperty("showIcons", showIconsCheckBox.isSelected());
            currentField.setUiProperty("helpText", helpTextUiField.getText().trim());
            
            // Notify listeners
            notifyPropertyChanged();
            
        } catch (Exception e) {
            logger.warn("Error updating field properties", e);
            JOptionPane.showMessageDialog(this, "Error updating properties: " + e.getMessage(), 
                "Property Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyPropertyChanged() {
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChanged(currentField);
        }
    }
    
    /**
     * Interface for property change events
     */
    public interface PropertyChangeListener {
        void propertyChanged(FieldDefinition field);
    }
}
