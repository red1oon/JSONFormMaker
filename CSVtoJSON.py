#!/usr/bin/env python3
"""
CSV to ADUI JSON Converter v1.1 - Enhanced Builder Compatibility
Converts CSV form definitions to ADUI-compliant JSON format matching JSON Form Builder output

Usage: python3 csv_to_adui_enhanced.py input.csv [--title "Custom Title"]

ENHANCED FEATURES v1.1:
- TaskListField format matches JSON Form Builder exactly
- Status/Priority values use uppercase format (NOT_STARTED, MEDIUM)
- Relationships as JSON string + separate dependencies array
- UI colors as JSON strings
- Additional builder-compatible fields (settings, resources, parentId)
- 🚨 CRITICAL: SelectField uses SAFE reference format (no validationType/description/icon/sortOrder)

CSV Format Expected:
Seq, Field Name, Component, Input

Component Types Supported:
- Text / Text Field → TextField
- Quantity / Quantity Field / NumberField / Number Field → NumberField  
- Select Field / SelectField → SelectField (with reference data)
- TaskListField → TaskListField (builder-compatible format)
"""

import csv
import json
import sys
import argparse
from pathlib import Path
from datetime import datetime
import re

class CSVToADUIConverterEnhanced:
    
    def __init__(self):
        # Component type mapping
        self.component_map = {
            'text': 'TextField',
            'text field': 'TextField', 
            'quantity': 'NumberField',
            'quantity field': 'NumberField',
            'numberfield': 'NumberField',        # Added: handles "NumberField"
            'number field': 'NumberField',       # Added: handles "Number Field"
            'select field': 'SelectField',
            'selectfield': 'SelectField',
            'tasklistfield': 'TaskListField'
        }
        
        # Default colors and icons for SelectField options
        self.default_colors = ["#4CAF50", "#FF9800", "#2196F3", "#9C27B0", "#F44336", "#607D8B", "#795548", "#FF5722"]
        self.default_icons = ["✅", "⚠️", "📋", "🔧", "🚨", "📊", "📁", "🎯"]
        
    def derive_project_title(self, csv_filename):
        """Extract project title from CSV filename"""
        stem = Path(csv_filename).stem
        # Convert CamelCase or snake_case to Title Case
        title = re.sub(r'([A-Z])', r' \1', stem).strip()
        title = title.replace('_', ' ').replace('-', ' ')
        return ' '.join(word.capitalize() for word in title.split())
    
    def normalize_component_type(self, component_str):
        """Normalize component type string to ADUI component"""
        if not component_str:
            return 'TextField'  # Default fallback
            
        component_key = component_str.lower().strip()
        return self.component_map.get(component_key, 'TextField')
    
    def generate_field_id(self, component_type, seq_num):
        """Generate field ID in format: COMPONENT_FIELD_N"""
        component_prefix = component_type.upper().replace('FIELD', '')
        return f"{component_prefix}_FIELD_{seq_num}"
    
    def parse_select_options(self, input_str):
        """Parse SelectField options with SAFE format (ADUI import bug fix)"""
        if not input_str or input_str.strip() == '':
            return []
            
        # Handle both newline and comma separation
        if '\n' in input_str:
            options = [opt.strip() for opt in input_str.split('\n') if opt.strip()]
        else:
            options = [opt.strip() for opt in input_str.split(',') if opt.strip()]
            
        # Generate reference values with SAFE format (bug fix)
        # REMOVED: validationType, description, icon, sortOrder (cause dropdown failure)
        reference_values = []
        for i, option in enumerate(options):
            color_idx = i % len(self.default_colors)
            
            reference_values.append({
                "key": option.upper().replace(' ', '_').replace('(', '').replace(')', ''),
                "display": option,
                "color": self.default_colors[color_idx]  # Only color works safely
            })
            
        return reference_values
    
    def parse_tasklist_nodes_enhanced(self, input_str):
        """Parse TaskListField nodes with builder-compatible format"""
        if not input_str or input_str.strip() == '':
            return {
                "tasks": [],
                "relationships": [],  # JSON string format
                "dependencies": [],   # Separate dependencies array
                "settings": {},
                "resources": []
            }
            
        # Split by comma for task nodes
        node_names = [node.strip() for node in input_str.split(',') if node.strip()]
        
        # Create tasks with builder-compatible format
        tasks = []
        for i, node_name in enumerate(node_names):
            task_id = f"TASK{i+1:03d}"
            tasks.append({
                "id": task_id,
                "name": node_name,
                "status": "NOT_STARTED",  # Builder format: uppercase
                "priority": "MEDIUM",     # Builder format: uppercase  
                "completion": 0,
                "assignee": "System",
                "phase": "Default",
                "description": f"Task: {node_name}",
                "estimatedHours": 999,
                "actualHours": 0,
                "startDate": None,        # Builder format: null dates
                "endDate": None,          # Builder format: null dates
                "parentId": None          # Builder format: parentId field
            })
        
        # Create relationships (for JSON string format)
        relationships = []
        dependencies = []  # Separate dependencies array (builder format)
        
        if len(tasks) > 1:
            for i in range(len(tasks) - 1):
                # Relationships array (for JSON string)
                relationships.append({
                    "from": tasks[i]["id"],
                    "to": tasks[i+1]["id"],
                    "type": "finish_to_start", 
                    "lag": 0,
                    "description": f"{tasks[i]['name']} to {tasks[i+1]['name']}"
                })
                
                # Dependencies array (builder format)
                dependencies.append({
                    "fromTaskId": tasks[i+1]["id"],  # Note: builder reverses the direction
                    "toTaskId": tasks[i]["id"],
                    "lagDays": 0,
                    "type": "finish-to-start"  # Note: builder uses dash, not underscore
                })
        
        # Builder-compatible settings
        settings = {
            "defaultStatus": "not_started",
            "defaultPriority": "low", 
            "autoCalculateProgress": False,
            "showProgressBars": False
        }
        
        return {
            "tasks": tasks,
            "relationships": relationships,  # Will be converted to JSON string
            "dependencies": dependencies,
            "settings": settings,
            "resources": []
        }
    
    def create_field_definition_enhanced(self, row, seq_num):
        """Create ADUI field definition with enhanced builder compatibility"""
        field_name = row.get('Field Name', f'Field {seq_num}')
        component_str = row.get('Component', 'Text')
        input_str = row.get('Input', '')
        
        component_type = self.normalize_component_type(component_str)
        field_id = self.generate_field_id(component_type, seq_num)
        
        # Base field definition (builder-compatible format)
        field_def = {
            "fieldId": field_id,
            "name": field_name,
            "component": component_type,
            "sequence": seq_num * 10,
            "description": "",  # Builder format: empty string instead of description
            "help": "",         # Builder format: help field
            "validation": {
                "maxLength": 0,  # Builder format: maxLength/minLength
                "minLength": 0
            },
            "ui": {
                "helpText": "",      # Builder format: empty helpText
                "placeholder": ""    # Builder format: placeholder field
            }
        }
        
        # Handle SelectField with reference data (SAFE format - bug fix)
        if component_type == 'SelectField':
            reference_values = self.parse_select_options(input_str)
            if reference_values:
                # SAFE reference format (no validationType - causes import bugs)
                field_def["reference"] = {
                    "id": f"{field_id}_REF",
                    "values": reference_values
                }
                field_def["validation"]["required"] = True
        
        # Handle TaskListField with enhanced builder-compatible format
        elif component_type == 'TaskListField':
            task_data = self.parse_tasklist_nodes_enhanced(input_str)
            
            # Builder-compatible UI format
            field_def["ui"].update({
                "allowZoomGraph": True,
                "showIcons": False,  # Builder format: showIcons field
                # Convert colors to JSON strings (builder format)
                "statusColors": json.dumps({
                    "completed": "#48BB78",
                    "in_progress": "#ED8936", 
                    "blocked": "#F56565",
                    "not_started": "#90CDF4"
                }),
                "priorityColors": json.dumps({
                    "critical": "#E53E3E",
                    "high": "#FF9800",
                    "normal": "#3182CE",
                    "low": "#38A169"
                })
            })
            
            # Builder-compatible data format
            field_def["data"] = {
                "tasks": task_data["tasks"],
                "dependencies": task_data["dependencies"],
                "settings": task_data["settings"],
                "resources": task_data["resources"],
                # Convert relationships to JSON string (builder format)
                "relationships": json.dumps(task_data["relationships"])
            }
        
        # Handle NumberField validation with min/max (replace base validation)
        elif component_type == 'NumberField':
            field_def["validation"] = {
                "required": True,
                "min": 0,
                "max": 9999
            }
            field_def["ui"]["helpText"] = f"Enter numeric value for {field_name.lower()}"
            
        return field_def
    
    def convert_csv_to_adui_enhanced(self, csv_file_path, custom_title=None):
        """Main conversion function with enhanced builder compatibility"""
        
        # Derive project title
        project_title = custom_title or self.derive_project_title(csv_file_path)
        window_id = project_title.upper().replace(' ', '_').replace('(', '').replace(')', '')
        
        # Read and parse CSV
        fields = []
        with open(csv_file_path, 'r', newline='', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            
            for row_num, row in enumerate(reader, 1):
                seq_num = row.get('Seq')
                
                # Handle missing sequence numbers
                if seq_num is None or seq_num == '':
                    seq_num = row_num
                else:
                    try:
                        seq_num = int(float(seq_num))
                    except (ValueError, TypeError):
                        seq_num = row_num
                
                # Skip empty rows
                if not row.get('Field Name') or row.get('Field Name').strip() == '':
                    continue
                    
                field_def = self.create_field_definition_enhanced(row, seq_num)
                fields.append(field_def)
        
        # Create ADUI JSON structure
        adui_json = {
            "windowId": window_id,
            "name": project_title,
            "description": f"Form generated from {Path(csv_file_path).name}",
            "windowType": "Transaction",
            "tabs": [
                {
                    "tabId": "MAIN_TAB",
                    "name": "Main",
                    "description": "Main form fields",
                    "sequence": 10,
                    "tabLevel": 0,
                    "isReadOnly": False,
                    "isSingleRow": True,
                    "fields": fields
                }
            ],
            "metadata": {
                "version": "1.1",
                "source": "csv-converter-enhanced-v1.1",
                "lastModified": datetime.now().isoformat() + "Z",
                "createdBy": "CSV to ADUI Converter Enhanced",
                "templateType": "csv-import-enhanced",
                "description": f"Auto-generated from {Path(csv_file_path).name} with builder compatibility",
                "originalFile": str(csv_file_path)
            }
        }
        
        return adui_json

def main():
    parser = argparse.ArgumentParser(description='Convert CSV to ADUI JSON format (Enhanced Builder Compatibility)')
    parser.add_argument('csv_file', help='Input CSV file path')
    parser.add_argument('--title', help='Custom project title (default: derived from filename)')
    parser.add_argument('--output', '-o', help='Output JSON file path (default: input_name_enhanced.json)')
    
    args = parser.parse_args()
    
    # Check if input file exists
    csv_path = Path(args.csv_file)
    if not csv_path.exists():
        print(f"Error: CSV file '{csv_path}' not found")
        sys.exit(1)
    
    # Determine output path
    if args.output:
        output_path = Path(args.output)
    else:
        # Default to _enhanced suffix to avoid overwriting original
        output_path = csv_path.with_name(f"{csv_path.stem}_enhanced.json")
    
    # Convert CSV to ADUI JSON
    converter = CSVToADUIConverterEnhanced()
    try:
        adui_json = converter.convert_csv_to_adui_enhanced(csv_path, args.title)
        
        # Write output file
        with open(output_path, 'w', encoding='utf-8') as outfile:
            json.dump(adui_json, outfile, indent=2, ensure_ascii=False)
        
        print(f"✅ Successfully converted '{csv_path}' to '{output_path}' (Enhanced v1.1)")
        print(f"📋 Project: {adui_json['name']}")
        print(f"📝 Fields: {len(adui_json['tabs'][0]['fields'])}")
        print(f"🏷️  Window ID: {adui_json['windowId']}")
        
        # Show field summary with enhanced details
        print("\n📋 Field Summary (Enhanced Format):")
        for field in adui_json['tabs'][0]['fields']:
            component = field['component']
            name = field['name']
            extra = ""
            
            if 'reference' in field:
                extra = f" ({len(field['reference']['values'])} options)"
            elif component == 'TaskListField' and 'data' in field:
                tasks_count = len(field['data']['tasks']) 
                deps_count = len(field['data']['dependencies'])
                extra = f" ({tasks_count} tasks, {deps_count} dependencies)"
                
            print(f"  • {name}: {component}{extra}")
            
        # Show TaskListField details
        for field in adui_json['tabs'][0]['fields']:
            if field['component'] == 'TaskListField':
                print(f"\n🔧 TaskListField '{field['name']}' Enhanced Details:")
                print(f"  • Status Format: {field['data']['tasks'][0]['status']} (Builder Compatible)")
                print(f"  • Priority Format: {field['data']['tasks'][0]['priority']} (Builder Compatible)")
                print(f"  • Date Format: startDate/endDate = null (Builder Compatible)")
                print(f"  • UI Colors: JSON strings (Builder Compatible)")
                print(f"  • Dependencies: {len(field['data']['dependencies'])} relationships")
                break
            
    except Exception as e:
        print(f"❌ Error converting CSV: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()
