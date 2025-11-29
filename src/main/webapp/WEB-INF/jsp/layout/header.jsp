<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<header class="site-header">
    <div class="logo">
        <a href="${ctx}/app/feed">Micro</a>
    </div>
    <form class="search-form" action="${ctx}/app/search" method="get">
        <input type="text" name="q" placeholder="搜索 Micro" aria-label="搜索" />
        <button type="submit">搜索</button>
    </form>
    <div class="user-menu">
        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <script>window.IS_LOGGED_IN = true;</script>
                <a href="${ctx}/app/profile?id=${sessionScope.userId}" class="avatar-small" aria-label="查看个人资料"></a>
                <button type="button" class="btn ghost" data-action="logout">退出</button>
            </c:when>
            <c:otherwise>
                <script>window.IS_LOGGED_IN = false;</script>
                <a href="${ctx}/app/login" class="btn ghost">登录</a>
                <a href="${ctx}/app/register" class="btn primary">注册</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>
<script src="${ctx}/static/js/interactions.js?v=1" defer></script>
