# Webapp - HealthZ
# Prerequisites

Homebrew installed on your system
MySQL installed via Homebrew
Java
Maven should be installed

# Start the SQL server

brew services start mysql

# Start the Springboot application

Run the web application using
mvn spring-boot:run

# Begin testing the endpoints using Postman

# Turn off the SQL server
brew services stop mysql

#  Test for the edgecases using Postman when DB is turned off


# AWS Setup:

Created an AWS account with Demo and Dev environments.
Added TA users and groups for teaching assistants.

# Test Created:

testSuccessfulHealthCheck: Expected status code 200 OK.
testHealthCheckWithBody: Expected status code 400 BAD REQUEST.
testNotAllowedMethods: Tested methods (POST, PUT, DELETE, PATCH, HEAD, OPTIONS) on /healthz — all expected 405 METHOD NOT ALLOWED.
testtocheckrequestparameters: Checked query parameters — expected 400 BAD REQUEST.
testDatabaseFailureScenario: Simulated database failure — expected 503 SERVICE UNAVAILABLE.

## Generate SSH Key and View Public Key
ssh-keygen
cat ~/.ssh/id_rsa.pub

# Copy the Script to the Server
scp -i /Users/saurabhgangal/.ssh/do3 /Users/saurabhgangal/uni/CSYE6225/webapp/setup_webapp.sh root@67.205.131.210:/opt/cyse6225/script2

# Make the Script Executable
chmod +x /opt/cyse6225/script2/setup_webapp.sh

# Create Necessary Directory
mkdir -p /opt/cyse6225/script2

# Run the Script
./setup_webapp.sh

# Access Hidden Files
ls -l /opt/cyse6225/  # to view hidden files
