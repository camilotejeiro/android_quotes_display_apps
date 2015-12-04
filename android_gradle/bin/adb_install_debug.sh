#!/bin/bash
# ======================================================================
# Android Applications, complete install and debug development script
# ======================================================================

# include our application properties.
source application_properties.sh

# ---
# Uninstall Prior versions of Application (just in case).
# ---
echo "* Uninstalling the $package_name App *"
echo ""
echo "adb uninstall $package_name"
echo ""

adb uninstall $package_name

# ---
# Install Application (debup release).
# ---
echo "* Installing the $application_name App *"
echo ""
echo "adb install ../$application_name/build/outputs/apk/$application_name-$release_mode.apk"
echo ""

adb install "../$application_name/build/outputs/apk/$application_name-$release_mode.apk"


# Logcat Debugging Script.
source adb_debug.sh
