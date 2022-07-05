package com.cheocharm.MapZ.common.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.cheocharm.MapZ.common.exception.S3.FailConvertToFileException;
import com.cheocharm.MapZ.common.exception.S3.FailDeleteFileException;
import com.cheocharm.MapZ.group.domain.GroupEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class S3Utils {

    private final AmazonS3Client amazonS3Client;
    private final String USER = "Mapz/User/";
    private final String GROUP = "Mapz/Group/";
    private final String DIARY = "Mapz/Diary/";
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadUserImage(MultipartFile multipartFile, String username) throws IOException {
        File file = convert(multipartFile).get();
        String key = USER + username; //dir + filename
        amazonS3Client.putObject(bucket, key, file);
        String imageURL = amazonS3Client.getUrl(bucket, key).toString();
        if (!file.delete()) {
            throw new FailDeleteFileException();
        };

        return imageURL;

    }

    public String uploadGroupImage(MultipartFile multipartFile, GroupEntity groupEntity) throws IOException {
        File file = convert(multipartFile).get();
        String key = GROUP + groupEntity.getGroupUUID(); //dir + filename
        amazonS3Client.putObject(bucket, key, file);
        String imageURL = amazonS3Client.getUrl(bucket, key).toString();
        if (!file.delete()) {
            throw new FailDeleteFileException();
        };

        return imageURL;

    }

    public Optional<File> convert(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        if(file.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(multipartFile.getBytes());
            }
            return Optional.of(file);
        }
        else {
            throw new FailConvertToFileException();
        }
    }

}
