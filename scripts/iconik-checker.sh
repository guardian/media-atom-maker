#!/usr/bin/env bash
set -euo pipefail

# Runs one Iconik API check per stored entity. Override the config, pacing, or
# retry count with ICONIK_CHECK_CONFIG_FILE, ICONIK_CHECK_DELAY_SECONDS, and
# ICONIK_CHECK_RETRY_LIMIT.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HMAC_REQUEST="${SCRIPT_DIR}/hmac/make-hmac-request.js"
CONFIG_FILE="${ICONIK_CHECK_CONFIG_FILE:-/etc/gu/media-atom-maker.private.conf}"
DELAY_SECONDS="${ICONIK_CHECK_DELAY_SECONDS:-1}"
RETRY_LIMIT="${ICONIK_CHECK_RETRY_LIMIT:-5}"
CHECKED=0

if ! command -v jq >/dev/null || ! command -v node >/dev/null; then
  echo "Error: iconik checker requires jq and node" >&2
  exit 1
fi

if [[ ! "$DELAY_SECONDS" =~ ^[0-9]+([.][0-9]+)?$ ]]; then
  echo "Error: ICONIK_CHECK_DELAY_SECONDS must be a non-negative number" >&2
  exit 1
fi

if [[ ! "$RETRY_LIMIT" =~ ^[0-9]+$ ]]; then
  echo "Error: ICONIK_CHECK_RETRY_LIMIT must be a non-negative integer" >&2
  exit 1
fi

hmac_request() {
  local path="$1"
  local attempt=0
  local response

  while true; do
    if response=$(node "$HMAC_REQUEST" \
      --path "$path" \
      --config-file "$CONFIG_FILE" 2>&1); then
      printf '%s\n' "$response"
      return
    fi

    local status
    status=$(sed -n 's/^ERROR! \([0-9][0-9][0-9]\).*/\1/p' <<<"$response")
    if [[ ! "$status" =~ ^(429|502|503|504)$ ]] || ((attempt >= RETRY_LIMIT)); then
      printf '%s\n' "$response" >&2
      return 1
    fi

    attempt=$((attempt + 1))
    local backoff=$((2 ** attempt))
    echo "Request returned HTTP ${status}; retrying in ${backoff}s (attempt ${attempt}/${RETRY_LIMIT})" >&2
    sleep "$backoff"
  done
}

load_array() {
  local path="$1"
  local response
  response=$(hmac_request "$path")
  if ! jq -e 'type == "array"' >/dev/null <<<"$response"; then
    echo "Error: expected an array response from ${path}" >&2
    return 1
  fi
  printf '%s' "$response"
}

validate_one() {
  local entity_type="$1"
  local id="$2"
  local encoded_id
  encoded_id=$(jq -rn --arg id "$id" '$id | @uri')

  echo "checking ${entity_type} ${id}" >&2
  local result
  result=$(hmac_request "api/iconik/validate/${entity_type}/${encoded_id}")
  if ! jq -e '.entities | length == 1' >/dev/null <<<"$result"; then
    echo "Error: expected one validation result for ${entity_type} ${id}" >&2
    return 1
  fi
  printf '%s\n' "$result"
  CHECKED=$((CHECKED + 1))
  sleep "$DELAY_SECONDS"
}

entity_ids=$(load_array "api/iconik/validate-entities")
while IFS=$'\t' read -r entity_type id; do
  validate_one "$entity_type" "$id"
done < <(jq -r '.[] | [.entityType, .id] | @tsv' <<<"$entity_ids")

echo "Checked ${CHECKED} Iconik objects." >&2
