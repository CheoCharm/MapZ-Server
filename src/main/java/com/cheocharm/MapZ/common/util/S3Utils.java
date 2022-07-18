package com.cheocharm.MapZ.common.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.cheocharm.MapZ.common.exception.S3.FailConvertToFileException;
import com.cheocharm.MapZ.common.exception.S3.FailDeleteFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        File file = convert(multipartFile);
        String key = USER + username; //dir + filename
        amazonS3Client.putObject(bucket, key, file);
        String imageURL = amazonS3Client.getUrl(bucket, key).toString();
        if (!file.delete()) {
            throw new FailDeleteFileException();
        }

        return imageURL;

    }

    public String uploadGroupImage(MultipartFile multipartFile, String groupUUID) {
        File file = convert(multipartFile);
        String key = GROUP + groupUUID; //dir + filename
        amazonS3Client.putObject(bucket, key, file);
        String imageURL = amazonS3Client.getUrl(bucket, key).toString();
        if (!file.delete()) {
            throw new FailDeleteFileException();
        }

        return imageURL;

    }

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    public File convert(MultipartFile multipartFile) {
        File file = new File(multipartFile.getOriginalFilename());
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new FailConvertToFileException();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return file;
    }

}
