document.addEventListener('DOMContentLoaded', () => {
    const usersBody = document.getElementById('admin-users-body');
    const postsBody = document.getElementById('admin-posts-body');
    const refreshUsersBtn = document.getElementById('refresh-users');
    const refreshPostsBtn = document.getElementById('refresh-posts');
    const stats = {
        users: document.getElementById('stat-users'),
        posts: document.getElementById('stat-posts'),
        comments: document.getElementById('stat-comments')
    };

    async function loadStats() {
        if (!stats.users || !stats.posts || !stats.comments) {
            return;
        }
        try {
            const data = await window.apiGet('/admin/stats');
            stats.users.textContent = data.users ?? '--';
            stats.posts.textContent = data.posts ?? '--';
            stats.comments.textContent = data.comments ?? '--';
        } catch (err) {
            console.warn('load stats failed', err);
            stats.users.textContent = stats.posts.textContent = stats.comments.textContent = '--';
        }
    }

    async function loadUsers() {
        if (!usersBody) return;
        usersBody.innerHTML = '<tr><td colspan="5" class="muted">加载中...</td></tr>';
        try {
            const data = await window.apiGet('/admin/users?limit=20');
            const items = data.items || [];
            usersBody.innerHTML = '';
            items.forEach((item) => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${item.id}</td>
                    <td>@${item.username}</td>
                    <td>${item.role || 'USER'}</td>
                    <td>${item.banned ? '封禁' : '正常'}</td>
                    <td>
                        <button class="btn ghost" data-ban="${item.id}" data-banned="${item.banned}">${item.banned ? '解封' : '封禁'}</button>
                    </td>
                `;
                usersBody.appendChild(row);
            });
        } catch (err) {
            usersBody.innerHTML = `<tr><td colspan="5" class="muted">${err.message}</td></tr>`;
        }
    }

    async function loadPosts() {
        if (!postsBody) return;
        postsBody.innerHTML = '<tr><td colspan="5" class="muted">加载中...</td></tr>';
        try {
            const data = await window.apiGet('/admin/posts?limit=20');
            const items = data.items || [];
            postsBody.innerHTML = '';
            items.forEach((item) => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${item.id}</td>
                    <td>${item.userId}</td>
                    <td>${(item.contentText || '').slice(0, 40)}</td>
                    <td>❤ ${item.likeCount || 0} / 💬 ${item.commentCount || 0}</td>
                    <td>
                        <button class="btn ghost" data-delete-post="${item.id}">删除</button>
                    </td>
                `;
                postsBody.appendChild(row);
            });
        } catch (err) {
            postsBody.innerHTML = `<tr><td colspan="5" class="muted">${err.message}</td></tr>`;
        }
    }

    async function toggleBan(userId, banned) {
        try {
            await window.apiPost(`/admin/${userId}/ban?banned=${!banned}`, {});
            loadUsers();
        } catch (err) {
            alert(err.message);
        }
    }

    async function deletePost(postId) {
        try {
            await window.apiDelete(`/admin/posts/${postId}`);
            loadPosts();
        } catch (err) {
            alert(err.message);
        }
    }

    if (usersBody) {
        usersBody.addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-ban]');
            if (!btn) return;
            const userId = btn.dataset.ban;
            const banned = btn.dataset.banned === 'true';
            toggleBan(userId, banned);
        });
    }

    if (postsBody) {
        postsBody.addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-delete-post]');
            if (!btn) return;
            const postId = btn.dataset.deletePost;
            if (confirm('确定要删除该帖子吗？')) {
                deletePost(postId);
            }
        });
    }

    refreshUsersBtn?.addEventListener('click', () => {
        loadUsers();
        loadStats();
    });
    refreshPostsBtn?.addEventListener('click', () => {
        loadPosts();
        loadStats();
    });

    loadStats();
    loadUsers();
    loadPosts();
});
