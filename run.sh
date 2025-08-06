#!/bin/bash

echo "🚀 Running JSONFormMaker..."

# Compile if needed
if [ ! -d "target/classes" ]; then
    echo "📦 Compiling application..."
    mvn compile
fi

# Run the application
mvn exec:java -Dexec.mainClass="com.adui.jsoncraft.main.JSONFormMakerApplication"
