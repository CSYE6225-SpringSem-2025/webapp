#!/bin/bash

# Exit on any error
set -e

# Configuration variables
APP_GROUP="saurabh_group"
APP_USER="saurabh_user"
APP_DIR="/opt/csye6225"
JAR_NAME="webapp-0.0.1-SNAPSHOT.jar"

echo "Starting setup script..."

# Update package lists and fix any broken dependencies
echo "Updating system packages..."
sudo apt-get update
sudo apt-get clean
sudo apt-get autoremove -y
sudo DEBIAN_FRONTEND=noninteractive apt-get upgrade -y

# Install dependencies
echo "Installing required packages..."
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y software-properties-common
sudo add-apt-repository -y universe
sudo apt-get update

# Install Java and MySQL client only (NOT server)
echo "Installing Java and MySQL client..."
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y \
    openjdk-17-jre \
    mysql-client

# Create application user and group
echo "Creating application user and group..."
sudo groupadd -f $APP_GROUP
sudo id -u $APP_USER &>/dev/null || sudo useradd -m -g $APP_GROUP -s /bin/bash $APP_USER

# Setup application directory
echo "Creating and setting up application directory..."
sudo mkdir -p $APP_DIR

# Move application files
echo "Moving application files..."
sudo mv /tmp/$JAR_NAME $APP_DIR/
sudo mv /tmp/application.properties $APP_DIR/

# Set permissions
echo "Setting permissions..."
sudo chown -R $APP_USER:$APP_GROUP $APP_DIR
sudo chmod -R 750 $APP_DIR

# Setup systemd service
echo "Setting up systemd service..."
sudo mv /tmp/webapp.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable webapp.service

# Verify installations
echo "Verifying installations..."
java -version
mysql --version

echo "Setup is complete!"