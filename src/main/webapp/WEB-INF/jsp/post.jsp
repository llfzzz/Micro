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
    <link rel="stylesheet" href="${ctx}/static/css/post.css" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <main class="main">
        <article class="card">
            <!-- Text Content (Top) -->
            <div class="post-text-container">
                <h2 class="content-text" data-full-text="${post.contentText}"></h2>
            </div>
            <p class="muted">发布于 ${post.createdAt}</p>
            
            <!-- Media Content (Bottom) -->
            <div class="post-media-container" style="display:none;" data-media='${post.mediaMetaJson}'></div>
            
            <div class="metrics">
                <span>❤ ${post.likeCount}</span>
                <span>💬 ${post.commentCount}</span>
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
<script src="${ctx}/static/js/post.js?v=2" defer></script>
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
