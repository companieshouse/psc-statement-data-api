#!/bin/bash

# Start script for psc-statement-data-api


PORT=8080
exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "psc-statement-data-api.jar"
