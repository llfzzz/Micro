document.addEventListener('DOMContentLoaded', () => {
    const feedList = document.getElementById('feed-list');
    const sentinel = document.getElementById('feed-sentinel');
    let offset = Number(feedList?.dataset.offset || 0);
    const limit = 10;
    let loading = false;
    let finished = false;

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

    // Cleanup legacy "View Detail" links if they exist
    document.querySelectorAll('.feed-card .link').forEach(link => {
        if (link.textContent.includes('查看详情')) {
            link.remove();
        }
    });

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
            return `<a href="${window.APP_CTX}/app/search?q=%23${encodeURIComponent(tag)}" class="link-tag">${match}</a>`;
        });

        // Linkify @mentions
        safe = safe.replace(/@([^#\s@]+)/g, (match, user) => {
            return `<a href="${window.APP_CTX}/app/search?q=@${encodeURIComponent(user)}" class="link-mention">${match}</a>`;
        });
        
        return safe;
    }

    document.querySelectorAll('.feed-card').forEach(card => {
        const textContainer = card.querySelector('.content-text');
        if (!textContainer) return;
        const fullText = textContainer.dataset.fullText || '';
        const mediaContainer = card.querySelector('.post-media-container');
        const mediaJson = mediaContainer ? mediaContainer.dataset.media : '[]';
        
        // 1. Text Folding
        const LIMIT = 30;
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
                } else if (e.target.tagName === 'A' || e.target.closest('a') || e.target.tagName === 'BUTTON' || e.target.closest('button') || e.target.closest('.carousel-prev') || e.target.closest('.carousel-next')) {
                    return;
                } else {
                    const postId = card.dataset.postId;
                    if (postId) {
                        window.location.href = `${window.APP_CTX}/app/post?id=${postId}`;
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
                    window.location.href = `${window.APP_CTX}/app/post?id=${postId}`;
                }
            });
        }
        
        // 2. Media Rendering
        try {
            const mediaList = JSON.parse(mediaJson || '[]');
            if (mediaList.length > 0 && mediaContainer) {
                mediaContainer.style.display = 'block';
                renderCarousel(mediaContainer, mediaList);
            }
        } catch (e) { console.error('Media parse error', e); }
    });

    function renderCarousel(container, mediaList) {
        container.innerHTML = ''; // Clear container to prevent duplication
        const getUrl = (m) => {
            if (m.url) return m.url;
            if (m.path) return `${window.APP_CTX}/static/uploads/${m.path}`;
            return '';
        };

        if (mediaList[0].type && mediaList[0].type.toLowerCase().startsWith('video')) {
            const src = getUrl(mediaList[0]);
            container.innerHTML = `<video src="${src}" controls style="width:100%"></video>`;
            return;
        }
        
        if (mediaList.length === 1) {
             const src = getUrl(mediaList[0]);
             container.innerHTML = `<img src="${src}" style="width:100%; display:block;">`;
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

    function renderFeed(items) {
        items.forEach((item) => {
            const card = document.createElement('article');
            card.className = 'card feed-card';
            card.dataset.postId = item.id;
            
            // Construct media JSON safely
            const mediaJson = item.mediaMetaJson || '[]';
            
            card.innerHTML = `
                <header>
                    <div>
                        <strong>@${item.username || 'anonymous'}</strong>
                        <p class="muted">${new Date(item.createdAt || Date.now()).toLocaleString()}</p>
                    </div>
                    <button class="btn ghost" data-like="${item.id}">❤ ${item.likeCount || 0}</button>
                </header>
                
                <!-- Text Content (Top) -->
                <div class="post-text-container">
                    <span class="content-text" data-full-text="${(item.contentText || '').replace(/"/g, '&quot;')}"></span>
                </div>

                <!-- Media Content (Bottom) -->
                <div class="post-media-container" style="display:none;" data-media='${mediaJson}'></div>
            `;
            feedList.appendChild(card);
            
            // Apply logic to new card
            const textContainer = card.querySelector('.content-text');
            const fullText = item.contentText || '';
            const mediaContainer = card.querySelector('.post-media-container');
            
            // 1. Text Folding
            const LIMIT = 30;
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
                    } else if (e.target.tagName === 'A' || e.target.closest('a') || e.target.tagName === 'BUTTON' || e.target.closest('button') || e.target.closest('.carousel-prev') || e.target.closest('.carousel-next')) {
                        return;
                    } else {
                        window.location.href = `${window.APP_CTX}/app/post?id=${item.id}`;
                    }
                });
            } else {
                textContainer.innerHTML = formatText(fullText);
                card.addEventListener('click', (e) => {
                    if (e.target.tagName === 'A' || e.target.closest('a') || e.target.tagName === 'BUTTON' || e.target.closest('button') || e.target.closest('.carousel-prev') || e.target.closest('.carousel-next')) {
                        return;
                    }
                    window.location.href = `${window.APP_CTX}/app/post?id=${item.id}`;
                });
            }
            
            // 2. Media Rendering
            try {
                const mediaList = JSON.parse(mediaJson);
                if (mediaList.length > 0 && mediaContainer) {
                    mediaContainer.style.display = 'block';
                    renderCarousel(mediaContainer, mediaList);
                }
            } catch (e) { console.error('Media parse error', e); }
        });
    }

    if (feedList && sentinel) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    loadMore();
                }
            });
        });
        observer.observe(sentinel);
    }
});
