<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 搜索结果</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css" />
    <link rel="stylesheet" href="${ctx}/static/css/profile.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
    </aside>
    <main class="main">
        <div class="card">
            <h2>搜索：${searchQuery}</h2>
        </div>

        <c:if test="${searchType == 'users'}">
            <div class="user-list">
                <c:forEach var="user" items="${userList}">
                    <div class="card profile-header small">
                        <div class="avatar-large" style="width: 48px; height: 48px;">
                            <c:if test="${not empty user.avatarPath}">
                                <img src="${ctx}/static/uploads/${user.avatarPath}" alt="头像" />
                            </c:if>
                        </div>
                        <div>
                            <strong>${user.displayName != null ? user.displayName : user.username}</strong>
                            <p class="muted">@${user.username}</p>
                            <a href="${ctx}/app/profile?id=${user.id}" class="btn ghost small">查看</a>
                        </div>
                    </div>
                </c:forEach>
                <c:if test="${empty userList}">
                    <div class="card"><p class="muted">未找到相关用户。</p></div>
                </c:if>
            </div>
        </c:if>

        <c:if test="${searchType == 'posts'}">
            <section id="feed-list">
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
                        <a href="${ctx}/app/post?id=${post.id}" class="link">查看详情</a>
                    </article>
                </c:forEach>
                <c:if test="${empty feedList}">
                    <div class="card"><p class="muted">未找到相关内容。</p></div>
                </c:if>
            </section>
        </c:if>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
