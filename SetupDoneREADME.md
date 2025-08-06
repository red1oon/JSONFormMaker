# JSONFormMaker - ADUI JSON Craft Studio

Visual JSON configuration builder for ADUI mobile forms.

## Overview

JSONFormMaker is a desktop application that allows users to visually design and generate ADUI JSON configurations using a comprehensive component set. The application provides a professional IDE-like experience for creating mobile form definitions without manual JSON editing.

## Features

- **Visual Form Designer**: Drag-and-drop interface for building forms
- **Component Palette**: 46+ component types organized by category
- **Properties Inspector**: Dynamic property editing based on component type
- **Real-time Validation**: Instant feedback on configuration errors
- **Template System**: Pre-built templates for common use cases
- **Export Options**: Generate clean, validated JSON configurations

## Requirements

- Java 11 or higher
- Maven 3.6+
- Ubuntu 18.04+ (or any Linux distribution)

## Quick Start

1. **Setup Project Structure**:
   ```bash
   chmod +x setup-project.sh
   ./setup-project.sh
   ```

2. **Build Application**:
   ```bash
   cd JSONFormMaker
   mvn clean compile
   ```

3. **Run Tests**:
   ```bash
   mvn test
   ```

4. **Package Application**:
   ```bash
   mvn package
   ```

## Project Structure

```
JSONFormMaker/
├── src/main/java/com/adui/jsoncraft/
│   ├── main/              # Main application classes
│   ├── palette/           # Component palette implementation
│   ├── canvas/            # Form designer canvas
│   ├── properties/        # Properties inspector
│   ├── model/             # Data models
│   ├── json/              # JSON generation/parsing
│   ├── validation/        # Validation engine
│   └── utils/             # Utility classes
├── src/main/resources/
│   ├── icons/             # Application icons
│   ├── templates/         # Form templates
│   └── config/            # Configuration files
└── docs/                  # Documentation
```

## Development

This project follows the ADUI JSON Programming Guide v2.0 specifications for component definitions and data structures.

## License

© 2025 ADUI JSON Craft Studio
