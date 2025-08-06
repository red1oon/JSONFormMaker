package com.adui.jsoncraft.properties.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.ReferenceData;

/**
 * Property editor for SelectField and related selection components
 * Manages reference data and selection-specific properties
 */
public class SelectFieldEditor extends ComponentDataEditor {
    
    // Reference data components
    private JTextField referenceIdField;
    private JTextField referenceNameField;
    private JCheckBox allowCustomValuesCheckbox;
    
    // Reference values table
    private JTable valuesTable;
    private DefaultTableModel tableModel;
    private JButton addValueButton;
    private JButton editValueButton;
    private JButton deleteValueButton;
    private JButton importButton;
    
    // UI properties
    private JCheckBox searchableCheckbox;
    private JCheckBox showDescriptionsCheckbox;
    private JCheckBox showIconsCheckbox;
    private JTextField placeholderField;
    
    public SelectFieldEditor(FieldDefinition field) {
        super(field);
    }
    
    @Override
    protected void initializeComponents() {
        // Reference data fields
        referenceIdField = new JTextField();
        referenceNameField = new JTextField();
        allowCustomValuesCheckbox = new JCheckBox("Allow Custom Values");
        
        // Reference values table
        String[] columns = {"Key", "Display", "Description", "Color", "Icon", "Sort"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Integer.class; // Sort column
                return String.class;
            }
        };
        valuesTable = new JTable(tableModel);
        valuesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        valuesTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Key
        valuesTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Display
        valuesTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Description
        valuesTable.getColumnModel().getColumn(3).setPreferredWidth(60);  // Color
        valuesTable.getColumnModel().getColumn(4).setPreferredWidth(40);  // Icon
        valuesTable.getColumnModel().getColumn(5).setPreferredWidth(50);  // Sort
        
        // Table buttons
        addValueButton = new JButton("Add");
        editValueButton = new JButton("Edit");
        deleteValueButton = new JButton("Delete");
        importButton = new JButton("Import CSV");
        
        // UI properties
        searchableCheckbox = new JCheckBox("Searchable");
        showDescriptionsCheckbox = new JCheckBox("Show Descriptions");
        showIconsCheckbox = new JCheckBox("Show Icons");
        placeholderField = new JTextField();
        
        // Event handlers
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        addValueButton.addActionListener(e -> addReferenceValue());
        editValueButton.addActionListener(e -> editReferenceValue());
        deleteValueButton.addActionListener(e -> deleteReferenceValue());
        importButton.addActionListener(e -> importFromCSV());
        
        // Update field when properties change
        referenceIdField.addActionListener(e -> notifyPropertyChanged());
        referenceNameField.addActionListener(e -> notifyPropertyChanged());
        allowCustomValuesCheckbox.addActionListener(e -> notifyPropertyChanged());
        searchableCheckbox.addActionListener(e -> notifyPropertyChanged());
        showDescriptionsCheckbox.addActionListener(e -> notifyPropertyChanged());
        showIconsCheckbox.addActionListener(e -> notifyPropertyChanged());
        placeholderField.addActionListener(e -> notifyPropertyChanged());
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Selection Properties"));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Reference Data tab
        tabbedPane.addTab("Reference Data", createReferenceDataPanel());
        
        // Values tab
        tabbedPane.addTab("Values", createValuesPanel());
        
        // UI Properties tab
        tabbedPane.addTab("UI Options", createUIPropertiesPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createReferenceDataPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Reference ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Reference ID:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(referenceIdField, gbc);
        
        // Reference Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Reference Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(referenceNameField, gbc);
        
        // Allow custom values
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(allowCustomValuesCheckbox, gbc);
        
        return panel;
    }
    
    private JPanel createValuesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table
        JScrollPane tableScroll = new JScrollPane(valuesTable);
        tableScroll.setPreferredSize(new Dimension(500, 200));
        panel.add(tableScroll, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addValueButton);
        buttonPanel.add(editValueButton);
        buttonPanel.add(deleteValueButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(importButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createUIPropertiesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Placeholder
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Placeholder:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(placeholderField, gbc);
        
        // Checkboxes
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(searchableCheckbox, gbc);
        
        gbc.gridy = 2;
        panel.add(showDescriptionsCheckbox, gbc);
        
        gbc.gridy = 3;
        panel.add(showIconsCheckbox, gbc);
        
        return panel;
    }
    
    @Override
    protected void loadFieldData() {
        if (field == null) return;
        
        // Load reference data
        ReferenceData reference = field.getReference();
        if (reference != null) {
            referenceIdField.setText(reference.getId() != null ? reference.getId() : "");
            referenceNameField.setText(reference.getName() != null ? reference.getName() : "");
            allowCustomValuesCheckbox.setSelected(reference.isAllowCustomValues());
            
            // Load reference values into table
            tableModel.setRowCount(0);
            if (reference.getValues() != null) {
                for (ReferenceData.ReferenceValue value : reference.getValues()) {
                    Object[] row = {
                        value.getKey(),
                        value.getDisplay(),
                        value.getDescription(),
                        value.getColor(),
                        value.getIcon(),
                        value.getSortOrder()
                    };
                    tableModel.addRow(row);
                }
            }
        }
        
        // Load UI properties
        searchableCheckbox.setSelected(Boolean.parseBoolean(field.getUiProperty("searchable")));
        showDescriptionsCheckbox.setSelected(Boolean.parseBoolean(field.getUiProperty("showDescriptions")));
        showIconsCheckbox.setSelected(Boolean.parseBoolean(field.getUiProperty("showIcons")));
        placeholderField.setText(field.getUiProperty("placeholder") != null ? field.getUiProperty("placeholder") : "");
    }
    
    @Override
    protected void saveFieldData() {
        if (field == null) return;
        
        // Save reference data
        ReferenceData reference = field.getReference();
        if (reference == null) {
            reference = new ReferenceData();
            field.setReference(reference);
        }
        
        reference.setId(referenceIdField.getText().trim());
        reference.setName(referenceNameField.getText().trim());
        reference.setAllowCustomValues(allowCustomValuesCheckbox.isSelected());
        
        // Save reference values from table
        List<ReferenceData.ReferenceValue> values = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            ReferenceData.ReferenceValue value = new ReferenceData.ReferenceValue();
            value.setKey((String) tableModel.getValueAt(i, 0));
            value.setDisplay((String) tableModel.getValueAt(i, 1));
            value.setDescription((String) tableModel.getValueAt(i, 2));
            value.setColor((String) tableModel.getValueAt(i, 3));
            value.setIcon((String) tableModel.getValueAt(i, 4));
            
            Object sortValue = tableModel.getValueAt(i, 5);
            if (sortValue instanceof Integer) {
                value.setSortOrder((Integer) sortValue);
            } else if (sortValue instanceof String) {
                try {
                    value.setSortOrder(Integer.parseInt((String) sortValue));
                } catch (NumberFormatException e) {
                    value.setSortOrder(10);
                }
            }
            
            values.add(value);
        }
        reference.setValues(values);
        
        // Save UI properties
        field.setUiProperty("searchable", searchableCheckbox.isSelected());
        field.setUiProperty("showDescriptions", showDescriptionsCheckbox.isSelected());
        field.setUiProperty("showIcons", showIconsCheckbox.isSelected());
        field.setUiProperty("placeholder", placeholderField.getText().trim());
    }
    
    @Override
    public boolean validateInput() {
        // Validate reference ID
        String refId = referenceIdField.getText().trim();
        if (refId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Reference ID is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate that we have at least one value
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "At least one reference value is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void addReferenceValue() {
        ReferenceValueDialog dialog = new ReferenceValueDialog(SwingUtilities.getWindowAncestor(this), "Add Reference Value", true);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            ReferenceData.ReferenceValue value = dialog.getValue();
            Object[] row = {
                value.getKey(),
                value.getDisplay(),
                value.getDescription(),
                value.getColor(),
                value.getIcon(),
                value.getSortOrder()
            };
            tableModel.addRow(row);
            notifyPropertyChanged();
        }
    }
    
    private void editReferenceValue() {
        int selectedRow = valuesTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Get current values
            ReferenceData.ReferenceValue value = new ReferenceData.ReferenceValue();
            value.setKey((String) tableModel.getValueAt(selectedRow, 0));
            value.setDisplay((String) tableModel.getValueAt(selectedRow, 1));
            value.setDescription((String) tableModel.getValueAt(selectedRow, 2));
            value.setColor((String) tableModel.getValueAt(selectedRow, 3));
            value.setIcon((String) tableModel.getValueAt(selectedRow, 4));
            value.setSortOrder((Integer) tableModel.getValueAt(selectedRow, 5));
            
            ReferenceValueDialog dialog = new ReferenceValueDialog(SwingUtilities.getWindowAncestor(this),"Edit Reference Value", true);
            dialog.setValue(value);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                value = dialog.getValue();
                tableModel.setValueAt(value.getKey(), selectedRow, 0);
                tableModel.setValueAt(value.getDisplay(), selectedRow, 1);
                tableModel.setValueAt(value.getDescription(), selectedRow, 2);
                tableModel.setValueAt(value.getColor(), selectedRow, 3);
                tableModel.setValueAt(value.getIcon(), selectedRow, 4);
                tableModel.setValueAt(value.getSortOrder(), selectedRow, 5);
                notifyPropertyChanged();
            }
        }
    }
    
    private void deleteReferenceValue() {
        int selectedRow = valuesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int result = JOptionPane.showConfirmDialog(this,
                "Delete this reference value?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                tableModel.removeRow(selectedRow);
                notifyPropertyChanged();
            }
        }
    }
    
    /**
     * Import reference values from CSV file
     * Expected CSV format: Key,Display,Description,Color,Icon,Sort
     * 
     * @version 1.0
     * @namespace com.adui.jsoncraft.properties.editors.SelectFieldEditor
     */
    private void importFromCSV() {
        // Create file chooser for CSV files
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "CSV Files (*.csv)", "csv"));
        fileChooser.setDialogTitle("Import Reference Values from CSV");
        
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File selectedFile = fileChooser.getSelectedFile();
        
        try {
            // Read and parse CSV file
            List<String[]> csvData = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                boolean isFirstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    // Skip empty lines
                    if (line.trim().isEmpty()) continue;
                    
                    // Parse CSV line (simple implementation)
                    String[] values = parseCSVLine(line);
                    
                    // Skip header if it looks like one
                    if (isFirstLine && isHeaderRow(values)) {
                        isFirstLine = false;
                        continue;
                    }
                    
                    if (values.length >= 2) { // At least Key and Display required
                        csvData.add(values);
                    }
                    isFirstLine = false;
                }
            }
            
            if (csvData.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No valid data found in CSV file.\nExpected format: Key,Display,Description,Color,Icon,Sort", 
                    "Import Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Confirm import
            int confirmation = JOptionPane.showConfirmDialog(this,
                String.format("Import %d reference values from CSV?\nThis will replace existing values.", csvData.size()),
                "Confirm Import", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }
            
            // Clear existing table data
            tableModel.setRowCount(0);
            
            // Import CSV data into table
            int rowCount = 0;
            for (String[] values : csvData) {
                try {
                    // Extract values with defaults
                    String key = values.length > 0 ? values[0].trim() : "";
                    String display = values.length > 1 ? values[1].trim() : "";
                    String description = values.length > 2 ? values[2].trim() : "";
                    String color = values.length > 3 ? values[3].trim() : "";
                    String icon = values.length > 4 ? values[4].trim() : "";
                    
                    // Parse sort order with default
                    Integer sortOrder = 10;
                    if (values.length > 5 && !values[5].trim().isEmpty()) {
                        try {
                            sortOrder = Integer.parseInt(values[5].trim());
                        } catch (NumberFormatException e) {
                            // Keep default value
                        }
                    }
                    
                    // Validate required fields
                    if (key.isEmpty()) {
                        key = "ITEM_" + (rowCount + 1); // Auto-generate key
                    }
                    if (display.isEmpty()) {
                        display = key; // Use key as display if empty
                    }
                    
                    // Add row to table
                    Object[] row = {key, display, description, color, icon, sortOrder};
                    tableModel.addRow(row);
                    rowCount++;
                    
                } catch (Exception e) {
                    // Log error but continue with other rows
                    System.err.println("Error processing CSV row " + (rowCount + 1) + ": " + e.getMessage());
                }
            }
            
            // Notify of changes to trigger save
            notifyPropertyChanged();
            
            // Show success message
            JOptionPane.showMessageDialog(this,
                String.format("Successfully imported %d reference values from CSV.", rowCount),
                "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error reading CSV file: " + e.getMessage(),
                "Import Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Unexpected error during import: " + e.getMessage(),
                "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Simple CSV line parser (handles basic quoting)
     */
    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        
        values.add(current.toString()); // Add last value
        return values.toArray(new String[0]);
    }

    /**
     * Check if the first row looks like a header
     */
    private boolean isHeaderRow(String[] values) {
        if (values.length == 0) return false;
        
        String firstValue = values[0].toLowerCase().trim();
        return firstValue.equals("key") || firstValue.equals("id") || 
               firstValue.equals("value") || firstValue.contains("key");
    }
    
    /**
     * Dialog for editing reference values
     */
    private static class ReferenceValueDialog extends JDialog {
        private ReferenceData.ReferenceValue value;
        private boolean confirmed = false;
        
        private JTextField keyField;
        private JTextField displayField;
        private JTextField descriptionField;
        private JTextField colorField;
        private JTextField iconField;
        private JSpinner sortSpinner;
        
        public ReferenceValueDialog(Window parent, String title, boolean modal) {
            super(parent, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
            initializeComponents();
            setupLayout();
            setupEventHandlers();
            pack();
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            keyField = new JTextField(15);
            displayField = new JTextField(20);
            descriptionField = new JTextField(30);
            colorField = new JTextField(10);
            iconField = new JTextField(5);
            sortSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 999, 10));
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Key
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(new JLabel("Key:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(keyField, gbc);
            
            // Display
            gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Display:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(displayField, gbc);
            
            // Description
            gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(descriptionField, gbc);
            
            // Color
            gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Color:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(colorField, gbc);
            
            // Icon
            gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Icon:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(iconField, gbc);
            
            // Sort Order
            gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Sort Order:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(sortSpinner, gbc);
            
            add(formPanel, BorderLayout.CENTER);
            
            // Buttons
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
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupEventHandlers() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
        
        private boolean validateAndSave() {
            String key = keyField.getText().trim();
            String display = displayField.getText().trim();
            
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Key is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (display.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Display text is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            value = new ReferenceData.ReferenceValue();
            value.setKey(key);
            value.setDisplay(display);
            value.setDescription(descriptionField.getText().trim());
            value.setColor(colorField.getText().trim());
            value.setIcon(iconField.getText().trim());
            value.setSortOrder((Integer) sortSpinner.getValue());
            
            return true;
        }
        
        public void setValue(ReferenceData.ReferenceValue value) {
            if (value != null) {
                keyField.setText(value.getKey() != null ? value.getKey() : "");
                displayField.setText(value.getDisplay() != null ? value.getDisplay() : "");
                descriptionField.setText(value.getDescription() != null ? value.getDescription() : "");
                colorField.setText(value.getColor() != null ? value.getColor() : "");
                iconField.setText(value.getIcon() != null ? value.getIcon() : "");
                sortSpinner.setValue(value.getSortOrder());
            }
        }
        
        public ReferenceData.ReferenceValue getValue() {
            return value;
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
    }
}
