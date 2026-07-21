#!/usr/bin/env bash
set -euo pipefail

# Backfills the asset versions table with entries from the upload pipeline cache.
#
# For each upload in the cache, creates a VersionClaim in the assets table with:
#   id: "<atomId>-<version>"
#   claimSource: UploadPipeline
#
# Uses batch-write-item for efficiency. Note: batch-write-item does not support
# condition expressions, so existing items will be overwritten with the same data.
# This is safe because the backfill only writes idempotent UploadPipeline claims.
#
# Usage:
#   ./scripts/backfill-asset-versions.sh --stage PROD --profile media-service [--dry-run] [--region eu-west-1]

STAGE=""
PROFILE=""
REGION="eu-west-1"
DRY_RUN=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --stage)
      STAGE="$2"
      shift 2
      ;;
    --profile)
      PROFILE="$2"
      shift 2
      ;;
    --region)
      REGION="$2"
      shift 2
      ;;
    --dry-run)
      DRY_RUN=true
      shift
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

if [[ -z "$STAGE" ]]; then
  echo "Error: --stage is required (e.g. PROD, CODE)"
  exit 1
fi

if [[ -z "$PROFILE" ]]; then
  echo "Error: --profile is required (e.g. media-service)"
  exit 1
fi

CACHE_TABLE="media-atom-pipeline-cache-${STAGE}"
ASSETS_TABLE="media-atom-maker-${STAGE}-atom-assets"
NOW_MS=$(( $(date +%s) * 1000 ))

echo "Cache table:  ${CACHE_TABLE}"
echo "Assets table: ${ASSETS_TABLE}"
echo "Region:       ${REGION}"
echo "Profile:      ${PROFILE}"
echo "Dry run:      ${DRY_RUN}"
echo ""

INSERTED=0
SKIPPED=0
ERRORS=0
TOTAL=0
BATCH_SIZE=25
# Number of items per scan page. Keeps each response small and makes the
# pagination loop below process the table incrementally rather than loading it
# all into memory at once.
PAGE_SIZE=500

# Writes a batch of items to DynamoDB using batch-write-item.
# Retries unprocessed items with exponential backoff.
write_batch() {
  local request_json="$1"
  local attempt=0
  local max_attempts=5

  while true; do
    local result
    if ! result=$(aws dynamodb batch-write-item \
      --region "$REGION" \
      --profile "$PROFILE" \
      --request-items "$request_json" \
      2>&1); then
      echo "  ERROR: batch-write-item failed: ${result}"
      local failed_count
      failed_count=$(echo "$request_json" | jq ".\"${ASSETS_TABLE}\" | length")
      ERRORS=$((ERRORS + failed_count))
      return
    fi

    local unprocessed
    unprocessed=$(echo "$result" | jq ".UnprocessedItems.\"${ASSETS_TABLE}\" // [] | length")

    if [[ $unprocessed -eq 0 ]]; then
      return
    fi

    attempt=$((attempt + 1))
    if [[ $attempt -ge $max_attempts ]]; then
      echo "  WARN: ${unprocessed} items still unprocessed after ${max_attempts} retries"
      ERRORS=$((ERRORS + unprocessed))
      return
    fi

    echo "  Retrying ${unprocessed} unprocessed items (attempt ${attempt})..."
    request_json=$(echo "$result" | jq '.UnprocessedItems')
    sleep $((2 ** attempt))
  done
}

process_page() {
  local page_json="$1"

  local count
  count=$(echo "$page_json" | jq '.Items | length')

  local batch_items=()

  for ((i = 0; i < count; i++)); do
    TOTAL=$((TOTAL + 1))

    local claim_id
    claim_id=$(echo "$page_json" | jq -r ".Items[$i].id.S")

    if [[ -z "$claim_id" || "$claim_id" == "null" ]]; then
      echo "  WARN: Skipping item $i — missing id"
      SKIPPED=$((SKIPPED + 1))
      continue
    fi

    if $DRY_RUN; then
      echo "  [DRY RUN] Would insert: ${claim_id}"
      INSERTED=$((INSERTED + 1))
      continue
    fi

    local original_filename
    original_filename=$(echo "$page_json" | jq -r ".Items[$i].metadata.M.originalFilename.S // empty")

    local start_timestamp
    start_timestamp=$(echo "$page_json" | jq -r ".Items[$i].metadata.M.startTimestamp.N // empty")
    local claim_timestamp=${start_timestamp:-$NOW_MS}

    local claimed_by_user
    claimed_by_user=$(echo "$page_json" | jq -r ".Items[$i].metadata.M.user.S // empty")

    # Build item JSON safely with jq to prevent injection
    local item_json
    item_json=$(jq -n \
      --arg id "$claim_id" \
      --argjson ts "$claim_timestamp" \
      --arg fn "$original_filename" \
      --arg claimed_by_user "$claimed_by_user" \
      '{
        PutRequest: {
          Item: (
            {id:{S:$id}, claimSource:{S:"UploadPipeline"}, claimedAtTimestamp:{N:($ts|tostring)}, claimedByUser:{S:$claimed_by_user}, fromBackfill:{BOOL:true}}
            | if $fn != "" then . + {originalFilename:{S:$fn}} else . end
          )
        }
      }')
    batch_items+=("$item_json")

    if [[ ${#batch_items[@]} -ge $BATCH_SIZE ]]; then
      local request_json
      request_json=$(printf '%s\n' "${batch_items[@]}" | jq -s --arg table "$ASSETS_TABLE" '{($table): .}')
      local errors_before=$ERRORS
      write_batch "$request_json"
      local batch_count=${#batch_items[@]}
      local new_errors=$((ERRORS - errors_before))
      INSERTED=$((INSERTED + batch_count - new_errors))
      echo "  Wrote batch of ${batch_count} items"
      batch_items=()
    fi
  done

  # Write remaining items
  if [[ ${#batch_items[@]} -gt 0 ]] && ! $DRY_RUN; then
    local request_json
    request_json=$(printf '%s\n' "${batch_items[@]}" | jq -s --arg table "$ASSETS_TABLE" '{($table): .}')
    local errors_before=$ERRORS
    write_batch "$request_json"
    local batch_count=${#batch_items[@]}
    local new_errors=$((ERRORS - errors_before))
    INSERTED=$((INSERTED + batch_count - new_errors))
    echo "  Wrote batch of ${batch_count} items"
  fi
}

echo "Scanning ${CACHE_TABLE}..."
echo ""

# Paginate through the full table using --starting-token
PAGE=1
NEXT_TOKEN=""

while true; do
  echo "Page ${PAGE}..."

  SCAN_ARGS=(
    dynamodb scan
    --table-name "$CACHE_TABLE"
    --region "$REGION"
    --profile "$PROFILE"
    --projection-expression "id, #m.originalFilename, #m.startTimestamp, #m.#u"
    --expression-attribute-names '{"#m":"metadata","#u":"user"}'
    --page-size "$PAGE_SIZE"
    --max-items "$PAGE_SIZE"
  )

  if [[ -n "$NEXT_TOKEN" ]]; then
    SCAN_ARGS+=(--starting-token "$NEXT_TOKEN")
  fi

  SCAN_RESULT=$(aws "${SCAN_ARGS[@]}")

  process_page "$SCAN_RESULT"

  NEXT_TOKEN=$(echo "$SCAN_RESULT" | jq -r '.NextToken // empty')
  if [[ -z "$NEXT_TOKEN" ]]; then
    break
  fi

  PAGE=$((PAGE + 1))
done

echo ""
echo "Done."
echo "  Total uploads processed: ${TOTAL}"
echo "  Inserted:                ${INSERTED}"
echo "  Skipped/already existed: ${SKIPPED}"
echo "  Errors:                  ${ERRORS}"
