package com.adui.jsoncraft.canvas.refactored.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Transferable wrapper for FieldDefinition objects in drag-and-drop operations
 * Enables Swing drag-and-drop system to transfer field data between components
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.components.FieldTransferable
 */
public class FieldTransferable implements Transferable {
    
    /**
     * Custom DataFlavor for FieldDefinition objects
     */
    public static final DataFlavor FIELD_FLAVOR = new DataFlavor(FieldDefinition.class, "Field Definition");
    
    private final FieldDefinition field;
    
    /**
     * Create transferable for field
     */
    public FieldTransferable(FieldDefinition field) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        this.field = field;
    }
    
    /**
     * Get available data flavors for transfer
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{FIELD_FLAVOR};
    }
    
    /**
     * Check if data flavor is supported
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FIELD_FLAVOR.equals(flavor);
    }
    
    /**
     * Get transfer data for specified flavor
     */
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return field;
    }
    
    /**
     * Get the field being transferred
     */
    public FieldDefinition getField() {
        return field;
    }
}