#!/bin/bash

# Start script for psc-statement-data-api


PORT=8080
exec java -jar -Dserver.port="${PORT}" "psc-statement-data-api.jar"
