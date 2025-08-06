package com.adui.jsoncraft.canvas.refactored.managers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.events.FormCanvasEventBus;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;

/**
 * Manages drag-and-drop operations for field reordering
 * Extracted from FormCanvas for clean separation of concerns
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.managers.DragDropManager
 */
public class DragDropManager {
    private static final Logger logger = LoggerFactory.getLogger(DragDropManager.class);
    
    // Drag state
    private FieldDefinition draggedField;
    private JPanel dropIndicator;
    private int dropIndex = -1;
    private JPanel currentTabPanel;
    
    private final FormCanvasEventBus eventBus;
    private final FieldManager fieldManager;
    
    public DragDropManager(FieldManager fieldManager) {
        this.fieldManager = fieldManager;
        this.eventBus = FormCanvasEventBus.getInstance();
        this.dropIndex = -1;
        createDropIndicator();
        logger.debug("DragDropManager initialized");
    }
    
    /**
     * Initialize drag operation
     */
    public void startDrag(FieldDefinition field, JPanel tabPanel) {
        this.draggedField = field;
        this.currentTabPanel = tabPanel;
        logger.debug("Started drag for field: {}", field.getFieldId());
    }
    
    /**
     * Handle drag over operation
     */
    public void handleDragOver(Point dropPoint, java.util.List<Component> fieldVisualizers) {
        if (currentTabPanel == null || fieldVisualizers == null) {
            return;
        }
        
        int newDropIndex = calculateDropIndex(dropPoint, fieldVisualizers);
        
        if (newDropIndex != dropIndex) {
            dropIndex = newDropIndex;
            showDropIndicator(dropIndex, fieldVisualizers);
        }
    }
    
    /**
     * Complete drop operation
     */
    public boolean completeDrop(WindowDefinition window, TabDefinition tab, 
                               java.util.List<Component> fieldVisualizers) {
        if (draggedField == null || tab == null || dropIndex < 0) {
            logger.warn("Cannot complete drop: invalid state");
            return false;
        }
        
        try {
            // Find current index of dragged field
            int currentIndex = -1;
            java.util.List<FieldDefinition> fields = tab.getFields();
            for (int i = 0; i < fields.size(); i++) {
                if (fields.get(i).equals(draggedField)) {
                    currentIndex = i;
                    break;
                }
            }
            
            if (currentIndex >= 0 && currentIndex != dropIndex) {
                // Perform the move using FieldManager
                boolean success = fieldManager.moveField(window, tab, currentIndex, dropIndex);
                
                if (success) {
                    logger.debug("Completed drop: moved field {} from {} to {}", 
                        draggedField.getFieldId(), currentIndex, dropIndex);
                }
                
                return success;
            }
            
            return true; // No move needed
            
        } finally {
            // Clean up drag state
            cleanupDrag();
        }
    }
    
    /**
     * Cancel drag operation
     */
    public void cancelDrag() {
        logger.debug("Cancelled drag operation");
        cleanupDrag();
    }
    
    /**
     * Calculate drop index based on drop point
     */
    private int calculateDropIndex(Point dropPoint, java.util.List<Component> fieldVisualizers) {
        if (currentTabPanel == null || fieldVisualizers == null) {
            return -1;
        }
        
        Component[] components = currentTabPanel.getComponents();
        
        for (int i = 0; i < components.length; i++) {
            Component comp = components[i];
            if (fieldVisualizers.contains(comp)) {
                Rectangle bounds = comp.getBounds();
                if (dropPoint.y <= bounds.y + bounds.height / 2) {
                    return fieldVisualizers.indexOf(comp);
                }
            }
        }
        
        // Drop at the end
        return fieldVisualizers.size();
    }
    
    /**
     * Show drop indicator at specified index
     */
    private void showDropIndicator(int index, java.util.List<Component> fieldVisualizers) {
        hideDropIndicator();
        
        if (currentTabPanel != null && index >= 0) {
            if (index < fieldVisualizers.size()) {
                // Insert before the field at index
                Component targetField = fieldVisualizers.get(index);
                int componentIndex = getComponentIndex(targetField);
                if (componentIndex >= 0) {
                    currentTabPanel.add(dropIndicator, componentIndex);
                }
            } else {
                // Insert at the end
                currentTabPanel.add(dropIndicator);
            }
            
            dropIndicator.setVisible(true);
            currentTabPanel.revalidate();
            currentTabPanel.repaint();
        }
    }
    
    /**
     * Hide drop indicator
     */
    private void hideDropIndicator() {
        if (dropIndicator != null && dropIndicator.isVisible()) {
            dropIndicator.setVisible(false);
            if (currentTabPanel != null) {
                currentTabPanel.remove(dropIndicator);
                currentTabPanel.revalidate();
                currentTabPanel.repaint();
            }
        }
    }
    
    /**
     * Get component index in tab panel
     */
    private int getComponentIndex(Component component) {
        if (currentTabPanel != null) {
            Component[] components = currentTabPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == component) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Create drop indicator component
     */
    private void createDropIndicator() {
        dropIndicator = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // Get font metrics from parent container or use default
                Font font = getFont();
                if (font == null) {
                    Container parent = getParent();
                    font = (parent != null) ? parent.getFont() : UIManager.getFont("Label.font");
                }
                
                // Calculate line height with padding
                FontMetrics fm = getFontMetrics(font);
                int lineHeight = (fm != null) ? fm.getHeight() : 16; // 16px fallback
                
                return new Dimension(0, lineHeight + 2); // Add 2px vertical padding
            }
            
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize(); // Enforce same minimum size
            }
            
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        
        dropIndicator.setBackground(Color.GREEN); // More visible color
        dropIndicator.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0)); // Top/bottom borders
        dropIndicator.setVisible(false);
    }
    /**
     * Clean up drag state
     */
    private void cleanupDrag() {
        hideDropIndicator();
        draggedField = null;
        dropIndex = -1;
        currentTabPanel = null;
    }
    
    // Getters
    public FieldDefinition getDraggedField() { return draggedField; }
    public boolean isDragging() { return draggedField != null; }
    public int getDropIndex() { return dropIndex; }
    
    /**
     * Transferable implementation for field drag-drop
     */
    public static class FieldTransferable implements Transferable {
        public static final DataFlavor FIELD_FLAVOR = new DataFlavor(FieldDefinition.class, "Field Definition");
        
        private final FieldDefinition field;
        
        public FieldTransferable(FieldDefinition field) {
            this.field = field;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{FIELD_FLAVOR};
        }
        
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return FIELD_FLAVOR.equals(flavor);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return field;
        }
    }
}
