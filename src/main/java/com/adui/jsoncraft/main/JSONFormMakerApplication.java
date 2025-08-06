package com.adui.jsoncraft.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.utils.ConfigManager;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * JSONFormMaker - ADUI JSON Craft Studio
 * Main Application Entry Point
 * 
 * Visual JSON configuration builder for ADUI mobile forms.
 * Provides drag-and-drop interface for creating form definitions
 * without manual JSON editing.
 * 
 * @version 1.0.0
 * @author ADUI Development Team
 */
public class JSONFormMakerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(JSONFormMakerApplication.class);
    
    // Application Constants
    public static final String APP_NAME = "JSONFormMaker";
    public static final String APP_TITLE = "ADUI JSON Craft Studio";
    public static final String APP_VERSION = "1.0.0";
    
    // UI Constants
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int MIN_WIDTH = 1000;
    private static final int MIN_HEIGHT = 600;
    
    /**
     * Application entry point
     */
    public static void main(String[] args) {
        logger.info("Starting {} v{}", APP_TITLE, APP_VERSION);
        
        // Set system properties for better UI experience
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", APP_TITLE);
        System.setProperty("sun.awt.noerasebackground", "true");
        
        // Initialize application on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                initializeApplication(args);
            } catch (Exception e) {
                logger.error("Failed to initialize application", e);
                showErrorDialog("Failed to start application", e);
                System.exit(1);
            }
        });
    }
    
    /**
     * Initialize the application
     */
    private static void initializeApplication(String[] args) {
        logger.info("Initializing application components...");
        
        // Set Look and Feel
        setLookAndFeel();
        
        // Initialize configuration
        ConfigManager.getInstance().initialize();
        
        // Ensure required directories exist
        createRequiredDirectories();
        
        // Create and show main window
        MainWindow mainWindow = new MainWindow();
        setupMainWindow(mainWindow);
        
        // Handle command line arguments
        handleCommandLineArgs(args, mainWindow);
        
        // Show the application
        mainWindow.setVisible(true);
        
        logger.info("Application initialized successfully");
    }
    
    /**
     * Set the application Look and Feel
     */
    private static void setLookAndFeel() {
        try {
            // Use FlatLaf for modern appearance
            FlatLightLaf.setup();
            
            // Set additional UI properties
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            
            logger.debug("Look and Feel set to FlatLaf");
            
        } catch (Exception e) {
            logger.warn("Failed to set FlatLaf, falling back to system default", e);
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception ex) {
                logger.warn("Failed to set system Look and Feel", ex);
            }
        }
    }
    
    /**
     * Create required application directories
     */
    private static void createRequiredDirectories() {
        try {
            // Create logs directory
            Path logsDir = Paths.get("logs");
            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
                logger.debug("Created logs directory: {}", logsDir.toAbsolutePath());
            }
            
            // Create default JSON directory in user's Documents
            String userHome = System.getProperty("user.home");
            Path documentsDir = Paths.get(userHome, "Documents");
            Path jsonDir = documentsDir.resolve("JSON");
            
            if (!Files.exists(jsonDir)) {
                Files.createDirectories(jsonDir);
                logger.info("Created default JSON directory: {}", jsonDir.toAbsolutePath());
                
                // Set as default location in config
                ConfigManager.getInstance().setProperty("file.defaultLocation", jsonDir.toString());
            }
            
            // Create database directory
            Path dbDir = Paths.get("data");
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir);
                logger.debug("Created database directory: {}", dbDir.toAbsolutePath());
            }
            
        } catch (Exception e) {
            logger.warn("Failed to create some required directories", e);
        }
    }
    
    /**
     * Setup the main window properties
     */
    private static void setupMainWindow(MainWindow mainWindow) {
        // Set window properties
        mainWindow.setTitle(APP_TITLE + " v" + APP_VERSION);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set window size and constraints
        mainWindow.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        mainWindow.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        
        // Center on screen
        mainWindow.setLocationRelativeTo(null);
        
        // Set application icon
        setApplicationIcon(mainWindow);
        
        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application shutting down...");
            ConfigManager.getInstance().saveConfiguration();
        }));
    }
    
    /**
     * Set application icon
     */
    private static void setApplicationIcon(JFrame frame) {
        try {
            // Try to load application icon
            // For now, use a default icon - can be replaced with actual icon file
            ImageIcon icon = createDefaultIcon();
            frame.setIconImage(icon.getImage());
            
        } catch (Exception e) {
            logger.warn("Failed to set application icon", e);
        }
    }
    
    /**
     * Create a default application icon
     */
    private static ImageIcon createDefaultIcon() {
        // Create a simple default icon
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        g2d.setColor(new Color(33, 150, 243)); // Material Blue
        g2d.fillRoundRect(2, 2, size - 4, size - 4, 8, 8);
        
        // Draw "J" for JSONFormMaker
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "J";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    /**
     * Handle command line arguments
     */
    private static void handleCommandLineArgs(String[] args, MainWindow mainWindow) {
        if (args.length > 0) {
            String filePath = args[0];
            File file = new File(filePath);
            
            if (file.exists() && file.isFile()) {
                logger.info("Opening file from command line: {}", filePath);
                // TODO: Implement file opening in MainWindow
                // mainWindow.openFile(file);
            } else {
                logger.warn("Invalid file path provided: {}", filePath);
            }
        }
    }
    
    /**
     * Show error dialog
     */
    private static void showErrorDialog(String message, Throwable throwable) {
        String errorMessage = message;
        if (throwable != null) {
            errorMessage += "\n\nError: " + throwable.getMessage();
        }
        
        JOptionPane.showMessageDialog(
            null,
            errorMessage,
            APP_TITLE + " - Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Get application information
     */
    public static String getApplicationInfo() {
        StringBuilder info = new StringBuilder();
        info.append(APP_TITLE).append(" v").append(APP_VERSION).append("\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        info.append("OS: ").append(System.getProperty("os.name")).append(" ");
        info.append(System.getProperty("os.version")).append("\n");
        info.append("Architecture: ").append(System.getProperty("os.arch"));
        return info.toString();
    }
}