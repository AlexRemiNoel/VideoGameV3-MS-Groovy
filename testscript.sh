#!/usr/bin/env bash

# --- Configuration ---

# Default Host and Port for your application
: ${HOST=localhost}
: ${PORT=8080}

# Base API path for games
GAMES_API_PATH="/api/v1/games"

# The specific Game ID to test
GAME_ID_TO_TEST="game-1" # <--- Make sure a game with this ID exists in your database!

# --- Global Variables ---

# Variable to hold the response body from the last curl command (used by assertCurl)
RESPONSE=""

# --- Helper Functions (copied from your reference/previous examples) ---

# assertCurl function: Checks the HTTP status code of a curl command
# Args:
#   $1: Expected HTTP status code
#   $2: The curl command string (excluding -s or -w, -s is added internally)
function assertCurl() {
    local expectedHttpCode=$1
    local curlCmd="$2 -s -w \"%{http_code}\""
    echo "Executing command: $2"

    local result=$(eval $curlCmd)
    local httpCode="${result:(-3)}"
    RESPONSE=''
    if (( ${#result} > 3 )); then
        RESPONSE="${result%???}"
    fi

    if [ "$httpCode" = "$expectedHttpCode" ]; then
        if [ "$httpCode" = "200" ] || [ "$httpCode" = "201" ] || [ "$httpCode" = "204" ]; then
            echo "Test OK (HTTP Code: $httpCode)"
        else
            if [ -n "$RESPONSE" ]; then
                 echo "Test OK (HTTP Code: $httpCode, Response snippet: ${RESPONSE:0:100}...)";
            else
                 echo "Test OK (HTTP Code: $httpCode)"
            fi
        fi
    else
        echo "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode."
        echo "- Failing command: $2"
        if [ -n "$RESPONSE" ]; then
             echo "- Response Body: $RESPONSE"
        else
             echo "- No Response Body received."
        fi
        exit 1 # Exit the script immediately on failure as per reference
    fi
}

# assertEqual function: Checks if two values are equal
# Args:
#   $1: Expected value
#   $2: Actual value
#   $3: Optional description of the test step
function assertEqual() {
    local expected="$1"
    local actual="$2"
    local description="${3:-Checking value equality}"

    echo "$description: Expected '$expected', Got '$actual'"

    if [ "$actual" = "$expected" ]; then
        echo "Test OK (actual value: '$actual')"
    else
        echo "Test FAILED, EXPECTED VALUE: '$expected', ACTUAL VALUE: '$actual'."
        exit 1
    fi
}

# --- Test Case ---

function testGetGameById() {
    echo -e "\n--- Running Test: GET $GAMES_API_PATH/$GAME_ID_TO_TEST ---"

    # Construct the full URL for the specific game
    local gameUrl="http://$HOST:$PORT$GAMES_API_PATH/$GAME_ID_TO_TEST"

    echo "Attempting to GET game with ID '$GAME_ID_TO_TEST' from $gameUrl"

    # 1. Perform the GET request and check for HTTP 200
    # We expect a 200 OK status code if the game exists and is found.
    assertCurl 200 "curl $gameUrl"

    # If assertCurl passed (HTTP code 200), the response body is in the global $RESPONSE variable.

    # 2. Verify the ID in the response body matches the requested ID
    echo "Verifying the returned game ID..."
    # Use jq to extract the 'id' field from the JSON response (based on your previous output)
    local returnedGameId=$(echo "$RESPONSE" | jq -r '.id')

    # Use assertEqual to compare the ID we requested with the ID returned in the response
    assertEqual "$GAME_ID_TO_TEST" "$returnedGameId" "Verifying returned game ID matches requested ID"

    # Optional: Add more checks here if needed, e.g., verifying other fields of the game-1 object
    # Example: Verify the genre
    # local expectedGenre="Some Expected Genre for game-1"
    # local returnedGenre=$(echo "$RESPONSE" | jq -r '.genre')
    # assertEqual "$expectedGenre" "$returnedGenre" "Verifying game genre"

    echo "--- Test: GET $GAMES_API_PATH/$GAME_ID_TO_TEST completed successfully ---"
}


# --- Main Execution Flow ---

# Exit immediately if any command fails (including those handled by assertCurl/assertEqual exiting)
set -e

echo "Starting API test for specific Game ID '$GAME_ID_TO_TEST'..."
echo "Target: http://$HOST:$PORT"

# --- IMPORTANT ---
# Ensure a game with ID "game-1" exists in your database before running this test.
# This script does NOT create the test data "game-1".

# Run the single test case
testGetGameById

echo -e "\n--- Single test finished successfully! ---"

exit 0 # Script finished successfully