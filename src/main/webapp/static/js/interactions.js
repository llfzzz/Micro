document.addEventListener('DOMContentLoaded', () => {
    // Global Event Delegation for Interactions
    document.body.addEventListener('click', async (e) => {
        const btn = e.target.closest('.metric-item');
        if (!btn) return;

        // Check Login
        if (!window.IS_LOGGED_IN) {
            e.preventDefault();
            e.stopPropagation();
            if (confirm('此操作需要登录，是否前往登录？')) {
                window.location.href = `${window.APP_CTX}/app/login`;
            }
            return;
        }

        const action = btn.dataset.action;
        const id = btn.dataset.id;

        if (action === 'like') {
            handleLike(btn, id);
        } else if (action === 'comment') {
            const username = btn.dataset.username;
            const displayName = btn.dataset.displayname;
            openCommentModal(id, username, displayName);
        } else if (action === 'delete') {
            handleDelete(btn, id);
        }
    });
});

async function handleDelete(btn, postId) {
    if (confirm('确定要删除这条动态吗？')) {
        try {
            await window.apiDelete(`/posts/${postId}`);
            const card = btn.closest('.feed-card');
            if (card) {
                card.style.transition = 'opacity 0.3s ease, height 0.3s ease';
                card.style.opacity = '0';
                setTimeout(() => card.remove(), 300);
            }
        } catch (e) {
            console.error('Delete failed', e);
            alert('删除失败: ' + e.message);
        }
    }
}

async function handleLike(btn, postId) {
    try {
        const res = await window.apiPost(`/posts/${postId}/like`);
        const countSpan = btn.querySelector('.like-count');
        if (countSpan) countSpan.textContent = res.likeCount;
        
        const iconContainer = btn.querySelector('.metric-icon');
        if (res.liked) {
            btn.dataset.liked = "true";
            if(iconContainer) {
                iconContainer.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="#e0245e" stroke="#e0245e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>`;
            }
        } else {
            btn.dataset.liked = "false";
            if(iconContainer) {
                iconContainer.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>`;
            }
        }
    } catch (e) {
        console.error('Like failed', e);
    }
}

function openCommentModal(postId, username, displayName) {
    // Remove existing modal if any
    const existing = document.querySelector('.modal-overlay');
    if (existing) existing.remove();

    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    
    const modal = document.createElement('div');
    modal.className = 'modal-content';
    
    modal.innerHTML = `
        <div class="modal-header">
            <button class="close-btn" style="background:none; border:none; cursor:pointer; padding:0; display:flex; align-items:center; justify-content:center;">
                <svg viewBox="0 0 24 24" aria-hidden="true" style="width: 24px; height: 24px; fill: #0f1419;"><g><path d="M10.59 12L4.54 5.96l1.42-1.42L12 10.59l6.04-6.05 1.42 1.42L13.41 12l6.05 6.04-1.42 1.42L12 13.41l-6.04 6.05-1.42-1.42L10.59 12z"></path></g></svg>
            </button>
        </div>
        <div class="modal-body">
            <div class="reply-info">
                评论 <span class="reply-user">@${username}</span>
            </div>
            <form id="modal-comment-form">
                <textarea name="content" placeholder="说点啥吧" required style="width: 100%; height: 150px; resize: none; padding: 12px; border-radius: 12px; border: 1px solid #e5e7eb; font-family: inherit; font-size: 1rem; margin-top: 10px;"></textarea>
                <div class="modal-footer" style="display: flex; justify-content: flex-end; margin-top: 16px;">
                    <button type="submit" class="btn primary rounded">评论</button>
                </div>
            </form>
        </div>
    `;

    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    // Focus
    setTimeout(() => modal.querySelector('textarea').focus(), 100);

    // Close Logic
    const close = () => overlay.remove();
    modal.querySelector('.close-btn').addEventListener('click', close);
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) close();
    });

    // Submit Logic
    const form = modal.querySelector('#modal-comment-form');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const content = form.content.value;
        try {
            await window.apiPost('/comments', { postId, content });
            close();
            // Optional: Refresh comments if on detail page, or show toast
            if (typeof refreshComments === 'function') {
                refreshComments();
            } else {
                alert('评论已发布');
            }
        } catch (err) {
            console.error(err);
            alert('评论失败');
        }
    });
}
