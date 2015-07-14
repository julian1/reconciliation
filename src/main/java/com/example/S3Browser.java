
package com.example;
// package uk.ac.rdg.resc.ncwms.config;

import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;



import com.amazonaws.auth.profile.ProfilesConfigFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

// JA

/*
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
*/

public class S3Browser {

    // See, http://stackoverflow.com/questions/5455284/how-can-i-get-only-one-level-of-objects-in-a-s3-bucket

    private final AmazonS3 s3;
    private final String bucketName;

    public S3Browser( AmazonS3 s3, String bucketName ) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }


    static private String tidyListingPath( String path )
    {
        // strip leading /
        while( !path.isEmpty() && path.charAt(0) == '/') {
            path = path.substring(1);
        }

        if( path.isEmpty()) {
            return "";
        }

        // must have '/' at end to enforce objects treated as children
        if(path.charAt(path.length() -1) != '/') {
            path = path + '/';
        }
        return path;
    }

    private ObjectListing getListing( String path )
    {
        path = tidyListingPath( path);

        return s3.listObjects(new ListObjectsRequest()
            .withBucketName(bucketName)
            .withPrefix(path)
            .withDelimiter("/")
        );
    }

    public List<String> getDirs( String path )
    {
        List<String> paths = new ArrayList<String>();

        ObjectListing objectListing = getListing( path );
        while( objectListing != null) {
            paths.addAll( objectListing.getCommonPrefixes() );

            if( objectListing.isTruncated()) {
                objectListing = s3.listNextBatchOfObjects(objectListing);
            } else {
                objectListing = null;
            }
        }
        return paths;
    }


    public List<String> getFiles( String path )
    {
        List<String> paths = new ArrayList<String>();

        ObjectListing objectListing = getListing( path );
        while( objectListing != null) {
            for( S3ObjectSummary summary : objectListing.getObjectSummaries()) {
                paths.add( summary.getKey());
            }

            if( objectListing.isTruncated()) {
                objectListing = s3.listNextBatchOfObjects(objectListing);
            } else {
                objectListing = null;
            }
        }

        // System.out.println("size is " + paths.size() )  ;
        return paths;
    }


    public InputStream getObject( String key ) {
        // throw or return null....
        // so we have to work with a file ...

        while( !key.isEmpty() && key.charAt(0) == '/') {
            key = key.substring(1);
        }

        S3Object object = s3.getObject( new GetObjectRequest(bucketName, key));
        InputStream objectData = object.getObjectContent();

        // Process the objectData stream.
        // objectData.close();
        return objectData;
    }
}

