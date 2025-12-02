<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 登录</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/auth.css?v=<%=System.currentTimeMillis()%>" />
</head>
<body class="auth-body">
<div class="auth-card">
    <div class="auth-header">
        <div class="auth-action">
            <a href="${ctx}/app/feed" class="auth-close-btn" aria-label="返回主页">
                <svg viewBox="0 0 24 24" aria-hidden="true" style="width: 20px; height: 20px; fill: currentColor;"><g><path d="M10.59 12L4.54 5.96l1.42-1.42L12 10.59l6.04-6.05 1.42 1.42L13.41 12l6.05 6.04-1.42 1.42L12 13.41l-6.04 6.05-1.42-1.42L10.59 12z"></path></g></svg>
            </a>
        </div>
    </div>
    <p class="muted">使用账号登录 Micro</p>
    <form id="login-form" class="auth-form">
        <label>
            <input type="text" name="username" placeholder="用户名/邮箱" required />
        </label>
        <label>
            <input type="password" name="password" placeholder="••••••••" required />
        </label>
        <button type="submit" class="btn primary">登录</button>
    </form>
    <p class="muted swap-link">还没有账号？<a href="${ctx}/app/register" style="color: #2563eb">立即注册</a></p>
</div>
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
