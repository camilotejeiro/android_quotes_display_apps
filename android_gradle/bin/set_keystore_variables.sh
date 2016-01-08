echo "You need to source this script in order to set the environment variables."

read -p "Please enter the keystore password: " keystore_password
export ANDROID_KEYSTORE_PASSWORD=$keystore_password

echo "* The ANDROID_KEYSTORE_PASSWORD variable is set"

read -p "Please enter the keystore key: " keystore_key
export ANDROID_KEYSTORE_KEY=$keystore_key

echo "* The ANDROID_KEYSTORE_KEY variable is set"
