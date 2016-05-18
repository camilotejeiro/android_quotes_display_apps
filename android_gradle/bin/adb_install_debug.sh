#!/bin/bash
# ======================================================================
# Android Applications, complete install and debug development script
# ======================================================================

# include our application properties.
source application_properties.sh

# ---
# Install Application.
# ---
source adb_install.sh

# ---
# Logcat Debugging Script.
# ---
source adb_debug.sh

