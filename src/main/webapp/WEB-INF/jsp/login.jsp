<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 登录</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/auth.css" />
</head>
<body class="auth-body">
<div class="auth-card">
    <h1>欢迎回来</h1>
    <p class="muted">使用账号登录 Micro</p>
    <form id="login-form" class="auth-form">
        <label>用户名或邮箱
            <input type="text" name="username" placeholder="username / email" required />
        </label>
        <label>密码
            <input type="password" name="password" placeholder="••••••••" required />
        </label>
        <button type="submit" class="btn primary">登录</button>
    </form>
    <p class="muted swap-link">还没有账号？<a href="${ctx}/app/register">立即注册</a></p>
</div>
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
