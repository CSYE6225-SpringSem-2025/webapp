#!/bin/bash

# Exit on any error
set -e

# Configuration variables
DB_NAME="webapp"
DB_USER="saurabh"
DB_ROOT_PASS="root"
DB_USER_PASS="saurabh"
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

# Install MySQL and Java with their dependencies
echo "Installing MySQL and Java..."
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y \
    mysql-server \
    openjdk-17-jre \
    libaio1 \
    libncurses5

# Start and configure MySQL
echo "Starting MySQL service..."
sudo systemctl start mysql
sudo systemctl enable mysql

# Configure MySQL root password and create database
echo "Configuring MySQL..."
# Set root password
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH auth_socket;"
sudo mysql -e "CREATE DATABASE IF NOT EXISTS $DB_NAME;"
sudo mysql -e "CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_USER_PASS';"
sudo mysql -e "GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';"
sudo mysql -e "FLUSH PRIVILEGES;"

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
sudo systemctl status mysql --no-pager

echo "Setup is complete!"