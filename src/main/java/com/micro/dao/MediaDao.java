package com.micro.dao;

import com.micro.entity.Media;

import java.util.List;

public interface MediaDao {
    long create(Media media);

    void bindToPost(long mediaId, long postId);

    List<Media> listByPost(long postId);
}
