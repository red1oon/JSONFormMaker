package com.adui.jsoncraft.canvas.refactored.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.canvas.refactored.events.FieldSelectionEvent;
import com.adui.jsoncraft.canvas.refactored.events.FormCanvasEventBus;
import com.adui.jsoncraft.canvas.refactored.managers.DragDropManager;
import com.adui.jsoncraft.model.FieldDefinition;

/**
 * Standalone Field Visualizer Component
 * Displays field with drag-drop support and selection highlighting
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.components.FieldVisualizer
 */
public class FieldVisualizer extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(FieldVisualizer.class);
    
    private final FieldDefinition field;
    private final FormCanvasEventBus eventBus;
    
    // UI Components
    private JLabel iconLabel;
    private JLabel nameLabel;
    private JLabel typeLabel;
    private JLabel requiredLabel;
    private JLabel dragHandle;
    
    // State
    private boolean selected = false;
    private boolean isDragging = false;
    private Point startPoint;
    
    // Drag drop manager reference
    private DragDropManager dragDropManager;
    
    private FieldTransferHandler transferHandler;
    
    public FieldVisualizer(FieldDefinition field) {
        this.field = field;
        this.eventBus = FormCanvasEventBus.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        updateDisplay();
        
        logger.debug("FieldVisualizer created for field: {}", field.getFieldId());
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        setPreferredSize(new Dimension(0, 50));
        
        // Icon for component type
        iconLabel = new JLabel("📝"); // Default icon, could be component-specific
        iconLabel.setPreferredSize(new Dimension(20, 20));
        
        // PRIMARY display: Field name (large, bold)
        nameLabel = new JLabel(field.getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        
        // SECONDARY display: Component type (small, gray, italic)
        typeLabel = new JLabel(field.getComponentType().getJsonName());
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.ITALIC, 9f));
        typeLabel.setForeground(Color.GRAY);
        
        // Required indicator
        requiredLabel = new JLabel();
        
        // Drag handle
        dragHandle = new JLabel("≡");
        dragHandle.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        dragHandle.setForeground(Color.GRAY);
        dragHandle.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        dragHandle.setToolTipText("Drag to reorder");
    }
    
    /**
     * Setup component layout
     */
    private void setupLayout() {
        // Left panel with drag handle, icon, and name
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setOpaque(false);
        leftPanel.add(dragHandle);
        leftPanel.add(iconLabel);
        
        // Name panel with primary and secondary labels
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.setOpaque(false);
        namePanel.add(nameLabel, BorderLayout.NORTH);
        namePanel.add(typeLabel, BorderLayout.SOUTH);
        leftPanel.add(namePanel);
        
        add(leftPanel, BorderLayout.WEST);
        add(requiredLabel, BorderLayout.EAST);
        
        setBorder(BorderFactory.createEtchedBorder());
        setBackground(Color.WHITE);
    }
    
    /**
     * Setup event handlers for mouse interaction and drag-drop
     */
    private void setupEventHandlers() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                isDragging = false;
                
                // Handle right-click for context menu (NEW ENHANCEMENT)
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e.getPoint());
                    return;
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Only handle left-click selection if we didn't drag
                if (!isDragging && !SwingUtilities.isRightMouseButton(e)) {
                    selectField();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                updateDisplay();
            }
        };
        
        MouseMotionAdapter motionHandler = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (startPoint != null) {
                    int deltaX = Math.abs(e.getX() - startPoint.x);
                    int deltaY = Math.abs(e.getY() - startPoint.y);
                    
                    if ((deltaX > 5 || deltaY > 5) && !isDragging && dragDropManager != null) {
                        isDragging = true;
                        logger.debug("Starting Swing drag operation for field: {}", field.getFieldId());
                        
                        // CRITICAL: Start Swing drag-and-drop operation
                        TransferHandler handler = getTransferHandler();
                        if (handler != null) {
                            handler.exportAsDrag(FieldVisualizer.this, e, TransferHandler.MOVE);
                        } else {
                            logger.warn("No TransferHandler available for drag operation");
                        }
                    }
                }
            }
        };
        
        // Apply handlers to main component and drag handle
        addMouseListener(mouseHandler);
        addMouseMotionListener(motionHandler);
        dragHandle.addMouseListener(mouseHandler);
        dragHandle.addMouseMotionListener(motionHandler);
    }
    
    /**
     * Start drag operation
     */
    private void startDragOperation(MouseEvent e) {
        isDragging = true;
        setBackground(new Color(200, 200, 200, 100));
        
        // Notify drag drop manager if available
        if (dragDropManager != null) {
            dragDropManager.startDrag(field, getTabPanel());
        }
        
        logger.debug("Started drag for field: {}", field.getFieldId());
    }
    
    /**
     * Select this field
     */
    private void selectField() {
        eventBus.fire(FieldSelectionEvent.selected(field));
        logger.debug("Field selected: {}", field.getFieldId());
    }
    
    /**
     * Show context menu (NEW ENHANCEMENT for right-click delete)
     */
    private void showContextMenu(Point point) {
        JPopupMenu contextMenu = new JPopupMenu();
        
        // Delete field option
        JMenuItem deleteItem = new JMenuItem("Delete Field");
        deleteItem.addActionListener(e -> deleteField());
        contextMenu.add(deleteItem);
        
        // Copy field option
        JMenuItem copyItem = new JMenuItem("Copy Field");
        copyItem.addActionListener(e -> copyField());
        contextMenu.add(copyItem);
        
        // Field properties option
        JMenuItem propertiesItem = new JMenuItem("Properties...");
        propertiesItem.addActionListener(e -> selectField());
        contextMenu.add(propertiesItem);
        
        contextMenu.show(this, point.x, point.y);
        logger.debug("Context menu shown for field: {}", field.getFieldId());
    }
    
    /**
     * Delete this field (NEW ENHANCEMENT)
     */
    private void deleteField() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete field '" + field.getName() + "'?",
            "Delete Field",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Fire deletion event for controller to handle
            eventBus.fire(new FieldDeletionEvent(field));
            logger.info("Field deletion requested: {}", field.getFieldId());
        }
    }
    
    /**
     * Copy this field (NEW ENHANCEMENT)
     */
    private void copyField() {
        // Fire copy event for controller to handle
        eventBus.fire(new FieldCopyEvent(field));
        logger.debug("Field copy requested: {}", field.getFieldId());
    }
    
    /**
     * Update visual display based on state
     */
    public void updateDisplay() {
        Border border;
        if (selected) {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLUE, 2),
                BorderFactory.createEmptyBorder(2, 8, 2, 8));
            setBackground(new Color(230, 240, 255));
        } else {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(2, 8, 2, 8));
            setBackground(Color.WHITE);
        }
        setBorder(border);
        
        // Update labels with current field data
        nameLabel.setText(field.getName());
        typeLabel.setText(field.getComponentType().getJsonName());
        updateRequiredLabel();
        
        repaint();
    }
    
    /**
     * Update required indicator
     */
    private void updateRequiredLabel() {
        if (field.isRequired()) {
            requiredLabel.setText("*");
            requiredLabel.setForeground(Color.RED);
            requiredLabel.setToolTipText("Required field");
        } else {
            requiredLabel.setText("");
            requiredLabel.setToolTipText(null);
        }
    }
    
    /**
     * Get parent tab panel for drag operations
     */
    JPanel getTabPanel() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof JPanel)) {
            parent = parent.getParent();
        }
        return (JPanel) parent;
    }
    
    // Getters and setters
    public FieldDefinition getField() { return field; }
    
    public boolean isSelected() { return selected; }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
        updateDisplay();
    }
    
    public void setDragDropManager(DragDropManager dragDropManager) {
        this.dragDropManager = dragDropManager;
        
        // CREATE and SET the transfer handler when DragDropManager is set
        if (dragDropManager != null) {
            this.transferHandler = new FieldTransferHandler(dragDropManager);
            setTransferHandler(this.transferHandler);
            logger.debug("TransferHandler set for field: {}", field.getFieldId());
        }
    }
    
    /**
     * Field deletion event (NEW ENHANCEMENT)
     */
    public static class FieldDeletionEvent {
        private final FieldDefinition field;
        
        public FieldDeletionEvent(FieldDefinition field) {
            this.field = field;
        }
        
        public FieldDefinition getField() { return field; }
    }
    
    /**
     * Field copy event (NEW ENHANCEMENT)
     */
    public static class FieldCopyEvent {
        private final FieldDefinition field;
        
        public FieldCopyEvent(FieldDefinition field) {
            this.field = field;
        }
        
        public FieldDefinition getField() { return field; }
    }
}
