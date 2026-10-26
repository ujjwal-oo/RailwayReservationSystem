#!/bin/bash
# Compile and run the console demo (Main). Use ./run-web.sh for the web app.
set -e
rm -f railway.db
mkdir -p bin
javac -cp lib/sqlite-jdbc-3.40.1.0.jar -d bin $(find src -name "*.java")
java -cp "bin:lib/sqlite-jdbc-3.40.1.0.jar" main.Main
