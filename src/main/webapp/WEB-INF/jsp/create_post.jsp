<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <main class="main">
        <article class="card">
            <h2>发布动态</h2>
            <form id="create-form">
                <div class="form-row">
                    <label for="contentText">内容</label>
                    <textarea id="contentText" name="contentText" class="textarea" placeholder="分享此刻..." required></textarea>
                </div>
                <div class="form-row">
                    <label for="linkUrl">外链（可选）</label>
                    <input type="url" id="linkUrl" name="linkUrl" placeholder="https://example.com" />
                </div>
                <div class="form-row">
                    <label>媒体文件</label>
                    <input type="file" id="media" multiple accept="image/*,video/*" />
                    <div id="media-preview" class="media-grid"></div>
                </div>
                <button type="submit" class="btn primary">立即发布</button>
            </form>
        </article>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/upload.js"></script>
<script>
    const form = document.getElementById('create-form');
    const mediaInput = document.getElementById('media');
    const preview = document.getElementById('media-preview');
    window.initUpload(mediaInput, preview);
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            contentText: form.contentText.value,
            linkUrl: form.linkUrl.value,
            mediaMetaJson: mediaInput.dataset.mediaMeta || '[]'
        };
        await window.apiPost('/posts', payload);
        window.location.href = `${window.APP_CTX}/app/feed`;
    });
</script>
</body>
</html>
