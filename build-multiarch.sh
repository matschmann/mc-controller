#!/bin/bash

# Multi-Architecture Build Script für MusicCast Controller
# Unterstützt amd64 und arm64 Architekturen

set -e

echo "🚀 Starting multi-architecture build for MusicCast Controller..."

# Prüfen ob Docker Buildx verfügbar ist
if ! docker buildx version > /dev/null 2>&1; then
    echo "❌ Docker Buildx ist nicht verfügbar. Bitte installieren Sie Docker Desktop oder aktivieren Sie Buildx."
    exit 1
fi

# Builder erstellen falls nicht vorhanden
BUILDER_NAME="musiccast-multiarch"
if ! docker buildx ls | grep -q "$BUILDER_NAME"; then
    echo "📦 Erstelle neuen Buildx Builder: $BUILDER_NAME"
    docker buildx create --name "$BUILDER_NAME" --driver docker-container --bootstrap
fi

# Builder verwenden
echo "🔧 Verwende Builder: $BUILDER_NAME"
docker buildx use "$BUILDER_NAME"

# Multi-Arch Build und Push (optional)
echo "🏗️  Building für amd64 und arm64..."

if [ "$1" = "--push" ]; then
    echo "📤 Building und pushing zu Registry..."
    docker buildx build \
        --platform linux/amd64,linux/arm64 \
        --tag musiccast-controller:latest \
        --tag musiccast-controller:multiarch \
        --push \
        .
else
    echo "💾 Building lokal (ohne Push)..."
    docker buildx build \
        --platform linux/amd64,linux/arm64 \
        --tag musiccast-controller:latest \
        --tag musiccast-controller:multiarch \
        --load \
        .
fi

echo "✅ Multi-Architecture Build erfolgreich abgeschlossen!"
echo ""
echo "📋 Verfügbare Images:"
docker images | grep musiccast-controller

echo ""
echo "🎯 Verwendung:"
echo "   Lokaler Build:     ./build-multiarch.sh"
echo "   Build mit Push:    ./build-multiarch.sh --push"
echo "   Docker Compose:    docker-compose up --build"
echo ""
echo "🔍 Architektur prüfen:"
echo "   docker buildx imagetools inspect musiccast-controller:latest"
