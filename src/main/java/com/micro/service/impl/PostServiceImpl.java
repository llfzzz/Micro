package com.micro.service.impl;

import com.micro.dao.LikeDao;
import com.micro.dao.PostDao;
import com.micro.entity.Post;
import com.micro.service.PostService;

import java.util.List;
import java.util.Optional;

public class PostServiceImpl implements PostService {

    private final PostDao postDao;
    private final LikeDao likeDao;

    public PostServiceImpl(PostDao postDao, LikeDao likeDao) {
        this.postDao = postDao;
        this.likeDao = likeDao;
    }

    @Override
    public long createPost(Post post) {
        return postDao.create(post);
    }

    @Override
    public Optional<Post> findById(long id) {
        return postDao.findById(id);
    }

    @Override
    public List<Post> getFeed(int offset, int limit) {
        return postDao.listFeed(offset, limit);
    }

    @Override
    public List<Post> getByUser(long userId, int offset, int limit) {
        return postDao.listByUser(userId, offset, limit);
    }

    @Override
    public boolean deletePost(long postId, long operatorId) {
        return postDao.softDelete(postId, operatorId);
    }

    @Override
    public boolean toggleLike(long postId, long userId) {
        boolean alreadyLiked = likeDao.exists(postId, userId);
        if (alreadyLiked) {
            boolean removed = likeDao.removeLike(postId, userId);
            if (removed) {
                postDao.updateCounts(postId, -1, 0, 0);
                return false;
            }
            return true;
        }
        boolean added = likeDao.addLike(postId, userId);
        if (added) {
            postDao.updateCounts(postId, 1, 0, 0);
            return true;
        }
        return alreadyLiked;
    }
}
