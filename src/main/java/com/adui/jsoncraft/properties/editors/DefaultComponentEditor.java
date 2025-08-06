package com.adui.jsoncraft.properties.editors;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Default component editor for components without specific editors
 * Shows basic component information and common properties
 */
public class DefaultComponentEditor extends ComponentDataEditor {
    private JLabel componentTypeLabel;
    private JLabel descriptionLabel;
    private JTextArea notesArea;
    
    public DefaultComponentEditor(FieldDefinition field) {
        super(field);
    }
    
    @Override
    protected void initializeComponents() {
        componentTypeLabel = new JLabel();
        componentTypeLabel.setFont(componentTypeLabel.getFont().deriveFont(Font.BOLD, 14f));
        
        descriptionLabel = new JLabel();
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.ITALIC));
        
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createLoweredBevelBorder());
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Component Information"));
        
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Component type
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        infoPanel.add(new JLabel("Component Type:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        infoPanel.add(componentTypeLabel, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        infoPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        infoPanel.add(descriptionLabel, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        infoPanel.add(new JLabel("Notes:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        infoPanel.add(new JScrollPane(notesArea), gbc);
        
        add(infoPanel, BorderLayout.CENTER);
    }
    
    @Override
    protected void loadFieldData() {
        if (field != null && field.getComponentType() != null) {
            componentTypeLabel.setText(field.getComponentType().getJsonName());
            descriptionLabel.setText(field.getComponentType().getDescription());
            
            // Load notes from field data
            Object notes = field.getDataProperty("componentNotes");
            notesArea.setText(notes != null ? notes.toString() : "");
        }
    }
    
    @Override
    protected void saveFieldData() {
        if (field != null) {
            String notes = notesArea.getText().trim();
            if (!notes.isEmpty()) {
                field.setDataProperty("componentNotes", notes);
            }
        }
    }
    
    @Override
    public boolean validateInput() {
        return true; // Default editor always validates
    }
}
