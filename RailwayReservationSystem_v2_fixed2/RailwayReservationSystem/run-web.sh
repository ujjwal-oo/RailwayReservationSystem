#!/bin/bash
# Compile and start the web app at http://127.0.0.1:8081
# (keeps railway.db; old databases are migrated automatically)
set -e
mkdir -p bin
javac -cp lib/sqlite-jdbc-3.40.1.0.jar -d bin $(find src -name "*.java")
java -cp "bin:lib/sqlite-jdbc-3.40.1.0.jar" WebServer
