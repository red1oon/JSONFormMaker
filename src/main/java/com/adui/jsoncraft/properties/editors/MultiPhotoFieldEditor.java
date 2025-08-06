package com.adui.jsoncraft.properties.editors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Property editor for MultiPhotoField - Multiple photo capture
 */
public class MultiPhotoFieldEditor extends ComponentDataEditor {
    
    private JSpinner maxPhotosSpinner;
    private JSlider qualitySlider;
    private JCheckBox geoTaggingCheckbox;
    
    public MultiPhotoFieldEditor(FieldDefinition field) {
        super(field);
    }
    
    @Override
    protected void initializeComponents() {
        maxPhotosSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        qualitySlider = new JSlider(1, 10, 8);
        qualitySlider.setMajorTickSpacing(1);
        qualitySlider.setPaintTicks(true);
        qualitySlider.setPaintLabels(true);
        
        geoTaggingCheckbox = new JCheckBox("Include GPS Location");
        
        maxPhotosSpinner.addChangeListener(e -> notifyPropertyChanged());
        qualitySlider.addChangeListener(e -> notifyPropertyChanged());
        geoTaggingCheckbox.addActionListener(e -> notifyPropertyChanged());
    }
    
    @Override
    protected void setupLayout() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Multi-Photo Properties"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Max Photos:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(maxPhotosSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(new JLabel("Quality:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(qualitySlider, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(geoTaggingCheckbox, gbc);
    }
    
    @Override
    protected void loadFieldData() {
        if (field != null) {
            Object maxPhotos = field.getDataProperty("maxPhotos");
            if (maxPhotos instanceof Integer) {
                maxPhotosSpinner.setValue(maxPhotos);
            }
            
            Object quality = field.getDataProperty("quality");
            if (quality instanceof Double) {
                qualitySlider.setValue((int) (((Double) quality) * 10));
            }
            
            Object geoTagging = field.getDataProperty("geoTagging");
            if (geoTagging instanceof Boolean) {
                geoTaggingCheckbox.setSelected((Boolean) geoTagging);
            }
        }
    }
    
    @Override
    protected void saveFieldData() {
        if (field != null) {
            field.setDataProperty("maxPhotos", maxPhotosSpinner.getValue());
            field.setDataProperty("quality", qualitySlider.getValue() / 10.0);
            field.setDataProperty("geoTagging", geoTaggingCheckbox.isSelected());
        }
    }
    
    @Override
    public boolean validateInput() {
        return true;
    }
}
