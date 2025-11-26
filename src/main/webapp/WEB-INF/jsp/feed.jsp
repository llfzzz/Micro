<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 时间线</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
        <div class="card">
            <h3>推荐话题</h3>
            <ul>
                <li>#微分享</li>
                <li>#技术流</li>
                <li>#今日摄影</li>
            </ul>
        </div>
    </aside>
    <main class="main">
        <section class="card feed-compose">
            <textarea class="textarea" placeholder="此处只是示意，发布请前往发布页" disabled></textarea>
            <div class="actions">
                <a href="${ctx}/app/create-post" class="btn primary">新建动态</a>
            </div>
        </section>
        <section id="feed-list" data-offset="${feedOffset != null ? feedOffset : 0}">
            <c:forEach var="post" items="${feedList}">
                <article class="card feed-card">
                    <header>
                        <div>
                            <strong>@${post.username}</strong>
                            <p class="muted">${post.createdAt}</p>
                        </div>
                        <div class="metrics">
                            <span>❤ ${post.likeCount}</span>
                            <span>💬 ${post.commentCount}</span>
                        </div>
                    </header>
                    <p>${post.contentText}</p>
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
<script src="${ctx}/static/js/feed.js" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
