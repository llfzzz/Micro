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

    async function loadPosts() {
        if (!userId) {
            return;
        }
        postsContainer.classList.add('loading');
        try {
            const params = new URLSearchParams({ userId, limit: '20' });
            const data = await window.apiGet(`/posts?${params.toString()}`);
            renderPosts(data.items || []);
        } catch (err) {
            postsContainer.innerHTML = `<p class="muted">${err.message}</p>`;
        } finally {
            postsContainer.classList.remove('loading');
        }
    }

    function renderPosts(items) {
        postsContainer.innerHTML = '';
        if (!items.length) {
            postsContainer.innerHTML = '<p class="muted">暂无动态。</p>';
            return;
        }
        items.forEach((item) => {
            const article = document.createElement('article');
            article.className = 'feed-card';
            article.innerHTML = `
                <header>
                    <strong>@${item.username || fallbackUsername}</strong>
                    <span class="muted">${new Date(item.createdAt || Date.now()).toLocaleString()}</span>
                </header>
                <p>${item.contentText || ''}</p>
                <a class="link" href="${window.APP_CTX || ''}/app/post?id=${item.id}">查看详情 →</a>
            `;
            postsContainer.appendChild(article);
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

    loadPosts();
    refreshFollowSummary();
});
