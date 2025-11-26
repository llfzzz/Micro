package com.micro.service;

import com.micro.entity.Media;

import java.io.InputStream;
import java.util.List;

public interface MediaService {
    Media storeFile(long uploaderId, String originalName, String contentType, long size, InputStream inputStream);

    List<Media> getMediaByPost(long postId);
}
