printf "%s\n" 'Important: You need to source this script in order to set the environment variables.'
printf "%s\n\n" 'Note: as you type characters wont be diplayed in the terminal'

read -s -p "Please enter the keystore password: " keystore_password
export ANDROID_KEYSTORE_PASSWORD=$keystore_password

printf "\n%s\n" "* The ANDROID_KEYSTORE_PASSWORD variable is set"

read -s -p "Please enter the keystore key: " keystore_key
export ANDROID_KEYSTORE_KEY=$keystore_key

printf "\n%s\n" "* The ANDROID_KEYSTORE_KEY variable is set"
