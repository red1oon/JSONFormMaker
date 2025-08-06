package com.adui.jsoncraft.properties.editors;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Base interface for component-specific data editors
 * Provides consistent interface for all property editors
 */
public abstract class ComponentDataEditor extends JPanel {
    private static final long serialVersionUID = 1L;
	protected FieldDefinition field;
    protected List<PropertyChangeListener> listeners;
    
    public ComponentDataEditor(FieldDefinition field) {
        this.field = field;
        this.listeners = new ArrayList<>();
        initializeComponents();
        setupLayout();
        loadFieldData();
    }
    
    /**
     * Initialize UI components
     */
    protected abstract void initializeComponents();
    
    /**
     * Setup component layout
     */
    protected abstract void setupLayout();
    
    /**
     * Load data from field into UI
     */
    protected abstract void loadFieldData();
    
    /**
     * Save UI data back to field
     */
    protected abstract void saveFieldData();
    
    /**
     * Validate current input
     */
    public abstract boolean validateInput();
    
    /**
     * Add property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify listeners of property changes
     */
    protected void notifyPropertyChanged() {
        saveFieldData();
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChanged(field);
        }
    }
    
    /**
     * Interface for property change events
     */
    public interface PropertyChangeListener {
        void propertyChanged(FieldDefinition field);
    }
}
