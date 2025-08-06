package com.adui.jsoncraft.model;

/**
 * Data model for field validation rules
 * Defines constraints and validation logic for form fields
 */
public class ValidationRules {
    private boolean required;
    private String requiredWhen;
    private Integer maxLength;
    private Integer minLength;
    private Number min;
    private Number max;
    private String minDate;
    private String maxDate;
    private String minDateTime;
    private String maxDateTime;
    private String pattern;
    private String patternMessage;
    private Integer decimalPlaces;
    private Integer minSelections;
    private Integer maxSelections;
    private Integer minFiles;
    private Integer maxFiles;
    private Long maxFileSize;
    private Long maxTotalSize;
    private String[] allowedFileTypes;
    private Integer minPhotos;
    private Integer maxPhotos;
    private Integer minCodes;
    private Integer maxCodes;
    private Integer minRows;
    private Integer maxRows;
    private Integer minInstances;
    private Integer maxInstances;
    
    public ValidationRules() {
        this.required = false;
    }
    
    // Getters and Setters
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    
    public String getRequiredWhen() { return requiredWhen; }
    public void setRequiredWhen(String requiredWhen) { this.requiredWhen = requiredWhen; }
    
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
    
    public Integer getMinLength() { return minLength; }
    public void setMinLength(Integer minLength) { this.minLength = minLength; }
    
    public Number getMin() { return min; }
    public void setMin(Number min) { this.min = min; }
    
    public Number getMax() { return max; }
    public void setMax(Number max) { this.max = max; }
    
    public String getMinDate() { return minDate; }
    public void setMinDate(String minDate) { this.minDate = minDate; }
    
    public String getMaxDate() { return maxDate; }
    public void setMaxDate(String maxDate) { this.maxDate = maxDate; }
    
    public String getMinDateTime() { return minDateTime; }
    public void setMinDateTime(String minDateTime) { this.minDateTime = minDateTime; }
    
    public String getMaxDateTime() { return maxDateTime; }
    public void setMaxDateTime(String maxDateTime) { this.maxDateTime = maxDateTime; }
    
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    
    public String getPatternMessage() { return patternMessage; }
    public void setPatternMessage(String patternMessage) { this.patternMessage = patternMessage; }
    
    public Integer getDecimalPlaces() { return decimalPlaces; }
    public void setDecimalPlaces(Integer decimalPlaces) { this.decimalPlaces = decimalPlaces; }
    
    public Integer getMinSelections() { return minSelections; }
    public void setMinSelections(Integer minSelections) { this.minSelections = minSelections; }
    
    public Integer getMaxSelections() { return maxSelections; }
    public void setMaxSelections(Integer maxSelections) { this.maxSelections = maxSelections; }
    
    public Integer getMinFiles() { return minFiles; }
    public void setMinFiles(Integer minFiles) { this.minFiles = minFiles; }
    
    public Integer getMaxFiles() { return maxFiles; }
    public void setMaxFiles(Integer maxFiles) { this.maxFiles = maxFiles; }
    
    public Long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(Long maxFileSize) { this.maxFileSize = maxFileSize; }
    
    public Long getMaxTotalSize() { return maxTotalSize; }
    public void setMaxTotalSize(Long maxTotalSize) { this.maxTotalSize = maxTotalSize; }
    
    public String[] getAllowedFileTypes() { return allowedFileTypes; }
    public void setAllowedFileTypes(String[] allowedFileTypes) { this.allowedFileTypes = allowedFileTypes; }
    
    public Integer getMinPhotos() { return minPhotos; }
    public void setMinPhotos(Integer minPhotos) { this.minPhotos = minPhotos; }
    
    public Integer getMaxPhotos() { return maxPhotos; }
    public void setMaxPhotos(Integer maxPhotos) { this.maxPhotos = maxPhotos; }
    
    public Integer getMinCodes() { return minCodes; }
    public void setMinCodes(Integer minCodes) { this.minCodes = minCodes; }
    
    public Integer getMaxCodes() { return maxCodes; }
    public void setMaxCodes(Integer maxCodes) { this.maxCodes = maxCodes; }
    
    public Integer getMinRows() { return minRows; }
    public void setMinRows(Integer minRows) { this.minRows = minRows; }
    
    public Integer getMaxRows() { return maxRows; }
    public void setMaxRows(Integer maxRows) { this.maxRows = maxRows; }
    
    public Integer getMinInstances() { return minInstances; }
    public void setMinInstances(Integer minInstances) { this.minInstances = minInstances; }
    
    public Integer getMaxInstances() { return maxInstances; }
    public void setMaxInstances(Integer maxInstances) { this.maxInstances = maxInstances; }
    
    // Utility Methods
    public boolean hasLengthConstraints() {
        return maxLength != null || minLength != null;
    }
    
    public boolean hasNumericConstraints() {
        return min != null || max != null;
    }
    
    public boolean hasDateConstraints() {
        return minDate != null || maxDate != null || minDateTime != null || maxDateTime != null;
    }
    
    public boolean hasFileConstraints() {
        return minFiles != null || maxFiles != null || maxFileSize != null || 
               maxTotalSize != null || allowedFileTypes != null;
    }
    
    public ValidationRules copy() {
        ValidationRules copy = new ValidationRules();
        copy.required = this.required;
        copy.requiredWhen = this.requiredWhen;
        copy.maxLength = this.maxLength;
        copy.minLength = this.minLength;
        copy.min = this.min;
        copy.max = this.max;
        copy.minDate = this.minDate;
        copy.maxDate = this.maxDate;
        copy.minDateTime = this.minDateTime;
        copy.maxDateTime = this.maxDateTime;
        copy.pattern = this.pattern;
        copy.patternMessage = this.patternMessage;
        copy.decimalPlaces = this.decimalPlaces;
        copy.minSelections = this.minSelections;
        copy.maxSelections = this.maxSelections;
        copy.minFiles = this.minFiles;
        copy.maxFiles = this.maxFiles;
        copy.maxFileSize = this.maxFileSize;
        copy.maxTotalSize = this.maxTotalSize;
        if (this.allowedFileTypes != null) {
            copy.allowedFileTypes = this.allowedFileTypes.clone();
        }
        copy.minPhotos = this.minPhotos;
        copy.maxPhotos = this.maxPhotos;
        copy.minCodes = this.minCodes;
        copy.maxCodes = this.maxCodes;
        copy.minRows = this.minRows;
        copy.maxRows = this.maxRows;
        copy.minInstances = this.minInstances;
        copy.maxInstances = this.maxInstances;
        return copy;
    }
}
