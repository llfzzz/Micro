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
