package com.adui.jsoncraft.properties.editors.task.ui;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.adui.jsoncraft.properties.editors.task.model.TaskNode;

/**
 * Transfer handler for drag-drop task reordering
 */
public class TaskNodeTransferHandler extends TransferHandler {
    
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof TaskNode) {
                return new TaskNodeTransferable((TaskNode) node.getUserObject());
            }
        }
        return null;
    }
    
    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDataFlavorSupported(TaskNodeTransferable.TASK_NODE_FLAVOR)) {
            return false;
        }
        
        JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
        return dropLocation.getPath() != null;
    }
    
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        
        try {
            TaskNode draggedTask = (TaskNode) support.getTransferable()
                .getTransferData(TaskNodeTransferable.TASK_NODE_FLAVOR);
            
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            TreePath targetPath = dropLocation.getPath();
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) targetPath.getLastPathComponent();
            
            if (targetNode.getUserObject() instanceof TaskNode) {
                TaskNode targetTask = (TaskNode) targetNode.getUserObject();
                
                // Update parent relationship
                draggedTask.setParentId(targetTask.getId());
                
                // Trigger model refresh
                JTree tree = (JTree) support.getComponent();
                ((TaskTreeModel) tree.getModel()).reload();
                
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}
