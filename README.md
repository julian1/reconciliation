
Ok, there's an issue in that S3 and db, will be out of sink depending on order of scan
  - eg, scan3, and while scanning new objects are added to db and s3, 
    but they won't be picked up in the db scan.


objects for which there is no index? - means the only way to know about it is to scan the s3 keys.
  ideally everything would be indexed.

  - would be nice to know...

### Notes,

would be maybe good to create a single view - representing all the schemas 
  - to enable finding a file, when we have no idea where it exists,


Use , comma separator

### Build
```
mvn clean install
```

#### to find the s3 key  - just go into the db, and look at the indexes

### Run
```
java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar

./run.sh

```



### TODO
- done - use mvn shade plugin.
- done - move the s3 authentication stuff out of the s3 browser class,
- done - support public authentication access
- done - use most recent s3 jdk version

- write very specific job actions - and then use command flag processing to test?

- get rid of the simplethreadpool and just use the executor...

- dump listing to file 
- or store in memory, handle 
- or store in a disk?
- retrieve from db?
- ncwms - code for cdm? 

---
Ok, it's retarded, it's trying to aggregate the jars from the previously mvn built project.

java -jar ./target/s3-example-1.0-SNAPSHOT-shaded.jar -db 'jdbc:postgresql://dbprod.emii.org.au/harvest?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory'  -u jfca -p xxxx 

----
So think we should pass off the command line options for the db  instantiation - separately... 

- may want to make the individual file lookup - in the db multithreaded...
- if handle in memory...


---

m.size() 65936
 s3_count: 0 db_count: 1 db_deleted: false url: IMOS/SRS/OC/LJCO/WQM-hourly/2015/01/14/IMOS_SRS-OC-LJCO_KOSTUZ_20150114T110840Z_SRC_FV01_WQM-hourly.nc
 s3_count: 1 db_count: 0 db_deleted: false url: IMOS/SRS/OC/LJCO/AERONET/Lucinda.lev20

