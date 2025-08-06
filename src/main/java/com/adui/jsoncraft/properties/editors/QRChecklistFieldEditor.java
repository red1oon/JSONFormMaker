package com.adui.jsoncraft.properties.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Property editor for QRChecklistField and FlippableQRChecklistField
 * Manages checklist items and QR scanning properties
 */
public class QRChecklistFieldEditor extends ComponentDataEditor {
    
    // Checklist items table
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JButton addItemButton;
    private JButton editItemButton;
    private JButton deleteItemButton;
    private JButton importItemsButton;
    
    // QR settings
    private JCheckBox continuousModeCheckbox;
    private JCheckBox audioBeepCheckbox;
    private JCheckBox vibrationCheckbox;
    private JSpinner timeoutSpinner;
    
    // Progress settings
    private JCheckBox showProgressCheckbox;
    private JCheckBox showVisualBarCheckbox;
    private JCheckBox showCountCheckbox;
    private JComboBox<String> animationCombo;
    
    public QRChecklistFieldEditor(FieldDefinition field) {
        super(field);
    }
    
    @Override
    protected void initializeComponents() {
        // Checklist items table
        String[] columns = {"ID", "QR Code", "Name", "Category", "Description"};
        tableModel = new DefaultTableModel(columns, 0);
        itemsTable = new JTable(tableModel);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Table buttons
        addItemButton = new JButton("Add Item");
        editItemButton = new JButton("Edit Item");
        deleteItemButton = new JButton("Delete Item");
        importItemsButton = new JButton("Import CSV");
        
        // QR settings
        continuousModeCheckbox = new JCheckBox("Continuous Scanning");
        audioBeepCheckbox = new JCheckBox("Audio Beep");
        vibrationCheckbox = new JCheckBox("Vibration Feedback");
        timeoutSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        
        // Progress settings
        showProgressCheckbox = new JCheckBox("Show Progress Percentage");
        showVisualBarCheckbox = new JCheckBox("Show Visual Progress Bar");
        showCountCheckbox = new JCheckBox("Show Item Count");
        animationCombo = new JComboBox<>(new String[]{"None", "Smooth", "Bounce", "Fade"});
        
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        addItemButton.addActionListener(e -> addChecklistItem());
        editItemButton.addActionListener(e -> editChecklistItem());
        deleteItemButton.addActionListener(e -> deleteChecklistItem());
        importItemsButton.addActionListener(e -> importFromCSV());
        
        // Notify on changes
        continuousModeCheckbox.addActionListener(e -> notifyPropertyChanged());
        audioBeepCheckbox.addActionListener(e -> notifyPropertyChanged());
        vibrationCheckbox.addActionListener(e -> notifyPropertyChanged());
        timeoutSpinner.addChangeListener(e -> notifyPropertyChanged());
        showProgressCheckbox.addActionListener(e -> notifyPropertyChanged());
        showVisualBarCheckbox.addActionListener(e -> notifyPropertyChanged());
        showCountCheckbox.addActionListener(e -> notifyPropertyChanged());
        animationCombo.addActionListener(e -> notifyPropertyChanged());
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("QR Checklist Properties"));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Items tab
        tabbedPane.addTab("Checklist Items", createItemsPanel());
        
        // QR Settings tab
        tabbedPane.addTab("QR Settings", createQRSettingsPanel());
        
        // Progress tab
        tabbedPane.addTab("Progress Display", createProgressPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table
        JScrollPane tableScroll = new JScrollPane(itemsTable);
        tableScroll.setPreferredSize(new Dimension(500, 200));
        panel.add(tableScroll, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addItemButton);
        buttonPanel.add(editItemButton);
        buttonPanel.add(deleteItemButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(importItemsButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createQRSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Continuous mode
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        panel.add(continuousModeCheckbox, gbc);
        
        // Audio beep
        gbc.gridy = 1;
        panel.add(audioBeepCheckbox, gbc);
        
        // Vibration
        gbc.gridy = 2;
        panel.add(vibrationCheckbox, gbc);
        
        // Timeout
        gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Scan Timeout (seconds):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(timeoutSpinner, gbc);
        
        return panel;
    }
    
    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Show progress
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        panel.add(showProgressCheckbox, gbc);
        
        // Show visual bar
        gbc.gridy = 1;
        panel.add(showVisualBarCheckbox, gbc);
        
        // Show count
        gbc.gridy = 2;
        panel.add(showCountCheckbox, gbc);
        
        // Animation
        gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Animation Style:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(animationCombo, gbc);
        
        return panel;
    }
    
    @Override
    protected void loadFieldData() {
        if (field == null) return;
        
        // Load checklist items
        Object itemsData = field.getDataProperty("items");
        if (itemsData instanceof List) {
            tableModel.setRowCount(0);
            List<?> items = (List<?>) itemsData;
            for (Object itemObj : items) {
                if (itemObj instanceof Map) {
                    Map<?, ?> item = (Map<?, ?>) itemObj;
                    Object[] row = {
                        item.get("id"),
                        item.get("qrCode"),
                        item.get("name"),
                        item.get("category"),
                        item.get("description")
                    };
                    tableModel.addRow(row);
                }
            }
        }
        
        // Load QR settings
        continuousModeCheckbox.setSelected(getBooleanProperty("continuousMode", true));
        audioBeepCheckbox.setSelected(getBooleanProperty("audioBeep", true));
        vibrationCheckbox.setSelected(getBooleanProperty("vibration", true));
        timeoutSpinner.setValue(getIntProperty("timeout", 5));
        
        // Load progress settings
        showProgressCheckbox.setSelected(getBooleanProperty("showProgress", true));
        showVisualBarCheckbox.setSelected(getBooleanProperty("showVisualBar", true));
        showCountCheckbox.setSelected(getBooleanProperty("showCount", true));
        
        String animation = getStringProperty("animation", "Smooth");
        animationCombo.setSelectedItem(animation);
    }
    
    @Override
    protected void saveFieldData() {
        if (field == null) return;
        
        // Save checklist items
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", tableModel.getValueAt(i, 0));
            item.put("qrCode", tableModel.getValueAt(i, 1));
            item.put("name", tableModel.getValueAt(i, 2));
            item.put("category", tableModel.getValueAt(i, 3));
            item.put("description", tableModel.getValueAt(i, 4));
            item.put("status", "N"); // Default status
            items.add(item);
        }
        field.setDataProperty("items", items);
        
        // Save QR settings
        field.setDataProperty("continuousMode", continuousModeCheckbox.isSelected());
        field.setDataProperty("audioBeep", audioBeepCheckbox.isSelected());
        field.setDataProperty("vibration", vibrationCheckbox.isSelected());
        field.setDataProperty("timeout", timeoutSpinner.getValue());
        
        // Save progress settings
        field.setDataProperty("showProgress", showProgressCheckbox.isSelected());
        field.setDataProperty("showVisualBar", showVisualBarCheckbox.isSelected());
        field.setDataProperty("showCount", showCountCheckbox.isSelected());
        field.setDataProperty("animation", animationCombo.getSelectedItem());
    }
    
    @Override
    public boolean validateInput() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "At least one checklist item is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        Object value = field.getDataProperty(key);
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return defaultValue;
    }
    
    private int getIntProperty(String key, int defaultValue) {
        Object value = field.getDataProperty(key);
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private String getStringProperty(String key, String defaultValue) {
        Object value = field.getDataProperty(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private void addChecklistItem() {
        ChecklistItemDialog dialog = new ChecklistItemDialog(SwingUtilities.getWindowAncestor(this), "Add Checklist Item", true);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Map<String, String> item = dialog.getItem();
            Object[] row = {
                item.get("id"),
                item.get("qrCode"),
                item.get("name"),
                item.get("category"),
                item.get("description")
            };
            tableModel.addRow(row);
            notifyPropertyChanged();
        }
    }
    
    private void editChecklistItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            Map<String, String> item = new HashMap<>();
            item.put("id", (String) tableModel.getValueAt(selectedRow, 0));
            item.put("qrCode", (String) tableModel.getValueAt(selectedRow, 1));
            item.put("name", (String) tableModel.getValueAt(selectedRow, 2));
            item.put("category", (String) tableModel.getValueAt(selectedRow, 3));
            item.put("description", (String) tableModel.getValueAt(selectedRow, 4));
            
            ChecklistItemDialog dialog = new ChecklistItemDialog(SwingUtilities.getWindowAncestor(this), "Edit Checklist Item", true);
            dialog.setItem(item);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                item = dialog.getItem();
                tableModel.setValueAt(item.get("id"), selectedRow, 0);
                tableModel.setValueAt(item.get("qrCode"), selectedRow, 1);
                tableModel.setValueAt(item.get("name"), selectedRow, 2);
                tableModel.setValueAt(item.get("category"), selectedRow, 3);
                tableModel.setValueAt(item.get("description"), selectedRow, 4);
                notifyPropertyChanged();
            }
        }
    }
    
    private void deleteChecklistItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int result = JOptionPane.showConfirmDialog(this,
                "Delete this checklist item?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                tableModel.removeRow(selectedRow);
                notifyPropertyChanged();
            }
        }
    }
    
    private void importFromCSV() {
        JOptionPane.showMessageDialog(this, "CSV Import feature coming soon!", "Import CSV", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Dialog for editing checklist items
     */
    private static class ChecklistItemDialog extends JDialog {
        private Map<String, String> item;
        private boolean confirmed = false;
        
        private JTextField idField;
        private JTextField qrCodeField;
        private JTextField nameField;
        private JTextField categoryField;
        private JTextArea descriptionArea;
        
        public ChecklistItemDialog(Window parent, String title, boolean modal) {
            super(parent, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
            initializeComponents();
            setupLayout();
            pack();
            setLocationRelativeTo(parent);
        }
        
        private void initializeComponents() {
            idField = new JTextField(15);
            qrCodeField = new JTextField(20);
            nameField = new JTextField(30);
            categoryField = new JTextField(15);
            descriptionArea = new JTextArea(3, 30);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
        }
        
        private void setupLayout() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // ID
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
            formPanel.add(new JLabel("ID:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(idField, gbc);
            
            // QR Code
            gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("QR Code:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(qrCodeField, gbc);
            
            // Name
            gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Name:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(nameField, gbc);
            
            // Category
            gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Category:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            formPanel.add(categoryField, gbc);
            
            // Description
            gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
            formPanel.add(new JScrollPane(descriptionArea), gbc);
            
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
        
        private boolean validateAndSave() {
            String id = idField.getText().trim();
            String qrCode = qrCodeField.getText().trim();
            String name = nameField.getText().trim();
            
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (qrCode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "QR Code is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            item = new HashMap<>();
            item.put("id", id);
            item.put("qrCode", qrCode);
            item.put("name", name);
            item.put("category", categoryField.getText().trim());
            item.put("description", descriptionArea.getText().trim());
            
            return true;
        }
        
        public void setItem(Map<String, String> item) {
            if (item != null) {
                idField.setText(item.get("id") != null ? item.get("id") : "");
                qrCodeField.setText(item.get("qrCode") != null ? item.get("qrCode") : "");
                nameField.setText(item.get("name") != null ? item.get("name") : "");
                categoryField.setText(item.get("category") != null ? item.get("category") : "");
                descriptionArea.setText(item.get("description") != null ? item.get("description") : "");
            }
        }
        
        public Map<String, String> getItem() {
            return item;
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
    }
}
