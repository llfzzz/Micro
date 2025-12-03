document.addEventListener('DOMContentLoaded', () => {
    const feedList = document.getElementById('feed-list');
    const sentinel = document.getElementById('feed-sentinel');
    let offset = Number(feedList?.dataset.offset || 0);
    const limit = 10;
    let loading = false;
    let finished = false;

    // --- Shared Utilities ---
    function formatText(text) {
        if (!text) return '';
        let safe = text.replace(/&/g, "&amp;")
                       .replace(/</g, "&lt;")
                       .replace(/>/g, "&gt;")
                       .replace(/"/g, "&quot;")
                       .replace(/'/g, "&#039;");
        
        safe = safe.replace(/#([^#\s@]+)/g, (match, tag) => {
            return `<a href="${window.APP_CTX}/app/search?q=%23${encodeURIComponent(tag)}" class="link-tag">${match}</a>`;
        });

        safe = safe.replace(/@([^#\s@]+)/g, (match, user) => {
            return `<a href="${window.APP_CTX}/app/search?q=@${encodeURIComponent(user)}" class="link-mention">${match}</a>`;
        });
        
        return safe;
    }

    function renderCarousel(container, mediaList) {
        container.innerHTML = '';
        const getUrl = (m) => {
            if (m.url) return m.url;
            if (m.path) return `${window.APP_CTX}/static/uploads/${m.path}`;
            return '';
        };

        if (mediaList[0].type && mediaList[0].type.toLowerCase().startsWith('video')) {
            const src = getUrl(mediaList[0]);
            container.innerHTML = `<video src="${src}" controls class="single-media" style="width:100%"></video>`;
            return;
        }
        
        if (mediaList.length === 1) {
             const src = getUrl(mediaList[0]);
             container.innerHTML = `<img src="${src}" class="single-media">`;
             return;
        }
        
        let currentIndex = 0;
        const wrapper = document.createElement('div');
        wrapper.className = 'carousel-wrapper';
        
        mediaList.forEach(m => {
            const slide = document.createElement('div');
            slide.className = 'carousel-slide';
            slide.innerHTML = `<img src="${getUrl(m)}">`;
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
        counter.textContent = `1 / ${mediaList.length}`;
        
        container.appendChild(wrapper);
        container.appendChild(prevBtn);
        container.appendChild(nextBtn);
        container.appendChild(counter);
        
        function updateSlide() {
            wrapper.style.transform = `translateX(-${currentIndex * 100}%)`;
            counter.textContent = `${currentIndex + 1} / ${mediaList.length}`;
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

    function initFeedCard(card) {
        const textContainer = card.querySelector('.content-text');
        if (!textContainer) return;
        const fullText = textContainer.dataset.fullText || '';
        const mediaContainer = card.querySelector('.post-media-container');
        const mediaJson = mediaContainer ? mediaContainer.dataset.media : '[]';
        
        // Text Folding
        const LIMIT = 140;
        if (fullText.length > LIMIT) {
            const truncated = fullText.substring(0, LIMIT);
            textContainer.innerHTML = `${formatText(truncated)}... <button class="expand-btn">展开</button>`;
            
            card.addEventListener('click', (e) => {
                if (e.target.classList.contains('expand-btn')) {
                    e.stopPropagation();
                    textContainer.innerHTML = `${formatText(fullText)} <button class="collapse-btn">收起</button>`;
                } else if (e.target.classList.contains('collapse-btn')) {
                    e.stopPropagation();
                    textContainer.innerHTML = `${formatText(truncated)}... <button class="expand-btn">展开</button>`;
                } else if (shouldIgnoreClick(e.target)) {
                    return;
                } else {
                    navigateToPost(card.dataset.postId);
                }
            });
        } else {
            textContainer.innerHTML = formatText(fullText);
            card.addEventListener('click', (e) => {
                if (shouldIgnoreClick(e.target)) return;
                navigateToPost(card.dataset.postId);
            });
        }
        
        // Media Rendering
        try {
            const mediaList = JSON.parse(mediaJson || '[]');
            if (mediaList.length > 0 && mediaContainer) {
                mediaContainer.style.display = 'block';
                renderCarousel(mediaContainer, mediaList);
            }
        } catch (e) { console.error('Media parse error', e); }
    }

    function shouldIgnoreClick(target) {
        return target.tagName === 'A' || target.closest('a') || 
               target.tagName === 'BUTTON' || target.closest('button') || 
               target.closest('.carousel-prev') || target.closest('.carousel-next') ||
               target.closest('.metric-item');
    }

    function navigateToPost(postId) {
        if (postId) {
            window.location.href = `${window.APP_CTX}/app/post?id=${postId}`;
        }
    }

    // --- Quick Publish Logic ---
    const quickForm = document.getElementById('quick-publish-form');
    if (quickForm) {
        const quickContent = document.getElementById('quick-content');
        const composeActions = document.getElementById('compose-actions');
        const suggestionsBox = document.getElementById('mention-suggestions');
        const mentionBtn = document.querySelector('[data-action="mention"]');
        const hashtagBtn = document.querySelector('[data-action="hashtag"]');

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
                    items = await window.apiGet(`/users?q=${encodeURIComponent(query)}`);
                } else if (type === '#') {
                    items = await window.apiGet(`/posts/tags?q=${encodeURIComponent(query)}`);
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
                        const avatar = `${window.APP_CTX}/api/users/${item.id}/avatar`;
                        div.innerHTML = `
                            <img src="${avatar}" onerror="this.style.display='none'">
                            <div>
                                <strong>${item.displayName || item.username}</strong>
                                <span>@${item.username}</span>
                            </div>
                        `;
                        div.addEventListener('click', () => {
                            insertSuggestion(item.displayName || item.username, atIndex);
                        });
                    } else {
                        div.innerHTML = `<div><strong>#${item}</strong></div>`;
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
            quickContent.value = `${before}${text} ${after}`;
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

    // --- Infinite Scroll Logic ---
    async function loadMore() {
        if (loading || finished) return;
        loading = true;
        try {
            const params = new URLSearchParams({ offset, limit, feed: 'true' });
            const data = await window.apiGet(`/posts?${params}`);
            renderFeed(data.items || []);
            offset += limit;
            if (!data.items || data.items.length < limit) {
                finished = true;
                sentinel.textContent = '没有更多了';
            }
        } catch (err) {
            sentinel.textContent = err.message;
        } finally {
            loading = false;
        }
    }

    function renderFeed(items) {
        items.forEach((item) => {
            const card = document.createElement('article');
            card.className = 'card feed-card';
            card.dataset.postId = item.id;
            
            const mediaJson = item.mediaMetaJson || '[]';
            
            card.innerHTML = `
                <div class="feed-avatar-col">
                    <a href="${window.APP_CTX}/app/profile?id=${item.userId}" class="avatar-link" onclick="event.stopPropagation()">
                        <div class="avatar">
                            <img src="${window.APP_CTX}/api/users/${item.userId}/avatar" alt="头像" onerror="this.style.display='none'">
                        </div>
                    </a>
                </div>
                <div class="feed-content-col">
                    <div class="post-header">
                        <a href="${window.APP_CTX}/app/profile?id=${item.userId}" class="profile-link" onclick="event.stopPropagation()">
                            <span class="display-name">${item.displayName || item.username || 'anonymous'}</span>
                            <span class="username">@${item.username || 'anonymous'}</span>
                        </a>
                        <span class="time-line">${new Date(item.createdAt || Date.now()).toLocaleString().replace(/\//g, '-').replace('T', ' ')}</span>
                    </div>
                    
                    <div class="post-text-container">
                        <span class="content-text" data-full-text="${(item.contentText || '').replace(/"/g, '&quot;')}"></span>
                    </div>

                    <div class="post-media-container" style="display:none;" data-media='${mediaJson}'></div>
                    
                    <div class="metrics">
                        <button class="metric-item" data-action="comment" data-id="${item.id}" data-username="${item.username}" data-displayname="${item.displayName || item.username}">
                            <span class="metric-icon">
                                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="none"><path d="M1.751 10c0-4.42 3.584-8 8.005-8h4.366c4.49 0 8.129 3.64 8.129 8.13 0 2.96-1.607 5.68-4.196 7.11l-8.054 4.46v-3.69h-.067c-4.49.1-8.183-3.51-8.183-8.01zm8.005-6c-3.317 0-6.005 2.69-6.005 6 0 3.37 2.77 6.08 6.138 6.01l.351-.01h1.761v2.3l5.087-2.81c1.951-1.08 3.163-3.13 3.163-5.36 0-3.39-2.744-6.13-6.129-6.13H9.756z"></path></svg>
                            </span>
                            <span>${item.commentCount || 0}</span>
                        </button>
                        <button class="metric-item" data-action="like" data-id="${item.id}" data-liked="${item.liked}">
                            <span class="metric-icon">
                                ${item.liked ? 
                                `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="#e0245e" stroke="#e0245e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>` : 
                                `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>`
                                }
                            </span>
                            <span class="like-count">${item.likeCount || 0}</span>
                        </button>
                    </div>
                </div>
            `;
            feedList.appendChild(card);
            initFeedCard(card);
        });
    }

    // Initialize existing cards
    document.querySelectorAll('.feed-card').forEach(initFeedCard);

    // Infinite Scroll
    window.addEventListener('scroll', () => {
        if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 500) {
            loadMore();
        }
    });
});