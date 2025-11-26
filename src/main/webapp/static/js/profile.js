document.addEventListener('DOMContentLoaded', () => {
    const postsContainer = document.getElementById('profile-posts');
    const followBtn = document.getElementById('follow-btn');
    if (!postsContainer) {
        return;
    }
    const userId = postsContainer.dataset.userId;

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
                    <strong>@${item.username || 'unknown'}</strong>
                    <span class="muted">${new Date(item.createdAt || Date.now()).toLocaleString()}</span>
                </header>
                <p>${item.contentText || ''}</p>
                <a class="link" href="${window.APP_CTX || ''}/app/post?id=${item.id}">查看详情 →</a>
            `;
            postsContainer.appendChild(article);
        });
    }

    if (followBtn) {
        followBtn.addEventListener('click', () => {
            followBtn.disabled = true;
            followBtn.textContent = '功能待实现';
        });
    }

    loadPosts();
});
