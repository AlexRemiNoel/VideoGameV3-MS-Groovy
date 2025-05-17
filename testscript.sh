#!/bin/bash

# --- Configuration ---
API_GATEWAY_URL="http://localhost:8080" # IMPORTANT: All requests go through this

# Base paths from your Swagger (API Gateway routes)
AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH="/api/v1/profile-dashboards"
LLMS_USER_BASE_PATH="/api/v1/user"
LLMS_GAME_BASE_PATH="/api/v1/games"
LLMS_DOWNLOAD_BASE_PATH="/api/v1/downloads"

# Variable to store the response body from check_status
LAST_RESPONSE_BODY=""

# --- Helper Functions ---
# check_status EXPECTED_STATUS CURL_COMMAND_STRING
# This function will now set LAST_RESPONSE_BODY
check_status() {
    EXPECTED_STATUS=$1
    CMD_TO_EXECUTE="${@:2}"
    LAST_RESPONSE_BODY="" # Reset

    echo "Executing: $CMD_TO_EXECUTE"
    BODY_TEMP_FILE=$(mktemp)
    # Execute the command, capture stdout to file, and http_code
    HTTP_STATUS=$(eval "$CMD_TO_EXECUTE -s -o $BODY_TEMP_FILE -w '%{http_code}'")
    LAST_RESPONSE_BODY=$(cat $BODY_TEMP_FILE)
    rm $BODY_TEMP_FILE

    echo "Expected Status: $EXPECTED_STATUS, Actual Status: $HTTP_STATUS"
    if [ "$HTTP_STATUS" -eq "$EXPECTED_STATUS" ]; then
        echo "PASS"
        echo "Response Body:"
        echo "$LAST_RESPONSE_BODY" | (jq . 2>/dev/null || echo "$LAST_RESPONSE_BODY")
        echo "-----------------------------------------------------"
        return 0 # Success
    else
        echo "FAIL"
        echo "Response Body:"
        echo "$LAST_RESPONSE_BODY" | (jq . 2>/dev/null || echo "$LAST_RESPONSE_BODY")
        echo "-----------------------------------------------------"
        return 1 # Failure
    fi
}

# extract_id JSON_RESPONSE ID_FIELD_NAME
extract_id() {
    JSON_RESPONSE=$1
    ID_FIELD_NAME=$2
    # Ensure jq gets the input correctly, especially if JSON_RESPONSE might be empty or not JSON
    ID=$(echo "$JSON_RESPONSE" | jq -r ".$ID_FIELD_NAME" 2>/dev/null)
    if [ -z "$ID" ] || [ "$ID" == "null" ]; then
        echo "Warning: Could not extract ID using field '$ID_FIELD_NAME' from response: $JSON_RESPONSE" >&2
    fi
    echo "$ID"
}

echo "============================================="
echo "Starting Microservice Integration Tests"
echo "API Gateway URL: $API_GATEWAY_URL"
echo "============================================="

TIMESTAMP=$(date +%s) # Generate timestamp once for related entities

# --- LLMS: User Service Tests (T12 Requirement) ---
echo ""
echo ">>> Testing LLMS: User Service ($LLMS_USER_BASE_PATH)"
echo "-----------------------------------------------------"

# 1. POST to Create User
echo "[USER SVC] Test 1: POST $LLMS_USER_BASE_PATH"
USER_POST_PAYLOAD="{\"username\": \"testuser_bash_$TIMESTAMP\", \"email\": \"testuser_bash_$TIMESTAMP@example.com\", \"password\": \"password123\", \"balance\": 50.0}"
CMD_USER_POST="curl -X POST -H \"Content-Type: application/json\" -d '$USER_POST_PAYLOAD' $API_GATEWAY_URL$LLMS_USER_BASE_PATH"
check_status 201 "$CMD_USER_POST" # FIX: Expected 201 Created
CREATED_USER_ID=""
if [ $? -eq 0 ]; then # Check if check_status passed
    CREATED_USER_ID=$(extract_id "$LAST_RESPONSE_BODY" "userId")
    echo "Created User ID: $CREATED_USER_ID"
else
    echo "User creation failed, subsequent tests requiring userId might fail or be skipped."
fi
echo ""

# 2. GET All Users (or GET User by ID if preferred)
echo "[USER SVC] Test 2: GET $LLMS_USER_BASE_PATH"
CMD_USER_GET_ALL="curl -X GET -H \"Accept: application/json\" $API_GATEWAY_URL$LLMS_USER_BASE_PATH"
check_status 200 "$CMD_USER_GET_ALL"
echo ""


# --- Aggregator Microservice: Profile Dashboard (T11 Requirements) ---
echo ""
echo ">>> Testing Aggregator: Profile Dashboard Service ($AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH)"
echo "-----------------------------------------------------"

PROFILE_DASHBOARD_USER_ID="123e4567-e89b-12d3-a456-426614174000"

if [ -z "$PROFILE_DASHBOARD_USER_ID" ] || [ "$PROFILE_DASHBOARD_USER_ID" == "null" ]; then
    echo "[AGGREGATOR] SKIPPING ALL TESTS: No valid CREATED_USER_ID."
else
    # AI5: POST Endpoint for the aggregate
    echo "[AGGREGATOR] Test 1: POST $AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH/$PROFILE_DASHBOARD_USER_ID (Ensure/Create Dashboard for User)"
    CMD_AGG_POST="curl -X POST -H \"Accept: application/hal+json\" $API_GATEWAY_URL$AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH/$PROFILE_DASHBOARD_USER_ID"
    check_status 201 "$CMD_AGG_POST"
    echo ""

    # AI5: GET by AggregateId
    echo "[AGGREGATOR] Test 2: GET $AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH/$PROFILE_DASHBOARD_USER_ID"
    CMD_AGG_GET_BY_ID="curl -X GET -H \"Accept: application/hal+json\" $API_GATEWAY_URL$AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH/$PROFILE_DASHBOARD_USER_ID"
    check_status 200 "$CMD_AGG_GET_BY_ID"
    echo ""

    # AI5: GET ALL Aggregates
    echo "[AGGREGATOR] Test 3: GET $AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH"
    CMD_AGG_GET_ALL="curl -X GET -H \"Accept: application/hal+json\" \"$API_GATEWAY_URL$AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH?page=0&size=5\""
    check_status 200 "$CMD_AGG_GET_ALL"
    echo ""

    # AI5: PUT by AggregateId
    echo "[AGGREGATOR] Test 4: PUT $AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH/$PROFILE_DASHBOARD_USER_ID"
    CMD_AGG_PUT="curl -X PUT -H \"Accept: application/hal+json\" $API_GATEWAY_URL$AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH/$PROFILE_DASHBOARD_USER_ID"
    check_status 200 "$CMD_AGG_PUT"
    echo ""

    # AI5: DELETE by AggregateId
    echo "[AGGREGATOR] Test 5: DELETE $AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH/$PROFILE_DASHBOARD_USER_ID"
    CMD_AGG_DELETE="curl -X DELETE -H \"Accept: application/hal+json\" $API_GATEWAY_URL$AGGREGATOR_PROFILE_DASHBOARD_BASE_PATH/$PROFILE_DASHBOARD_USER_ID"
    check_status 204 "$CMD_AGG_DELETE" # FIX: Expected 204 No Content
    echo ""
fi


# --- LLMS: Game Service Tests (T12 Requirement) ---
echo ""
echo ">>> Testing LLMS: Game Service ($LLMS_GAME_BASE_PATH)"
echo "-----------------------------------------------------"

# 1. POST to Create Game
echo "[GAME SVC] Test 1: POST $LLMS_GAME_BASE_PATH"
CREATED_GAME_ID="" # Initialize
if [ -n "$CREATED_USER_ID" ] && [ "$CREATED_USER_ID" != "null" ]; then
    GAME_POST_PAYLOAD="{\"title\": \"Bash Test Game $TIMESTAMP\", \"price\": 29.99, \"description\": \"A game created by bash script\", \"publisher\": \"Bash Inc.\", \"developer\": \"Script Kiddies\", \"genre\": \"ACTION\", \"userId\": \"$CREATED_USER_ID\"}"
    CMD_GAME_POST="curl -X POST -H \"Content-Type: application/json\" -d '$GAME_POST_PAYLOAD' $API_GATEWAY_URL$LLMS_GAME_BASE_PATH"
    check_status 201 "$CMD_GAME_POST" # Swagger says 200 OK. If your service returns 201, change this.
                                     # If it still returns 500, you need to debug your game service.
    if [ $? -eq 0 ]; then
        CREATED_GAME_ID=$(extract_id "$LAST_RESPONSE_BODY" "id")
        echo "Created Game ID: $CREATED_GAME_ID"
    else
        echo "Game creation failed."
    fi
else
    echo "[GAME SVC] Test 1: SKIPPED POST (No CREATED_USER_ID for game payload)"
fi
echo ""

# 2. GET All Games
echo "[GAME SVC] Test 2: GET $LLMS_GAME_BASE_PATH"
CMD_GAME_GET_ALL="curl -X GET -H \"Accept: application/json\" $API_GATEWAY_URL$LLMS_GAME_BASE_PATH"
check_status 200 "$CMD_GAME_GET_ALL"
echo ""


# --- LLMS: Download Service Tests (T12 Requirement) ---
echo ""
echo ">>> Testing LLMS: Download Service ($LLMS_DOWNLOAD_BASE_PATH)"
echo "-----------------------------------------------------"

# 1. POST to Create Download
echo "[DOWNLOAD SVC] Test 1: POST $LLMS_DOWNLOAD_BASE_PATH"
DOWNLOAD_POST_PAYLOAD="{\"sourceUrl\": \"http://example.com/download/bash_test_$TIMESTAMP.zip\"}"
CMD_DOWNLOAD_POST="curl -X POST -H \"Content-Type: application/json\" -d '$DOWNLOAD_POST_PAYLOAD' $API_GATEWAY_URL$LLMS_DOWNLOAD_BASE_PATH"
check_status 201 "$CMD_DOWNLOAD_POST" # FIX: Expected 201 Created
CREATED_DOWNLOAD_ID=""
if [ $? -eq 0 ]; then
    CREATED_DOWNLOAD_ID=$(extract_id "$LAST_RESPONSE_BODY" "id")
    echo "Created Download ID: $CREATED_ DOWNLOAD_ID"
else
    echo "Download creation failed."
fi
echo ""

# 2. GET All Downloads
echo "[DOWNLOAD SVC] Test 2: GET $LLMS_DOWNLOAD_BASE_PATH"
CMD_DOWNLOAD_GET_ALL="curl -X GET -H \"Accept: application/json\" $API_GATEWAY_URL$LLMS_DOWNLOAD_BASE_PATH"
check_status 200 "$CMD_DOWNLOAD_GET_ALL"
echo ""

# --- Clean up created User ---
if [ -n "$CREATED_USER_ID" ] && [ "$CREATED_USER_ID" != "null" ]; then
    echo ""
    echo ">>> Cleaning up: Deleting created User ($CREATED_USER_ID)"
    echo "-----------------------------------------------------"
    CMD_USER_DELETE="curl -X DELETE $API_GATEWAY_URL$LLMS_USER_BASE_PATH/$CREATED_USER_ID"
    check_status 204 "$CMD_USER_DELETE" # FIX: Expected 204 No Content
else
    echo "No user ID to clean up or user creation failed."
fi


echo "============================================="
echo "Integration Tests Finished"
echo "============================================="