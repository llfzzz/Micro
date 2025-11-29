<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 主页</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css?v=3" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
        <div class="card">
            <h3>推荐话题</h3>
            <ul>
                <li>#哈哈</li>
                <li>#哈哈哈</li>
                <li>#哈哈哈哈</li>
            </ul>
        </div>
    </aside>
    <main class="main">
        <section class="card feed-compose">
            <form id="quick-publish-form" style="position: relative;">
                <textarea id="quick-content" class="textarea" placeholder="分享此刻..." rows="1"></textarea>
                <div id="mention-suggestions" class="suggestions-box" style="display:none;"></div>
                <div class="actions" id="compose-actions" style="display: none; justify-content: space-between; align-items: center;">
                    <div class="tools">
                        <button type="button" class="btn icon-btn" data-action="mention" title="提及用户">@</button>
                        <button type="button" class="btn icon-btn" data-action="hashtag" title="添加话题">#</button>
                    </div>
                    <button type="submit" class="btn primary">发布动态</button>
                </div>
            </form>
        </section>
        <section id="feed-list" data-offset="${feedOffset != null ? feedOffset : 0}">
            <c:forEach var="post" items="${feedList}">
                <article class="card feed-card" data-post-id="${post.id}">
                    <div class="feed-avatar-col">
                        <div class="avatar" aria-hidden="true">
                            <c:if test="${not empty post.avatarPath}">
                                <img src="${ctx}/static/uploads/${post.avatarPath}" alt="头像" />
                            </c:if>
                        </div>
                    </div>
                    <div class="feed-content-col">
                        <div class="post-header">
                            <span class="display-name">${post.displayName != null ? post.displayName : post.username}</span>
                            <span class="username">@${post.username}</span>
                            <span class="time-line">${fn:replace(post.createdAt,'T',' ')}</span>
                        </div>
                        
                        <!-- Text Content -->
                        <div class="post-text-container">
                            <span class="content-text" data-full-text="${fn:escapeXml(post.contentText)}"></span>
                        </div>

                        <!-- Media Content -->
                        <div class="post-media-container" style="display:none;" data-media='${post.mediaMetaJson}'></div>
                        
                        <div class="metrics">
                            <span>❤ ${post.likeCount}</span>
                            <span>💬 ${post.commentCount}</span>
                        </div>
                    </div>
                </article>
            </c:forEach>
            <c:if test="${empty feedList}">
                <div class="card">
                    <p class="muted">暂无动态，稍后再来看看。</p>
                </div>
            </c:if>
        </section>
        <div id="feed-sentinel" class="load-indicator">加载中...</div>
    </main>
</div>
<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
<script>window.APP_CTX='${ctx}';</script>
<script src="${ctx}/static/js/api.js"></script>
<script src="${ctx}/static/js/feed.js?v=2" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        // --- Quick Publish Logic (Existing) ---
        const quickForm = document.getElementById('quick-publish-form');
        const quickContent = document.getElementById('quick-content');
        const composeActions = document.getElementById('compose-actions');
        const suggestionsBox = document.getElementById('mention-suggestions');
        const mentionBtn = document.querySelector('[data-action="mention"]');
        const hashtagBtn = document.querySelector('[data-action="hashtag"]');

        if (quickContent && composeActions) {
            quickContent.addEventListener('focus', () => {
                quickContent.rows = 3;
                composeActions.style.display = 'flex';
            });

            mentionBtn?.addEventListener('click', () => insertText('@'));
            hashtagBtn?.addEventListener('click', () => insertText('#'));

            function insertText(char) {
                const start = quickContent.selectionStart;
                const end = quickContent.selectionEnd;
                const text = quickContent.value;
                quickContent.value = text.substring(0, start) + char + text.substring(end);
                quickContent.selectionStart = quickContent.selectionEnd = start + 1;
                quickContent.focus();
                quickContent.dispatchEvent(new Event('input'));
            }

            quickContent.addEventListener('input', async (e) => {
                const cursor = quickContent.selectionStart;
                const text = quickContent.value;
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
                const content = quickContent.value;
                const before = content.substring(0, atIndex + 1);
                const after = content.substring(quickContent.selectionStart);
                quickContent.value = `\${before}\${text} \${after}`;
                suggestionsBox.style.display = 'none';
                quickContent.focus();
            }

            document.addEventListener('click', (e) => {
                if (!quickForm.contains(e.target)) {
                    suggestionsBox.style.display = 'none';
                    if (quickContent.value.trim() === '') {
                        quickContent.rows = 1;
                        composeActions.style.display = 'none';
                    }
                }
            });
            
            quickForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const content = quickContent.value.trim();
                if (!content) return;
                try {
                    await window.apiPost('/posts', { contentText: content, mediaMetaJson: '[]' });
                    window.location.reload();
                } catch (err) { alert('发布失败: ' + err.message); }
            });
        }

        // --- Feed Display Logic (New) ---
        function formatText(text) {
            if (!text) return '';
            // Escape HTML
            let safe = text.replace(/&/g, "&amp;")
                           .replace(/</g, "&lt;")
                           .replace(/>/g, "&gt;")
                           .replace(/"/g, "&quot;")
                           .replace(/'/g, "&#039;");
            
            // Linkify #hashtags
            safe = safe.replace(/#([^#\s@]+)/g, (match, tag) => {
                return `<a href="\${window.APP_CTX}/app/search?q=%23\${encodeURIComponent(tag)}" class="link-tag">\${match}</a>`;
            });

            // Linkify @mentions
            safe = safe.replace(/@([^#\s@]+)/g, (match, user) => {
                return `<a href="\${window.APP_CTX}/app/search?q=@\${encodeURIComponent(user)}" class="link-mention">\${match}</a>`;
            });
            
            return safe;
        }

        document.querySelectorAll('.feed-card').forEach(card => {
            const textContainer = card.querySelector('.content-text');
            const fullText = textContainer.dataset.fullText || '';
            const mediaContainer = card.querySelector('.post-media-container');
            const mediaJson = mediaContainer.dataset.media;
            
            // 1. Text Folding
            const LIMIT = 30;
            if (fullText.length > LIMIT) {
                const truncated = fullText.substring(0, LIMIT);
                textContainer.innerHTML = `\${formatText(truncated)}... <button class="expand-btn">展开</button>`;
                
                card.addEventListener('click', (e) => {
                    if (e.target.classList.contains('expand-btn')) {
                        e.stopPropagation();
                        textContainer.innerHTML = `\${formatText(fullText)} <button class="collapse-btn">收起</button>`;
                    } else if (e.target.classList.contains('collapse-btn')) {
                        e.stopPropagation();
                        textContainer.innerHTML = `\${formatText(truncated)}... <button class="expand-btn">展开</button>`;
                    } else if (e.target.tagName === 'A' || e.target.closest('a') || e.target.tagName === 'BUTTON' || e.target.closest('button') || e.target.closest('.carousel-prev') || e.target.closest('.carousel-next')) {
                        // Do not navigate if clicking links or buttons
                        return;
                    } else {
                        // Navigate to detail
                        const postId = card.dataset.postId;
                        if (postId) {
                            window.location.href = `\${window.APP_CTX}/app/post?id=\${postId}`;
                        }
                    }
                });
            } else {
                textContainer.innerHTML = formatText(fullText);
                card.addEventListener('click', (e) => {
                    if (e.target.tagName === 'A' || e.target.closest('a') || e.target.tagName === 'BUTTON' || e.target.closest('button') || e.target.closest('.carousel-prev') || e.target.closest('.carousel-next')) {
                        return;
                    }
                    const postId = card.dataset.postId;
                    if (postId) {
                        window.location.href = `\${window.APP_CTX}/app/post?id=\${postId}`;
                    }
                });
            }
            
            // 2. Media Rendering
            try {
                const mediaList = JSON.parse(mediaJson || '[]');
                if (mediaList.length > 0) {
                    mediaContainer.style.display = 'block';
                    renderCarousel(mediaContainer, mediaList);
                }
            } catch (e) { console.error('Media parse error', e); }
        });

        function renderCarousel(container, mediaList) {
            // Helper to get URL
            const getUrl = (m) => {
                if (m.url) return m.url; // Local preview or full URL
                if (m.path) return `\${window.APP_CTX}/static/uploads/\${m.path}`;
                return '';
            };

            // Video (Single)
            if (mediaList[0].type && mediaList[0].type.startsWith('video')) {
                const src = getUrl(mediaList[0]);
                container.innerHTML = `<video src="\${src}" controls style="width:100%"></video>`;
                return;
            }
            
            // Images
            if (mediaList.length === 1) {
                 const src = getUrl(mediaList[0]);
                 container.innerHTML = `<img src="\${src}" style="width:100%; display:block;">`;
                 return;
            }
            
            // Carousel
            let currentIndex = 0;
            const wrapper = document.createElement('div');
            wrapper.className = 'carousel-wrapper';
            
            mediaList.forEach(m => {
                const slide = document.createElement('div');
                slide.className = 'carousel-slide';
                slide.innerHTML = `<img src="\${getUrl(m)}">`;
                wrapper.appendChild(slide);
            });
            
            const prevBtn = document.createElement('button');
            prevBtn.className = 'carousel-prev';
            prevBtn.innerHTML = '&#10094;';
            
            const nextBtn = document.createElement('button');
            nextBtn.className = 'carousel-next';
            nextBtn.innerHTML = '&#10095;';
            
            const counter = document.createElement('div');
            counter.className = 'carousel-counter';
            counter.textContent = `1 / \${mediaList.length}`;
            
            container.appendChild(wrapper);
            container.appendChild(prevBtn);
            container.appendChild(nextBtn);
            container.appendChild(counter);
            
            function updateSlide() {
                wrapper.style.transform = `translateX(-\${currentIndex * 100}%)`;
                counter.textContent = `\${currentIndex + 1} / \${mediaList.length}`;
            }
            
            prevBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                currentIndex = (currentIndex - 1 + mediaList.length) % mediaList.length;
                updateSlide();
            });
            
            nextBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                currentIndex = (currentIndex + 1) % mediaList.length;
                updateSlide();
            });
        }
    });
</script>
</body>
</html>
