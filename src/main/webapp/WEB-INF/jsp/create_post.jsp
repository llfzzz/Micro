<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 发布动态</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css" />
    <style>
        .step-section { display: none; }
        .step-section.active { display: block; }
        
        .media-grid {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            margin-bottom: 20px;
        }
        
        .media-item, .add-media-btn {
            width: 108px; /* 9:16 ratio based on width */
            height: 192px;
            border-radius: 8px;
            position: relative;
            overflow: hidden;
            background: #f5f5f5;
            flex-shrink: 0;
        }
        
        .add-media-btn {
            border: 2px dashed #ccc;
            display: flex;
            justify-content: center;
            align-items: center;
            cursor: pointer;
            transition: all 0.2s;
            color: #999;
        }
        
        .add-media-btn:hover {
            border-color: #666;
            color: #666;
        }
        
        .add-media-btn.disabled {
            background: #eee;
            border-color: #ddd;
            color: #ccc;
            cursor: not-allowed;
            pointer-events: none;
        }
        
        .add-media-btn::after {
            content: '+';
            font-size: 48px;
            font-weight: 300;
        }
        
        .media-item img, .media-item video {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        
        .media-remove {
            position: absolute;
            top: 4px;
            right: 4px;
            background: rgba(0,0,0,0.5);
            color: white;
            border-radius: 50%;
            width: 20px;
            height: 20px;
            text-align: center;
            line-height: 18px;
            cursor: pointer;
            font-size: 14px;
        }

        .toolbar {
            display: flex;
            gap: 10px;
            margin-top: 10px;
        }
        
        .suggestions-box {
            border: 1px solid #eee;
            background: white;
            position: absolute;
            width: 300px;
            max-height: 200px;
            overflow-y: auto;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            z-index: 100;
            border-radius: 8px;
            scrollbar-width: none; /* Firefox */
        }
        .suggestions-box::-webkit-scrollbar {
            display: none; /* Chrome/Safari */
        }
        
        .suggestion-item {
            padding: 10px;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .suggestion-item:hover {
            background-color: #f9f9f9;
        }
        .suggestion-item img {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            object-fit: cover;
        }
    </style>
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
    </aside>
    <main class="main">
        <article class="card">
            <div class="card-header">
                <h2>发布动态</h2>
            </div>
            
            <form id="create-form">
                <!-- Step 1: Media Selection -->
                <div id="step-media" class="step-section active">
                    <p class="muted mb-2">请选择视频（1个）或图片（最多10张）</p>
                    <div class="media-grid" id="media-preview-list">
                        <div class="add-media-btn" id="add-media-btn"></div>
                    </div>
                    <input type="file" id="file-input" hidden multiple accept="image/*,video/*">
                    
                    <div class="form-actions">
                        <button type="button" id="btn-next" class="btn primary" disabled>下一步</button>
                    </div>
                </div>

                <!-- Step 2: Text Editing -->
                <div id="step-text" class="step-section">
                    <div class="form-row" style="position: relative;">
                        <textarea id="content-text" name="contentText" class="textarea" placeholder="分享此刻..." rows="6" required></textarea>
                        <div id="mention-suggestions" class="suggestions-box" style="display:none;"></div>
                    </div>
                    
                    <div class="toolbar">
                        <button type="button" class="btn icon-btn" data-action="mention" title="提及用户">@</button>
                        <button type="button" class="btn icon-btn" data-action="hashtag" title="添加话题">#</button>
                    </div>

                    <div class="form-actions mt-4">
                        <button type="button" id="btn-back" class="btn ghost">返回修改</button>
                        <button type="submit" class="btn primary">立即发布</button>
                    </div>
                </div>
            </form>
        </article>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        const fileInput = document.getElementById('file-input');
        const addBtn = document.getElementById('add-media-btn');
        const previewList = document.getElementById('media-preview-list');
        const btnNext = document.getElementById('btn-next');
        const btnBack = document.getElementById('btn-back');
        const stepMedia = document.getElementById('step-media');
        const stepText = document.getElementById('step-text');
        const createForm = document.getElementById('create-form');
        const contentText = document.getElementById('content-text');
        
        // State
        let mediaFiles = []; // Array of { file, type, url (local or remote) }
        let uploadedMeta = []; // Array of uploaded results

        // --- Media Handling ---
        addBtn.addEventListener('click', () => fileInput.click());

        fileInput.addEventListener('change', async (e) => {
            const files = Array.from(e.target.files);
            if (files.length === 0) return;

            // Determine current state
            const hasVideo = mediaFiles.some(m => m.type.startsWith('video'));
            const imageCount = mediaFiles.filter(m => m.type.startsWith('image')).length;

            for (const file of files) {
                const isVideo = file.type.startsWith('video');
                const isImage = file.type.startsWith('image');

                if (isVideo) {
                    if (mediaFiles.length > 0) {
                        alert('视频只能单独选择，且不能与图片混选');
                        continue;
                    }
                    mediaFiles.push({ file, type: file.type, url: URL.createObjectURL(file) });
                } else if (isImage) {
                    if (hasVideo || mediaFiles.some(m => m.type.startsWith('video'))) {
                        alert('图片不能与视频混选');
                        continue;
                    }
                    if (mediaFiles.length >= 10) {
                        alert('最多只能选择10张图片');
                        break;
                    }
                    mediaFiles.push({ file, type: file.type, url: URL.createObjectURL(file) });
                }
            }
            
            renderMedia();
            fileInput.value = ''; // Reset
        });

        function renderMedia() {
            // Clear existing previews (except add button)
            const items = previewList.querySelectorAll('.media-item');
            items.forEach(el => el.remove());

            mediaFiles.forEach((media, index) => {
                const div = document.createElement('div');
                div.className = 'media-item';
                
                let content = '';
                if (media.type.startsWith('video')) {
                    content = `<video src="\${media.url}#t=0.1" preload="metadata"></video>`;
                } else {
                    content = `<img src="\${media.url}">`;
                }
                
                div.innerHTML = `
                    \${content}
                    <div class="media-remove" data-index="\${index}">×</div>
                `;
                previewList.insertBefore(div, addBtn);
            });

            // Update Add Button State
            const hasVideo = mediaFiles.some(m => m.type.startsWith('video'));
            const imageCount = mediaFiles.length;
            
            if (hasVideo) {
                addBtn.classList.add('disabled');
            } else if (imageCount >= 10) {
                addBtn.classList.add('disabled');
            } else {
                addBtn.classList.remove('disabled');
            }

            // Update Next Button
            btnNext.disabled = mediaFiles.length === 0;
            
            // Bind remove events
            document.querySelectorAll('.media-remove').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const idx = parseInt(e.target.dataset.index);
                    mediaFiles.splice(idx, 1);
                    renderMedia();
                });
            });
        }

        // --- Navigation ---
        btnNext.addEventListener('click', async () => {
            // Upload files if not already uploaded (simplified: upload all now)
            // In a real app, we might upload as we select, but here we do it on Next.
            // Show loading state?
            btnNext.textContent = '处理中...';
            btnNext.disabled = true;
            
            try {
                uploadedMeta = [];
                for (const media of mediaFiles) {
                    const formData = new FormData();
                    formData.append('file', media.file);
                    const res = await fetch(`\${window.APP_CTX}/api/upload`, {
                        method: 'POST',
                        body: formData
                    });
                    const json = await res.json();
                    if (json.success) {
                        uploadedMeta.push(json.data);
                    } else {
                        throw new Error(json.error || '上传失败');
                    }
                }
                
                stepMedia.classList.remove('active');
                stepText.classList.add('active');
            } catch (err) {
                alert('上传媒体失败: ' + err.message);
            } finally {
                btnNext.textContent = '下一步';
                btnNext.disabled = false;
            }
        });

        btnBack.addEventListener('click', () => {
            stepText.classList.remove('active');
            stepMedia.classList.add('active');
        });

        // --- Text & Autocomplete (Adapted from Feed) ---
        const suggestionsBox = document.getElementById('mention-suggestions');
        const mentionBtn = document.querySelector('[data-action="mention"]');
        const hashtagBtn = document.querySelector('[data-action="hashtag"]');

        mentionBtn.addEventListener('click', () => insertText('@'));
        hashtagBtn.addEventListener('click', () => insertText('#'));

        function insertText(char) {
            const start = contentText.selectionStart;
            const end = contentText.selectionEnd;
            const text = contentText.value;
            contentText.value = text.substring(0, start) + char + text.substring(end);
            contentText.selectionStart = contentText.selectionEnd = start + 1;
            contentText.focus();
            contentText.dispatchEvent(new Event('input'));
        }

        contentText.addEventListener('input', async () => {
            const cursor = contentText.selectionStart;
            const text = contentText.value;
            const beforeCursor = text.substring(0, cursor);
            
            const lastAt = beforeCursor.lastIndexOf('@');
            const lastHash = beforeCursor.lastIndexOf('#');
            
            let triggerChar = null;
            let triggerIndex = -1;
            
            if (lastAt > lastHash) {
                triggerChar = '@';
                triggerIndex = lastAt;
            } else if (lastHash > lastAt) {
                triggerChar = '#';
                triggerIndex = lastHash;
            }
            
            if (triggerIndex !== -1) {
                const query = beforeCursor.substring(triggerIndex + 1);
                if (!/\\s/.test(query)) {
                    await showSuggestions(triggerChar, query, triggerIndex);
                    return;
                }
            }
            suggestionsBox.style.display = 'none';
        });

        async function showSuggestions(type, query, atIndex) {
            if (!query) {
                suggestionsBox.style.display = 'none';
                return;
            }
            try {
                let items = [];
                if (type === '@') {
                    items = await window.apiGet(`/users?q=\${encodeURIComponent(query)}`);
                } else if (type === '#') {
                    items = await window.apiGet(`/posts/tags?q=\${encodeURIComponent(query)}`);
                }

                if (!items || items.length === 0) {
                    suggestionsBox.style.display = 'none';
                    return;
                }
                
                suggestionsBox.innerHTML = '';
                items.forEach(item => {
                    const div = document.createElement('div');
                    div.className = 'suggestion-item';
                    
                    if (type === '@') {
                        const avatar = item.avatarPath ? `\${window.APP_CTX}/static/uploads/\${item.avatarPath}` : 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg"/>';
                        div.innerHTML = `
                            <img src="\${avatar}" onerror="this.style.display='none'">
                            <div>
                                <strong>\${item.displayName || item.username}</strong>
                                <span>@\${item.username}</span>
                            </div>
                        `;
                        div.addEventListener('click', () => {
                            insertSuggestion(item.displayName || item.username, atIndex);
                        });
                    } else {
                        div.innerHTML = `<div><strong>#\${item}</strong></div>`;
                        div.addEventListener('click', () => {
                            insertSuggestion(item, atIndex);
                        });
                    }
                    suggestionsBox.appendChild(div);
                });
                suggestionsBox.style.display = 'block';
            } catch (err) {
                console.error(err);
            }
        }

        function insertSuggestion(text, atIndex) {
            const content = contentText.value;
            const before = content.substring(0, atIndex + 1);
            const after = content.substring(contentText.selectionStart);
            contentText.value = `\${before}\${text} \${after}`;
            suggestionsBox.style.display = 'none';
            contentText.focus();
        }

        // --- Submit ---
        createForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                contentText: contentText.value,
                mediaMetaJson: JSON.stringify(uploadedMeta)
            };
            try {
                await window.apiPost('/posts', payload);
                window.location.href = 'feed';
            } catch (err) {
                alert('发布失败: ' + err.message);
            }
        });
    });
</script>
</body>
</html>
