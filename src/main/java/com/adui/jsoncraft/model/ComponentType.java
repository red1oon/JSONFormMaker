package com.adui.jsoncraft.model;

/**
 * Enumeration of all ADUI component types
 * Based on ADUI JSON Programming Guide v2.0
 */
public enum ComponentType {
    // Basic Input Components
    TEXT_FIELD("TextField", "String", "Single-line text input", "📝"),
    TEXT_AREA_FIELD("TextAreaField", "Text", "Multi-line text input", "📄"),
    NUMBER_FIELD("NumberField", "Integer", "Numeric input", "🔢"),
    DECIMAL_FIELD("DecimalField", "Decimal", "Decimal number input", "🔢"),
    CURRENCY_FIELD("CurrencyField", "Currency", "Money values", "💰"),
    PERCENTAGE_FIELD("PercentageField", "Percentage", "Percentage values", "📊"),
    YES_NO_FIELD("YesNoField", "YesNo", "Boolean toggle", "✅"),
    EMAIL_FIELD("EmailField", "Email", "Email address", "📧"),
    PHONE_FIELD("PhoneField", "Phone", "Phone number", "📞"),
    URL_FIELD("URLField", "URL", "Web address", "🔗"),
    PASSWORD_FIELD("PasswordField", "Password", "Password input", "🔒"),
    RICH_TEXT_FIELD("RichTextField", "RichText", "Formatted text", "📝"),
    AUTO_COMPLETE_FIELD("AutoCompleteField", "AutoComplete", "Predictive text", "🔍"),
    
    // Date & Time Components
    DATE_FIELD("DateField", "Date", "Date selection", "📅"),
    TIME_FIELD("TimeField", "Time", "Time selection", "⏰"),
    DATE_TIME_FIELD("DateTimeField", "DateTime", "Date and time", "📅"),
    DATE_RANGE_FIELD("DateRangeField", "DateRange", "Date range picker", "📅"),
    TIME_RANGE_FIELD("TimeRangeField", "TimeRange", "Time range picker", "⏰"),
    
    // Measurement & Coordinates
    MEASUREMENT_FIELD("MeasurementField", "Measurement", "Physical measurements", "📏"),
    COORDINATE_FIELD("CoordinateField", "Coordinate", "GPS coordinates", "📍"),
    SLIDER_FIELD("SliderField", "Slider", "Range slider", "🎚️"),
    RATING_FIELD("RatingField", "Rating", "Star rating", "⭐"),
    
    // Selection Components
    SELECT_FIELD("SelectField", "List", "Dropdown selection", "📋"),
    MULTI_SELECT_FIELD("MultiSelectField", "MultiSelect", "Multiple selection", "☑️"),
    RADIO_BUTTON_GROUP_FIELD("RadioButtonGroupField", "RadioGroup", "Radio button group", "🔘"),
    CHECKBOX_GROUP_FIELD("CheckboxGroupField", "CheckboxGroup", "Checkbox group", "☑️"),
    TAG_SELECT_FIELD("TagSelectField", "TagSelect", "Tag selection", "🏷️"),
    TREE_SELECT_FIELD("TreeSelectField", "TreeSelect", "Hierarchical selection", "🌳"),
    
    // Media & Scanning Components
    QR_CODE_FIELD("QRCodeField", "QRCode", "QR code scanner", "📱"),
    CAMERA_FIELD("CameraField", "Camera", "Photo capture", "📷"),
    QR_COLLECTOR_FIELD("QRCollectorField", "QRCollector", "Multiple QR collection", "📱"),
    MULTI_PHOTO_FIELD("MultiPhotoField", "MultiPhoto", "Multiple photos", "📷"),
    FILE_UPLOAD_FIELD("FileUploadField", "FileUpload", "File attachment", "📎"),
    AUDIO_RECORDING_FIELD("AudioRecordingField", "AudioRecording", "Voice recording", "🎤"),
    VIDEO_RECORDING_FIELD("VideoRecordingField", "VideoRecording", "Video recording", "📹"),
    DOCUMENT_SCANNER_FIELD("DocumentScannerField", "DocumentScanner", "Document scanning", "📄"),
    SIGNATURE_FIELD("SignatureField", "Signature", "Digital signature", "✍️"),
    
    // Display & Information Components
    LABEL_FIELD("LabelField", "Label", "Read-only text", "📄"),
    IMAGE_DISPLAY_FIELD("ImageDisplayField", "ImageDisplay", "Image display", "🖼️"),
    QR_DISPLAY_FIELD("QRDisplayField", "QRDisplay", "QR code display", "📱"),
    BARCODE_DISPLAY_FIELD("BarcodeDisplayField", "BarcodeDisplay", "Barcode display", "📱"),
    MAP_DISPLAY_FIELD("MapDisplayField", "MapDisplay", "Map display", "🗺️"),
    HTML_CONTENT_FIELD("HTMLContentField", "HTMLContent", "HTML content", "🌐"),
    
    // Interactive Tools
    CALCULATOR_FIELD("CalculatorField", "Calculator", "Built-in calculator", "🧮"),
    COUNTER_FIELD("CounterField", "Counter", "Counter control", "🔢"),
    TIMER_FIELD("TimerField", "Timer", "Timer functionality", "⏱️"),
    STOPWATCH_FIELD("StopwatchField", "Stopwatch", "Stopwatch", "⏱️"),
    DRAWING_FIELD("DrawingField", "Drawing", "Drawing canvas", "🎨"),
    SKETCH_FIELD("SketchField", "Sketch", "Sketching canvas", "🎨"),
    
    // BIM & Construction Components
    MATERIAL_SELECT_FIELD("MaterialSelectField", "MaterialSelect", "Material selection", "🔧"),
    EQUIPMENT_SELECT_FIELD("EquipmentSelectField", "EquipmentSelect", "Equipment selection", "⚙️"),
    LOCATION_HIERARCHY_FIELD("LocationHierarchyField", "LocationHierarchy", "Location hierarchy", "🏢"),
    WEATHER_CONDITIONS_FIELD("WeatherConditionsField", "WeatherConditions", "Weather data", "🌤️"),
    SAFETY_CHECKLIST_FIELD("SafetyChecklistField", "SafetyChecklist", "Safety checklist", "🦺"),
    INSPECTION_SCORE_FIELD("InspectionScoreField", "InspectionScore", "Inspection scoring", "📋"),
    DEFECT_CATEGORY_FIELD("DefectCategoryField", "DefectCategory", "Defect classification", "🔍"),
    
    // Data Collection Components
    SURVEY_GRID_FIELD("SurveyGridField", "SurveyGrid", "Survey matrix", "📊"),
    MATRIX_FIELD("MatrixField", "Matrix", "Data grid", "📊"),
    REPEATING_GROUP_FIELD("RepeatingGroupField", "RepeatingGroup", "Repeatable sections", "🔄"),
    CONDITIONAL_SECTION_FIELD("ConditionalSectionField", "ConditionalSection", "Conditional fields", "❓"),
    WIZARD_STEP_FIELD("WizardStepField", "WizardStep", "Multi-step process", "🪄"),
    ACCORDION_FIELD("AccordionField", "Accordion", "Collapsible sections", "📂"),
    
    // Interactive Lists
    QR_CHECKLIST_FIELD("QRChecklistField", "QRChecklist", "QR checklist", "📋"),
    FLIPPABLE_QR_CHECKLIST_FIELD("FlippableQRChecklistField", "FlippableQRChecklist", "Card checklist", "🃏"),
    
    // Advanced Components
    TASK_LIST_FIELD("TaskListField", "TaskList", "Project management", "📋");
    
    private final String jsonName;
    private final String displayType;
    private final String description;
    private final String icon;
    
    ComponentType(String jsonName, String displayType, String description, String icon) {
        this.jsonName = jsonName;
        this.displayType = displayType;
        this.description = description;
        this.icon = icon;
    }
    
    public String getJsonName() { return jsonName; }
    public String getDisplayType() { return displayType; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    
    public static ComponentType fromJsonName(String jsonName) {
        for (ComponentType type : values()) {
            if (type.jsonName.equals(jsonName)) {
                return type;
            }
        }
        return TEXT_FIELD; // Default fallback
    }
    
    public ComponentCategory getCategory() {
        switch (this) {
            case TEXT_FIELD:
            case TEXT_AREA_FIELD:
            case NUMBER_FIELD:
            case DECIMAL_FIELD:
            case CURRENCY_FIELD:
            case PERCENTAGE_FIELD:
            case YES_NO_FIELD:
            case EMAIL_FIELD:
            case PHONE_FIELD:
            case URL_FIELD:
            case PASSWORD_FIELD:
            case RICH_TEXT_FIELD:
            case AUTO_COMPLETE_FIELD:
                return ComponentCategory.BASIC_INPUT;
                
            case DATE_FIELD:
            case TIME_FIELD:
            case DATE_TIME_FIELD:
            case DATE_RANGE_FIELD:
            case TIME_RANGE_FIELD:
                return ComponentCategory.DATE_TIME;
                
            case MEASUREMENT_FIELD:
            case COORDINATE_FIELD:
            case SLIDER_FIELD:
            case RATING_FIELD:
                return ComponentCategory.MEASUREMENT;
                
            case SELECT_FIELD:
            case MULTI_SELECT_FIELD:
            case RADIO_BUTTON_GROUP_FIELD:
            case CHECKBOX_GROUP_FIELD:
            case TAG_SELECT_FIELD:
            case TREE_SELECT_FIELD:
                return ComponentCategory.SELECTION;
                
            case QR_CODE_FIELD:
            case CAMERA_FIELD:
            case QR_COLLECTOR_FIELD:
            case MULTI_PHOTO_FIELD:
            case FILE_UPLOAD_FIELD:
            case AUDIO_RECORDING_FIELD:
            case VIDEO_RECORDING_FIELD:
            case DOCUMENT_SCANNER_FIELD:
            case SIGNATURE_FIELD:
                return ComponentCategory.MEDIA_SCANNING;
                
            case LABEL_FIELD:
            case IMAGE_DISPLAY_FIELD:
            case QR_DISPLAY_FIELD:
            case BARCODE_DISPLAY_FIELD:
            case MAP_DISPLAY_FIELD:
            case HTML_CONTENT_FIELD:
                return ComponentCategory.DISPLAY;
                
            case CALCULATOR_FIELD:
            case COUNTER_FIELD:
            case TIMER_FIELD:
            case STOPWATCH_FIELD:
            case DRAWING_FIELD:
            case SKETCH_FIELD:
                return ComponentCategory.INTERACTIVE;
                
            case MATERIAL_SELECT_FIELD:
            case EQUIPMENT_SELECT_FIELD:
            case LOCATION_HIERARCHY_FIELD:
            case WEATHER_CONDITIONS_FIELD:
            case SAFETY_CHECKLIST_FIELD:
            case INSPECTION_SCORE_FIELD:
            case DEFECT_CATEGORY_FIELD:
                return ComponentCategory.BIM_CONSTRUCTION;
                
            case SURVEY_GRID_FIELD:
            case MATRIX_FIELD:
            case REPEATING_GROUP_FIELD:
            case CONDITIONAL_SECTION_FIELD:
            case WIZARD_STEP_FIELD:
            case ACCORDION_FIELD:
                return ComponentCategory.DATA_COLLECTION;
                
            case QR_CHECKLIST_FIELD:
            case FLIPPABLE_QR_CHECKLIST_FIELD:
                return ComponentCategory.INTERACTIVE_LISTS;
                
            case TASK_LIST_FIELD:
                return ComponentCategory.ADVANCED;
                
            default:
                return ComponentCategory.BASIC_INPUT;
        }
    }
    
    public enum ComponentCategory {
        BASIC_INPUT("Basic Input", "📝"),
        DATE_TIME("Date & Time", "📅"),
        MEASUREMENT("Measurement & Coordinates", "📏"),
        SELECTION("Selection", "📋"),
        MEDIA_SCANNING("Media & Scanning", "📱"),
        DISPLAY("Display & Information", "🖼️"),
        INTERACTIVE("Interactive Tools", "🧮"),
        BIM_CONSTRUCTION("BIM & Construction", "🏗️"),
        DATA_COLLECTION("Data Collection", "📊"),
        INTERACTIVE_LISTS("Interactive Lists", "📋"),
        ADVANCED("Advanced", "⚙️");
        
        private final String displayName;
        private final String icon;
        
        ComponentCategory(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
}
