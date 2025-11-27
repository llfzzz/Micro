<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>Micro · 时间线</title>
    <link rel="stylesheet" href="${ctx}/static/css/base.css" />
    <link rel="stylesheet" href="${ctx}/static/css/feed.css" />
</head>
<body>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp" />
<div class="container">
    <aside class="aside">
        <jsp:include page="/WEB-INF/jsp/layout/nav.jsp" />
        <div class="card">
            <h3>推荐话题</h3>
            <ul>
                <li>#微分享</li>
                <li>#技术流</li>
                <li>#今日摄影</li>
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
                <article class="card feed-card">
                    <header>
                        <div>
                            <strong>@${post.username}</strong>
                            <p class="muted">${post.createdAt}</p>
                        </div>
                        <div class="metrics">
                            <span>❤ ${post.likeCount}</span>
                            <span>💬 ${post.commentCount}</span>
                        </div>
                    </header>
                    <p>${post.contentText}</p>
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
<script src="${ctx}/static/js/feed.js" defer></script>
<script src="${ctx}/static/js/auth.js" defer></script>
<script>
    document.addEventListener('DOMContentLoaded', () => {
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

            // Handle @ and # buttons
            mentionBtn?.addEventListener('click', () => insertText('@'));
            hashtagBtn?.addEventListener('click', () => insertText('#'));

            function insertText(char) {
                const start = quickContent.selectionStart;
                const end = quickContent.selectionEnd;
                const text = quickContent.value;
                const before = text.substring(0, start);
                const after = text.substring(end);
                quickContent.value = before + char + after;
                quickContent.selectionStart = quickContent.selectionEnd = start + 1;
                quickContent.focus();
                // Trigger input event to check for suggestions
                quickContent.dispatchEvent(new Event('input'));
            }

            // Autocomplete logic
            quickContent.addEventListener('input', async (e) => {
                const cursor = quickContent.selectionStart;
                const text = quickContent.value;
                const beforeCursor = text.substring(0, cursor);
                
                // Check for @ mention
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
                    // Only search if no spaces (simple check)
                    if (!/\s/.test(query)) {
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
                                const nameToInsert = item.displayName || item.username;
                                insertSuggestion(nameToInsert, atIndex);
                            });
                        } else {
                            div.innerHTML = `
                                <div>
                                    <strong>#\${item}</strong>
                                </div>
                            `;
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
                const before = content.substring(0, atIndex + 1); // include @ or #
                const after = content.substring(quickContent.selectionStart);
                quickContent.value = `\${before}\${text} \${after}`;
                suggestionsBox.style.display = 'none';
                quickContent.focus();
            }

            // Hide suggestions on click outside
            document.addEventListener('click', (e) => {
                if (!quickForm.contains(e.target)) {
                    suggestionsBox.style.display = 'none';
                    if (quickContent.value.trim() === '') {
                        quickContent.rows = 1;
                        composeActions.style.display = 'none';
                    }
                }
            });
        }

        if (quickForm) {
            quickForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const content = quickContent.value.trim();
                if (!content) return;

                try {
                    await window.apiPost('/posts', {
                        contentText: content,
                        mediaMetaJson: '[]'
                    });
                    window.location.reload();
                } catch (err) {
                    alert('发布失败: ' + err.message);
                }
            });
        }
    });
</script>
</body>
</html>
