package com.adui.jsoncraft.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.model.ComponentType;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.properties.editors.ComponentDataEditor;
import com.adui.jsoncraft.properties.editors.DefaultComponentEditor;
import com.adui.jsoncraft.properties.editors.MatrixFieldEditor;
import com.adui.jsoncraft.properties.editors.MultiPhotoFieldEditor;
import com.adui.jsoncraft.properties.editors.QRChecklistFieldEditor;
import com.adui.jsoncraft.properties.editors.QRCollectorFieldEditor;
import com.adui.jsoncraft.properties.editors.SelectFieldEditor;
import com.adui.jsoncraft.properties.editors.TaskListFieldEditor;
import com.adui.jsoncraft.properties.editors.WeatherConditionsFieldEditor;

/**
 * Factory for creating component-specific property editors
 * Uses strategy pattern to provide appropriate editor for each component type
 */
public class PropertyEditorFactory {
    private static final Logger logger = LoggerFactory.getLogger(PropertyEditorFactory.class);
    
    /**
     * Create appropriate editor for component type
     */
    public static ComponentDataEditor createEditor(ComponentType type, FieldDefinition field) {
        if (type == null || field == null) {
            return new DefaultComponentEditor(field);
        }
        
        try {
            switch (type) {
                case SELECT_FIELD:
                case MULTI_SELECT_FIELD:
                case RADIO_BUTTON_GROUP_FIELD:
                case CHECKBOX_GROUP_FIELD:
                    return new SelectFieldEditor(field);
                    
                case QR_CHECKLIST_FIELD:
                case FLIPPABLE_QR_CHECKLIST_FIELD:
                    return new QRChecklistFieldEditor(field);
                    
                case TASK_LIST_FIELD:
                    return new TaskListFieldEditor(field);
                    
                case MULTI_PHOTO_FIELD:
                    return new MultiPhotoFieldEditor(field);
                    
                case QR_COLLECTOR_FIELD:
                    return new QRCollectorFieldEditor(field);
                    
                case MATRIX_FIELD:
                case SURVEY_GRID_FIELD:
                    return new MatrixFieldEditor(field);
                    
                case WEATHER_CONDITIONS_FIELD:
                    return new WeatherConditionsFieldEditor(field);
                    
                default:
                    return new DefaultComponentEditor(field);
            }
        } catch (Exception e) {
            logger.error("Failed to create editor for component type: " + type, e);
            return new DefaultComponentEditor(field);
        }
    }
    
    /**
     * Check if component type has specific editor
     */
    public static boolean hasSpecificEditor(ComponentType type) {
        switch (type) {
            case SELECT_FIELD:
            case MULTI_SELECT_FIELD:
            case RADIO_BUTTON_GROUP_FIELD:
            case CHECKBOX_GROUP_FIELD:
            case QR_CHECKLIST_FIELD:
            case FLIPPABLE_QR_CHECKLIST_FIELD:
            case TASK_LIST_FIELD:
            case MULTI_PHOTO_FIELD:
            case QR_COLLECTOR_FIELD:
            case MATRIX_FIELD:
            case SURVEY_GRID_FIELD:
            case WEATHER_CONDITIONS_FIELD:
                return true;
            default:
                return false;
        }
    }
}
