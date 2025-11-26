<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<nav class="side-nav">
    <ul>
        <li><a href="${ctx}/app/feed">时间线</a></li>
        <li><a href="${ctx}/app/create-post">发布</a></li>
        <li><a href="${ctx}/app/profile">我的主页</a></li>
        <li><a href="${ctx}/app/admin" class="muted">管理员</a></li>
    </ul>
</nav>
