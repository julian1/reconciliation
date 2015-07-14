#!/bin/bash

DB='jdbc:postgresql://dbprod.emii.org.au/harvest?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory'

. ./credentials.sh

# 
if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
	-db "$DB" -u "$USER" -p "$PASS" \
	-bucket		imos-data \
    -prefix		IMOS/OceanCurrent/GSLA \
    -s3Regex	'^IMOS/OceanCurrent/GSLA/DM00/.*/.*_GSLA_FV02_DM00_.*.nc.gz$' \
    -schema		gsla_dm00
fi

if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
	-db "$DB" -u "$USER" -p "$PASS" \
	-bucket		imos-data \
    -prefix		IMOS/OceanCurrent/GSLA \
    -s3Regex	'^IMOS/OceanCurrent/GSLA/NRT00/.*/.*_GSLA_FV02_NRT00_.*.nc.gz$' \
    -schema		gsla_nrt00
fi


