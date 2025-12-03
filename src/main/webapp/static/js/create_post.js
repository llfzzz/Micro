document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('file-input');
    const addBtn = document.getElementById('add-media-btn');
    const previewList = document.getElementById('media-preview-list');
    const btnNext = document.getElementById('btn-next');
    const btnBack = document.getElementById('btn-back');
    const stepMedia = document.getElementById('step-media');
    const stepText = document.getElementById('step-text');
    const createForm = document.getElementById('create-form');
    const contentText = document.getElementById('content-text');
    
    // State
    let mediaFiles = []; // Array of { file, type, url (local or remote) }
    let uploadedMeta = []; // Array of uploaded results

    // --- Media Handling ---
    if (addBtn && fileInput) {
        addBtn.addEventListener('click', () => fileInput.click());

        fileInput.addEventListener('change', async (e) => {
            const files = Array.from(e.target.files);
            if (files.length === 0) return;

            // Determine current state
            const hasVideo = mediaFiles.some(m => m.type.startsWith('video'));
            const imageCount = mediaFiles.filter(m => m.type.startsWith('image')).length;

            for (const file of files) {
                const isVideo = file.type.startsWith('video');
                const isImage = file.type.startsWith('image');

                if (isVideo) {
                    if (mediaFiles.length > 0) {
                        alert('视频只能单独选择，且不能与图片混选');
                        continue;
                    }
                    mediaFiles.push({ file, type: file.type, url: URL.createObjectURL(file) });
                } else if (isImage) {
                    if (hasVideo || mediaFiles.some(m => m.type.startsWith('video'))) {
                        alert('图片不能与视频混选');
                        continue;
                    }
                    if (mediaFiles.length >= 10) {
                        alert('最多只能选择10张图片');
                        break;
                    }
                    mediaFiles.push({ file, type: file.type, url: URL.createObjectURL(file) });
                }
            }
            
            renderMedia();
            fileInput.value = ''; // Reset
        });
    }

    function renderMedia() {
        // Clear existing previews (except add button)
        const items = previewList.querySelectorAll('.media-item');
        items.forEach(el => el.remove());

        mediaFiles.forEach((media, index) => {
            const div = document.createElement('div');
            div.className = 'media-item';
            
            let content = '';
            if (media.type.startsWith('video')) {
                content = `<video src="${media.url}#t=0.1" preload="metadata"></video>`;
            } else {
                content = `<img src="${media.url}">`;
            }
            
            div.innerHTML = `
                ${content}
                <div class="media-remove" data-index="${index}">×</div>
            `;
            previewList.insertBefore(div, addBtn);
        });

        // Update Add Button State
        const hasVideo = mediaFiles.some(m => m.type.startsWith('video'));
        const imageCount = mediaFiles.length;
        
        if (hasVideo) {
            addBtn.classList.add('disabled');
        } else if (imageCount >= 10) {
            addBtn.classList.add('disabled');
        } else {
            addBtn.classList.remove('disabled');
        }

        // Update Next Button
        btnNext.disabled = mediaFiles.length === 0;
        
        // Bind remove events
        document.querySelectorAll('.media-remove').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const idx = parseInt(e.target.dataset.index);
                mediaFiles.splice(idx, 1);
                renderMedia();
            });
        });
    }

    // --- Navigation ---
    if (btnNext) {
        btnNext.addEventListener('click', async () => {
            // Upload files if not already uploaded (simplified: upload all now)
            // In a real app, we might upload as we select, but here we do it on Next.
            // Show loading state?
            btnNext.textContent = '处理中...';
            btnNext.disabled = true;
            
            try {
                uploadedMeta = [];
                for (const media of mediaFiles) {
                    const formData = new FormData();
                    formData.append('file', media.file);
                    const res = await fetch(`${window.APP_CTX}/api/upload`, {
                        method: 'POST',
                        body: formData
                    });
                    const json = await res.json();
                    if (json.success) {
                        uploadedMeta.push(json.data);
                    } else {
                        throw new Error(json.error || '上传失败');
                    }
                }
                
                stepMedia.classList.remove('active');
                stepText.classList.add('active');
            } catch (err) {
                alert('上传媒体失败: ' + err.message);
            } finally {
                btnNext.textContent = '下一步';
                btnNext.disabled = false;
            }
        });
    }

    if (btnBack) {
        btnBack.addEventListener('click', () => {
            stepText.classList.remove('active');
            stepMedia.classList.add('active');
        });
    }

    // --- Text & Autocomplete (Adapted from Feed) ---
    const suggestionsBox = document.getElementById('mention-suggestions');
    const mentionBtn = document.querySelector('[data-action="mention"]');
    const hashtagBtn = document.querySelector('[data-action="hashtag"]');

    if (mentionBtn) mentionBtn.addEventListener('click', () => insertText('@'));
    if (hashtagBtn) hashtagBtn.addEventListener('click', () => insertText('#'));

    function insertText(char) {
        if (!contentText) return;
        const start = contentText.selectionStart;
        const end = contentText.selectionEnd;
        const text = contentText.value;
        contentText.value = text.substring(0, start) + char + text.substring(end);
        contentText.selectionStart = contentText.selectionEnd = start + 1;
        contentText.focus();
        contentText.dispatchEvent(new Event('input'));
    }

    if (contentText) {
        contentText.addEventListener('input', async () => {
            const cursor = contentText.selectionStart;
            const text = contentText.value;
            const beforeCursor = text.substring(0, cursor);
            
            const lastAt = beforeCursor.lastIndexOf('@');
            const lastHash = beforeCursor.lastIndexOf('#');
            
            let triggerChar = null;
            let triggerIndex = -1;
            
            if (lastAt > lastHash) {
                triggerChar = '@';
                triggerIndex = lastAt;
            } else if (lastHash > lastAt) {
                triggerChar = '#';
                triggerIndex = lastHash;
            }
            
            if (triggerIndex !== -1) {
                const query = beforeCursor.substring(triggerIndex + 1);
                if (!/\s/.test(query)) {
                    await showSuggestions(triggerChar, query, triggerIndex);
                    return;
                }
            }
            suggestionsBox.style.display = 'none';
        });
    }

    async function showSuggestions(type, query, atIndex) {
        if (!query) {
            suggestionsBox.style.display = 'none';
            return;
        }
        try {
            let items = [];
            if (type === '@') {
                items = await window.apiGet(`/users?q=${encodeURIComponent(query)}`);
            } else if (type === '#') {
                items = await window.apiGet(`/posts/tags?q=${encodeURIComponent(query)}`);
            }

            if (!items || items.length === 0) {
                suggestionsBox.style.display = 'none';
                return;
            }
            
            suggestionsBox.innerHTML = '';
            items.forEach(item => {
                const div = document.createElement('div');
                div.className = 'suggestion-item';
                
                if (type === '@') {
                    const avatar = item.avatarPath ? `${window.APP_CTX}/static/uploads/${item.avatarPath}` : 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg"/>';
                    div.innerHTML = `
                        <img src="${avatar}" onerror="this.style.display='none'">
                        <div>
                            <strong>${item.displayName || item.username}</strong>
                            <span>@${item.username}</span>
                        </div>
                    `;
                    div.addEventListener('click', () => {
                        insertSuggestion(item.displayName || item.username, atIndex);
                    });
                } else {
                    div.innerHTML = `<div><strong>#${item}</strong></div>`;
                    div.addEventListener('click', () => {
                        insertSuggestion(item, atIndex);
                    });
                }
                suggestionsBox.appendChild(div);
            });
            suggestionsBox.style.display = 'block';
        } catch (err) {
            console.error(err);
        }
    }

    function insertSuggestion(text, atIndex) {
        const content = contentText.value;
        const before = content.substring(0, atIndex + 1);
        const after = content.substring(contentText.selectionStart);
        contentText.value = `${before}${text} ${after}`;
        suggestionsBox.style.display = 'none';
        contentText.focus();
    }

    // --- Submit ---
    if (createForm) {
        createForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                contentText: contentText.value,
                mediaMetaJson: JSON.stringify(uploadedMeta)
            };
            try {
                await window.apiPost('/posts', payload);
                window.location.href = 'feed';
            } catch (err) {
                alert('发布失败: ' + err.message);
            }
        });
    }
});