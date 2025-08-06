#!/bin/bash

# Universal Maven Project Packaging Script v2.0
# Automatically detects GitHub repo and project details
# Works with ANY Maven project - just drop this script and run!

set -e

echo "🚀 Universal GitHub Project Packager v2.0"
echo "=========================================="

# Auto-detect project information
DIST_DIR="distribution"
BUILD_DIR="$DIST_DIR/build"
RELEASE_DIR="$DIST_DIR/release"

# Detect GitHub repository information
if [ -d ".git" ]; then
    REPO_URL=$(git config --get remote.origin.url 2>/dev/null || echo "")
    if [ -n "$REPO_URL" ]; then
        # Parse GitHub URL (handles both HTTPS and SSH)
        if [[ "$REPO_URL" =~ github\.com[:/]([^/]+)/([^/]+)(\.git)?$ ]]; then
            GITHUB_USER="${BASH_REMATCH[1]}"
            GITHUB_REPO="${BASH_REMATCH[2]}"
            GITHUB_REPO="${GITHUB_REPO%.git}"  # Remove .git suffix if present
            GITHUB_FULL_REPO="$GITHUB_USER/$GITHUB_REPO"
            echo "📂 Detected GitHub repo: $GITHUB_FULL_REPO"
        else
            echo "⚠️  Warning: Not a GitHub repository or unknown URL format"
            GITHUB_FULL_REPO="UNKNOWN/UNKNOWN"
        fi
    else
        echo "⚠️  Warning: No Git remote origin found"
        GITHUB_FULL_REPO="UNKNOWN/UNKNOWN"
    fi
else
    echo "⚠️  Warning: Not a Git repository"
    GITHUB_FULL_REPO="UNKNOWN/UNKNOWN"
fi

# Auto-detect Maven project information
if [ -f "pom.xml" ]; then
    PROJECT_NAME=$(grep -m1 "<artifactId>" pom.xml | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d ' ')
    PROJECT_VERSION=$(grep -m1 "<version>" pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
    echo "📋 Detected Maven project: $PROJECT_NAME v$PROJECT_VERSION"
    
    # Look for main class in pom.xml
    MAIN_CLASS=$(grep -A5 "exec-maven-plugin" pom.xml | grep "mainClass" | sed 's/.*<mainClass>\(.*\)<\/mainClass>.*/\1/' | tr -d ' ' | head -1)
    if [ -z "$MAIN_CLASS" ]; then
        MAIN_CLASS="com.example.Main"  # Default fallback
    fi
    echo "🎯 Main class: $MAIN_CLASS"
else
    echo "❌ Error: No pom.xml found - this script requires a Maven project"
    exit 1
fi

# Version management (project-specific)
VERSION_FILE="$DIST_DIR/version.properties"
if [ -f "$VERSION_FILE" ]; then
    source "$VERSION_FILE"
else
    # Extract version from Maven (remove -SNAPSHOT)
    VERSION=$(echo "$PROJECT_VERSION" | sed 's/-SNAPSHOT//')
    BUILD_NUMBER=1
    echo "🔄 Creating initial version file: $VERSION"
fi

echo "📦 Packaging version: $VERSION (Build $BUILD_NUMBER)"

# Create directories
mkdir -p "$BUILD_DIR"
mkdir -p "$RELEASE_DIR"

# Build the Maven project
echo "🔨 Building Maven project..."
mvn clean package -DskipTests

# Find the built JAR file (handle different naming conventions)
BUILT_JAR=""
if [ -f "target/$PROJECT_NAME-$PROJECT_VERSION-jar-with-dependencies.jar" ]; then
    BUILT_JAR="target/$PROJECT_NAME-$PROJECT_VERSION-jar-with-dependencies.jar"
elif [ -f "target/$PROJECT_NAME-$PROJECT_VERSION.jar" ]; then
    BUILT_JAR="target/$PROJECT_NAME-$PROJECT_VERSION.jar"
else
    # Find any JAR file in target directory
    BUILT_JAR=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
fi

if [ -z "$BUILT_JAR" ] || [ ! -f "$BUILT_JAR" ]; then
    echo "❌ Build failed - No JAR file found in target/"
    ls -la target/ || echo "Target directory doesn't exist"
    exit 1
fi

echo "✅ Build successful: $BUILT_JAR"

# Create release structure
RELEASE_VERSION_DIR="$RELEASE_DIR/v$VERSION"
mkdir -p "$RELEASE_VERSION_DIR"

echo "📦 Creating release package..."

# Copy main JAR with clean naming
RELEASE_JAR_NAME="${PROJECT_NAME,,}-$VERSION.jar"  # Convert to lowercase
cp "$BUILT_JAR" "$RELEASE_VERSION_DIR/$RELEASE_JAR_NAME"

# Create Windows launcher
cat > "$RELEASE_VERSION_DIR/${PROJECT_NAME}.bat" << EOF
@echo off
title $PROJECT_NAME v$VERSION
echo Starting $PROJECT_NAME v$VERSION...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Java not found
    echo.
    echo Please install Java 11 or higher from:
    echo https://adoptopenjdk.net/
    echo.
    pause
    exit /b 1
)

REM Run the application
java -jar "$RELEASE_JAR_NAME"

if errorlevel 1 (
    echo.
    echo ❌ Application failed to start
    echo.
    pause
)
EOF

# Create Linux/Mac launcher
cat > "$RELEASE_VERSION_DIR/${PROJECT_NAME,,}.sh" << EOF
#!/bin/bash

echo "Starting $PROJECT_NAME v$VERSION..."

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java not found"
    echo "Please install Java 11 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=\$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "\$JAVA_VERSION" -lt 11 ]; then
    echo "❌ Error: Java 11 or higher required"
    echo "Current version: \$JAVA_VERSION"
    exit 1
fi

# Run the application
java -jar "$RELEASE_JAR_NAME"
EOF

# Make shell script executable
chmod +x "$RELEASE_VERSION_DIR/${PROJECT_NAME,,}.sh"

# Create comprehensive README
cat > "$RELEASE_VERSION_DIR/README.txt" << EOF
$PROJECT_NAME v$VERSION
$(printf '=%.0s' $(seq 1 $((${#PROJECT_NAME} + ${#VERSION} + 3))))

QUICK START:
-----------
Windows: Double-click ${PROJECT_NAME}.bat
Mac/Linux: Run ./${PROJECT_NAME,,}.sh

REQUIREMENTS:
------------
- Java 11 or higher
- 512 MB RAM minimum
- 50 MB disk space

INSTALLATION:
------------
1. Ensure Java 11+ is installed
2. Extract this package to any folder
3. Run the launcher for your operating system

SUPPORT:
--------
Project: https://github.com/$GITHUB_FULL_REPO
Issues:  https://github.com/$GITHUB_FULL_REPO/issues
Latest:  https://github.com/$GITHUB_FULL_REPO/releases

TECHNICAL INFO:
--------------
Version: $VERSION
Build: $BUILD_NUMBER
Built: $(date)
Main Class: $MAIN_CLASS
Java Target: 11+

Generated by Universal GitHub Packager v2.0
EOF

# Create GitHub configuration for UpdateChecker
cat > "$RELEASE_VERSION_DIR/github-config.properties" << EOF
# Auto-generated GitHub configuration
github.user=$GITHUB_USER
github.repo=$GITHUB_REPO
github.fullRepo=$GITHUB_FULL_REPO
github.apiUrl=https://api.github.com/repos/$GITHUB_FULL_REPO/releases/latest
github.releasesUrl=https://github.com/$GITHUB_FULL_REPO/releases
project.name=$PROJECT_NAME
project.version=$VERSION
project.buildNumber=$BUILD_NUMBER
generated.timestamp=$(date -u +%Y-%m-%dT%H:%M:%SZ)
EOF

# Create version metadata for update checking
cat > "$RELEASE_VERSION_DIR/version.json" << EOF
{
  "version": "$VERSION",
  "buildNumber": $BUILD_NUMBER,
  "releaseDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "projectName": "$PROJECT_NAME",
  "githubRepo": "$GITHUB_FULL_REPO",
  "downloadUrl": "https://github.com/$GITHUB_FULL_REPO/releases/download/v$VERSION/$RELEASE_JAR_NAME",
  "changelogUrl": "https://github.com/$GITHUB_FULL_REPO/releases/tag/v$VERSION",
  "minimumJavaVersion": "11",
  "fileSize": $(stat -c%s "$RELEASE_VERSION_DIR/$RELEASE_JAR_NAME" 2>/dev/null || stat -f%z "$RELEASE_VERSION_DIR/$RELEASE_JAR_NAME" 2>/dev/null || echo 0),
  "mainClass": "$MAIN_CLASS"
}
EOF

# Create distribution ZIP
echo "📁 Creating distribution ZIP..."
cd "$RELEASE_DIR"
ZIP_NAME="${PROJECT_NAME,,}-$VERSION.zip"
zip -r "$ZIP_NAME" "v$VERSION/"
cd ../..

# Calculate file sizes for summary
JAR_SIZE=$(du -h "$RELEASE_VERSION_DIR/$RELEASE_JAR_NAME" | cut -f1)
ZIP_SIZE=$(du -h "$RELEASE_DIR/$ZIP_NAME" | cut -f1)

echo ""
echo "✅ Package created successfully!"
echo ""
echo "📦 Release Files:"
echo "   📄 $RELEASE_JAR_NAME ($JAR_SIZE)"
echo "   📄 ${PROJECT_NAME}.bat (Windows launcher)"
echo "   📄 ${PROJECT_NAME,,}.sh (Linux/Mac launcher)"
echo "   📄 github-config.properties (Auto-update config)"
echo "   📄 $ZIP_NAME ($ZIP_SIZE)"
echo ""
echo "🚀 Ready for GitHub release!"
echo "   Repository: $GITHUB_FULL_REPO"
echo "   Tag: v$VERSION"
echo "   Upload: $RELEASE_DIR/$ZIP_NAME"
echo ""

# Update version tracking
NEW_BUILD=$((BUILD_NUMBER + 1))
cat > "$VERSION_FILE" << EOF
VERSION=$VERSION
BUILD_NUMBER=$NEW_BUILD

# Auto-detected project info:
PROJECT_NAME=$PROJECT_NAME
GITHUB_REPO=$GITHUB_FULL_REPO
MAIN_CLASS=$MAIN_CLASS

# Version history will be tracked here
# $VERSION.1 - Build $BUILD_NUMBER ($(date +%Y-%m-%d))
EOF

echo "📋 Next build will be: $VERSION (Build $NEW_BUILD)"
echo ""

# Generate GitHub release commands
if [ "$GITHUB_FULL_REPO" != "UNKNOWN/UNKNOWN" ]; then
    echo "🔗 GitHub Release Commands:"
    echo "   git add ."
    echo "   git commit -m \"Release v$VERSION\""
    echo "   git tag v$VERSION"
    echo "   git push origin main"
    echo "   git push origin v$VERSION"
    echo ""
    echo "   Then upload $ZIP_NAME to GitHub releases page:"
    echo "   https://github.com/$GITHUB_FULL_REPO/releases/new"
else
    echo "⚠️  Configure GitHub remote to enable automatic release commands"
fi

echo ""
echo "🎉 Packaging complete! Your app is ready for distribution."