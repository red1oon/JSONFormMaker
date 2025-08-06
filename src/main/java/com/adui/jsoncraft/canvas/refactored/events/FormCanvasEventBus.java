package com.adui.jsoncraft.canvas.refactored.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event bus for FormCanvas using Observer pattern
 * Provides type-safe, decoupled communication between components
 * 
 * @version 1.0
 * @namespace com.adui.jsoncraft.canvas.refactored.events.FormCanvasEventBus
 */
public class FormCanvasEventBus {
    private static final Logger logger = LoggerFactory.getLogger(FormCanvasEventBus.class);
    
    // Singleton instance
    private static FormCanvasEventBus instance;
    
    // Type-safe listener storage
    private final Map<Class<?>, List<Object>> listeners;
    
    private FormCanvasEventBus() {
        this.listeners = new ConcurrentHashMap<>();
        logger.debug("FormCanvasEventBus initialized");
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized FormCanvasEventBus getInstance() {
        if (instance == null) {
            instance = new FormCanvasEventBus();
        }
        return instance;
    }
    
    /**
     * Register a listener for specific event types
     */
    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> eventType, EventListener<T> listener) {
        if (eventType == null || listener == null) {
            logger.warn("Cannot register null event type or listener");
            return;
        }
        
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.debug("Registered listener for event type: {}", eventType.getSimpleName());
    }
    
    /**
     * Unregister a listener
     */
    public <T> void unregister(Class<T> eventType, EventListener<T> listener) {
        if (eventType == null || listener == null) {
            return;
        }
        
        List<Object> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listeners.remove(eventType);
            }
            logger.debug("Unregistered listener for event type: {}", eventType.getSimpleName());
        }
    }
    
    /**
     * Fire an event to all registered listeners
     */
    @SuppressWarnings("unchecked")
    public <T> void fire(T event) {
        if (event == null) {
            logger.warn("Cannot fire null event");
            return;
        }
        
        Class<?> eventType = event.getClass();
        List<Object> eventListeners = listeners.get(eventType);
        
        if (eventListeners != null && !eventListeners.isEmpty()) {
            logger.debug("Firing event: {} to {} listeners", 
                eventType.getSimpleName(), eventListeners.size());
            
            // Fire to all listeners (copy to avoid concurrent modification)
            List<Object> safeListeners = new CopyOnWriteArrayList<>(eventListeners);
            for (Object listener : safeListeners) {
                try {
                    ((EventListener<T>) listener).onEvent(event);
                } catch (Exception e) {
                    logger.error("Error in event listener for {}: {}", 
                        eventType.getSimpleName(), e.getMessage(), e);
                }
            }
        } else {
            logger.debug("No listeners registered for event type: {}", eventType.getSimpleName());
        }
    }
    
    /**
     * Get listener count for an event type
     */
    public int getListenerCount(Class<?> eventType) {
        List<Object> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    /**
     * Clear all listeners (for testing/cleanup)
     */
    public void clearAllListeners() {
        listeners.clear();
        logger.debug("Cleared all event listeners");
    }
    
    /**
     * Clear listeners for specific event type
     */
    public void clearListeners(Class<?> eventType) {
        listeners.remove(eventType);
        logger.debug("Cleared listeners for event type: {}", eventType.getSimpleName());
    }
    
    /**
     * Generic event listener interface
     */
    @FunctionalInterface
    public interface EventListener<T> {
        void onEvent(T event);
    }
}
