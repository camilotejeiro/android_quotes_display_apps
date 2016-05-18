#!/bin/bash
# ======================================================================
# Android Applications, install development script
# ======================================================================

# include our application properties.
source application_properties.sh

# ---
# Uninstall Prior versions of Application (just in case).
# ---
printf "%s\n\n" "* Uninstalling the $app_package_name App"

printf "%s\n" "adb uninstall $app_package_name"


adb uninstall $app_package_name

# ---
# Install Application (debup release).
# ---
printf "\n%s\n\n" "* Installing the $application_name App"

printf "%s\n" "adb install ../RandomQuotes/$application_name/build/outputs/apk/$application_name-$release_mode.apk"

adb install "../RandomQuotes/$application_name/build/outputs/apk/$application_name-$release_mode.apk"
