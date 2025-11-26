document.addEventListener('DOMContentLoaded', () => {
    const commentForm = document.getElementById('comment-form');
    const commentList = document.getElementById('comment-list');
    const postId = commentForm?.dataset.postId || commentList?.dataset.postId;

    async function refreshComments() {
        if (!postId) return;
        const data = await window.apiGet(`/comments?postId=${postId}`);
        commentList.innerHTML = '';
        (data.items || []).forEach((item) => {
            const el = document.createElement('div');
            el.className = 'comment-item';
            el.innerHTML = `
                <strong>@${item.username || 'user'}</strong>
                <p>${item.content}</p>
            `;
            commentList.appendChild(el);
        });
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
            refreshComments();
        });
    }

    refreshComments();
});
