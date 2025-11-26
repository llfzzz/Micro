<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 帖子详情</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/post.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <main class="main">
        <article class="card">
            <div class="post-hero">
                <c:forEach var="media" items="${postMedia}">
                    <c:choose>
                        <c:when test="${media.type eq 'VIDEO'}">
                            <video controls src="${ctx}/static/uploads/${media.path}"></video>
                        </c:when>
                        <c:otherwise>
                            <img src="${ctx}/static/uploads/${media.path}" alt="媒体" />
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
            <h2>${post.contentText}</h2>
            <p class="muted">发布于 ${post.createdAt}</p>
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
<script src="${ctx}/static/js/post.js" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
