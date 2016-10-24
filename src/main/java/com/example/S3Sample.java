
package com.example;

import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.S3Browser;
// import com.example.S3ToFileAdaptor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ListObjectsRequest;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;

import java.sql.*;


import java.util.Map;
import java.util.HashMap;

import java.lang.RuntimeException;



interface Event {

    // public string format();
}


class PropertyEvent implements Event
{
    public String name;
    public String value;

    public PropertyEvent(String name, String value) {

        this.name = name;
        this.value = value;
    }
}


class S3Event implements Event
{
    public String url;

    public S3Event(String url) {

        this.url = url;
    }
}


class DbEvent implements Event
{
    public String   url;
    public long     id;
    public boolean  deleted;

    public DbEvent(String url, long id, boolean deleted) {

        this.id = id;
        this.url = url;
        this.deleted = deleted;
    }
}


interface ReportBuilder {

    public void event( Event event);
    public void finish();
}


// TODO change name ReportStateMachine

class ReportBuilder1 implements ReportBuilder {

    class Record {

        String    url;
        int       db_count;
        boolean   db_deleted;
        int       s3_count;

        public Record(String url) {
            this.url = url;
            this.db_count = 0;
            this.s3_count= 0;
            this.db_deleted = false;
        }
    }

    Map<String, Record> m;

    // could treat entirely generically rather than coupling... eg. List< String, String >
    String            schema;
    String            bucket;
    String            prefix;

    Pattern           s3Regex;
    Pattern           dbRegex;

    public ReportBuilder1() {

        this.m = new HashMap<String, Record>();
    }


    public Record getRecord(String url) {

        Record r = m.get(url);
        if(r == null) {
            r = new Record(url);
            m.put(url, r);
            if(!m.containsKey(url)) {
                throw new RuntimeException("couldn't get record!!!!");
            }
        }
        return r;
    }


    public void event(Event event)
        throws RuntimeException {


        if(event instanceof PropertyEvent) {

            PropertyEvent e = (PropertyEvent)event;
            if(e.name == "schema")
                schema = e.value;
            else if(e.name == "bucket")
                bucket = e.value;
            else if(e.name ==  "prefix")
                prefix = e.value;
            else if(e.name == "s3Regex") {
                s3Regex = Pattern.compile(e.value); // ".*\\.nc$";
            } else if(e.name == "dbRegex") {
                dbRegex = Pattern.compile(e.value);
            }
        }
        else if(event instanceof S3Event) {

            S3Event e = (S3Event)event;

            Helpers.clearTerminal();
            System.err.print( "a S3 event " + m.size() + " " + Helpers.truncate(e.url, 80) + '\r' );

            // ignore anything that doesn't match the pattern
            if(s3Regex == null || s3Regex.matcher(e.url).matches()) {

                Record r = getRecord(e.url);
                r.s3_count += 1;
            }

        } else if(event instanceof DbEvent) {

            DbEvent e = (DbEvent)event;

            Helpers.clearTerminal();
            System.err.print( "a Db event " + m.size() + " " + e.id + " " + Helpers.truncate(e.url, 80) + '\r' );

            if(dbRegex == null || dbRegex.matcher(e.url).matches()) {
                Record r = getRecord(e.url);
                r.db_count += 1;
                r.db_deleted = e.deleted;
            }
        } else {

            throw new RuntimeException("unknown event!!!!");
        }
    }


    public void printRecord( Record record) {

        // TODO, change to format record

        System.out.println(
              " s3_count: "     + record.s3_count
            + ", db_count: "    + record.db_count
            + ", db_deleted: "  + record.db_deleted
            + ", url: "         + record.url
        );
    }

    public void finish() {

        System.out.println("");
        System.out.println("****************");

        System.out.println("bucket:    " + bucket);
        System.out.println("prefix:    " + prefix);
        System.out.println("schema:    " + schema);
        System.out.println("s3Regex:   " + (s3Regex != null ? s3Regex.pattern() : "<none>"));
        System.out.println("dbRegex:   " + (dbRegex != null ? dbRegex.pattern() : "<none>"));

        System.out.println( "total records " + m.size()  );

        System.out.println( "================");
        System.out.println( "total counts");

        // report total counts
        int s3_count = 0;
        int db_count = 0;
        int db_count_ignore_deleted = 0;

        for (Map.Entry<String, Record> entry : m.entrySet()) {

            Record record = entry.getValue();
            s3_count += record.s3_count;
            db_count += record.db_count;
  
            if(!record.db_deleted) 
              db_count_ignore_deleted += record.db_count;
        }

        System.out.println("s3_count " + s3_count);
        System.out.println("db_count " + db_count);
        System.out.println("db_count_ignore_deleted " + db_count_ignore_deleted);
        System.out.println("s3_count - db_count_ignore_deleted " + (s3_count - db_count_ignore_deleted));


        System.out.println( "================");
        System.out.println( "records with counts greater than one");

        // report counts more than one...
        for (Map.Entry<String, Record> entry : m.entrySet()) {

            Record record = entry.getValue();
            if( record.s3_count > 1) {
                printRecord(record);
            }
            if( record.db_count > 1) {
                printRecord(record);
            }
        }


        System.out.println( "================");
        System.out.println( "s3 / db differences");

        for (Map.Entry<String, Record> entry : m.entrySet()) {

            Record record = entry.getValue();

            // report if different
            if(record.s3_count != record.db_count) {

              // ignore deleted instances, still in the db.
              if(record.s3_count == 0 && record.db_count == 1 && record.db_deleted == true)
                  continue;

              System.out.println(
                    " s3_count: "     + record.s3_count
                  + ", db_count: "    + record.db_count
                  + ", db_deleted: "  + record.db_deleted
                  + ", url: "         + record.url
            );
            }
        }
    }
}





class Helpers {


    static String truncate(String s, int size) {

        if(s.length() > size) {
            // chop out the middle, since prefix and suffix are most important
            return s.substring(0, size - 10) + "..." + s.substring(s.length() - 10, s.length());
        } else {
            return s;
        }
    }

    // might be easier, if we recorded the last write size in the buffer?
    static void clearTerminal() {

        // http://forum.codecall.net/topic/59142-how-to-clear-the-console-screen-with-ansi-any-language/
        // https://github.com/mikiobraun/java-terminal/blob/master/src/main/java/org/marge/javaterminal/Terminal.java
        System.err.print('\033');
    }

    static void clearTerminalAttributes() {

        System.err.print("\033[0m");
        System.out.print("\033[0m");
    }

/*
    static void processS3Objects( S3Browser browser, String prefix, ReportBuilder report) throws IOException
    {
        // DEPRECATED!

        // we only use recursion on things that are not complete file
        // although we could set up the recursion to work in parallel,

        // drill-down on incomplete keys
        for (String dir : browser.getDirs(prefix)) {
            // System.out.println(" - " + dir );
            processS3Objects( browser, dir, report);
        }

        // get objects at the current level
        for( String file : browser.getFiles( prefix)) {
            report.event(new S3Event(file));
            // ++count;
            // bail out early,
        }
    }
*/


    static void processS3Objects2( AmazonS3 s3, String bucketName, String prefix, ReportBuilder report) throws IOException
    {
        // add date of report...
        report.event(new PropertyEvent("bucket", bucketName));
        report.event(new PropertyEvent("prefix", prefix));

        ObjectListing objectListing = s3.listObjects(
            new ListObjectsRequest()
            .withBucketName(bucketName)
            .withPrefix(prefix)
            // .withDelimiter("/")
        );

        while(objectListing != null) {

            for( S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                String url = summary.getKey();
                report.event(new S3Event(url));
            }

            if(objectListing.isTruncated()) {
                objectListing = s3.listNextBatchOfObjects(objectListing);
            } else {
                objectListing = null;
            }
        }
    }


    static void processDbEntries(Connection conn, String schema, ReportBuilder report)
      throws java.sql.SQLException
      {
          report.event(new PropertyEvent("schema", schema));

          // Batch results set
          conn.setAutoCommit(false);

          String query = "select id,url,deleted from " + schema + ".indexed_file ";

          PreparedStatement s = conn.prepareStatement(query);

          s.setFetchSize(10000);

          ResultSet r = s.executeQuery();

          while (r.next()) {

              long id         = r.getLong("id");
              String url      = r.getString("url");
              boolean deleted = r.getBoolean("deleted");

              report.event( new DbEvent(url, id, deleted));
          }
    }


    static Connection getConnection(CommandLine cmd, Options options) {

        String username = cmd.getOptionValue("u");
        String password = cmd.getOptionValue("p");
        String connectionString = cmd.getOptionValue("d");
        String databaseDriver = cmd.getOptionValue("D", "org.postgresql.Driver");

        // be much nicer if didn't have to pass options here,
        if (username == null
            || password == null
            || connectionString == null
            || databaseDriver == null) {
            // TODO, shouldn't be refering to the more specific class here,
            S3Sample.usage(options);
        }

        try {
            Class.forName(databaseDriver).newInstance();
        }
        catch (Exception e){
            System.out.printf("Check classpath. Cannot load db driver: '%s'%n", databaseDriver);
            e.printStackTrace();
            System.exit(1);
        }

        // TODO fix resource management
        Connection result = null;

        try {
            result = DriverManager.getConnection(connectionString, username, password);
        }
        catch (SQLException e){
            System.out.printf("Cannot connect to db: '%s'%n", connectionString);
            e.printStackTrace();
            System.exit(1);
        }

        return result;
    }
}




public class S3Sample {

    // TODO this function should be in the pool,

    // Pattern isFilePattern;
    // use a context for all this?  or use this class as a context,

    // static string buf = "";

    public static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("S3Sample", options);
        System.exit(1);
    }

    public static void main(String[] args)
        throws Exception {

        /* The first parameter is a java.lang.String that represents the option. The
        second parameter is a boolean that specifies whether the option requires an
        argument or not. In the case of a boolean option (sometimes referred to as a
        flag) an argument value is not present so false is passed. The third parameter
        is the description of the option. This description will be used in the usage
        text of the application.
        */

        Options options = new Options();

        // db credentials
        options.addOption("u", "username", true, "Database user.");
        options.addOption("p", "password", true, "Database password.");
        options.addOption("d", "db",       true, "Database connection string.");
        options.addOption("D", "driver",   true, "Database driver class.");  // TODO make optional

        // schema,
        options.addOption("schema",  "schema",   true, "schema");
        options.addOption("dbRegex", "dbRegex",  true, "db regex");

        // s3
        options.addOption("bucket",  "bucket",   true, "s3 bucket");
        options.addOption("prefix",  "prefix",   true, "s3 prefix");    // Change name to prefix...
        options.addOption("s3Regex", "s3Regex",  true, "s3 regex");


        // should probably only parse once...
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            usage(options);
        }

        // try to get connection as first thing we do, to avoid delaying errors in authentication
        // until after the s3 bucket has been scanned
        Connection conn = Helpers.getConnection(cmd, options);

        // AmazonS3 s3 = new S3Authenticate().doit("./aws_credentials" , "default");
        AmazonS3 s3 = new AmazonS3Client();

        String schema = cmd.getOptionValue("schema");
        String bucket = cmd.getOptionValue("bucket");
        String prefix = cmd.getOptionValue("prefix");
        String s3Regex = cmd.getOptionValue("s3Regex");
        String dbRegex = cmd.getOptionValue("dbRegex");

        if (schema == null
            || bucket == null
            || prefix == null
          ) {
            S3Sample.usage(options);
        }

        // -r 1
        ReportBuilder report = new ReportBuilder1();

        if(s3Regex != null) {
            report.event(new PropertyEvent("s3Regex", s3Regex));
        }
        if(dbRegex != null) {
            report.event(new PropertyEvent("dbRegex", dbRegex));
        }


        S3Browser browser = new S3Browser( s3, bucket  );

        // Helpers.processS3Objects( browser, prefix, report);

        Helpers.processS3Objects2(s3, bucket, prefix, report);

        Helpers.processDbEntries(conn, schema, report) ;

        report.finish();
    }
}





// imos-data/IMOS/SRS/
// S3ToFileAdaptor s3ToFileAdaptor = new S3ToFileAdaptor( browser, "/tmp/ncwms" );
// recurse( pool, browser, s3ToFileAdaptor, "" );
// recurse( pool, browser, s3ToFileAdaptor, "/IMOS/ACORN/gridded_1h-avg-current-map_QC/ROT/2014/01" );
// recurse( pool, browser, s3ToFileAdaptor, "/IMOS/SRS/sst/ghrsst/L3U-S/n19/2015" );
            // System.out.println(" got " + file );
            // System.out.println( file );
            // pool.post( new WorkerThread( s3ToFileAdaptor, file ) );


/*
class SimpleThreadPool {
  // TODO do we even need this...

   ExecutorService executor ;

    public SimpleThreadPool() {
        executor = Executors.newFixedThreadPool(15);
    }

    public void post( Runnable worker )
    {
        executor.execute(worker);
    }

    public void waitForCompletion()
    {
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Thread.sleep(1000ms) etc ...
        }
    }
}


class WorkerThread implements Runnable {

    private S3ToFileAdaptor s3ToFileAdaptor;
    private String file;

    public WorkerThread( S3ToFileAdaptor s3ToFileAdaptor, String s){
        this.s3ToFileAdaptor = s3ToFileAdaptor;
        this.file=s;
    }

    @Override
    public void run() {
        // System.out.println(Thread.currentThread().getName()+" Start. Command = "+file);
        // processCommand();
        try {

            String f = s3ToFileAdaptor.getObjectFilename( file );
            s3ToFileAdaptor.closeFile( f );


//            extract as in-memory buffer
 //           byte [] buf = s3ToFileAdaptor.getObjectBytes( file );


        } catch( IOException e )
        {
            e.printStackTrace();
        }
        // System.out.println(Thread.currentThread().getName()+" End.");
    }

    @Override
    public String toString(){
        return this.file;
    }
}
*/


/*
      // String to be scanned to find the pattern.
      String line = "This order was placed for QT3000! OK?";
      String pattern = "(.*)(\\d+)(.*)";

      // Create a Pattern object
      Pattern r = Pattern.compile(pattern);

      // Now create matcher object.
      Matcher m = r.matcher(line);
      if (m.find( )) {

*/


