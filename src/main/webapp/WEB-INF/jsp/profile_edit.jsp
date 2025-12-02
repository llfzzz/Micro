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
    <link rel="stylesheet" href="${ctx}/static/css/profile-edit-modal.css?v=<%=System.currentTimeMillis()%>" />
</head>
<body>
<div class="modal-overlay">
    <div class="modal-content">
        <form id="profile-form" data-user-id="${profile.id}">
            <div class="modal-header">
                <div class="modal-close-btn" onclick="history.back()" aria-label="关闭">
                    <svg viewBox="0 0 24 24" aria-hidden="true" style="width: 20px; height: 20px; fill: currentColor;"><g><path d="M10.59 12L4.54 5.96l1.42-1.42L12 10.59l6.04-6.05 1.42 1.42L13.41 12l6.05 6.04-1.42 1.42L12 13.41l-6.04 6.05-1.42-1.42L10.59 12z"></path></g></svg>
                </div>
                <div class="modal-title">编辑个人资料</div>
                <button type="submit" class="modal-save-btn">保存</button>
            </div>
            
            <div class="modal-body">
                <!-- Banner Section -->
                <div class="edit-banner-container">
                    <div id="banner-preview" style="width: 100%; height: 100%;">
                        <img src="${ctx}/api/users/${profile.id}/banner" class="edit-banner-img" alt="背景图" onerror="this.style.display='none'" />
                    </div>
                    <label for="bannerInput" class="camera-overlay" aria-label="添加背景照片">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><g><path d="M9.697 3H11v2h-.697l-3 2H5c-.276 0-.5.224-.5.5v11c0 .276.224.5.5.5h14c.276 0 .5-.224.5-.5V12h2v6c0 1.381-1.119 2.5-2.5 2.5H5c-1.381 0-2.5-1.119-2.5-2.5V8c0-1.381 1.119-2.5 2.5-2.5h1.697l3-2zM12 10.5c-1.105 0-2 .895-2 2s.895 2 2 2 2-.895 2-2-.895-2-2-2zm-4 2c0-2.209 1.791-4 4-4s4 1.791 4 4-1.791 4-4 4-4-1.791-4-4zM17 2c0 1.657-1.343 3-3 3v1c1.657 0 3 1.343 3 3h1c0-1.657 1.343-3 3-3V5c-1.657 0-3-1.343-3-3h-1z"></path></g></svg>
                    </label>
                    <input type="file" id="bannerInput" accept="image/jpeg,image/png,image/webp" />
                </div>

                <!-- Avatar Section -->
                <div class="edit-avatar-container">
                    <div id="avatar-preview" style="width: 100%; height: 100%; border-radius: 50%; overflow: hidden;">
                        <img src="${ctx}/api/users/${profile.id}/avatar" class="edit-avatar-img" alt="头像" onerror="this.style.display='none'" />
                    </div>
                    <label for="avatarInput" class="camera-overlay" aria-label="添加头像照片">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><g><path d="M9.697 3H11v2h-.697l-3 2H5c-.276 0-.5.224-.5.5v11c0 .276.224.5.5.5h14c.276 0 .5-.224.5-.5V12h2v6c0 1.381-1.119 2.5-2.5 2.5H5c-1.381 0-2.5-1.119-2.5-2.5V8c0-1.381 1.119-2.5 2.5-2.5h1.697l3-2zM12 10.5c-1.105 0-2 .895-2 2s.895 2 2 2 2-.895 2-2-.895-2-2-2zm-4 2c0-2.209 1.791-4 4-4s4 1.791 4 4-1.791 4-4 4-4-1.791-4-4zM17 2c0 1.657-1.343 3-3 3v1c1.657 0 3 1.343 3 3h1c0-1.657 1.343-3 3-3V5c-1.657 0-3-1.343-3-3h-1z"></path></g></svg>
                    </label>
                    <input type="file" id="avatarInput" accept="image/jpeg,image/png,image/webp" />
                </div>

                <!-- Form Fields -->
                <div class="edit-form-fields">
                    <div class="form-group">
                        <label for="displayName" class="form-label">昵称</label>
                        <input type="text" id="displayName" name="displayName" class="form-input" maxlength="50"
                               value="${fn:escapeXml(profile.displayName != null ? profile.displayName : '')}" />
                    </div>
                    
                    <div class="form-group">
                        <label for="bio" class="form-label">简介</label>
                        <textarea id="bio" name="bio" class="form-textarea" rows="3" maxlength="160">${fn:escapeXml(profile.bio != null ? profile.bio : '')}</textarea>
                    </div>
                </div>
            </div>
        </form>
        <div id="profile-flash" class="form-flash" role="status" aria-live="polite" style="display:none; position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%); z-index: 2001;"></div>
    </div>
</div>
<script>
    window.APP_CTX='${ctx}';
</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/profile-edit.js?v=<%=System.currentTimeMillis()%>" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
</body>
</html>
