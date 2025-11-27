<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="target" value="${followUser}" />
<c:set var="type" value="${followType}" />
<c:set var="typeLabel" value="${type == 'following' ? '关注' : '粉丝'}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · ${typeLabel}列表</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/profile.css" />
    <link rel="stylesheet" href="${ctx}/static/css/follow.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
        <div class="card profile-meta">
            <h3>${fn:escapeXml(target.displayName != null ? target.displayName : target.username)}</h3>
            <ul>
                <li>粉丝：<span>${followerCount}</span></li>
                <li>关注：<span>${followingCount}</span></li>
            </ul>
            <a href="${ctx}/app/profile?id=${target.id}" class="btn ghost full-width">返回主页</a>
        </div>
    </aside>
    <main class="main">
        <section class="card follow-card">
            <div class="card-header">
                <div>
                    <p class="muted small">@${target.username}</p>
                    <h2>${typeLabel}</h2>
                </div>
                <a href="${ctx}/app/profile" class="btn ghost">我的主页</a>
            </div>
            <div class="follow-tabs">
                <a href="${ctx}/app/follows?type=followers&id=${target.id}"
                   class="${type == 'followers' ? 'active' : ''}">粉丝 (${followerCount})</a>
                <a href="${ctx}/app/follows?type=following&id=${target.id}"
                   class="${type == 'following' ? 'active' : ''}">关注 (${followingCount})</a>
            </div>
            <div id="follow-list" class="follow-list" data-type="${type}" data-user-id="${target.id}">
                <div id="follow-empty" class="follow-empty" hidden>暂无数据。</div>
            </div>
            <button id="follow-load-more" class="btn ghost full-width">加载更多</button>
        </section>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>
    window.APP_CTX='${ctx}';
</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/follows.js" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
