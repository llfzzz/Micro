<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="owner" value="${isOwner ne null ? isOwner : false}" />
<c:set var="followingState" value="${isFollowing ne null ? isFollowing : false}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 个人主页</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/profile.css?v=<%=System.currentTimeMillis()%>" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css?v=3" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
                <div class="card profile-meta">
            <h3>账号概览</h3>
            <ul>
                <li>注册时间：<span>${profileUser.createdAt != null ? fn:replace(profileUser.createdAt,'T',' ') : '未知'}</span></li>
                <li>角色：<span>${profileUser.role != null ? profileUser.role : 'USER'}</span></li>
                <li>状态：<span>${profileUser.banned ? '封禁' : '正常'}</span></li>
            </ul>
        </div>
    </aside>
    <main class="main">
        <c:choose>
            <c:when test="${not empty profileUser}">
                <div class="profile-top-nav">
                    <div class="nav-left">
                        <a href="${ctx}/app/feed" class="nav-back-btn" aria-label="返回">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><g><path d="M20 11H7.414l4.293-4.293c.39-.39.39-1.023 0-1.414s-1.023-.39-1.414 0l-6 6c-.39.39-.39 1.023 0 1.414l6 6c.195.195.45.293.707.293s.512-.098.707-.293c.39-.39.39-1.023 0-1.414L7.414 13H20c.553 0 1-.447 1-1s-.447-1-1-1z"></path></g></svg>
                        </a>
                        <div class="nav-title">
                            <h2>${profileUser.displayName != null ? profileUser.displayName : profileUser.username}</h2>
                            <span class="nav-subtitle">${profileStats.postCount} 动态</span>
                        </div>
                    </div>
                    <div class="nav-right">
                        <div class="search-container">
                            <form action="${ctx}/app/search" method="GET" class="profile-search-form">
                                <input type="hidden" name="type" value="user_posts">
                                <input type="hidden" name="uid" value="${profileUser.id}">
                                <button type="button" class="nav-search-btn" aria-label="搜索">
                                    <svg viewBox="0 0 24 24" aria-hidden="true"><g><path d="M21.53 20.47l-3.66-3.66C19.195 15.24 20 13.214 20 11c0-4.97-4.03-9-9-9s-9 4.03-9 9 4.03 9 9 9c2.215 0 4.24-.804 5.808-2.13l3.66 3.66c.147.146.34.22.53.22s.385-.073.53-.22c.295-.293.295-.767.002-1.06zM3.5 11c0-4.135 3.365-7.5 7.5-7.5s7.5 3.365 7.5 7.5-3.365 7.5-7.5 7.5-7.5-3.365-7.5-7.5z"></path></g></svg>
                                </button>
                                <input type="text" name="q" class="profile-search-input" placeholder="搜索动态">
                            </form>
                        </div>
                    </div>
                </div>

                <div class="profile-banner">
                    <img src="${ctx}/api/users/${profileUser.id}/banner" alt="背景图" onerror="this.style.display='none'" />
                </div>

                <section class="card profile-header">
                    <div class="profile-header-top">
                        <div class="avatar-large" aria-hidden="true">
                            <img src="${ctx}/api/users/${profileUser.id}/avatar" alt="头像" onerror="this.style.display='none'" />
                        </div>
                        <div class="profile-actions">
                            <c:choose>
                                <c:when test="${not owner}">
                                    <button id="follow-btn" class="btn ghost" data-user-id="${profileUser.id}" data-following="${followingState}" data-logged-in="${not empty sessionScope.userId}">
                                        <span data-follow-text>${followingState ? '已关注' : '关注'}</span>
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <a href="${ctx}/app/profile/edit" class="btn ghost">编辑资料</a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    
                    <div class="profile-info">
                        <h2 class="profile-name">${profileUser.displayName != null ? profileUser.displayName : profileUser.username}</h2>
                        <p class="profile-username muted">@${profileUser.username}</p>
                        <p class="profile-bio">${profileUser.bio != null ? profileUser.bio : '这个人很神秘，什么都没有写。'}</p>
                        
                        <div class="stats">
                            <a class="stat-link" href="${ctx}/app/follows?type=following&id=${profileUser.id}">
                                <span id="stat-following" class="stat-value">${profileStats.followingCount != null ? profileStats.followingCount : 0}</span>
                                <span class="stat-label">关注</span>
                            </a>
                            <a class="stat-link" href="${ctx}/app/follows?type=followers&id=${profileUser.id}">
                                <span id="stat-followers" class="stat-value">${profileStats.followerCount != null ? profileStats.followerCount : 0}</span>
                                <span class="stat-label">粉丝</span>
                            </a>
                        </div>
                    </div>
                </section>
                <section class="card no-padding">
                    <div class="profile-tabs">
                        <a href="${ctx}/app/profile?id=${profileUser.id}&tab=posts" class="tab-item ${currentTab == 'posts' ? 'active' : ''}">
                            <span>动态</span>
                        </a>
                        <a href="${ctx}/app/profile?id=${profileUser.id}&tab=likes" class="tab-item ${currentTab == 'likes' ? 'active' : ''}">
                            <span>赞过</span>
                        </a>
                        <a href="${ctx}/app/profile?id=${profileUser.id}&tab=replies" class="tab-item ${currentTab == 'replies' ? 'active' : ''}">
                            <span>评论</span>
                        </a>
                    </div>
                    
                    <c:choose>
                        <c:when test="${currentTab == 'replies'}">
                            <div id="profile-replies" data-user-id="${profileUser.id}" data-username="${profileUser.username}">
                                <c:forEach var="reply" items="${profileReplies}">
                                    <article class="card feed-card reply-thread">
                                        <div class="thread-line"></div>
                                        <!-- Parent Post Context -->
                                        <div class="thread-parent" data-post-id="${reply.postId}" style="cursor: pointer;">
                                            <div class="feed-avatar-col">
                                                <a href="${ctx}/app/profile?id=${reply.postAuthorId}" class="avatar-link" onclick="event.stopPropagation()">
                                                    <div class="avatar" aria-hidden="true">
                                                        <img src="${ctx}/api/users/${reply.postAuthorId}/avatar" class="post-avatar-img" data-userid="${reply.postAuthorId}" alt="头像" onerror="this.style.display='none'" />
                                                    </div>
                                                </a>
                                            </div>
                                            <div class="feed-content-col">
                                                <div class="post-header">
                                                    <a href="${ctx}/app/profile?id=${reply.postAuthorId}" class="profile-link" onclick="event.stopPropagation()">
                                                        <span class="display-name" data-userid="${reply.postAuthorId}">${reply.postAuthorDisplayName != null ? reply.postAuthorDisplayName : reply.postAuthorUsername}</span>
                                                        <span class="username">@${reply.postAuthorUsername}</span>
                                                    </a>
                                                    <span class="time-line">${fn:replace(reply.postCreatedAt,'T',' ')}</span>
                                                </div>
                                                
                                                <!-- Text Content -->
                                                <div class="post-text-container">
                                                    <span class="content-text" data-full-text="${fn:escapeXml(reply.postContent)}"></span>
                                                </div>

                                                <!-- Media Content -->
                                                <div class="post-media-container" style="display:none;" data-media='${fn:escapeXml(reply.postMediaMetaJson)}'></div>
                                                
                                                <div class="metrics">
                                                    <button class="metric-item" data-action="comment" data-id="${reply.postId}" data-username="${reply.postAuthorUsername}" data-displayname="${reply.postAuthorDisplayName != null ? reply.postAuthorDisplayName : reply.postAuthorUsername}">
                                                        <span class="metric-icon">
                                                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="none"><path d="M1.751 10c0-4.42 3.584-8 8.005-8h4.366c4.49 0 8.129 3.64 8.129 8.13 0 2.96-1.607 5.68-4.196 7.11l-8.054 4.46v-3.69h-.067c-4.49.1-8.183-3.51-8.183-8.01zm8.005-6c-3.317 0-6.005 2.69-6.005 6 0 3.37 2.77 6.08 6.138 6.01l.351-.01h1.761v2.3l5.087-2.81c1.951-1.08 3.163-3.13 3.163-5.36 0-3.39-2.744-6.13-6.129-6.13H9.756z"></path></svg>
                                                        </span>
                                                        <span>${reply.postCommentCount}</span>
                                                    </button>
                                                    <button class="metric-item" data-action="like" data-id="${reply.postId}" data-liked="${reply.postLiked}">
                                                        <span class="metric-icon">
                                                            <c:choose>
                                                                <c:when test="${reply.postLiked}">
                                                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="#e0245e" stroke="#e0245e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                        <span class="like-count">${reply.postLikeCount}</span>
                                                    </button>
                                                    <c:if test="${reply.postAuthorId == sessionScope.userId}">
                                                        <button class="metric-item delete-post-btn" data-action="delete" data-id="${reply.postId}" title="删除动态">
                                                            <span class="metric-icon">
                                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                                                            </span>
                                                        </button>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <!-- Reply Content -->
                                        <div class="thread-reply">
                                            <div class="feed-avatar-col">
                                                <div class="avatar">
                                                    <img src="${ctx}/api/users/${profileUser.id}/avatar" alt="头像" onerror="this.style.display='none'" />
                                                </div>
                                            </div>
                                            <div class="feed-content-col">
                                                <div class="post-header">
                                                    <span class="display-name">${profileUser.displayName != null ? profileUser.displayName : profileUser.username}</span>
                                                    <span class="username">@${profileUser.username}</span>
                                                    <span class="time-line">${fn:replace(reply.createdAt,'T',' ')}</span>
                                                </div>
                                                <div class="post-text-container">
                                                    ${fn:escapeXml(reply.content)}
                                                </div>
                                            </div>
                                        </div>
                                    </article>
                                </c:forEach>
                                <c:if test="${empty profileReplies}">
                                    <div class="empty-state">暂无评论。</div>
                                </c:if>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div id="profile-posts" data-user-id="${profileUser.id}" data-username="${profileUser.username}">
                                <!-- Profile Posts List -->
                                <c:forEach var="post" items="${profilePosts}">
                                    <article class="card feed-card" data-post-id="${post.id}">
                                        <div class="feed-avatar-col">
                                            <a href="${ctx}/app/profile?id=${post.userId}" class="avatar-link" onclick="event.stopPropagation()">
                                                <div class="avatar" aria-hidden="true">
                                                    <img src="${ctx}/api/users/${post.userId}/avatar" class="post-avatar-img" data-userid="${post.userId}" alt="头像" onerror="this.style.display='none'" />
                                                </div>
                                            </a>
                                        </div>
                                        <div class="feed-content-col">
                                            <div class="post-header">
                                                <a href="${ctx}/app/profile?id=${post.userId}" class="profile-link" onclick="event.stopPropagation()">
                                                    <span class="display-name" data-userid="${post.userId}">${post.displayName != null ? post.displayName : post.username}</span>
                                                    <span class="username">@${post.username}</span>
                                                </a>
                                                <span class="time-line">${fn:replace(post.createdAt,'T',' ')}</span>
                                            </div>
                                            
                                            <!-- Text Content -->
                                            <div class="post-text-container">
                                                <span class="content-text" data-full-text="${fn:escapeXml(post.contentText)}"></span>
                                            </div>

                                            <!-- Media Content -->
                                            <div class="post-media-container" style="display:none;" data-media='${post.mediaMetaJson}'></div>
                                            
                                            <div class="metrics">
                                                <button class="metric-item" data-action="comment" data-id="${post.id}" data-username="${post.username}" data-displayname="${post.displayName != null ? post.displayName : post.username}">
                                                    <span class="metric-icon">
                                                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="none"><path d="M1.751 10c0-4.42 3.584-8 8.005-8h4.366c4.49 0 8.129 3.64 8.129 8.13 0 2.96-1.607 5.68-4.196 7.11l-8.054 4.46v-3.69h-.067c-4.49.1-8.183-3.51-8.183-8.01zm8.005-6c-3.317 0-6.005 2.69-6.005 6 0 3.37 2.77 6.08 6.138 6.01l.351-.01h1.761v2.3l5.087-2.81c1.951-1.08 3.163-3.13 3.163-5.36 0-3.39-2.744-6.13-6.129-6.13H9.756z"></path></svg>
                                                    </span>
                                                    <span>${post.commentCount}</span>
                                                </button>
                                                <button class="metric-item" data-action="like" data-id="${post.id}" data-liked="${post.liked}">
                                                    <span class="metric-icon">
                                                        <c:choose>
                                                            <c:when test="${post.liked}">
                                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="#e0245e" stroke="#e0245e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <span class="like-count">${post.likeCount}</span>
                                                </button>
                                                <c:if test="${post.userId == sessionScope.userId}">
                                                    <button class="metric-item delete-post-btn" data-action="delete" data-id="${post.id}" title="删除动态">
                                                        <span class="metric-icon">
                                                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                                                        </span>
                                                    </button>
                                                </c:if>
                                            </div>
                                        </div>
                                    </article>
                                </c:forEach>
                                <c:if test="${empty profilePosts}">
                                    <div class="empty-state">暂无动态。</div>
                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>
            <c:otherwise>
                <section class="card">
                    <p class="muted">未找到用户信息。</p>
                </section>
            </c:otherwise>
        </c:choose>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/profile.js?v=<%=System.currentTimeMillis()%>" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
