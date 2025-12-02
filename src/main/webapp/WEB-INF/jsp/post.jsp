<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 帖子详情</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css?v=4" />
    <link rel="stylesheet" href="${ctx}/static/css/post.css?v=4" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container post-detail-container">
    <main class="main">
        <div class="card" style="display: flex; align-items: center; padding: 0 16px; height: 53px; margin-bottom: 16px;">
            <button onclick="window.history.back()" style="background:none; border:none; cursor:pointer; padding: 8px; margin-left: -8px; border-radius: 50%; display: flex; align-items: center; justify-content: center;" onmouseover="this.style.backgroundColor='rgba(15,20,25,0.1)'" onmouseout="this.style.backgroundColor='transparent'">
                <svg viewBox="0 0 24 24" aria-hidden="true" style="width: 20px; height: 20px; fill: #0f1419;"><g><path d="M7.414 13l5.043 5.04-1.414 1.42L3.586 12l7.457-7.46 1.414 1.42L7.414 11H21v2H7.414z"></path></g></svg>
            </button>
            <span style="font-size: 20px; font-weight: 700; margin-left: 24px; color: #0f1419;">动态</span>
        </div>
        <article class="card feed-card">
            <div class="feed-avatar-col">
                <div class="avatar" aria-hidden="true">
                    <img src="${ctx}/api/users/${post.userId}/avatar" alt="头像" onerror="this.style.display='none'" />
                </div>
            </div>
            <div class="feed-content-col">
                <div class="post-header">
                    <span class="display-name">${post.displayName != null ? post.displayName : post.username}</span>
                    <span class="username">@${post.username}</span>
                    <span class="time-line">发布于 ${fn:replace(post.createdAt,'T',' ')}</span>
                </div>
                
                <!-- Text Content -->
                <div class="post-text-container">
                    <h2 class="content-text" data-full-text="${fn:escapeXml(post.contentText)}"></h2>
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
                </div>
            </div>
        </article>
        <section class="card comment-form">
            <h3>评论</h3>
            <form id="comment-form" data-post-id="${post.id}">
                <textarea class="textarea" name="content" placeholder="说点什么..." required></textarea>
                <button class="btn primary" type="submit">发表评论</button>
            </form>
            <div id="comment-list" data-post-id="${post.id}" class="comment-list"></div>
        </section>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/post.js?v=5" defer></script>
<script src="${ctx}/static/js/interactions.js" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        const textContainer = document.querySelector('.content-text');
        if (textContainer) {
            const fullText = textContainer.dataset.fullText || '';
            textContainer.innerHTML = formatText(fullText);
        }

        function formatText(text) {
            if (!text) return '';
            let safe = text.replace(/&/g, "&amp;")
                           .replace(/</g, "&lt;")
                           .replace(/>/g, "&gt;")
                           .replace(/"/g, "&quot;")
                           .replace(/'/g, "&#039;");
            
            safe = safe.replace(/#([^#\s@]+)/g, (match, tag) => {
                return `<a href="\${window.APP_CTX}/app/search?q=%23\${encodeURIComponent(tag)}" class="link-tag">\${match}</a>`;
            });

            safe = safe.replace(/@([^#\s@]+)/g, (match, user) => {
                return `<a href="\${window.APP_CTX}/app/search?q=@\${encodeURIComponent(user)}" class="link-mention">\${match}</a>`;
            });
            return safe;
        }
    });
</script>
</body>
</html>
