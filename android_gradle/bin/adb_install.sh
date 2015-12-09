#!/bin/bash
# ======================================================================
# Android Applications, install development script
# ======================================================================

# include our application properties.
source application_properties.sh

# ---
# Uninstall Prior versions of Application (just in case).
# ---
echo "* Uninstalling the $app_package_name App *"
echo ""
echo "adb uninstall $app_package_name"
echo ""

adb uninstall $app_package_name

# ---
# Install Application (debup release).
# ---
echo "* Installing the $application_name App *"
echo ""
echo "adb install ../RandomQuotes/$application_name/build/outputs/apk/$application_name-$release_mode.apk"
echo ""

adb install "../RandomQuotes/$application_name/build/outputs/apk/$application_name-$release_mode.apk"
