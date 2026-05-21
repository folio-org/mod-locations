#!/bin/bash
# API Linting Script for OpenAPI Specifications
# Uses Spectral to validate OpenAPI files

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$SCRIPT_DIR/../api"

echo "================================================"
echo "API Linting with Spectral"
echo "================================================"
echo ""

# Check if spectral is installed
if ! command -v spectral &> /dev/null; then
    echo "❌ Spectral is not installed."
    echo ""
    echo "To install Spectral, run:"
    echo "  npm install -g @stoplight/spectral-cli"
    echo ""
    exit 1
fi

echo "✓ Spectral found: $(spectral --version)"
echo ""

# Function to lint a file
lint_file() {
    local file=$1
    local filename=$(basename "$file")
    
    echo "Linting: $filename"
    echo "----------------------------------------"
    
    if spectral lint "$file"; then
        echo "✅ $filename passed"
    else
        echo "❌ $filename has issues"
        return 1
    fi
    echo ""
}

# Track if any linting failed
FAILED=0

# Lint all YAML files in the api directory
echo "API Specification:"
echo "----------------------------------------"
lint_file "$API_DIR/mod-locations.yaml" || FAILED=1

echo ""
echo "================================================"
if [ $FAILED -eq 0 ]; then
    echo "✅ All API specifications passed linting!"
    echo "================================================"
    exit 0
else
    echo "❌ Some API specifications have linting issues"
    echo "================================================"
    exit 1
fi
