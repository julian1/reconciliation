#!/bin/bash

DB='jdbc:postgresql://dbprod.emii.org.au/harvest?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory'

. ./credentials.sh

# cars
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     CSIRO/Climatology/CARS \
    -s3Regex    '^CSIRO/Climatology/CARS/.*monthly.nc$' \
    -schema     generic_timestep \
    -dbRegex    '^CSIRO/Climatology/CARS/.*monthly.nc$'



# cars weekly
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     CSIRO/Climatology/CARS \
    -s3Regex    '^CSIRO/Climatology/CARS/.*weekly.nc$' \
    -schema     generic_timestep \
    -dbRegex    '^CSIRO/Climatology/CARS/.*weekly.nc$'

