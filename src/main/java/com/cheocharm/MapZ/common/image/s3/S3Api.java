package com.cheocharm.MapZ.common.image.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class S3Api {

    private final AmazonS3Client amazonS3Client;

    public String uploadImage(String bucket, String fileName, InputStream inputStream, ObjectMetadata objectMetadata) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
        return getImageURL(bucket, fileName);
    }

    private String getImageURL(String bucket, String fileName) {
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    public void removeImage(String bucket, String imageURL) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, imageURL));
    }
}
