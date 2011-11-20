#!/bin/bash

if [[ "$BASH_SOURCE" == "$0" ]]
then
    echo this script must be sourced so that it can modify
    echo the current shell\'s environment variables
    exit
fi

stty -echo
read -p "Sauce Labs Username: " username
echo
read -p "Sauce Labs API Key: " api_key
echo
stty echo
 
export SAUCE_LABS_USERNAME=$username
export SAUCE_LABS_API_KEY=$api_key
 
echo 'Username exported to $SAUCE_LABS_USERNAME'
echo 'API Key exported to $SAUCE_LABS_API_KEY'
