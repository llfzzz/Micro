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

    function renderFeed(items) {
        items.forEach((item) => {
            const card = document.createElement('article');
            card.className = 'card feed-card';
            card.innerHTML = `
                <header>
                    <div>
                        <strong>@${item.username || 'anonymous'}</strong>
                        <p class="muted">${new Date(item.createdAt || Date.now()).toLocaleString()}</p>
                    </div>
                    <button class="btn ghost" data-like="${item.id}">❤ ${item.likeCount || 0}</button>
                </header>
                <p>${formatContent(item.contentText || '')}</p>
            `;
            feedList.appendChild(card);
        });
    }

    function formatContent(text) {
        if (!text) return '';
        // Format #tags
        text = text.replace(/#([\w\u4e00-\u9fa5]+)/g, (match, tag) => {
            return `<a href="${window.APP_CTX || ''}/app/search?q=%23${encodeURIComponent(tag)}" class="link-tag">${match}</a>`;
        });
        // Format @mentions
        text = text.replace(/@([\w\u4e00-\u9fa5]+)/g, (match, name) => {
            return `<a href="${window.APP_CTX || ''}/app/search?q=@${encodeURIComponent(name)}" class="link-mention">${match}</a>`;
        });
        return text;
    }

    // Initial format for server-rendered posts
    document.querySelectorAll('.feed-card p').forEach(p => {
        p.innerHTML = formatContent(p.textContent);
    });

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
