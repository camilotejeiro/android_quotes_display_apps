#!/bin/bash
# ======================================================================
# Android Applications logcat script.
# ======================================================================

source application_properties.sh

# initialize our vars.
log_filter_index=0
log_filter=""

# get length of our classes array
classes_array_length=${#package_classes[@]}

# ---
# Build our logcat filters.
# ---
# Putting the package classes from our properties file that we would like 
# to debug
for ((index=0; index<classes_array_length; index++))
do
    log_tag[log_filter_index]="${package_classes[index]}"
    log_priority[log_filter_index++]='D'
done

# Warnings and above all are logged.
log_tag[log_filter_index]='*';
log_priority[log_filter_index++]='W'

# whitelist our application filters and blacklist(silence) everything else 
# log_tag[log_filter_index]='*'
# log_priority[log_filter_index++]='S'

# ---
# Apply logcat filters.
# ---
printf "%s\n" '* Starting android applications debugging script'

# concatenate, append and create our filters.
for ((index=0; index<$log_filter_index; index++))
do
    log_filter+="${log_tag[index]}:${log_priority[index]} "
done

printf "\n%s\n\n" "adb logcat $log_filter"

adb logcat $log_filter
