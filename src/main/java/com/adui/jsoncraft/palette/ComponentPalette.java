package com.adui.jsoncraft.palette;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.model.ComponentType;

/**
 * Component Palette for JSONFormMaker
 * Displays categorized tree of available ADUI components
 */
public class ComponentPalette extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ComponentPalette.class);
    
    private JTree componentTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JTextField searchField;
    private List<ComponentPaletteListener> listeners;
    
    public ComponentPalette() {
        this.listeners = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        populateTree();
        
        logger.debug("Component palette initialized");
    }
    
    private void initializeComponents() {
        // Search field
        searchField = new JTextField();
        searchField.setToolTipText("Search components...");
        
        // Component tree
        rootNode = new DefaultMutableTreeNode("Components");
        treeModel = new DefaultTreeModel(rootNode);
        componentTree = new JTree(treeModel);
        
        // Tree configuration
        componentTree.setRootVisible(false);
        componentTree.setShowsRootHandles(true);
        componentTree.setCellRenderer(new ComponentTreeCellRenderer());
        componentTree.setRowHeight(20);
        
        // Enable drag and drop (for future implementation)
        componentTree.setDragEnabled(true);
        componentTree.setTransferHandler(new ComponentTransferHandler());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Component Palette"));
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.add(new JLabel("🔍"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Tree panel
        JScrollPane treeScroll = new JScrollPane(componentTree);
        treeScroll.setPreferredSize(new Dimension(220, 400));
        
        add(searchPanel, BorderLayout.NORTH);
        add(treeScroll, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Search functionality
        searchField.addActionListener(e -> filterComponents(searchField.getText()));
        
        // Double-click to add component
        componentTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = componentTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof ComponentType) {
                            ComponentType type = (ComponentType) node.getUserObject();
                            notifyComponentSelected(type);
                        }
                    }
                }
            }
        });
        
        // Right-click context menu
        componentTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
    }
    
    private void populateTree() {
        // Create category nodes
        for (ComponentType.ComponentCategory category : ComponentType.ComponentCategory.values()) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            
            // Add components to category
            for (ComponentType type : ComponentType.values()) {
                if (type.getCategory() == category) {
                    DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(type);
                    categoryNode.add(componentNode);
                }
            }
            
            // Only add category if it has components
            if (categoryNode.getChildCount() > 0) {
                rootNode.add(categoryNode);
            }
        }
        
        // Expand first few categories by default
        for (int i = 0; i < Math.min(3, rootNode.getChildCount()); i++) {
            TreePath path = new TreePath(new Object[]{rootNode, rootNode.getChildAt(i)});
            componentTree.expandPath(path);
        }
        
        treeModel.reload();
        logger.debug("Populated component tree with {} categories", rootNode.getChildCount());
    }
    
    private void filterComponents(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            populateTree(); // Reset to full tree
            return;
        }
        
        String filter = searchText.toLowerCase();
        rootNode.removeAllChildren();
        
        // Create filtered tree
        for (ComponentType.ComponentCategory category : ComponentType.ComponentCategory.values()) {
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            
            for (ComponentType type : ComponentType.values()) {
                if (type.getCategory() == category) {
                    if (type.getJsonName().toLowerCase().contains(filter) ||
                        type.getDescription().toLowerCase().contains(filter) ||
                        category.getDisplayName().toLowerCase().contains(filter)) {
                        
                        DefaultMutableTreeNode componentNode = new DefaultMutableTreeNode(type);
                        categoryNode.add(componentNode);
                    }
                }
            }
            
            if (categoryNode.getChildCount() > 0) {
                rootNode.add(categoryNode);
            }
        }
        
        // Expand all categories when searching
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            TreePath path = new TreePath(new Object[]{rootNode, rootNode.getChildAt(i)});
            componentTree.expandPath(path);
        }
        
        treeModel.reload();
        logger.debug("Filtered components with '{}', found {} categories", searchText, rootNode.getChildCount());
    }
    
    private void showContextMenu(MouseEvent e) {
        TreePath path = componentTree.getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            
            JPopupMenu popup = new JPopupMenu();
            
            if (node.getUserObject() instanceof ComponentType) {
                ComponentType type = (ComponentType) node.getUserObject();
                
                JMenuItem addItem = new JMenuItem("Add " + type.getJsonName());
                addItem.addActionListener(ae -> notifyComponentSelected(type));
                popup.add(addItem);
                
                popup.addSeparator();
                
                JMenuItem infoItem = new JMenuItem("Show Information");
                infoItem.addActionListener(ae -> showComponentInfo(type));
                popup.add(infoItem);
                
            } else if (node.getUserObject() instanceof ComponentType.ComponentCategory) {
                ComponentType.ComponentCategory category = (ComponentType.ComponentCategory) node.getUserObject();
                
                JMenuItem expandItem = new JMenuItem("Expand Category");
                expandItem.addActionListener(ae -> componentTree.expandPath(path));
                popup.add(expandItem);
                
                JMenuItem collapseItem = new JMenuItem("Collapse Category");
                collapseItem.addActionListener(ae -> componentTree.collapsePath(path));
                popup.add(collapseItem);
            }
            
            popup.show(componentTree, e.getX(), e.getY());
        }
    }
    
    private void showComponentInfo(ComponentType type) {
        String info = String.format(
            "Component: %s\n\n" +
            "Display Type: %s\n" +
            "Category: %s\n" +
            "Description: %s\n\n" +
            "JSON Name: %s",
            type.name(),
            type.getDisplayType(),
            type.getCategory().getDisplayName(),
            type.getDescription(),
            type.getJsonName()
        );
        
        JOptionPane.showMessageDialog(this, info, 
            "Component Information - " + type.getJsonName(), 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void notifyComponentSelected(ComponentType type) {
        for (ComponentPaletteListener listener : listeners) {
            listener.componentSelected(type);
        }
        logger.debug("Component selected: {}", type.getJsonName());
    }
    
    public void addComponentPaletteListener(ComponentPaletteListener listener) {
        listeners.add(listener);
    }
    
    public void removeComponentPaletteListener(ComponentPaletteListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Custom tree cell renderer for component tree
     */
    private static class ComponentTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();
                
                if (userObject instanceof ComponentType.ComponentCategory) {
                    ComponentType.ComponentCategory category = (ComponentType.ComponentCategory) userObject;
                    setText(category.getIcon() + " " + category.getDisplayName());
                    setFont(getFont().deriveFont(Font.BOLD));
                    
                } else if (userObject instanceof ComponentType) {
                    ComponentType type = (ComponentType) userObject;
                    setText(type.getIcon() + " " + type.getJsonName());
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setToolTipText(type.getDescription());
                }
            }
            
            return this;
        }
    }
    
    /**
     * Transfer handler for drag and drop (placeholder for future implementation)
     */
    private static class ComponentTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
        
        // TODO: Implement actual drag and drop functionality
    }
    
    /**
     * Interface for component palette events
     */
    public interface ComponentPaletteListener {
        void componentSelected(ComponentType type);
    }
}
