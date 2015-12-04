#!/bin/bash
# ======================================================================
# Android Applications logcat script.
# ======================================================================

source application_properties.sh

# initialize our vars.
log_filter_index=0
log_filter=""

# ---
# Build our logcat filters.
# ---
# Warnings and above are logged.
log_tag[log_filter_index]='*';
log_priority[log_filter_index++]='W'

# Add clasess we would like to debug. 
log_tag[log_filter_index]="$package_name.RandomQuotesWidget";
log_priority[log_filter_index++]='D'
log_tag[log_filter_index]="$package_name.AppConfiguration";
log_priority[log_filter_index++]='D'
log_tag[log_filter_index]="$package_name.PreferencesStorage";
log_priority[log_filter_index++]='D'


# whitelist our application filters and blacklist(silence) everything else 
log_tag[log_filter_index]='*'
log_priority[log_filter_index++]='S'

# ---
# Apply logcat filters.
# ---
echo "* Starting android applications debugging script *"

# concatenate, append and create our filters.
for ((index=0; index<$log_filter_index; index++))
do
    log_filter+="${log_tag[index]}:${log_priority[index]} "
done

echo ""
echo "adb logcat $log_filter"
echo ""

adb logcat $log_filter
