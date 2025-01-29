# webapp
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


