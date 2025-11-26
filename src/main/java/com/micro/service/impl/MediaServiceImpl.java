package com.micro.service.impl;

import com.micro.dao.MediaDao;
import com.micro.entity.Media;
import com.micro.service.MediaService;
import com.micro.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

public class MediaServiceImpl implements MediaService {

    private final MediaDao mediaDao;
    private final String storageRoot;

    public MediaServiceImpl(MediaDao mediaDao, String storageRoot) {
        this.mediaDao = mediaDao;
        this.storageRoot = storageRoot;
    }

    @Override
    public Media storeFile(long uploaderId, String originalName, String contentType, long size, InputStream inputStream) {
        try (InputStream in = inputStream) {
            String relativePath = FileUtil.saveToStorage(in, storageRoot, uploaderId, originalName);
            Media media = new Media();
            media.setUploaderId(uploaderId);
            media.setOriginalName(originalName);
            media.setType(deduceType(contentType));
            media.setPath(relativePath);
            media.setSize(size);
            media.setCreatedAt(LocalDateTime.now());
            long mediaId = mediaDao.create(media);
            media.setId(mediaId);
            return media;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to store media", e);
        }
    }

    @Override
    public List<Media> getMediaByPost(long postId) {
        return mediaDao.listByPost(postId);
    }

    private String deduceType(String mime) {
        if (mime == null) {
            return "OTHER";
        }
        if (mime.startsWith("image/")) {
            return "IMAGE";
        }
        if (mime.startsWith("video/")) {
            return "VIDEO";
        }
        return "OTHER";
    }
}
