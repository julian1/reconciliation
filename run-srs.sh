#!/bin/bash

DB='jdbc:postgresql://dbprod.emii.org.au/harvest?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory'

. ./credentials.sh

# "^IMOS/SRS/ALTIMETRY/.*\\.nc$"
if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     IMOS/SRS/ALTIMETRY \
    -s3Regex    '.*\.nc$' \
    -schema     srs_altimetry
fi


# "^IMOS/SRS/OC/gridded/.*\\.nc$
if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     IMOS/SRS/OC/gridded \
    -s3Regex    '.*\.nc$' \
    -schema     srs_oc
fi


# "^IMOS/SRS/OC/BODBAW/.*\\.nc$"
# OK no files,
if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     IMOS/SRS/OC/BODBAW \
    -s3Regex    '.*\.nc$' \
    -schema     srs_oc_bodbaw
fi


# OK, one file only,
# "^IMOS/SRS/OC/LJCO/AERONET/Lucinda\\.lev20$"
if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     IMOS/SRS/OC/LJCO/AERONET \
    -s3Regex    '^IMOS/SRS/OC/LJCO/AERONET/Lucinda.lev20$' \
    -schema     srs_oc_ljco_aeronet
fi

# OK, one file
# "^IMOS/SRS/OC/LJCO/.*\\.nc$"
if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     IMOS/SRS/OC/LJCO \
    -s3Regex    '.*\.nc$' \
    -schema     srs_oc_ljco_wws
fi

# no issues
# "^IMOS/SRS/OC/radiometer/.*\\.nc$"
if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     IMOS/SRS/OC/radiometer \
    -s3Regex    '.*\.nc$' \
    -schema     srs_oc_soop_rad
fi

# sst 
# databag, "^IMOS/SRS/SST/ghrsst/.*\\.nc$"
if true; then
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar \
    -db "$DB" -u "$USER" -p "$PASS" \
    -bucket     imos-data \
    -prefix     IMOS/SRS/SST/ghrsst \
    -s3Regex    '.*\.nc$' \
    -schema     srs_sst
fi

