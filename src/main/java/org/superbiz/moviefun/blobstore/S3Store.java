package org.superbiz.moviefun.blobstore;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;


import java.io.*;
import java.util.Optional;



public class S3Store implements BlobStore {

    AmazonS3Client amazonS3Client;
    String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.amazonS3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }


    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(blob.contentType);
        amazonS3Client.putObject(photoStorageBucket, blob.name, blob.inputStream, metadata);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        S3Object object = null;
        try{
            object = amazonS3Client.getObject(photoStorageBucket, name);
        }
        catch (AmazonServiceException e){
            return Optional.empty();
        }

        String contentType = object.getObjectMetadata().getContentType();
        String blobname = object.getKey();
        InputStream is = object.getObjectContent();

        Blob newBlob = new Blob(blobname,is, contentType);


        return Optional.of(newBlob);


    }


    @Override
    public void deleteAll() {
        // Delete all blobs from the store
    }


}
