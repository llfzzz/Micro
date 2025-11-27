<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 发布动态</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
        <div class="card helper-card">
            <h3>发布提示</h3>
            <ul class="helper-list">
                <li>支持 JPG / PNG / GIF / MP4 等常见格式。</li>
                <li>单个文件建议不超过 50MB，最多 6 个媒体。</li>
                <li>内容需遵守社区规范，违规将被移除。</li>
            </ul>
        </div>
    </aside>
    <main class="main">
        <article class="card">
            <div class="card-header">
                <div>
                    <p class="muted">准备好分享你的灵感了吗？</p>
                    <h2>发布动态</h2>
                </div>
                <a href="${ctx}/app/feed" class="btn ghost">取消</a>
            </div>
            <form id="create-form">
                <div class="form-row">
                    <label for="contentText">内容</label>
                    <textarea id="contentText" name="contentText" class="textarea" placeholder="分享此刻..." required></textarea>
                </div>
                <div class="form-row">
                    <label>媒体文件</label>
                    <input type="file" id="media" multiple accept="image/*,video/*" />
                    <p class="muted small">可拖拽批量上传，支持图片与视频混合。</p>
                    <div id="media-preview" class="media-grid"></div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn ghost" data-action="clear">清空</button>
                    <button type="submit" class="btn primary">立即发布</button>
                </div>
            </form>
        </article>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/upload.js"></script>
<script src="${ctx}/static/js/auth.js" defer></script>
<script>
    const form = document.getElementById('create-form');
    const mediaInput = document.getElementById('media');
    const preview = document.getElementById('media-preview');
    const clearBtn = form.querySelector('[data-action="clear"]');
    window.initUpload(mediaInput, preview);
    clearBtn.addEventListener('click', () => {
        form.reset();
        mediaInput.value = '';
        mediaInput.dataset.mediaMeta = '[]';
        preview.innerHTML = '';
    });
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            contentText: form.contentText.value,
            mediaMetaJson: mediaInput.dataset.mediaMeta || '[]'
        };
        await window.apiPost('/posts', payload);
        window.location.href = 'feed';
    });
</script>
</body>
</html>
