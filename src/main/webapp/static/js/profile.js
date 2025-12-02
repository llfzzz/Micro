document.addEventListener('DOMContentLoaded', () => {
    const postsContainer = document.getElementById('profile-posts');
    const repliesContainer = document.getElementById('profile-replies');
    const mainContainer = postsContainer || repliesContainer;
    
    const followBtn = document.getElementById('follow-btn');
    
    // If neither container exists, we might not be on a valid profile page part, 
    // but we should still try to run other logic if possible, or just return if critical data is missing.
    if (!mainContainer && !followBtn) {
        return;
    }

    const followText = followBtn?.querySelector('[data-follow-text]');
    const followersStat = document.getElementById('stat-followers');
    const followingStat = document.getElementById('stat-following');
    
    // Get userId from container or fallback to follow button
    const userId = mainContainer ? mainContainer.dataset.userId : followBtn?.dataset.userId;
    const fallbackUsername = mainContainer ? mainContainer.dataset.username : 'user';

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

    // 1. Text Folding
    document.querySelectorAll('.feed-card, .thread-parent').forEach(card => {
        const textContainer = card.querySelector('.content-text') || card.querySelector('.post-text-container');
        if (!textContainer) return;
        // If it's .post-text-container, it might not have data-full-text if it wasn't set up that way.
        // In profile.jsp for replies, we used ${fn:escapeXml(reply.postContent)} directly inside div.
        // So we might need to grab innerText if data-full-text is missing, but be careful about already folded text.
        // However, for the reply parent post, we didn't implement folding structure in JSP yet.
        // Let's just handle the click for navigation for now.
        
        card.addEventListener('click', (e) => {
            if (e.target.classList.contains('expand-btn') || e.target.classList.contains('collapse-btn')) {
                return; // Handled by folding logic if present
            }
            if (e.target.tagName === 'A' || e.target.closest('a') || e.target.tagName === 'BUTTON' || e.target.closest('button') || e.target.closest('.carousel-prev') || e.target.closest('.carousel-next')) {
                return;
            }
            const postId = card.dataset.postId;
            if (postId) {
                window.location.href = `${window.APP_CTX}/app/post?id=${postId}`;
            }
        });
        
        // Only apply folding logic if we have the structure
        const contentTextSpan = card.querySelector('.content-text');
        if (contentTextSpan) {
             const fullText = contentTextSpan.dataset.fullText || '';
             const LIMIT = 30;
             if (fullText.length > LIMIT) {
                 const truncated = fullText.substring(0, LIMIT);
                 contentTextSpan.innerHTML = `${formatText(truncated)}... <button class="expand-btn">展开</button>`;
                 
                 card.addEventListener('click', (e) => {
                     if (e.target.classList.contains('expand-btn')) {
                         e.stopPropagation();
                         contentTextSpan.innerHTML = `${formatText(fullText)} <button class="collapse-btn">收起</button>`;
                     } else if (e.target.classList.contains('collapse-btn')) {
                         e.stopPropagation();
                         contentTextSpan.innerHTML = `${formatText(truncated)}... <button class="expand-btn">展开</button>`;
                     }
                 });
             } else {
                 contentTextSpan.innerHTML = formatText(fullText);
             }
        }
    });

    // 2. Media Rendering
    document.querySelectorAll('.post-media-container').forEach(mediaContainer => {
        const mediaJson = mediaContainer.dataset.media;
        try {
            const mediaList = JSON.parse(mediaJson || '[]');
            if (mediaList.length > 0) {
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
        
        if (followBtn.dataset.loggedIn === 'false') {
            if (confirm('请先登录以关注用户。是否前往登录？')) {
                window.location.href = `${window.APP_CTX}/app/login`;
            }
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

    // Search Toggle Logic
    const searchBtn = document.querySelector('.nav-search-btn');
    const searchForm = document.querySelector('.profile-search-form');
    const searchInput = document.querySelector('.profile-search-input');

    if (searchBtn && searchForm && searchInput) {
        searchBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (searchForm.classList.contains('active')) {
                if (searchInput.value.trim() !== '') {
                    searchForm.submit();
                } else {
                    searchForm.classList.remove('active');
                    // Wait for transition to finish before hiding? 
                    // Actually CSS handles opacity/width, so we don't need to set display:none manually if width is 0.
                    // But to be safe for focus:
                    searchInput.blur();
                }
            } else {
                searchForm.classList.add('active');
                searchInput.focus();
            }
        });

        searchInput.addEventListener('blur', () => {
            if (searchInput.value.trim() === '') {
                searchForm.classList.remove('active');
            }
        });
        
        // Allow Enter key to submit
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchForm.submit();
            }
        });
    }
});
