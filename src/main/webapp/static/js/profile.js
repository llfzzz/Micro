document.addEventListener('DOMContentLoaded', () => {
    const postsContainer = document.getElementById('profile-posts');
    const followBtn = document.getElementById('follow-btn');
    if (!postsContainer) {
        return;
    }
    const followText = followBtn?.querySelector('[data-follow-text]');
    const followersStat = document.getElementById('stat-followers');
    const followingStat = document.getElementById('stat-following');
    const userId = postsContainer.dataset.userId;
    const fallbackUsername = postsContainer.dataset.username || 'user';

    // Cleanup legacy "View Detail" links if they exist
    document.querySelectorAll('.feed-card .link').forEach(link => {
        if (link.textContent.includes('查看详情')) {
            link.remove();
        }
    });

    // --- Feed Display Logic (Copied from feed.jsp) ---
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
        const getUrl = (m) => {
            if (m.url) return m.url;
            if (m.path) return `${window.APP_CTX}/static/uploads/${m.path}`;
            return '';
        };

        if (mediaList[0].type && mediaList[0].type.startsWith('video')) {
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

    async function refreshFollowSummary() {
        if (!userId) {
            return;
        }
        try {
            const data = await window.apiGet(`/follows/${userId}`);
            updateFollowView(data);
        } catch (err) {
            // swallow to avoid blocking profile rendering
            console.warn('follow summary load failed', err);
        }
    }

    function updateFollowView(data) {
        if (followersStat && typeof data.followers === 'number') {
            followersStat.textContent = data.followers;
        }
        if (followingStat && typeof data.following === 'number') {
            followingStat.textContent = data.following;
        }
        updateFollowButton(Boolean(data.followingState));
    }

    function updateFollowButton(isFollowing) {
        if (!followBtn) {
            return;
        }
        followBtn.dataset.following = String(isFollowing);
        if (followText) {
            followText.textContent = isFollowing ? '已关注' : '关注';
        }
    }

    followBtn?.addEventListener('click', async () => {
        if (!userId) {
            return;
        }
        followBtn.disabled = true;
        const isFollowing = followBtn.dataset.following === 'true';
        try {
            if (isFollowing) {
                await window.apiDelete(`/follows/${userId}`);
            } else {
                await window.apiPost(`/follows/${userId}`, {});
            }
            await refreshFollowSummary();
        } catch (err) {
            alert(err.message);
        } finally {
            followBtn.disabled = false;
        }
    });

    refreshFollowSummary();
});
