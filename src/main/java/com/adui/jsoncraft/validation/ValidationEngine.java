package com.adui.jsoncraft.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adui.jsoncraft.model.ComponentType;
import com.adui.jsoncraft.model.FieldDefinition;
import com.adui.jsoncraft.model.ReferenceData;
import com.adui.jsoncraft.model.TabDefinition;
import com.adui.jsoncraft.model.ValidationRules;
import com.adui.jsoncraft.model.WindowDefinition;

/**
 * Validation Engine for JSONFormMaker
 * Validates window definitions before JSON generation
 */
public class ValidationEngine {
    private static final Logger logger = LoggerFactory.getLogger(ValidationEngine.class);
    
    // Validation patterns
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\w\\s\\-\\.\\(\\)]+$");
    
    /**
     * Validate complete window definition
     */
    public ValidationResult validateWindow(WindowDefinition window) {
        List<ValidationError> errors = new ArrayList<>();
        List<ValidationWarning> warnings = new ArrayList<>();
        
        if (window == null) {
            errors.add(new ValidationError("WINDOW_NULL", "Window definition cannot be null"));
            return new ValidationResult(errors, warnings);
        }
        
        // Validate window properties
        validateWindowProperties(window, errors, warnings);
        
        // Validate tabs
        validateTabs(window.getTabs(), errors, warnings);
        
        // Validate overall structure
        validateOverallStructure(window, errors, warnings);
        
        return new ValidationResult(errors, warnings);
    }
    
    /**
     * Validate window-level properties
     */
    private void validateWindowProperties(WindowDefinition window, List<ValidationError> errors, List<ValidationWarning> warnings) {
        // Window ID validation
        if (window.getWindowId() == null || window.getWindowId().trim().isEmpty()) {
            errors.add(new ValidationError("WINDOW_ID_EMPTY", "Window ID is required"));
        } else if (!ID_PATTERN.matcher(window.getWindowId()).matches()) {
            errors.add(new ValidationError("WINDOW_ID_INVALID", 
                "Window ID must start with uppercase letter and contain only uppercase letters, numbers, and underscores"));
        }
        
        // Window name validation
        if (window.getName() == null || window.getName().trim().isEmpty()) {
            errors.add(new ValidationError("WINDOW_NAME_EMPTY", "Window name is required"));
        } else if (window.getName().length() > 100) {
            warnings.add(new ValidationWarning("WINDOW_NAME_LONG", "Window name is quite long (>100 characters)"));
        }
        
        // Window type validation
        if (window.getWindowType() == null) {
            warnings.add(new ValidationWarning("WINDOW_TYPE_NULL", "Window type not specified, defaulting to 'Transaction'"));
        } else if (!window.getWindowType().equals("Transaction") && !window.getWindowType().equals("Query")) {
            warnings.add(new ValidationWarning("WINDOW_TYPE_UNKNOWN", "Unknown window type: " + window.getWindowType()));
        }
        
        // Description validation
        if (window.getDescription() != null && window.getDescription().length() > 500) {
            warnings.add(new ValidationWarning("WINDOW_DESC_LONG", "Window description is quite long (>500 characters)"));
        }
    }
    
    /**
     * Validate tabs
     */
    private void validateTabs(List<TabDefinition> tabs, List<ValidationError> errors, List<ValidationWarning> warnings) {
        if (tabs == null || tabs.isEmpty()) {
            errors.add(new ValidationError("NO_TABS", "Window must have at least one tab"));
            return;
        }
        
        Set<String> tabIds = new HashSet<>();
        Set<Integer> sequences = new HashSet<>();
        
        for (int i = 0; i < tabs.size(); i++) {
            TabDefinition tab = tabs.get(i);
            String tabContext = "Tab " + (i + 1);
            
            validateTabProperties(tab, tabContext, errors, warnings);
            
            // Check for duplicate tab IDs
            if (tab.getTabId() != null) {
                if (tabIds.contains(tab.getTabId())) {
                    errors.add(new ValidationError("DUPLICATE_TAB_ID", 
                        tabContext + ": Duplicate tab ID '" + tab.getTabId() + "'"));
                } else {
                    tabIds.add(tab.getTabId());
                }
            }
            
            // Check for duplicate sequences
            if (sequences.contains(tab.getSequence())) {
                warnings.add(new ValidationWarning("DUPLICATE_TAB_SEQUENCE", 
                    tabContext + ": Duplicate tab sequence " + tab.getSequence()));
            } else {
                sequences.add(tab.getSequence());
            }
            
            // Validate fields within tab
            validateFields(tab.getFields(), tabContext, errors, warnings);
        }
    }
    
    /**
     * Validate individual tab properties
     */
    private void validateTabProperties(TabDefinition tab, String context, List<ValidationError> errors, List<ValidationWarning> warnings) {
        // Tab ID validation
        if (tab.getTabId() == null || tab.getTabId().trim().isEmpty()) {
            errors.add(new ValidationError("TAB_ID_EMPTY", context + ": Tab ID is required"));
        } else if (!ID_PATTERN.matcher(tab.getTabId()).matches()) {
            errors.add(new ValidationError("TAB_ID_INVALID", 
                context + ": Tab ID must follow naming convention (uppercase, letters/numbers/underscores)"));
        }
        
        // Tab name validation
        if (tab.getName() == null || tab.getName().trim().isEmpty()) {
            errors.add(new ValidationError("TAB_NAME_EMPTY", context + ": Tab name is required"));
        }
        
        // Sequence validation
        if (tab.getSequence() <= 0) {
            warnings.add(new ValidationWarning("TAB_SEQUENCE_INVALID", 
                context + ": Tab sequence should be positive (typically 10, 20, 30...)"));
        }
    }
    
    /**
     * Validate fields within a tab
     */
    private void validateFields(List<FieldDefinition> fields, String tabContext, List<ValidationError> errors, List<ValidationWarning> warnings) {
        if (fields == null || fields.isEmpty()) {
            warnings.add(new ValidationWarning("NO_FIELDS", tabContext + ": Tab has no fields"));
            return;
        }
        
        Set<String> fieldIds = new HashSet<>();
        Set<Integer> sequences = new HashSet<>();
        
        for (int i = 0; i < fields.size(); i++) {
            FieldDefinition field = fields.get(i);
            String fieldContext = tabContext + ", Field " + (i + 1);
            
            validateFieldProperties(field, fieldContext, errors, warnings);
            
            // Check for duplicate field IDs
            if (field.getFieldId() != null) {
                if (fieldIds.contains(field.getFieldId())) {
                    errors.add(new ValidationError("DUPLICATE_FIELD_ID", 
                        fieldContext + ": Duplicate field ID '" + field.getFieldId() + "'"));
                } else {
                    fieldIds.add(field.getFieldId());
                }
            }
            
            // Check for duplicate sequences
            if (sequences.contains(field.getSequence())) {
                warnings.add(new ValidationWarning("DUPLICATE_FIELD_SEQUENCE", 
                    fieldContext + ": Duplicate field sequence " + field.getSequence()));
            } else {
                sequences.add(field.getSequence());
            }
            
            // Validate component-specific requirements
            validateComponentSpecific(field, fieldContext, errors, warnings);
        }
    }
    
    /**
     * Validate individual field properties
     */
    private void validateFieldProperties(FieldDefinition field, String context, List<ValidationError> errors, List<ValidationWarning> warnings) {
        // Field ID validation
        if (field.getFieldId() == null || field.getFieldId().trim().isEmpty()) {
            errors.add(new ValidationError("FIELD_ID_EMPTY", context + ": Field ID is required"));
        } else if (!ID_PATTERN.matcher(field.getFieldId()).matches()) {
            errors.add(new ValidationError("FIELD_ID_INVALID", 
                context + ": Field ID must follow naming convention"));
        }
        
        // Field name validation
        if (field.getName() == null || field.getName().trim().isEmpty()) {
            errors.add(new ValidationError("FIELD_NAME_EMPTY", context + ": Field name is required"));
        }
        
        // Component type validation
        if (field.getComponentType() == null) {
            errors.add(new ValidationError("COMPONENT_TYPE_NULL", context + ": Component type is required"));
        }
        
        // Sequence validation
        if (field.getSequence() <= 0) {
            warnings.add(new ValidationWarning("FIELD_SEQUENCE_INVALID", 
                context + ": Field sequence should be positive"));
        }
        
        // Validation rules
        if (field.getValidation() != null) {
            validateValidationRules(field.getValidation(), context, errors, warnings);
        }
    }
    
    /**
     * Validate validation rules
     */
    private void validateValidationRules(ValidationRules validation, String context, List<ValidationError> errors, List<ValidationWarning> warnings) {
        // Pattern validation
        if (validation.getPattern() != null) {
            try {
                Pattern.compile(validation.getPattern());
            } catch (PatternSyntaxException e) {
                errors.add(new ValidationError("INVALID_REGEX", 
                    context + ": Invalid regex pattern '" + validation.getPattern() + "': " + e.getMessage()));
            }
        }
        
        // Numeric constraints
        if (validation.getMin() != null && validation.getMax() != null) {
            if (validation.getMin().doubleValue() > validation.getMax().doubleValue()) {
                errors.add(new ValidationError("INVALID_RANGE", 
                    context + ": Minimum value cannot be greater than maximum value"));
            }
        }
        
        // Length constraints
        if (validation.getMinLength() != null && validation.getMaxLength() != null) {
            if (validation.getMinLength() > validation.getMaxLength()) {
                errors.add(new ValidationError("INVALID_LENGTH_RANGE", 
                    context + ": Minimum length cannot be greater than maximum length"));
            }
        }
        
        // Selection constraints
        if (validation.getMinSelections() != null && validation.getMaxSelections() != null) {
            if (validation.getMinSelections() > validation.getMaxSelections()) {
                errors.add(new ValidationError("INVALID_SELECTION_RANGE", 
                    context + ": Minimum selections cannot be greater than maximum selections"));
            }
        }
    }
    
    /**
     * Validate component-specific requirements
     */
    private void validateComponentSpecific(FieldDefinition field, String context, List<ValidationError> errors, List<ValidationWarning> warnings) {
        ComponentType type = field.getComponentType();
        if (type == null) return;
        
        switch (type) {
            case SELECT_FIELD:
            case MULTI_SELECT_FIELD:
            case RADIO_BUTTON_GROUP_FIELD:
            case CHECKBOX_GROUP_FIELD:
                validateSelectionField(field, context, errors, warnings);
                break;
                
            case QR_CHECKLIST_FIELD:
            case FLIPPABLE_QR_CHECKLIST_FIELD:
                validateQRChecklistField(field, context, errors, warnings);
                break;
                
            case TASK_LIST_FIELD:
                validateTaskListField(field, context, errors, warnings);
                break;
                
            default:
                // No specific validation needed
                break;
        }
    }
    
    /**
     * Validate selection fields (dropdowns, radio buttons, etc.)
     */
    private void validateSelectionField(FieldDefinition field, String context, List<ValidationError> errors, List<ValidationWarning> warnings) {
        ReferenceData reference = field.getReference();
        if (reference == null) {
            warnings.add(new ValidationWarning("NO_REFERENCE_DATA", 
                context + ": Selection field should have reference data"));
            return;
        }
        
        if (reference.getValues() == null || reference.getValues().isEmpty()) {
            warnings.add(new ValidationWarning("NO_REFERENCE_VALUES", 
                context + ": Reference data has no values"));
        }
    }
    
    /**
     * Validate QR checklist fields
     */
    private void validateQRChecklistField(FieldDefinition field, String context, List<ValidationError> errors, List<ValidationWarning> warnings) {
        Object itemsData = field.getDataProperty("items");
        if (itemsData == null) {
            warnings.add(new ValidationWarning("NO_CHECKLIST_ITEMS", 
                context + ": QR checklist field should have items"));
        }
    }
    
    /**
     * Validate task list fields
     */
    private void validateTaskListField(FieldDefinition field, String context, List<ValidationError> errors, List<ValidationWarning> warnings) {
        Object tasksData = field.getDataProperty("tasks");
        if (tasksData == null) {
            warnings.add(new ValidationWarning("NO_TASK_DATA", 
                context + ": Task list field should have task data"));
        }
    }
    
    /**
     * Validate overall window structure
     */
    private void validateOverallStructure(WindowDefinition window, List<ValidationError> errors, List<ValidationWarning> warnings) {
        // Check for reasonable number of tabs
        if (window.getTabs() != null && window.getTabs().size() > 10) {
            warnings.add(new ValidationWarning("MANY_TABS", 
                "Window has many tabs (" + window.getTabs().size() + "). Consider organizing content differently."));
        }
        
        // Check for reasonable total field count
        int totalFields = window.getFieldCount();
        if (totalFields > 50) {
            warnings.add(new ValidationWarning("MANY_FIELDS", 
                "Window has many fields (" + totalFields + "). Consider breaking into multiple forms."));
        }
        
        // Check for empty tabs
        if (window.getTabs() != null) {
            for (TabDefinition tab : window.getTabs()) {
                if (tab.getFieldCount() == 0) {
                    warnings.add(new ValidationWarning("EMPTY_TAB", 
                        "Tab '" + tab.getName() + "' has no fields"));
                }
            }
        }
    }
    
    /**
     * Validation result container
     */
    public static class ValidationResult {
        private final List<ValidationError> errors;
        private final List<ValidationWarning> warnings;
        
        public ValidationResult(List<ValidationError> errors, List<ValidationWarning> warnings) {
            this.errors = errors != null ? errors : new ArrayList<>();
            this.warnings = warnings != null ? warnings : new ArrayList<>();
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<ValidationError> getErrors() { return errors; }
        public List<ValidationWarning> getWarnings() { return warnings; }
        
        public String getSummary() {
            if (isValid()) {
                return warnings.isEmpty() ? "Valid" : 
                    "Valid with " + warnings.size() + " warning(s)";
            } else {
                return errors.size() + " error(s)" + 
                    (warnings.isEmpty() ? "" : ", " + warnings.size() + " warning(s)");
            }
        }
    }
    
    /**
     * Validation error
     */
    public static class ValidationError {
        private final String code;
        private final String message;
        
        public ValidationError(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() { return code; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return "[ERROR] " + code + ": " + message;
        }
    }
    
    /**
     * Validation warning
     */
    public static class ValidationWarning {
        private final String code;
        private final String message;
        
        public ValidationWarning(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() { return code; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return "[WARNING] " + code + ": " + message;
        }
    }
}
