<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 主页</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css?v=3" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
        <div class="card">
            <h3>推荐话题</h3>
            <ul>
                <li>#哈哈</li>
                <li>#哈哈哈</li>
                <li>#哈哈哈哈</li>
            </ul>
        </div>
    </aside>
    <main class="main">
        <section class="card feed-compose">
            <form id="quick-publish-form" style="position: relative;">
                <textarea id="quick-content" class="textarea" placeholder="分享此刻..." rows="1"></textarea>
                <div id="mention-suggestions" class="suggestions-box" style="display:none;"></div>
                <div class="actions" id="compose-actions" style="display: none; justify-content: space-between; align-items: center;">
                    <div class="tools">
                        <button type="button" class="btn icon-btn" data-action="mention" title="提及用户">@</button>
                        <button type="button" class="btn icon-btn" data-action="hashtag" title="添加话题">#</button>
                    </div>
                    <button type="submit" class="btn primary">发布动态</button>
                </div>
            </form>
        </section>
        <section id="feed-list" data-offset="${feedOffset != null ? feedOffset : 0}">
            <c:forEach var="post" items="${feedList}">
                <article class="card feed-card" data-post-id="${post.id}">
                    <div class="feed-avatar-col">
                        <a href="${ctx}/app/profile?id=${post.userId}" class="avatar-link" onclick="event.stopPropagation()">
                            <div class="avatar" aria-hidden="true">
                                <img src="${ctx}/api/users/${post.userId}/avatar" alt="头像" onerror="this.style.display='none'" />
                            </div>
                        </a>
                    </div>
                    <div class="feed-content-col">
                        <div class="post-header">
                            <a href="${ctx}/app/profile?id=${post.userId}" class="profile-link" onclick="event.stopPropagation()">
                                <span class="display-name">${post.displayName != null ? post.displayName : post.username}</span>
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
            <c:if test="${empty feedList}">
                <div class="card">
                    <p class="muted">暂无动态，稍后再来看看。</p>
                </div>
            </c:if>
        </section>
        <div id="feed-sentinel" class="load-indicator">加载中...</div>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/feed.js?v=2" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
