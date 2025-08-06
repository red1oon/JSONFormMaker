package com.adui.jsoncraft.canvas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.model.ComponentType;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.WindowDefinition;
import com.adui.jsoncraft.palette.ComponentPalette;

/**
 * Form Canvas for JSONFormMaker v1.1.1
 * Main design area with drag-drop form creation and field reordering
 * 
 * @version 1.1.1 - Fixed display hierarchy and restored drag functionality
 * @namespace com.adui.jsoncraft.canvas.FormCanvas
 */
public class FormCanvas extends JPanel implements ComponentPalette.ComponentPaletteListener {
    private static final Logger logger = LoggerFactory.getLogger(FormCanvas.class);
    
    private WindowDefinition currentWindow;
    private TabDefinition currentTab;
    private JTabbedPane tabPane;
    private JPanel currentTabPanel;
    private List<FieldVisualizer> fieldVisualizers;
    private List<FormCanvasListener> listeners;
    private FieldVisualizer selectedField;
    
    // Drag and drop state
    private FieldVisualizer draggedField;
    private JPanel dropIndicator;
    private int dropIndex = -1;
    
    // Window properties panel
    private JPanel windowPropertiesPanel;
    private JTextField windowIdField;
    private JTextField windowNameField;
    private JTextArea windowDescField;
    
    public FormCanvas() {
        this.fieldVisualizers = new ArrayList<>();
        this.listeners = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        createNewWindow();
        
        logger.debug("Form canvas v1.1.1 initialized with drag-to-reorder support");
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Form Designer"));
        
        // Window properties panel
        createWindowPropertiesPanel();
        
        // Tab pane for form design
        tabPane = new JTabbedPane();
        tabPane.setTabPlacement(JTabbedPane.TOP);
        
        // Add tab button
        JButton addTabButton = new JButton("+");
        addTabButton.setToolTipText("Add new tab");
        addTabButton.addActionListener(e -> addNewTab());
        tabPane.addTab("", null, new JPanel(), "Add new tab");
        tabPane.setTabComponentAt(0, addTabButton);
        
        // Create drop indicator
        dropIndicator = new JPanel();
        dropIndicator.setBackground(new Color(0, 120, 215, 100));
        dropIndicator.setPreferredSize(new Dimension(0, 3));
        dropIndicator.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
        dropIndicator.setVisible(false);
    }
    
    private void createWindowPropertiesPanel() {
        windowPropertiesPanel = new JPanel(new GridBagLayout());
        windowPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Window Properties"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Window ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        windowPropertiesPanel.add(new JLabel("Window ID:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        windowIdField = new JTextField(20);
        windowIdField.addActionListener(e -> updateWindowProperties());
        windowPropertiesPanel.add(windowIdField, gbc);
        
        // Window Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        windowPropertiesPanel.add(new JLabel("Window Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        windowNameField = new JTextField(20);
        windowNameField.addActionListener(e -> updateWindowProperties());
        windowPropertiesPanel.add(windowNameField, gbc);
        
        // Window Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        windowPropertiesPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 0.5;
        windowDescField = new JTextArea(3, 20);
        windowDescField.setLineWrap(true);
        windowDescField.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(windowDescField);
        windowPropertiesPanel.add(descScroll, gbc);
    }
    
    private void setupLayout() {
        // Top: Window properties
        add(windowPropertiesPanel, BorderLayout.NORTH);
        
        // Center: Tab pane for form design
        add(tabPane, BorderLayout.CENTER);
        
        // Bottom: Instructions
        JLabel instructionsLabel = new JLabel("Double-click components from palette to add, or drag and drop fields to reorder");
        instructionsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        instructionsLabel.setFont(instructionsLabel.getFont().deriveFont(Font.ITALIC));
        add(instructionsLabel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Tab selection change
        tabPane.addChangeListener(e -> {
            int selectedIndex = tabPane.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < currentWindow.getTabCount()) {
                currentTab = currentWindow.getTabs().get(selectedIndex);
                currentTabPanel = (JPanel) tabPane.getComponentAt(selectedIndex);
                refreshCurrentTab();
                notifyTabChanged(currentTab);
            }
        });
    }
    
    public void createNewWindow() {
        currentWindow = new WindowDefinition();
        currentWindow.setWindowId("NEW_WINDOW");
        currentWindow.setName("New Window");
        currentWindow.setDescription("New window definition");
        
        // Update UI
        windowIdField.setText(currentWindow.getWindowId());
        windowNameField.setText(currentWindow.getName());
        windowDescField.setText(currentWindow.getDescription());
        
        // Clear tabs and add first tab
        clearTabs();
        addNewTab();
        
        notifyWindowChanged(currentWindow);
        logger.info("Created new window: {}", currentWindow.getWindowId());
    }
    
    private void clearTabs() {
        // Remove all tabs except the "+" button
        while (tabPane.getTabCount() > 1) {
            tabPane.removeTabAt(0);
        }
        fieldVisualizers.clear();
    }
    
    private void addNewTab() {
        int tabNumber = currentWindow.getTabCount() + 1;
        String tabId = "TAB_" + tabNumber;
        String tabName = "Tab " + tabNumber;
        
        TabDefinition newTab = new TabDefinition(tabId, tabName);
        newTab.setSequence(tabNumber * 10);
        currentWindow.addTab(newTab);
        
        // Create tab panel
        JPanel tabPanel = createTabPanel(newTab);
        
        // Insert before the "+" button
        int insertIndex = tabPane.getTabCount() - 1;
        tabPane.insertTab(tabName, null, tabPanel, "Tab: " + tabName, insertIndex);
        
        // Select the new tab
        tabPane.setSelectedIndex(insertIndex);
        currentTab = newTab;
        currentTabPanel = tabPanel;
        
        refreshCurrentTab();
        logger.debug("Added new tab: {}", tabId);
    }
    
    private JPanel createTabPanel(TabDefinition tab) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Drop target for adding fields and reordering
        new DropTarget(panel, new FieldDropHandler());
        
        return panel;
    }
    
    private void refreshCurrentTab() {
        if (currentTab == null || currentTabPanel == null) return;
        
        currentTabPanel.removeAll();
        fieldVisualizers.clear();
        
        // Add field visualizers for each field in tab
        for (FieldDefinition field : currentTab.getFields()) {
            FieldVisualizer visualizer = new FieldVisualizer(field);
            visualizer.addFieldListener(new FieldVisualizerListener());
            fieldVisualizers.add(visualizer);
            currentTabPanel.add(visualizer);
            currentTabPanel.add(Box.createVerticalStrut(5));
        }
        
        // Add placeholder if no fields
        if (currentTab.getFields().isEmpty()) {
            JLabel placeholder = new JLabel("Double-click components from palette to add fields here");
            placeholder.setForeground(Color.GRAY);
            placeholder.setHorizontalAlignment(SwingConstants.CENTER);
            placeholder.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
            currentTabPanel.add(placeholder);
        }
        
        currentTabPanel.revalidate();
        currentTabPanel.repaint();
    }
    
    @Override
    public void componentSelected(ComponentType type) {
        if (currentTab != null) {
            // Create new field
            String fieldId = generateFieldId(type);
            FieldDefinition newField = new FieldDefinition(fieldId, type.getDisplayType(), type);
            newField.setSequence((currentTab.getFieldCount() + 1) * 10);
            
            // Add to current tab
            currentTab.addField(newField);
            refreshCurrentTab();
            
            // Select the new field
            if (!fieldVisualizers.isEmpty()) {
                FieldVisualizer lastVisualizer = fieldVisualizers.get(fieldVisualizers.size() - 1);
                selectField(lastVisualizer);
            }
            
            logger.debug("Added field: {} of type: {}", fieldId, type.getJsonName());
        }
    }
    
    private String generateFieldId(ComponentType type) {
        String baseName = type.getJsonName().toUpperCase().replace("FIELD", "");
        int counter = 1;
        String fieldId = baseName + "_" + counter;
        
        // Ensure unique ID
        while (currentTab.getField(fieldId) != null) {
            counter++;
            fieldId = baseName + "_" + counter;
        }
        
        return fieldId;
    }
    
    private void selectField(FieldVisualizer visualizer) {
        // Deselect previous
        if (selectedField != null) {
            selectedField.setSelected(false);
        }
        
        // Select new
        selectedField = visualizer;
        if (selectedField != null) {
            selectedField.setSelected(true);
            notifyFieldSelected(selectedField.getField());
        }
    }
    
    private void updateWindowProperties() {
        if (currentWindow != null) {
            currentWindow.setWindowId(windowIdField.getText().trim());
            currentWindow.setName(windowNameField.getText().trim());
            currentWindow.setDescription(windowDescField.getText().trim());
            notifyWindowChanged(currentWindow);
        }
    }
    
    public void setCurrentWindow(WindowDefinition window) {
        this.currentWindow = window;
        
        if (window != null) {
            // Update window properties
            windowIdField.setText(window.getWindowId() != null ? window.getWindowId() : "");
            windowNameField.setText(window.getName() != null ? window.getName() : "");
            windowDescField.setText(window.getDescription() != null ? window.getDescription() : "");
            
            // Rebuild tabs
            clearTabs();
            for (TabDefinition tab : window.getTabs()) {
                JPanel tabPanel = createTabPanel(tab);
                int insertIndex = tabPane.getTabCount() - 1;
                tabPane.insertTab(tab.getName(), null, tabPanel, "Tab: " + tab.getName(), insertIndex);
            }
            
            if (window.getTabCount() > 0) {
                tabPane.setSelectedIndex(0);
                currentTab = window.getTabs().get(0);
                currentTabPanel = (JPanel) tabPane.getComponentAt(0);
                refreshCurrentTab();
            }
        }
        
        notifyWindowChanged(window);
    }
    
    // Event handling
    public void addFormCanvasListener(FormCanvasListener listener) {
        listeners.add(listener);
    }
    
    public void removeFormCanvasListener(FormCanvasListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyWindowChanged(WindowDefinition window) {
        for (FormCanvasListener listener : listeners) {
            listener.windowChanged(window);
        }
    }
    
    private void notifyTabChanged(TabDefinition tab) {
        for (FormCanvasListener listener : listeners) {
            listener.tabChanged(tab);
        }
    }
    
    private void notifyFieldSelected(FieldDefinition field) {
        for (FormCanvasListener listener : listeners) {
            listener.fieldSelected(field);
        }
    }
    
    /**
     * Field visualizer component with drag-and-drop support
     */
    private class FieldVisualizer extends JPanel {
        private final FieldDefinition field;
        private JLabel iconLabel;
        private JLabel nameLabel;
        private JLabel typeLabel;
        private JLabel requiredLabel;
        private JLabel dragHandle;
        private boolean selected = false;
        
        // Drag state
        private boolean isDragging = false;
        private Point startPoint;
        
        public FieldVisualizer(FieldDefinition field) {
            this.field = field;
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            setPreferredSize(new Dimension(0, 50));
            
            // Create components with correct hierarchy - Component type is primary
            iconLabel = new JLabel(field.getComponentType().getIcon());
            
            // Main display: Component type (large, bold) - what the user sees prominently
            nameLabel = new JLabel(field.getComponentType().getJsonName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
            
            // Secondary info: Field ID (small, gray) - technical identifier
            typeLabel = new JLabel(field.getFieldId());
            typeLabel.setFont(typeLabel.getFont().deriveFont(Font.PLAIN, 9f));
            typeLabel.setForeground(Color.GRAY);
            
            requiredLabel = new JLabel();
            
            // Drag handle
            dragHandle = new JLabel("≡");
            dragHandle.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            dragHandle.setForeground(Color.GRAY);
            dragHandle.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            dragHandle.setToolTipText("Drag to reorder");
            
            // Layout - make the display clearer
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            leftPanel.setOpaque(false);
            leftPanel.add(dragHandle);
            leftPanel.add(iconLabel);
            
            JPanel namePanel = new JPanel(new BorderLayout());
            namePanel.setOpaque(false);
            namePanel.add(nameLabel, BorderLayout.NORTH);
            namePanel.add(typeLabel, BorderLayout.SOUTH);
            leftPanel.add(namePanel);
            
            add(leftPanel, BorderLayout.WEST);
            add(requiredLabel, BorderLayout.EAST);
            
            setBorder(BorderFactory.createEtchedBorder());
            setBackground(Color.WHITE);
            
            // Enable drag and drop
            setTransferHandler(new FieldTransferHandler());
            
            // Mouse handler for both click selection and drag initiation
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    isDragging = false;
                    draggedField = FieldVisualizer.this;
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Only handle click if we didn't drag
                    if (!isDragging && !SwingUtilities.isRightMouseButton(e)) {
                        selectField(FieldVisualizer.this);
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
                    // Start drag if moved far enough from start point
                    if (startPoint != null) {
                        int deltaX = Math.abs(e.getX() - startPoint.x);
                        int deltaY = Math.abs(e.getY() - startPoint.y);
                        
                        if ((deltaX > 5 || deltaY > 5) && !isDragging) {
                            isDragging = true;
                            setBackground(new Color(200, 200, 200, 100));
                            
                            TransferHandler handler = getTransferHandler();
                            if (handler != null) {
                                handler.exportAsDrag(FieldVisualizer.this, e, TransferHandler.MOVE);
                            }
                        }
                    }
                }
            };
            
            // Apply handlers to the main component and drag handle
            addMouseListener(mouseHandler);
            addMouseMotionListener(motionHandler);
            dragHandle.addMouseListener(mouseHandler);
            dragHandle.addMouseMotionListener(motionHandler);
            
            updateDisplay();
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
            updateDisplay();
        }
        
        private void updateDisplay() {
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
            
            // Update labels with correct display hierarchy
            nameLabel.setText(field.getComponentType().getJsonName());
            typeLabel.setText(field.getFieldId());
            updateRequiredLabel();
            
            repaint();
        }
        
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
        
        public FieldDefinition getField() {
            return field;
        }
        
        public void addFieldListener(FieldVisualizerListener listener) {
            // Add mouse listeners for events if needed
        }
    }
    
    /**
     * Transfer handler for field drag and drop operations
     */
    private class FieldTransferHandler extends TransferHandler {
        
        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof FieldVisualizer) {
                FieldVisualizer visualizer = (FieldVisualizer) c;
                return new FieldTransferable(visualizer.getField());
            }
            return null;
        }
        
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(FieldTransferable.FIELD_FLAVOR);
        }
        
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            
            try {
                FieldDefinition draggedFieldDef = (FieldDefinition) support.getTransferable()
                    .getTransferData(FieldTransferable.FIELD_FLAVOR);
                
                // Find drop location
                if (dropIndex >= 0 && currentTab != null) {
                    int currentIndex = currentTab.getFields().indexOf(draggedFieldDef);
                    if (currentIndex >= 0 && currentIndex != dropIndex) {
                        // Move field in the model
                        currentTab.moveField(currentIndex, dropIndex);
                        
                        // Refresh UI
                        refreshCurrentTab();
                        
                        // Reselect the moved field
                        if (dropIndex < fieldVisualizers.size()) {
                            selectField(fieldVisualizers.get(dropIndex));
                        }
                        
                        logger.debug("Moved field {} from position {} to {}", 
                            draggedFieldDef.getFieldId(), currentIndex, dropIndex);
                        
                        notifyWindowChanged(currentWindow);
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.error("Error importing field", e);
            }
            
            return false;
        }
        
        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            // Clean up drag state
            draggedField = null;
            dropIndex = -1;
            hideDropIndicator();
        }
    }
    
    /**
     * Transferable wrapper for field definitions
     */
    private static class FieldTransferable implements Transferable {
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
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return field;
        }
    }
    
    /**
     * Drop handler for field reordering within tabs
     */
    private class FieldDropHandler implements DropTargetListener {
        
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
                
                // Calculate drop position
                Point dropPoint = dtde.getLocation();
                int newDropIndex = calculateDropIndex(dropPoint);
                
                if (newDropIndex != dropIndex) {
                    dropIndex = newDropIndex;
                    showDropIndicator(dropIndex);
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
            hideDropIndicator();
        }
        
        @Override
        public void drop(DropTargetDropEvent dtde) {
            if (dtde.isDataFlavorSupported(FieldTransferable.FIELD_FLAVOR)) {
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                
                try {
                    FieldDefinition draggedFieldDef = (FieldDefinition) dtde.getTransferable()
                        .getTransferData(FieldTransferable.FIELD_FLAVOR);
                    
                    if (dropIndex >= 0 && currentTab != null) {
                        int currentIndex = currentTab.getFields().indexOf(draggedFieldDef);
                        if (currentIndex >= 0 && currentIndex != dropIndex) {
                            // Adjust drop index if moving down
                            int targetIndex = dropIndex;
                            if (currentIndex < dropIndex) {
                                targetIndex = Math.max(0, dropIndex - 1);
                            }
                            
                            // Move field in the model
                            currentTab.moveField(currentIndex, targetIndex);
                            
                            // Refresh UI
                            refreshCurrentTab();
                            
                            // Reselect the moved field
                            if (targetIndex < fieldVisualizers.size()) {
                                selectField(fieldVisualizers.get(targetIndex));
                            }
                            
                            logger.debug("Dropped field {} at position {}", 
                                draggedFieldDef.getFieldId(), targetIndex);
                            
                            notifyWindowChanged(currentWindow);
                            dtde.dropComplete(true);
                        } else {
                            dtde.dropComplete(false);
                        }
                    } else {
                        dtde.dropComplete(false);
                    }
                } catch (Exception e) {
                    logger.error("Error handling drop", e);
                    dtde.dropComplete(false);
                }
            } else {
                dtde.rejectDrop();
            }
            
            hideDropIndicator();
            dropIndex = -1;
        }
        
        private int calculateDropIndex(Point dropPoint) {
            if (currentTabPanel == null || fieldVisualizers.isEmpty()) {
                return 0;
            }
            
            // Find the component at the drop point
            Component[] components = currentTabPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                Component comp = components[i];
                if (comp instanceof FieldVisualizer) {
                    Rectangle bounds = comp.getBounds();
                    if (dropPoint.y <= bounds.y + bounds.height / 2) {
                        return fieldVisualizers.indexOf(comp);
                    }
                }
            }
            
            // Drop at the end
            return fieldVisualizers.size();
        }
    }
    
    private void showDropIndicator(int index) {
        hideDropIndicator();
        
        if (currentTabPanel != null && index >= 0) {
            if (index < fieldVisualizers.size()) {
                // Insert before the field at index
                FieldVisualizer targetField = fieldVisualizers.get(index);
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
    
    private void hideDropIndicator() {
        if (dropIndicator.isVisible()) {
            dropIndicator.setVisible(false);
            if (currentTabPanel != null) {
                currentTabPanel.remove(dropIndicator);
                currentTabPanel.revalidate();
                currentTabPanel.repaint();
            }
        }
    }
    
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
    
    // Getters
    public WindowDefinition getCurrentWindow() { return currentWindow; }
    public TabDefinition getCurrentTab() { return currentTab; }
    public FieldDefinition getSelectedField() { 
        return selectedField != null ? selectedField.getField() : null; 
    }
    
    /**
     * Field visualizer event listener
     */
    private class FieldVisualizerListener {
        // Add specific field events if needed
    }
    
    /**
     * Interface for form canvas events
     */
    public interface FormCanvasListener {
        void windowChanged(WindowDefinition window);
        void tabChanged(TabDefinition tab);
        void fieldSelected(FieldDefinition field);
    }
}