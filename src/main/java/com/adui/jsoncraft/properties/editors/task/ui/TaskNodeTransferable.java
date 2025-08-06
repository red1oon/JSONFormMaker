package com.adui.jsoncraft.properties.editors.task.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import com.adui.jsoncraft.properties.editors.task.model.TaskNode;

/**
 * Transferable wrapper for TaskNode
 */
public class TaskNodeTransferable implements Transferable {
    public static final DataFlavor TASK_NODE_FLAVOR = 
        new DataFlavor(TaskNode.class, "Task Node");
    
    private final TaskNode taskNode;
    
    public TaskNodeTransferable(TaskNode taskNode) {
        this.taskNode = taskNode;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{TASK_NODE_FLAVOR};
    }
    
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return TASK_NODE_FLAVOR.equals(flavor);
    }
    
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
            return taskNode;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
