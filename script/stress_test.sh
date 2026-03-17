#!/bin/bash

# Configuration
# URL updated as per your snippet
API="/kxreport/getPDF?report=A00&db=kxtest&option=0"
URL="http://localhost:8080${API}"
# URL="http://149.118.140.159:8080${API}"
# URL="https://149.118.140.159:8443${API}"
# URL="http://149.118.140.159${API}"
# URL="https://149.118.140.159${API}"
echo "URL: $URL"

COUNT=200
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MEM_SCRIPT="${SCRIPT_DIR}/mem.sh"

echo "==============================================================================="
echo " STRESS TEST: /getPDF (50 times, 1s interval)"
echo "==============================================================================="
echo "Target: $URL"
echo "Start:  $(date)"
echo "==============================================================================="

# Ensure mem.sh exists
if [ ! -f "$MEM_SCRIPT" ]; then
    echo "Error: $MEM_SCRIPT not found."
    exit 1
fi

# Initial memory state
echo "--- PRE-TEST MEMORY ---"
bash "$MEM_SCRIPT"

echo ""
echo "Starting requests..."
TEST_START_TIME=$(date +%s)

for i in $(seq 1 $COUNT); do
    # Execute request
    # -s: silent, -o /dev/null: discard PDF
    curl -s -k  -o /dev/null "$URL"
    
    echo -n "#"
    if [ $((i % 80)) -eq 0 ]; then
        echo " $i"
    fi
done

TEST_END_TIME=$(date +%s)
DURATION=$((TEST_END_TIME - TEST_START_TIME))

echo ""
echo "==============================================================================="
# Call memory script after the loop completes
echo "--- POST-TEST MEMORY ---"
bash "$MEM_SCRIPT"
echo "==============================================================================="
echo "Test complete at $(date)"
echo "Total Elapsed Time: ${DURATION}s"
echo "==============================================================================="
