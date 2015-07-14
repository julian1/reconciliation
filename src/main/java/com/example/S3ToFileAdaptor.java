
package com.example;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
// import java.lang.Exception;
import java.io.IOException;

import java.io.File;

import java.io.ByteArrayOutputStream;

import com.example.S3Browser;



public class S3ToFileAdaptor
{
    private S3Browser browser;
    private String cacheLocation;

    private long totalBytes;
    private long totalCount;



    // should ensure that we have th
    // this should be factored out of here.,

    public S3ToFileAdaptor( S3Browser browser, String cacheLocation) {
        this.browser = browser;
        this.cacheLocation = cacheLocation;
        this.totalBytes = 0;
        this.totalCount = 0;
    }   

    private void copyStream(InputStream input, OutputStream output)
        throws IOException
    {   
        // avoid dependency on org.apache.commons.io.IOUtils
        byte[] buffer = new byte[16384]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) 
        {   
            output.write(buffer, 0, bytesRead);
            totalBytes += bytesRead;
        }   
    }   


    private byte [] streamToByteArray(InputStream input)
        throws IOException
    {   
 
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            totalBytes += nRead;
        }

        buffer.flush();
        return buffer.toByteArray();
    }


    private String getFilePath(String key)
    {   
        return cacheLocation + "/" + key.replace( "/", "-" ); // kiss
    }   



    public byte [] getObjectBytes( String key ) throws IOException  {

        System.out.println( "JA " + (System.currentTimeMillis() /1000) 
          + " S3ToFile getObject() " + key 
          + " totalBytes " + totalBytes 
          + " totalCount " + totalCount
        );

        ++totalCount;

        InputStream is = browser.getObject( key );
        return streamToByteArray(is);
    }   



    public String getObjectFilename( String key ) throws IOException  {
    // public String getObject( String key ) {
        // returns the filename

        System.out.println( "JA " + (System.currentTimeMillis() /1000) 
          + " S3ToFile getObject() " + key 
          + " totalBytes " + totalBytes 
          + " totalCount " + totalCount
        );

        ++totalCount;

        InputStream is = null;
        OutputStream os = null;
        String filename = cacheLocation + "/" + key.replace( "/", "-" ); // kiss
        try {
            is = browser.getObject( key );
            // should delete file first? or try without
            os = new FileOutputStream( getFilePath(key));
            copyStream( is, os);
        }   
        catch( IOException e ) { 
            System.out.println( "JA exception " + e.getMessage() );
            throw e;
        } finally {
            is.close();
            os.close();
        }   

        // System.out.println( "&&&&&&&&&&&\n JA S3ToFile returning  " + filename );
        // should return a File structure
        return filename;
    }   

    public void closeFile( String filename ) { 

        System.out.println( "JA S3ToFile.closeObject() " + filename );

        try {
            // boolean result = new File(getFilePath(key)).delete();
            boolean result = new File(filename).delete();
            System.out.println( "JA result " + result );

        } catch( Exception e) {
            e.printStackTrace();
        }   
    }   
}

