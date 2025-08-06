#!/bin/bash

echo "🔨 Building JSONFormMaker..."

# Clean and compile
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Build successful"
else
    echo "❌ Build failed"
    exit 1
fi
