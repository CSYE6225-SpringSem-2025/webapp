#!/bin/bash

# Exit on any error
set -e

# Configuration variables
DB_NAME="webapp"
DB_USER="saurabh"
DB_ROOT_PASS="Saurabh"
DB_USER_PASS="saurabh"
APP_GROUP="saurabh_group"
APP_USER="saurabh_user"
APP_DIR="/opt/csye6225"
JAR_NAME="webapp-0.0.1-SNAPSHOT.jar"

echo "Starting setup script..."

# Update system packages
echo "Updating system packages..."
sudo apt-get update -y && sudo apt-get upgrade -y

# Install required packages
echo "Installing required packages..."
sudo apt-get install -y mysql-server openjdk-17-jdk unzip

# Configure MySQL
echo "Configuring MySQL..."
sudo systemctl enable mysql
sudo systemctl start mysql

# Configure MySQL root password and create database
echo "Setting up MySQL users and database..."
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '$DB_ROOT_PASS';"
sudo mysql -e "FLUSH PRIVILEGES;"

# Create database and user
sudo mysql -u root -p"$DB_ROOT_PASS" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME;"
sudo mysql -u root -p"$DB_ROOT_PASS" -e "CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_USER_PASS';"
sudo mysql -u root -p"$DB_ROOT_PASS" -e "GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';"
sudo mysql -u root -p"$DB_ROOT_PASS" -e "FLUSH PRIVILEGES;"

# Create application user and group
echo "Creating application user and group..."
sudo groupadd $APP_GROUP || echo "Group already exists"
sudo useradd -m -g $APP_GROUP -s /bin/bash $APP_USER || echo "User already exists"

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

# Move setup script to application directory for reference
sudo mv $0 $APP_DIR/

echo "Setup is complete!"