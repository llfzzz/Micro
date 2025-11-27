(function () {
    const listEl = document.getElementById('follow-list');
    if (!listEl) {
        return;
    }
    const type = listEl.dataset.type;
    const userId = listEl.dataset.userId;
    const emptyState = document.getElementById('follow-empty');
    const loadMoreBtn = document.getElementById('follow-load-more');
    const ctx = window.APP_CTX || '';
    let offset = 0;
    const limit = 20;
    let loading = false;
    let total = 0;

    async function loadPage() {
        if (loading) {
            return;
        }
        loading = true;
        setLoadState(true, '加载中…');
        try {
            const data = await window.apiGet(`/follows/${userId}/${type}?offset=${offset}&limit=${limit}`);
            total = data.total || 0;
            if ((data.items || []).length === 0 && offset === 0) {
                emptyState.hidden = false;
                loadMoreBtn.hidden = true;
                return;
            }
            emptyState.hidden = true;
            renderItems(data.items || []);
            offset += data.items.length;
            if (offset >= total) {
                loadMoreBtn.hidden = true;
            } else {
                setLoadState(false, '加载更多');
            }
        } catch (err) {
            setLoadState(false, err.message || '加载失败，重试');
        } finally {
            loading = false;
        }
    }

    function renderItems(items) {
        items.forEach((user) => {
            const item = document.createElement('div');
            item.className = 'follow-item';

            const info = document.createElement('div');
            info.className = 'info';

            const avatar = buildAvatar(user);
            const meta = document.createElement('div');
            const nameEl = document.createElement('strong');
            nameEl.textContent = user.displayName || user.username;
            const handleEl = document.createElement('span');
            handleEl.className = 'muted';
            handleEl.textContent = `@${user.username}`;
            meta.append(nameEl, handleEl);
            info.append(avatar, meta);

            const link = document.createElement('a');
            link.className = 'btn ghost';
            link.href = `${ctx}/app/profile?id=${user.id}`;
            link.textContent = '查看';

            item.append(info, link);
            listEl.appendChild(item);
        });
    }

    function buildAvatar(user) {
        if (user.avatarPath) {
            const img = document.createElement('img');
            img.className = 'avatar-small';
            img.alt = '头像';
            img.src = `${ctx}/static/uploads/${user.avatarPath}`;
            return img;
        }
        const fallback = document.createElement('div');
        fallback.className = 'avatar-placeholder';
        const seed = (user.displayName || user.username || '?').trim();
        fallback.textContent = seed ? seed.charAt(0).toUpperCase() : '?';
        return fallback;
    }

    function setLoadState(disabled, label) {
        if (!loadMoreBtn) {
            return;
        }
        loadMoreBtn.disabled = disabled;
        loadMoreBtn.hidden = false;
        loadMoreBtn.textContent = label;
    }

    loadMoreBtn?.addEventListener('click', loadPage);
    loadPage();
})();
