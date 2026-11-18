#!/usr/bin/env bash
set -euo pipefail

sdk_dir=""
if [[ -f local.properties ]]; then
    sdk_dir=$(sed -n 's/^[[:space:]]*sdk\.dir[[:space:]]*=[[:space:]]*//p' local.properties | tail -n 1)
fi
sdk_dir=${sdk_dir:-${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}}

if [[ -n "$sdk_dir" && -x "$sdk_dir/platform-tools/adb" ]]; then
    adb="$sdk_dir/platform-tools/adb"
elif command -v adb >/dev/null 2>&1; then
    adb=$(command -v adb)
else
    echo "Error: adb not found. Configure sdk.dir in local.properties or ANDROID_HOME." >&2
    exit 1
fi

device_list=$("$adb" devices)
mapfile -t devices < <(printf '%s\n' "$device_list" | awk 'NR > 1 && $2 == "device" { print $1 }')

if (( ${#devices[@]} > 1 )); then
    echo "Error: more than one device is connected. Leave only one device connected." >&2
    exit 1
fi

if (( ${#devices[@]} == 0 )); then
    emulator="${sdk_dir:+$sdk_dir/emulator/emulator}"
    if [[ -z "$emulator" || ! -x "$emulator" ]]; then
        if command -v emulator >/dev/null 2>&1; then
            emulator=$(command -v emulator)
        else
            echo "Error: Android emulator not found. Configure sdk.dir or ANDROID_HOME." >&2
            exit 1
        fi
    fi

    avd=${AVD:-Pixel_7}
    avds=$("$emulator" -list-avds)
    if ! grep -Fxq -- "$avd" <<< "$avds"; then
        echo "Error: Android virtual device '$avd' not found. Set AVD to an available device." >&2
        exit 1
    fi

    echo "Starting Android virtual device: $avd"
    "$emulator" -avd "$avd" &
    emulator_pid=$!
    deadline=$((SECONDS + 180))
    serial=""
    while (( SECONDS < deadline )); do
        if ! kill -0 "$emulator_pid" 2>/dev/null; then
            wait "$emulator_pid" || true
            echo "Error: the emulator exited before Android was ready." >&2
            exit 1
        fi
        device_list=$("$adb" devices)
        mapfile -t devices < <(printf '%s\n' "$device_list" | awk 'NR > 1 && $2 == "device" { print $1 }')
        if (( ${#devices[@]} > 1 )); then
            echo "Error: more than one device is connected. Leave only one device connected." >&2
            exit 1
        fi
        if (( ${#devices[@]} == 1 )) && [[ ${devices[0]} == emulator-* ]]; then
            serial=${devices[0]}
            if [[ $("$adb" -s "$serial" shell getprop sys.boot_completed | tr -d '\r') == 1 ]]; then
                break
            fi
        fi
        sleep 2
    done
    if [[ -z "$serial" ]] || [[ $("$adb" -s "$serial" shell getprop sys.boot_completed | tr -d '\r') != 1 ]]; then
        echo "Error: the emulator did not finish booting within 180 seconds." >&2
        exit 1
    fi
else
    serial=${devices[0]}
fi

echo "Installing PocketSplit on $serial"
"$adb" -s "$serial" install -r app/build/outputs/apk/debug/app-debug.apk
echo "Starting PocketSplit"
start_output=$("$adb" -s "$serial" shell am start -W -n com.develop.pocketsplit/.MainActivity)
printf '%s\n' "$start_output"
if ! grep -q '^Status: ok' <<< "$start_output"; then
    echo "Error: Android did not confirm that PocketSplit started." >&2
    exit 1
fi
