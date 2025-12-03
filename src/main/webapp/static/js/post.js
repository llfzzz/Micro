document.addEventListener('DOMContentLoaded', () => {
    const commentForm = document.getElementById('comment-form');
    const commentList = document.getElementById('comment-list');
    const postId = commentForm?.dataset.postId || commentList?.dataset.postId;

    window.refreshComments = async function() {
        if (!postId) return;
        try {
            const data = await window.apiGet(`/comments?postId=${postId}`);
            commentList.innerHTML = '';
            (data.items || []).forEach((item) => {
                const el = document.createElement('div');
                el.className = 'comment-item';
                // Force flex layout inline to ensure it works even if CSS is cached or missing
                el.style.display = 'flex';
                el.style.gap = '12px';
                
                const avatarUrl = `${window.APP_CTX}/api/users/${item.userId}/avatar`;
                const profileUrl = `${window.APP_CTX}/app/profile?id=${item.userId}`;
                const timeStr = new Date(item.createdAt).toLocaleString().replace(/\//g, '-').replace('T', ' ');
                const displayName = item.displayName || item.username || 'user';
                const username = item.username || 'user';

                el.innerHTML = `
                    <div class="feed-avatar-col">
                        <a href="${profileUrl}" class="avatar-link">
                            <div class="avatar">
                                <img src="${avatarUrl}" alt="avatar" onerror="this.style.display='none'">
                            </div>
                        </a>
                    </div>
                    <div class="comment-content">
                        <div class="post-header" style="margin-bottom: 2px;">
                            <a href="${profileUrl}" class="profile-link">
                                <span class="display-name">${displayName}</span>
                                <span class="username">@${username}</span>
                            </a>
                            <span class="time-line">${timeStr}</span>
                        </div>
                        <div class="comment-text">${formatText(item.content)}</div>
                    </div>
                `;
                commentList.appendChild(el);
            });
        } catch (err) {
            console.error('Failed to load comments', err);
        }
    }

    function formatText(text) {
        if (!text) return '';
        return text.replace(/&/g, "&amp;")
                   .replace(/</g, "&lt;")
                   .replace(/>/g, "&gt;")
                   .replace(/"/g, "&quot;")
                   .replace(/'/g, "&#039;");
    }

    if (commentForm) {
        commentForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                postId,
                content: commentForm.content.value
            };
            await window.apiPost('/comments', payload);
            commentForm.reset();
            window.refreshComments();
        });
    }

    window.refreshComments();

    // --- Media Rendering (Copied from feed.js) ---
    const mediaContainer = document.querySelector('.post-media-container');
    if (mediaContainer) {
        const mediaJson = mediaContainer.dataset.media;
        try {
            const mediaList = JSON.parse(mediaJson || '[]');
            if (mediaList.length > 0) {
                mediaContainer.style.display = 'block';
                renderCarousel(mediaContainer, mediaList);
            }
        } catch (e) { console.error('Media parse error', e); }
    }

    function renderCarousel(container, mediaList) {
        container.innerHTML = ''; // Clear container to prevent duplication
        const getUrl = (m) => {
            if (m.url) return m.url;
            if (m.path) return `${window.APP_CTX}/static/uploads/${m.path}`;
            return '';
        };

        // Single Item
        if (mediaList.length === 1) {
             const m = mediaList[0];
             const src = getUrl(m);
             if (m.type && m.type.toLowerCase().startsWith('video')) {
                 container.innerHTML = `<video src="${src}" controls class="detail-media"></video>`;
             } else {
                 container.innerHTML = `<img src="${src}" class="detail-media">`;
             }
             return;
        }
        
        // Multiple Items (Switching Mode for Adaptive Size)
        let currentIndex = 0;
        const wrapper = document.createElement('div');
        wrapper.className = 'detail-carousel-wrapper';
        
        const slides = [];
        
        mediaList.forEach((m, index) => {
            const slide = document.createElement('div');
            slide.className = 'detail-carousel-slide';
            slide.style.display = index === 0 ? 'block' : 'none'; // Only show first initially
            
            if (m.type && m.type.toLowerCase().startsWith('video')) {
                slide.innerHTML = `<video src="${getUrl(m)}" controls class="detail-media"></video>`;
            } else {
                slide.innerHTML = `<img src="${getUrl(m)}" class="detail-media">`;
            }
            
            wrapper.appendChild(slide);
            slides.push(slide);
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
            slides.forEach((s, i) => {
                s.style.display = i === currentIndex ? 'block' : 'none';
            });
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
});
