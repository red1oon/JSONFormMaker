package com.adui.jsoncraft.properties.editors.task.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.adui.jsoncraft.properties.editors.task.model.ResourceDefinition;

/**
 * Enhanced Resource Edit Dialog - Following TaskEditDialog design patterns
 * 
 * Version: 1.2.0
 * Namespace: com.adui.jsoncraft.properties.editors.task.dialogs.ResourceEditDialog
 */
public class ResourceEditDialog extends JDialog {
    private static final long serialVersionUID = 1L;
	private ResourceDefinition resource;
    private ResourceDefinition originalResource;
    private boolean confirmed = false;
    
    // Form components
    private JTextField idField;
    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JSpinner rateSpinner;
    private JSlider availabilitySlider;
    private JLabel availabilityLabel;
    private JTextArea skillsArea;
    
    public ResourceEditDialog(Frame parent, ResourceDefinition resource) {
        super(parent, resource == null ? "Add Resource" : "Edit Resource", true);
        this.originalResource = resource;
        this.resource = resource != null ? copyResource(resource) : createNewResource();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadResourceData();
        
        setSize(520, 420);  // Increased size for better spacing
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private ResourceDefinition copyResource(ResourceDefinition original) {
        ResourceDefinition copy = new ResourceDefinition(
            original.getId(),
            original.getName(), 
            original.getType(),
            original.getRate(),
            original.getAvailability()
        );
        copy.setSkills(original.getSkills());
        return copy;
    }
    
    private ResourceDefinition createNewResource() {
        return new ResourceDefinition("", "", "person", 0.0, 100);
    }
    
    private void initializeComponents() {
        // Initialize components - will be created in setupLayout() for better organization
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main form panel with GridBagLayout for precise control (following TaskEditDialog pattern)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 15);  // Generous spacing like TaskEditDialog
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 0: ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel idLabel = new JLabel("Resource ID:");
        idLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        formPanel.add(idLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        idField = new JTextField(25);
        idField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        formPanel.add(idField, gbc);
        
        // Row 1: Name  
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        JLabel nameLabel = new JLabel("Resource Name:");
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField(30);
        nameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        formPanel.add(nameField, gbc);
        
        // Row 2: Type and Rate (side by side like TaskEditDialog)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        JLabel typeLabel = new JLabel("Resource Type:");
        typeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        formPanel.add(typeLabel, gbc);
        
        // Sub-panel for Type and Rate
        JPanel typeRatePanel = new JPanel(new GridBagLayout());
        GridBagConstraints subGbc = new GridBagConstraints();
        subGbc.insets = new Insets(0, 0, 0, 15);
        
        typeCombo = new JComboBox<>(new String[]{"person", "equipment", "material", "budget"});
        typeCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        typeCombo.setPreferredSize(new Dimension(120, 25));
        subGbc.gridx = 0; subGbc.anchor = GridBagConstraints.WEST;
        typeRatePanel.add(typeCombo, subGbc);
        
        JLabel rateLabel = new JLabel("Rate/Hour: $");
        rateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        subGbc.gridx = 1; subGbc.insets = new Insets(0, 20, 0, 5);
        typeRatePanel.add(rateLabel, subGbc);
        
        rateSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 9999.99, 0.25));
        JSpinner.NumberEditor rateEditor = new JSpinner.NumberEditor(rateSpinner, "0.00");
        rateSpinner.setEditor(rateEditor);
        rateSpinner.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        rateSpinner.setPreferredSize(new Dimension(100, 25));
        subGbc.gridx = 2; subGbc.insets = new Insets(0, 0, 0, 0);
        typeRatePanel.add(rateSpinner, subGbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(typeRatePanel, gbc);
        
        // Row 3: Availability slider with improved layout
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        JLabel availLabel = new JLabel("Availability:");
        availLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        formPanel.add(availLabel, gbc);
        
        JPanel availabilityPanel = new JPanel(new BorderLayout(10, 5));
        availabilitySlider = new JSlider(0, 100, 100);
        availabilitySlider.setMajorTickSpacing(25);
        availabilitySlider.setMinorTickSpacing(5);
        availabilitySlider.setPaintTicks(true);
        availabilitySlider.setPaintLabels(true);
        availabilitySlider.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        
        availabilityLabel = new JLabel("100%");
        availabilityLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        availabilityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        availabilityLabel.setPreferredSize(new Dimension(50, 25));
        availabilityLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        availabilityPanel.add(availabilitySlider, BorderLayout.CENTER);
        availabilityPanel.add(availabilityLabel, BorderLayout.EAST);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(availabilityPanel, gbc);
        
        // Row 4: Skills (text area like TaskEditDialog description)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel skillsLabel = new JLabel("Skills & Notes:");
        skillsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        formPanel.add(skillsLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        skillsArea = new JTextArea(4, 30);
        skillsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        skillsArea.setLineWrap(true);
        skillsArea.setWrapStyleWord(true);
        skillsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane skillsScroll = new JScrollPane(skillsArea);
        skillsScroll.setPreferredSize(new Dimension(350, 80));
        skillsScroll.setBorder(BorderFactory.createLoweredBevelBorder());
        formPanel.add(skillsScroll, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel with proper spacing (following TaskEditDialog pattern)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        // Make buttons same size and style
        Dimension buttonSize = new Dimension(90, 32);
        Font buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        
        okButton.setPreferredSize(buttonSize);
        okButton.setFont(buttonFont);
        cancelButton.setPreferredSize(buttonSize);
        cancelButton.setFont(buttonFont);
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Button event handlers
        okButton.addActionListener(e -> {
            if (validateAndSaveResource()) {
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        // Set default button
        getRootPane().setDefaultButton(okButton);
    }
    
    private void setupEventHandlers() {
        // Update availability label as slider moves
        availabilitySlider.addChangeListener(e -> {
            int value = availabilitySlider.getValue();
            availabilityLabel.setText(value + "%");
            
            // Color coding for availability
            if (value >= 80) {
                availabilityLabel.setForeground(new Color(0, 128, 0)); // Green
            } else if (value >= 50) {
                availabilityLabel.setForeground(new Color(255, 140, 0)); // Orange  
            } else {
                availabilityLabel.setForeground(new Color(220, 20, 60)); // Red
            }
        });
        
        // Auto-generate ID from name if ID is empty (like TaskEditDialog)
        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (idField.getText().trim().isEmpty() && !nameField.getText().trim().isEmpty()) {
                    String autoId = generateResourceId(nameField.getText());
                    idField.setText(autoId);
                }
            }
        });
        
        // Update rate spinner format based on resource type
        typeCombo.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();
            if ("budget".equals(selectedType)) {
                JSpinner.NumberEditor budgetEditor = new JSpinner.NumberEditor(rateSpinner, "¤#,##0.00");
                rateSpinner.setEditor(budgetEditor);
            } else {
                JSpinner.NumberEditor hourlyEditor = new JSpinner.NumberEditor(rateSpinner, "0.00");
                rateSpinner.setEditor(hourlyEditor);
            }
        });
    }
    
    private String generateResourceId(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "")
            .toUpperCase();
    }
    
    private void loadResourceData() {
        if (resource != null) {
            idField.setText(resource.getId() != null ? resource.getId() : "");
            nameField.setText(resource.getName() != null ? resource.getName() : "");
            typeCombo.setSelectedItem(resource.getType() != null ? resource.getType() : "person");
            rateSpinner.setValue(resource.getRate());
            availabilitySlider.setValue(resource.getAvailability());
            skillsArea.setText(resource.getSkills() != null ? resource.getSkills() : "");
            
            // Update availability label
            availabilitySlider.getChangeListeners()[0].stateChanged(
                new javax.swing.event.ChangeEvent(availabilitySlider));
        }
    }
    
    private boolean validateAndSaveResource() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        
        // Validation (following TaskEditDialog validation pattern)
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Resource ID is required.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            idField.requestFocus();
            return false;
        }
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Resource name is required.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        // ID format validation
        if (!id.matches("^[A-Z][A-Z0-9_]*$")) {
            JOptionPane.showMessageDialog(this, 
                "Resource ID must start with a letter and contain only uppercase letters, numbers, and underscores.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            idField.requestFocus();
            return false;
        }
        
        // Save the data
        resource.setId(id);
        resource.setName(name);
        resource.setType((String) typeCombo.getSelectedItem());
        resource.setRate(((Number) rateSpinner.getValue()).doubleValue());
        resource.setAvailability(availabilitySlider.getValue());
        resource.setSkills(skillsArea.getText().trim());
        
        return true;
    }
    
    public ResourceDefinition getResource() {
        return resource;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}