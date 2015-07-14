
/*
  Note, the AWSCredentials class which
  which might be easier than reading a file,
  http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentials.html
*/

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


/*
    // http://stackoverflow.com/questions/813710/java-1-6-determine-symbolic-links
    public static boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
          canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }
*/




public class S3Authenticate {

    // See, http://stackoverflow.com/questions/5455284/how-can-i-get-only-one-level-of-objects-in-a-s3-bucket

/*
    private final AmazonS3 s3;
    private final String bucketName;

    public S3Authenticate( AmazonS3 s3, String bucketName ) {
        this.s3 = s3;
        this.bucketName = bucketName;
    }

*/
    public S3Authenticate() {
    }

    public AmazonS3 doit( String credentialsPath, String profileName /*, String bucketName */ ) {

        // this.bucketName = bucketName;
        AWSCredentials credentials = null;
        try {
            File file = new File( credentialsPath );

            System.out.println("\n******");
            System.out.println("file '" + file + "'" );
/*
            System.out.println("canonical file '" + file.getCanonicalFile() + "'" );
            System.out.println("absolute file '" + file.getAbsoluteFile() + "'" );

            if(file.exists() ) {
                System.out.println("JA file exists ");
            } else {
                System.out.println("JA file doesn't exist");
            }

            if(!file.isDirectory()) {
                System.out.println("JA file not a directory");
            } else {
                System.out.println("JA is a directory");
            }

            if( isSymlink(file)) {
                System.out.println("It's a symbolic link '" );
            } else {
                System.out.println("It's not a symbolic link '" );
            }
*/

/*            if( file.isSymbolicLink()) {
                System.out.println("JA file is a symbollic link");
            }
 */

            ProfilesConfigFile config = new ProfilesConfigFile( file );
            ProfileCredentialsProvider provider = new ProfileCredentialsProvider(config, profileName);
            credentials = provider.getCredentials();

        } catch (Exception e) {

            System.out.println("JA couldn't load credentials " + e.getMessage() );

            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        System.out.println("authenticating");
        AmazonS3 s3 = new AmazonS3Client(credentials);
        System.out.println("done authenticating");

        return s3;
    }


}
