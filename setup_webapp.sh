#!/bin/bash

#db ke configs
DB_NAME="webapp"
DB_USER="saurabh"
DB_ROOT_PASS="Saurabh"
DB_ENGINE="mysql"
APP_GROUP="saurabh_group"
APP_USER="saurabh_user"
APP_DIR="/opt/csye6225"
ZIP_FILE="webapp.zip" # yha par file dalni hai when i go full remote

apt update -y && apt upgrade -y

apt install -y mysql-server unzip

echo "PART 2."
systemctl enable mysql
systemctl start mysql

echo "PART 3"
mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Saurabh'; FLUSH PRIVILEGES;"

mysql -u root -p"Saurabh" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME;"
mysql -u root -p"Saurabh" -e "CREATE USER IF NOT EXISTS 'saurabh'@'%' IDENTIFIED BY 'saurabh';"
mysql -u root -p"Saurabh" -e "GRANT ALL PRIVILEGES ON $DB_NAME.* TO 'saurabh'@'%';"

groupadd saurabh_group
useradd -m -g saurabh_group -s /bin/bash saurabh_user

# Setup application directory
echo "Creating and setting up application directory..."
mkdir -p /opt/csye6225
chown saurabh_user:saurabh_group /opt/csye6225
chmod 750 /opt/csye6225


unzip -o webapp.zip -d /opt/csye6225

#OWNER - READ WRITE EXECUTE
#GROUP - SIRF R & E
chown -R saurabh_user:saurabh_group /opt/csye6225
chmod -R 750 /opt/csye6225

# to move file is $0
mv $0 /opt/csye6225/

echo "Setup is complete!"
