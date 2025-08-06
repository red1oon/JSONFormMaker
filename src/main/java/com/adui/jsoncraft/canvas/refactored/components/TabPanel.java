package com.adui.jsoncraft.canvas.refactored.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.managers.DragDropManager;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;

/**
 * Tab Panel Component for Form Canvas
 * Manages the display and layout of fields within a tab
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.components.TabPanel
 */
public class TabPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(TabPanel.class);
    private WindowDefinition windowDefinition;
    private final TabDefinition tabDefinition;
    private final List<FieldVisualizer> fieldVisualizers;

	private DragDropManager dragDropManager;
    
    public TabPanel(TabDefinition tabDefinition) {
        this.tabDefinition = tabDefinition;
        this.fieldVisualizers = new ArrayList<>();
        
        initializeComponents();
        setupDropTarget();  
        refreshFields();
        
        logger.debug("TabPanel created for tab: {}", tabDefinition.getTabId());
    }
    
    public void setWindowDefinition(WindowDefinition windowDefinition) {
        this.windowDefinition = windowDefinition;
    }
    
    public void setDragDropManager(DragDropManager dragDropManager) {
        this.dragDropManager = dragDropManager;
        setupDropTarget();
        // Update existing visualizers
        for (FieldVisualizer viz : fieldVisualizers) {
            viz.setDragDropManager(dragDropManager);
        }
    }
    
    public TabPanel(TabDefinition tabDefinition, DragDropManager dragDropManager) {
        this.tabDefinition = tabDefinition;
        this.fieldVisualizers = new ArrayList<>();
        this.dragDropManager = dragDropManager;  // Store reference
        
        initializeComponents();
        setupDropTarget();  // ADD this call
        refreshFields();
        
        logger.debug("TabPanel created for tab: {}", tabDefinition.getTabId());
    }
    
    /**
     * Initialize panel layout
     */
    private void initializeComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        
        // Add scroll capability
        setAutoscrolls(true);
    }
    
    /**
     * Refresh field visualizers based on tab definition
     */
    public void refreshFields() {
        // Clear existing visualizers
        clearFields();
        
        // Create visualizers for each field
        if (tabDefinition.getFields() != null) {
            for (FieldDefinition field : tabDefinition.getFields()) {
                addFieldVisualizer(field);
            }
        }
        
        // Add spacing at the end
        add(Box.createVerticalGlue());
        
        revalidate();
        repaint();
        
        logger.debug("Refreshed {} fields for tab: {}", 
            fieldVisualizers.size(), tabDefinition.getTabId());
    }
    
    /**
     * Add field visualizer
     */
    public FieldVisualizer addFieldVisualizer(FieldDefinition field) {
        FieldVisualizer visualizer = new FieldVisualizer(field);
        
        // SET the DragDropManager on the visualizer
        if (dragDropManager != null) {
            visualizer.setDragDropManager(dragDropManager);
        }
        
        fieldVisualizers.add(visualizer);
        
        // Add with some vertical spacing
        if (getComponentCount() > 0) {
            add(Box.createRigidArea(new Dimension(0, 5)));
        }
        add(visualizer);
        
        logger.debug("Added field visualizer: {}", field.getFieldId());
        return visualizer;
    }
    
    /**
     * Remove field visualizer
     */
    public boolean removeFieldVisualizer(FieldDefinition field) {
        for (int i = 0; i < fieldVisualizers.size(); i++) {
            FieldVisualizer visualizer = fieldVisualizers.get(i);
            if (visualizer.getField().equals(field)) {
                fieldVisualizers.remove(i);
                remove(visualizer);
                revalidate();
                repaint();
                
                logger.debug("Removed field visualizer: {}", field.getFieldId());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clear all field visualizers
     */
    public void clearFields() {
        fieldVisualizers.clear();
        removeAll();
    }
    
    /**
     * Find field visualizer by field definition
     */
    public FieldVisualizer findFieldVisualizer(FieldDefinition field) {
        return fieldVisualizers.stream()
            .filter(visualizer -> visualizer.getField().equals(field))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get all field visualizers
     */
    public List<FieldVisualizer> getFieldVisualizers() {
        return new ArrayList<>(fieldVisualizers);
    }
    
    /**
     * Select field visualizer
     */
    public void selectField(FieldDefinition field) {
        // Clear all selections first
        fieldVisualizers.forEach(visualizer -> visualizer.setSelected(false));
        
        // Select the target field
        FieldVisualizer targetVisualizer = findFieldVisualizer(field);
        if (targetVisualizer != null) {
            targetVisualizer.setSelected(true);
        }
    }
    
    /**
     * Clear all selections
     */
    public void clearSelection() {
        fieldVisualizers.forEach(visualizer -> visualizer.setSelected(false));
    }
    
    /**
     * Get tab definition
     */
    public TabDefinition getTabDefinition() {
        return tabDefinition;
    }
    
    /**
     * Get field count
     */
    public int getFieldCount() {
        return fieldVisualizers.size();
    }
    
    /**
     * Check if tab is empty
     */
    public boolean isEmpty() {
        return fieldVisualizers.isEmpty();
    }
    
    private void setupDropTarget() {
        if (dragDropManager != null) {
            // Create drop target that accepts field transfers
            new DropTarget(this, new TabPanelDropHandler());
            logger.debug("DropTarget configured for tab: {}", tabDefinition.getTabId());
        }
    }
    
    private class TabPanelDropHandler implements DropTargetListener {
        
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(FieldTransferable.FIELD_FLAVOR)) {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
            } else {
                dtde.rejectDrag();
            }
        }
        
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(FieldTransferable.FIELD_FLAVOR)) {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                
                // Delegate visual feedback to DragDropManager
                if (dragDropManager != null) {
                    java.util.List<Component> visualizerComponents = new ArrayList<>();
                    for (FieldVisualizer viz : fieldVisualizers) {
                        visualizerComponents.add(viz);
                    }
                    dragDropManager.handleDragOver(dtde.getLocation(), visualizerComponents);
                }
            } else {
                dtde.rejectDrag();
            }
        }
        
        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // Handle action changes if needed
        }
        
        @Override
        public void dragExit(DropTargetEvent dte) {
            // Clean up visual feedback via DragDropManager
            // dragDropManager.handleDragExit(); // TODO: Add method if needed
        }
        
        @Override
        public void drop(DropTargetDropEvent dtde) {
            if (dtde.isDataFlavorSupported(FieldTransferable.FIELD_FLAVOR)) {
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                
                try {
                    // Get transferred field (for validation, actual field comes from DragDropManager state)
                    FieldDefinition draggedField = (FieldDefinition) dtde.getTransferable()
                        .getTransferData(FieldTransferable.FIELD_FLAVOR);
                    
                    // Create list of FieldVisualizer components for DragDropManager
                    java.util.List<Component> visualizerComponents = new ArrayList<>();
                    for (FieldVisualizer viz : fieldVisualizers) {
                        visualizerComponents.add(viz);
                    }
                    
                    // Delegate to DragDropManager with correct parameters
                    if (dragDropManager != null) {
                    	dragDropManager.handleDragOver(dtde.getLocation(), visualizerComponents);
                        // Note: Passing null for window - DragDropManager gets it from context
                        boolean success = dragDropManager.completeDrop(windowDefinition, tabDefinition, visualizerComponents);
                        dtde.dropComplete(success);
                        
                        if (success) {
                            refreshFields(); // Refresh to show new order
                        }
                    } else {
                        dtde.dropComplete(false);
                    }
                    
                } catch (UnsupportedFlavorException | IOException e) {
                    logger.error("Error handling drop", e);
                    dtde.dropComplete(false);
                }
            } else {
                dtde.rejectDrop();
            }
        }
    }

}
