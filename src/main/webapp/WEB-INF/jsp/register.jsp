<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 注册</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/auth.css" />
</head>
<body class="auth-body">
<div class="auth-card">
    <h1>加入 Micro</h1>
    <p class="muted">创建你的账号</p>
    <form id="register-form" class="auth-form">
        <label>用户名
            <input type="text" name="username" required />
        </label>
        <label>昵称
            <input type="text" name="displayName" required />
        </label>
        <label>邮箱
            <input type="email" name="email" required />
        </label>
        <label>密码
            <input type="password" name="password" required />
        </label>
        <button type="submit" class="btn primary">注册</button>
    </form>
    <p class="muted swap-link">已经有账号？<a href="${ctx}/app/login" style="color: #2563eb">前往登录</a></p>
</div>
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
