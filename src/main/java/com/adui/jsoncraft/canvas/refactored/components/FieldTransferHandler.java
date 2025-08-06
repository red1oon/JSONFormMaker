package com.adui.jsoncraft.canvas.refactored.components;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.managers.DragDropManager;
import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Transfer handler for FieldVisualizer drag-and-drop operations
 * Bridges Swing's drag-drop system with our DragDropManager
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.components.FieldTransferHandler
 */
public class FieldTransferHandler extends TransferHandler {
    private static final Logger logger = LoggerFactory.getLogger(FieldTransferHandler.class);
    
    private final DragDropManager dragDropManager;
    
    /**
     * Create transfer handler with drag-drop manager reference
     */
    public FieldTransferHandler(DragDropManager dragDropManager) {
        this.dragDropManager = dragDropManager;
    }
    
    /**
     * Get supported source actions (MOVE for field reordering)
     */
    @Override
    public int getSourceActions(JComponent component) {
        return MOVE;
    }
    
    /**
     * Create transferable object for drag operation
     */
    @Override
    protected Transferable createTransferable(JComponent component) {
        if (component instanceof FieldVisualizer) {
            FieldVisualizer visualizer = (FieldVisualizer) component;
            FieldDefinition field = visualizer.getField();
            
            // Notify drag start to our manager
            JPanel tabPanel = visualizer.getTabPanel();
            if (dragDropManager != null && tabPanel != null) {
                dragDropManager.startDrag(field, tabPanel);
            }
            
            logger.debug("Created transferable for field: {}", field.getFieldId());
            return new FieldTransferable(field);
        }
        
        logger.warn("Cannot create transferable: component is not FieldVisualizer");
        return null;
    }
    
    /**
     * Check if import is supported
     */
    @Override
    public boolean canImport(TransferSupport support) {
        // Only support field reordering within the same container
        return support.isDataFlavorSupported(FieldTransferable.FIELD_FLAVOR) &&
               support.getComponent() instanceof JPanel;
    }
    
    /**
     * Import transferred data (handle drop)
     */
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        
        // Note: The actual drop handling is done by TabPanel's DropTarget
        // This importData is mainly for compatibility, but the real work
        // happens in TabPanel.TabPanelDropHandler
        logger.debug("Import data called on TransferHandler (delegated to TabPanel)");
        return true;
    }
    
    /**
     * Clean up after drag operation completes
     */
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        // Clean up drag state
    	if (dragDropManager != null) {
            dragDropManager.cancelDrag(); // Force cleanup
        }
        
        logger.debug("Export done for action: {}", action);
    }
}