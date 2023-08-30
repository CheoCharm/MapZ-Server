package com.mapz.api.common.image;

import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface ImageHandler {

    String uploadImage(MultipartFile multipartFile, ImageDirectory usage);

    CompletableFuture<String> uploadImageAsync(MultipartFile multipartFile, ImageDirectory usage);

    void deleteImage(String imageURL);
}
