<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="profile" value="${profileUser}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 编辑资料</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/profile.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
        <div class="card helper-card">
            <h3>编辑提示</h3>
            <ul class="helper-list">
                <li>昵称与简介将公开展示。</li>
                <li>头像支持 PNG / JPG / GIF，单个不超过 5MB。</li>
                <li>完成后可返回主页查看最终效果。</li>
            </ul>
            <a class="btn ghost full-width" href="${ctx}/app/profile">← 返回主页</a>
        </div>
    </aside>
    <main class="main">
        <section class="card profile-edit-card">
            <div class="card-header">
                <div>
                    <p class="muted small">完善资料，帮助朋友快速找到你。</p>
                    <h2>编辑资料</h2>
                </div>
                <a href="${ctx}/app/profile" class="btn ghost">查看主页</a>
            </div>
            <form id="profile-form" data-user-id="${profile.id}">
                <div class="profile-edit-grid">
                    <div>
                           <label for="displayName">昵称</label>
                           <input type="text" id="displayName" name="displayName" maxlength="100"
                               value="${fn:escapeXml(profile.displayName != null ? profile.displayName : '')}" placeholder="昵称" />
                    </div>
                    <div>
                        <label for="email">邮箱</label>
                           <input type="email" id="email" name="email" maxlength="100"
                               value="${fn:escapeXml(profile.email != null ? profile.email : '')}" placeholder="name@example.com" />
                    </div>
                </div>
                <div class="form-row">
                    <label for="bio">简介</label>
                        <textarea id="bio" name="bio" class="textarea" rows="4" maxlength="500"
                                  placeholder="介绍一下你自己~">${fn:escapeXml(profile.bio != null ? profile.bio : '')}</textarea>
                </div>
                <div class="avatar-upload">
                    <div class="avatar-large" id="avatar-preview">
                        <c:if test="${not empty profile.avatarPath}">
                            <img src="${ctx}/static/uploads/${profile.avatarPath}" alt="当前头像" />
                        </c:if>
                    </div>
                    <div>
                        <label for="avatarInput">头像</label>
                        <input type="file" id="avatarInput" accept="image/*" />
                        <p class="muted small">选择图片后自动上传。</p>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="reset" class="btn ghost">重置</button>
                    <button type="submit" class="btn primary">保存修改</button>
                </div>
            </form>
            <div id="profile-flash" class="form-flash" role="status" aria-live="polite"></div>
        </section>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>
    window.APP_CTX='${ctx}';
</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/profile-edit.js" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
