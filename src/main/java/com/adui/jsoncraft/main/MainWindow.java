package com.adui.jsoncraft.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.utils.ConfigManager;

/**
 * Main application window for JSONFormMaker
 * Provides the primary user interface with menu, toolbar, and panels
 * Integrated with ApplicationController for full functionality
 * 
 * Version: 1.1.0 - FIXED VISIBILITY ISSUES
 * Namespace: com.adui.jsoncraft.main.MainWindow
 */
public class MainWindow extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    
    // UI Components
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JSplitPane mainSplitPane;
    private JSplitPane rightSplitPane;
    private JSplitPane verticalSplitPane;
    
    // Panels - Initially placeholder, replaced by ApplicationController
    private JPanel palettePanel;
    private JPanel canvasPanel;
    private JPanel propertiesPanel;
    private JPanel consolePanel;
    
    // Status bar
    private JPanel statusBar;
    private JLabel statusLabel;
    private JLabel fileStatusLabel;
    private JLabel validationStatusLabel;
    
    // Application Controller (handles the real functionality)
    private ApplicationController applicationController;

    public void setApplicationController(ApplicationController controller) {
        this.applicationController = controller;
        setupFileMenu(); // Setup menu actions
    }

    private void setupFileMenu() {
        JMenuBar menuBar = getJMenuBar(); // Get existing menu bar or create new
        if (menuBar == null) {
            menuBar = new JMenuBar();
            setJMenuBar(menuBar);
        }
        
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> applicationController.newWindow()); // ✅ Use existing method
        fileMenu.add(newItem);
        
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> applicationController.openFile()); // ✅ Use existing method
        fileMenu.add(openItem);
        
        fileMenu.addSeparator();
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> applicationController.saveFile()); // ✅ Use existing method
        fileMenu.add(saveItem);
        
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
            InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener(e -> applicationController.saveAsFile()); // ✅ Use existing method
        fileMenu.add(saveAsItem);
        
        // ✅ Add recent files menu
        fileMenu.addSeparator();
        JMenu recentMenu = new JMenu("Recent Files");
        fileMenu.add(recentMenu);
        // TODO: Populate recent files from persistence service
        
        menuBar.add(fileMenu, 0); // Add as first menu
    }
    
    public MainWindow() {
        super("JSONFormMaker - ADUI JSON Craft Studio");
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadConfiguration();
        initializeApplicationController();
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));
        
        logger.info("Main window initialized");
    }
    
    private void initializeComponents() {
        // Create menu bar
        createMenuBar();
        
        // Create toolbar
        createToolBar();
        
        // Create main panels (placeholder initially)
        createPanels();
        
        // Create status bar
        createStatusBar();
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newItem.addActionListener(e -> newWindow());
        
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        openItem.addActionListener(e -> openFile());
        
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveItem.addActionListener(e -> saveFile());
        
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift S"));
        saveAsItem.addActionListener(e -> saveAsFile());
        
        fileMenu.addSeparator();
        
        // Recent files submenu
        JMenu recentMenu = new JMenu("Recent Files");
        recentMenu.add(new JMenuItem("(No recent files)"));
        fileMenu.add(recentMenu);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitItem.addActionListener(e -> exitApplication());
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(recentMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));
        undoItem.addActionListener(e -> undo());
        undoItem.setEnabled(false); // TODO: Implement undo/redo
        
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Y"));
        redoItem.addActionListener(e -> redo());
        redoItem.setEnabled(false); // TODO: Implement undo/redo
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        
        JMenuItem findItem = new JMenuItem("Find...");
        findItem.setAccelerator(KeyStroke.getKeyStroke("ctrl F"));
        findItem.addActionListener(e -> showFindDialog());
        findItem.setEnabled(false); // TODO: Implement find
        
        editMenu.add(findItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        
        JCheckBoxMenuItem showPaletteItem = new JCheckBoxMenuItem("Component Palette", true);
        showPaletteItem.addActionListener(e -> togglePalette(showPaletteItem.isSelected()));
        
        JCheckBoxMenuItem showPropertiesItem = new JCheckBoxMenuItem("Properties Panel", true);
        showPropertiesItem.addActionListener(e -> toggleProperties(showPropertiesItem.isSelected()));
        
        JCheckBoxMenuItem showConsoleItem = new JCheckBoxMenuItem("Console", false);
        showConsoleItem.addActionListener(e -> toggleConsole(showConsoleItem.isSelected()));
        
        viewMenu.add(showPaletteItem);
        viewMenu.add(showPropertiesItem);
        viewMenu.add(showConsoleItem);
        viewMenu.addSeparator();
        
        JMenuItem validateItem = new JMenuItem("Validate");
        validateItem.setAccelerator(KeyStroke.getKeyStroke("F9"));
        validateItem.addActionListener(e -> validateWindow());
        
        JMenuItem previewItem = new JMenuItem("Preview JSON");
        previewItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        previewItem.addActionListener(e -> previewJson());
        
        viewMenu.add(validateItem);
        viewMenu.add(previewItem);
        
        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('T');
        
        JMenuItem optionsItem = new JMenuItem("Options...");
        optionsItem.addActionListener(e -> showOptionsDialog());
        
        toolsMenu.add(optionsItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem helpItem = new JMenuItem("Help Topics");
        helpItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        helpItem.addActionListener(e -> showHelp());
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(helpItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        
        // File operations
        JButton newBtn = new JButton("New");
        newBtn.setToolTipText("Create new window (Ctrl+N)");
        newBtn.addActionListener(e -> newWindow());
        
        JButton openBtn = new JButton("Open");
        openBtn.setToolTipText("Open existing file (Ctrl+O)");
        openBtn.addActionListener(e -> openFile());
        
        JButton saveBtn = new JButton("Save");
        saveBtn.setToolTipText("Save current file (Ctrl+S)");
        saveBtn.addActionListener(e -> saveFile());
        
        toolBar.add(newBtn);
        toolBar.add(openBtn);
        toolBar.add(saveBtn);
        toolBar.addSeparator();
        
        // Edit operations
        JButton undoBtn = new JButton("Undo");
        undoBtn.setToolTipText("Undo last action (Ctrl+Z)");
        undoBtn.addActionListener(e -> undo());
        undoBtn.setEnabled(false);
        
        JButton redoBtn = new JButton("Redo");
        redoBtn.setToolTipText("Redo last undone action (Ctrl+Y)");
        redoBtn.addActionListener(e -> redo());
        redoBtn.setEnabled(false);
        
        toolBar.add(undoBtn);
        toolBar.add(redoBtn);
        toolBar.addSeparator();
        
        // Validation and preview
        JButton validateBtn = new JButton("Validate");
        validateBtn.setToolTipText("Validate current configuration (F9)");
        validateBtn.addActionListener(e -> validateWindow());
        
        JButton previewBtn = new JButton("Preview");
        previewBtn.setToolTipText("Preview generated JSON (F5)");
        previewBtn.addActionListener(e -> previewJson());
        
        toolBar.add(validateBtn);
        toolBar.add(previewBtn);
    }
    
    private void createPanels() {
        // Left panel - Component Palette (placeholder)
        palettePanel = new JPanel(new BorderLayout()); 
        palettePanel.setPreferredSize(new Dimension(280, 600));
        palettePanel.setMinimumSize(new Dimension(250, 400));
        
        JLabel paletteLabel = new JLabel("Loading components...", SwingConstants.CENTER);
        paletteLabel.setForeground(Color.GRAY);
        palettePanel.add(paletteLabel, BorderLayout.CENTER);
        
        // Center panel - Form Canvas (placeholder)
        canvasPanel = new JPanel(new BorderLayout()); 
        canvasPanel.setPreferredSize(new Dimension(600, 600));
        canvasPanel.setMinimumSize(new Dimension(400, 400));
        canvasPanel.setBackground(Color.WHITE);
        
        JLabel canvasLabel = new JLabel("Drag components here to build your form", SwingConstants.CENTER);
        canvasLabel.setForeground(Color.GRAY);
        canvasPanel.add(canvasLabel, BorderLayout.CENTER);
        
        // Right panel - Properties Inspector (placeholder)
        propertiesPanel = new JPanel(new BorderLayout());
        propertiesPanel.setPreferredSize(new Dimension(350, 600));
        propertiesPanel.setMinimumSize(new Dimension(300, 400));
        
        JLabel propertiesLabel = new JLabel("Select a component to edit properties", SwingConstants.CENTER);
        propertiesLabel.setForeground(Color.GRAY);
        propertiesPanel.add(propertiesLabel, BorderLayout.CENTER);
        
        // Console panel (initially hidden)
        consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBorder(BorderFactory.createTitledBorder("Console"));
        consolePanel.setPreferredSize(new Dimension(0, 150));
        consolePanel.setVisible(false);
        
        JTextArea consoleText = new JTextArea();
        consoleText.setEditable(false);
        consoleText.setBackground(Color.BLACK);
        consoleText.setForeground(Color.GREEN);
        consoleText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane consoleScroll = new JScrollPane(consoleText);
        consolePanel.add(consoleScroll, BorderLayout.CENTER);
    }
    
    private void createStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setPreferredSize(new Dimension(0, 25));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        fileStatusLabel = new JLabel("No file");
        fileStatusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        validationStatusLabel = new JLabel("Valid");
        validationStatusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        validationStatusLabel.setForeground(Color.GREEN.darker());
        
        rightStatusPanel.add(fileStatusLabel);
        rightStatusPanel.add(new JSeparator(SwingConstants.VERTICAL));
        rightStatusPanel.add(validationStatusLabel);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(rightStatusPanel, BorderLayout.EAST);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Add toolbar
        add(toolBar, BorderLayout.NORTH);
        
        // Create split panes for resizable layout
        rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvasPanel, propertiesPanel);
        rightSplitPane.setResizeWeight(0.8);
        rightSplitPane.setDividerSize(6);
        
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, palettePanel, rightSplitPane);
        mainSplitPane.setResizeWeight(0.0);
        mainSplitPane.setDividerSize(6);
        
        // Create vertical split for console (initially console not visible)
        verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainSplitPane, consolePanel);
        verticalSplitPane.setResizeWeight(1.0);
        verticalSplitPane.setDividerSize(6);
        
        add(verticalSplitPane, BorderLayout.CENTER);
        
        // Add status bar
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void loadConfiguration() {
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            
            // Load window size and position
            int width = configManager.getIntProperty("window.width", 1200);
            int height = configManager.getIntProperty("window.height", 800);
            setSize(width, height);
            
            // Load panel sizes
            int paletteWidth = configManager.getIntProperty("palette.width", 280);
            int propertiesWidth = configManager.getIntProperty("properties.width", 350);
            
            palettePanel.setPreferredSize(new Dimension(paletteWidth, 0));
            propertiesPanel.setPreferredSize(new Dimension(propertiesWidth, 0));
            
            logger.info("Configuration loaded");
        } catch (Exception e) {
            logger.warn("Failed to load configuration: {}", e.getMessage());
        }
    }
    
    private void initializeApplicationController() {
        // Initialize the application controller which provides the real functionality
        try {
            applicationController = new ApplicationController(this);
            
            // Replace placeholder panels with real components
            replaceComponentPanels();
            
            logger.info("Application controller initialized");
        } catch (Exception e) {
            logger.error("Failed to initialize application controller", e);
            updateStatus("Failed to initialize application controller");
        }
    }
    
    private void replaceComponentPanels() {
        if (applicationController != null) {
            try {
                // Remove placeholder content and add real components
                palettePanel.removeAll();
                palettePanel.add(applicationController.getComponentPalette(), BorderLayout.CENTER);
                
                canvasPanel.removeAll();
                canvasPanel.add(applicationController.getFormCanvas(), BorderLayout.CENTER);
                
                propertiesPanel.removeAll();
                propertiesPanel.add(applicationController.getPropertyInspector(), BorderLayout.CENTER);
                
                // Refresh the UI
                revalidate();
                repaint();
                
                logger.debug("Replaced placeholder panels with real components");
            } catch (Exception e) {
                logger.error("Failed to replace component panels", e);
                updateStatus("Failed to load UI components");
            }
        }
    }
    
    // Public methods for ApplicationController to access
    public void setPalettePanel(JPanel newPalettePanel) {
        if (newPalettePanel != null) {
            mainSplitPane.setLeftComponent(newPalettePanel);
            mainSplitPane.setDividerLocation(280);
        }
    }
    
    public void setCanvasPanel(JPanel newCanvasPanel) {
        if (newCanvasPanel != null) {
            rightSplitPane.setLeftComponent(newCanvasPanel);
        }
    }
    
    public void setPropertiesPanel(JPanel newPropertiesPanel) {
        if (newPropertiesPanel != null) {
            rightSplitPane.setRightComponent(newPropertiesPanel);
            rightSplitPane.setDividerLocation(-350);
        }
    }
    
    // Public methods for status updates - FIXED VISIBILITY
    public void updateStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
        logger.debug("Status: {}", status);
    }
    
    public void updateFileStatus(String fileStatus) {
        if (fileStatusLabel != null) {
            fileStatusLabel.setText(fileStatus);
        }
    }
    
    public void updateValidationStatus(boolean isValid, String message) {
        if (validationStatusLabel != null) {
            validationStatusLabel.setText(message);
            validationStatusLabel.setForeground(isValid ? Color.GREEN.darker() : Color.RED.darker());
        }
    }
    
    // Menu and toolbar action handlers - ALL PUBLIC TO AVOID VISIBILITY ISSUES
    public void newWindow() {
        if (applicationController != null) {
            applicationController.newWindow();
        } else {
            updateStatus("New window - ApplicationController not ready");
        }
    }
    
    public void openFile() {
        if (applicationController != null) {
            applicationController.openFile();
        } else {
            updateStatus("Open file - ApplicationController not ready");
        }
    }
    
    public void saveFile() {
        if (applicationController != null) {
            applicationController.saveFile();
        } else {
            updateStatus("Save file - ApplicationController not ready");
        }
    }
    
    public void saveAsFile() {
        if (applicationController != null) {
            applicationController.saveAsFile();
        } else {
            updateStatus("Save as file - ApplicationController not ready");
        }
    }
    
    public void undo() {
        updateStatus("Undo - Not implemented yet");
        // TODO: Implement undo functionality
    }
    
    public void redo() {
        updateStatus("Redo - Not implemented yet");
        // TODO: Implement redo functionality
    }
    
    public void validateWindow() {
        if (applicationController != null) {
            applicationController.validateJson();
        } else {
            updateStatus("Validate - ApplicationController not ready");
        }
    }
    
    public void previewJson() {
        if (applicationController != null) {
            applicationController.previewForm();
        } else {
            updateStatus("Preview - ApplicationController not ready");
        }
    }
    
    public void showFindDialog() {
        updateStatus("Find - Not implemented yet");
        // TODO: Implement find functionality
    }
    
    public void showOptionsDialog() {
        updateStatus("Options - Not implemented yet");
        // TODO: Implement options dialog
    }
    
    public void showHelp() {
        updateStatus("Help - Not implemented yet");
        // TODO: Implement help system
    }
    
    public void showAbout() {
        JOptionPane.showMessageDialog(this,
            "JSONFormMaker - ADUI JSON Craft Studio\n" +
            "Version 1.1.0\n\n" +
            "A powerful tool for creating ADUI JSON configurations\n" +
            "with drag-and-drop interface design.\n\n" +
            "Built with Java Swing and modern design patterns.",
            "About JSONFormMaker",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void exitApplication() {
        // Check for unsaved changes
        if (applicationController != null) {
            // TODO: Check for unsaved changes
            // if (applicationController.hasUnsavedChanges()) {
            //     int result = JOptionPane.showConfirmDialog(this,
            //         "You have unsaved changes. Exit anyway?",
            //         "Unsaved Changes",
            //         JOptionPane.YES_NO_OPTION);
            //     if (result != JOptionPane.YES_OPTION) {
            //         return;
            //     }
            // }
        }
        
        // Save configuration
        try {
            ConfigManager configManager = ConfigManager.getInstance();
            
            // Save window state
            configManager.setIntProperty("window.width", getWidth());
            configManager.setIntProperty("window.height", getHeight());
            configManager.setIntProperty("palette.width", palettePanel.getWidth());
            configManager.setIntProperty("properties.width", propertiesPanel.getWidth());
            
            configManager.saveConfiguration();
            logger.info("Configuration saved");
        } catch (Exception e) {
            logger.warn("Failed to save configuration: {}", e.getMessage());
        }
        
        logger.info("Application exiting");
        System.exit(0);
    }
    
    // Panel toggle methods - FIXED VISIBILITY
    public void togglePalette(boolean visible) {
        palettePanel.setVisible(visible);
        mainSplitPane.resetToPreferredSizes();
        revalidate();
    }
    
    public void toggleProperties(boolean visible) {
        propertiesPanel.setVisible(visible);
        rightSplitPane.resetToPreferredSizes();
        revalidate();
    }
    
    public void toggleConsole(boolean visible) {
        consolePanel.setVisible(visible);
        verticalSplitPane.resetToPreferredSizes();
        revalidate();
    }
    
    // Getter for ApplicationController
    public ApplicationController getApplicationController() {
        return applicationController;
    }
}