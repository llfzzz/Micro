<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 管理面板</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/admin.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container admin-container">
    <main class="main">
        <section class="card stat-grid">
            <div class="stat" data-stat="users">
                <h4>用户总数</h4>
                <strong id="stat-users">${adminStats.users != null ? adminStats.users : '--'}</strong>
            </div>
            <div class="stat" data-stat="posts">
                <h4>帖子总数</h4>
                <strong id="stat-posts">${adminStats.posts != null ? adminStats.posts : '--'}</strong>
            </div>
            <div class="stat" data-stat="comments">
                <h4>评论总数</h4>
                <strong id="stat-comments">${adminStats.comments != null ? adminStats.comments : '--'}</strong>
            </div>
        </section>
        <section class="card">
            <div class="section-header">
                <h3>用户列表</h3>
                <button class="btn ghost" id="refresh-users">刷新</button>
            </div>
            <table class="admin-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>用户名</th>
                    <th>角色</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody id="admin-users-body">
                <c:choose>
                    <c:when test="${not empty adminUsers}">
                        <c:forEach var="user" items="${adminUsers}">
                            <tr>
                                <td>${user.id}</td>
                                <td>@${user.username}</td>
                                <td>${user.role}</td>
                                <td>${user.banned ? '封禁' : '正常'}</td>
                                <td>
                                    <button class="btn ghost" data-ban="${user.id}" data-banned="${user.banned}">${user.banned ? '解封' : '封禁'}</button>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="5" class="muted">暂无数据</td></tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </section>
        <section class="card">
            <div class="section-header">
                <h3>最新帖子</h3>
                <button class="btn ghost" id="refresh-posts">刷新</button>
            </div>
            <table class="admin-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>作者</th>
                    <th>内容</th>
                    <th>互动</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody id="admin-posts-body">
                <c:choose>
                    <c:when test="${not empty adminPosts}">
                        <c:forEach var="post" items="${adminPosts}">
                            <tr>
                                <td>${post.id}</td>
                                <td>${post.userId}</td>
                                <td>${fn:length(post.contentText) > 40 ? fn:substring(post.contentText, 0, 40) : post.contentText}</td>
                                <td>❤ ${post.likeCount} / 💬 ${post.commentCount}</td>
                                <td>
                                    <button class="btn ghost" data-delete-post="${post.id}">删除</button>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="5" class="muted">暂无数据</td></tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </section>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/admin.js" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
