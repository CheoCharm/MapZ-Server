package com.cheocharm.MapZ.common.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class S3Utils {

    private final AmazonS3Client amazonS3Client;
    private final String USER = "Mapz/User/";
    private final String GROUP = "Mapz/Group/";
    private final String DIARY = "Mapz/Diary/";
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadUserImage(MultipartFile multipartFile, String username) {
        String key = USER + username;
        uploadImage(multipartFile, key);

        return amazonS3Client.getUrl(bucket, key).toString();
    }

    public String uploadGroupImage(MultipartFile multipartFile, String groupUUID) {
        String key = GROUP + groupUUID;
        uploadImage(multipartFile, key);

        return amazonS3Client.getUrl(bucket, key).toString();

    }

    public List<String> uploadDiaryImage(List<MultipartFile> files, Long diaryId) {
        String directory = DIARY.concat(String.valueOf(diaryId)).concat("/");
        List<String> imageURLs = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            String key = directory.concat(UUID.randomUUID().toString());
            uploadImage(multipartFile, key);
            imageURLs.add(amazonS3Client.getUrl(bucket, key).toString());
        }
        return imageURLs;
    }

    private void uploadImage(MultipartFile multipartFile, String key) {
        try (final InputStream inputStream = multipartFile.getInputStream()) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());

            amazonS3Client.putObject(new PutObjectRequest(bucket, key, inputStream, objectMetadata));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteImages(List<String> imageURLs) {
        for (String imageURL : imageURLs) {
            deleteImage(imageURL);
        }
    }

    public void deleteImage(String imageURL) {
        amazonS3Client.deleteObject(bucket, imageURL);
    }
}
