package com.cheocharm.MapZ.common.image.s3;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.cheocharm.MapZ.common.exception.common.InputStreamIOException;
import com.cheocharm.MapZ.common.image.ImageHandler;
import com.cheocharm.MapZ.common.image.ImageDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class S3ImageService implements ImageHandler {

    private final S3Api s3Api;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String uploadImage(MultipartFile multipartFile, ImageDirectory usage) {
        try (final InputStream inputStream = multipartFile.getInputStream()) {
            final ObjectMetadata objectMetadata = createObjectMetadata(multipartFile);
            final String fileName = createFileName(usage);
            return s3Api.uploadImage(bucket, fileName, inputStream, objectMetadata);
        } catch (IOException e) {
            throw new InputStreamIOException(e);
        }
    }

    @Async
    @Override
    public CompletableFuture<String> uploadImageAsync(MultipartFile multipartFile, ImageDirectory usage) {
        try (final InputStream inputStream = multipartFile.getInputStream()) {
            final ObjectMetadata objectMetadata = createObjectMetadata(multipartFile);
            final String fileName = createFileName(usage);
            return CompletableFuture.completedFuture(s3Api.uploadImage(bucket, fileName, inputStream, objectMetadata));
        } catch (IOException e) {
            throw new InputStreamIOException(e);
        }
    }

    @Async
    @Override
    public void deleteImage(String imageURL) {
        s3Api.removeImage(bucket, imageURL);
    }

    private ObjectMetadata createObjectMetadata(MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());
        return objectMetadata;
    }

    private String createFileName(ImageDirectory usage) {
        return Arrays.stream(ImageDirectory.values())
                .filter(imageUsage -> imageUsage.equals(usage))
                .findAny()
                .map(usage1 -> usage1.getBaseDirectory().concat(UUID.randomUUID().toString()))
                .orElseThrow(RuntimeException::new);
    }
}
