#!/bin/bash

if [[ "$BASH_SOURCE" == "$0" ]]
then
    echo this script must be sourced so that it can modify
    echo the current shell\'s environment variables
    exit
fi

stty -echo
read -p "AWS Email Access Key: " aws_email_access_key
echo
read -p "AWS Email Secret Key: " aws_email_secret_key
echo
read -p "Elastic Email Username: " elastic_email_username
echo
read -p "Elastic Email API Key: " elastic_email_api_key
echo
read -p "GMail Username: " gmail_username
echo
read -p "GMail Password: " gmail_password
echo
read -p "Pop GMail Username: " pop_gmail_username
echo
read -p "Pop GMail Password: " pop_gmail_password
echo
stty echo
 
export AWS_EMAIL_ACCESS_KEY=$aws_email_access_key
export AWS_EMAIL_SECRET_KEY=$aws_email_secret_key
export ELASTIC_EMAIL_USERNAME=$elastic_email_username
export ELASTIC_EMAIL_API_KEY=$elastic_email_api_key
export GMAIL_USERNAME=$gmail_username
export GMAIL_PASSWORD=$gmail_password
export POP_GMAIL_USERNAME=$pop_gmail_username
export POP_GMAIL_PASSWORD=$pop_gmail_password

echo 'AWS Email Access Key exported to $AWS_EMAIL_ACCESS_KEY'
echo 'AWS Email Secret Key exported to $AWS_EMAIL_SECRETY_KEY'
echo 'Elastic Email Username exported to $ELASTIC_EMAIL_USERNAME'
echo 'Elastic Email API Key exported to $ELASTIC_EMAIL_API_KEY'
echo 'GMail Username exported to $GMAIL_USERNAME'
echo 'GMail Password exported to $GMAIL_PASSWORD'
echo 'Pop GMail Username exported to $POP_GMAIL_USERNAME'
echo 'Pop GMail Password exported to $POP_GMAIL_PASSWORD'
