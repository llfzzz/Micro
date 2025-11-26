<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="owner" value="${isOwner ne null ? isOwner : false}" />
<c:set var="followingState" value="${isFollowing ne null ? isFollowing : false}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 个人主页</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/profile.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
        <div class="card profile-meta">
            <h3>账号概览</h3>
            <ul>
                <li>注册时间：<span>${profileUser.createdAt != null ? profileUser.createdAt : '未知'}</span></li>
                <li>角色：<span>${profileUser.role != null ? profileUser.role : 'USER'}</span></li>
                <li>状态：<span>${profileUser.banned ? '封禁' : '正常'}</span></li>
            </ul>
        </div>
    </aside>
    <main class="main">
        <c:choose>
            <c:when test="${not empty profileUser}">
                <section class="card profile-header">
                    <div class="avatar-large" aria-hidden="true">
                        <c:if test="${not empty profileUser.avatarPath}">
                            <img src="${ctx}/static/uploads/${profileUser.avatarPath}" alt="头像" />
                        </c:if>
                    </div>
                    <div>
                        <h2>${profileUser.displayName != null ? profileUser.displayName : profileUser.username}</h2>
                        <p class="muted">@${profileUser.username}</p>
                        <p>${profileUser.bio != null ? profileUser.bio : '这个人很神秘，什么都没有写。'}</p>
                        <div class="stats">
                            <div>
                                <span id="stat-posts">${profileStats.postCount != null ? profileStats.postCount : 0}</span>
                                <small>帖子</small>
                            </div>
                            <div>
                                <span id="stat-followers">${profileStats.followerCount != null ? profileStats.followerCount : 0}</span>
                                <small>粉丝</small>
                            </div>
                            <div>
                                <span id="stat-following">${profileStats.followingCount != null ? profileStats.followingCount : 0}</span>
                                <small>关注</small>
                            </div>
                        </div>
                        <c:choose>
                            <c:when test="${not owner}">
                                <button id="follow-btn" class="btn ghost" data-user-id="${profileUser.id}" data-following="${followingState}">
                                    <span data-follow-text>${followingState ? '已关注' : '关注'}</span>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <a href="${ctx}/app/profile/edit" class="btn ghost">编辑资料</a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>
                <section class="card">
                    <div class="section-header">
                        <h3>最近动态</h3>
                        <a href="${ctx}/app/create-post" class="btn ghost">发布</a>
                    </div>
                    <div id="profile-posts" data-user-id="${profileUser.id}" data-username="${profileUser.username}">
                        <c:forEach var="post" items="${profilePosts}">
                            <article class="feed-card">
                                <header>
                                    <strong>@${profileUser.username}</strong>
                                    <span class="muted">${post.createdAt}</span>
                                </header>
                                <p>${post.contentText}</p>
                                <a href="${ctx}/app/post?id=${post.id}" class="link">查看详情 →</a>
                            </article>
                        </c:forEach>
                        <c:if test="${empty profilePosts}">
                            <p class="muted">尚无动态。</p>
                        </c:if>
                    </div>
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
<script src="${ctx}/static/js/profile.js" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
