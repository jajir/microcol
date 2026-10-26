#!/bin/bash
set -euo pipefail
budget=()
for argument in "$@"; do
    if [[ "$argument" == --smoke ]]; then budget=(--budget 300); fi
done
exec python3 "$(dirname "$0")/local_slot.py" "${budget[@]}" -- python3 "$(dirname "$0")/verify.py" "$@"
