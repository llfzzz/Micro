window.initUpload = (input, previewContainer) => {
    if (!input) return;
    input.addEventListener('change', async () => {
        previewContainer.innerHTML = '';
        const files = Array.from(input.files || []);
        const uploads = [];
        for (const file of files) {
            const formData = new FormData();
            formData.append('file', file);
            uploads.push(uploadFile(formData, file, previewContainer));
        }
        const results = await Promise.all(uploads);
        input.dataset.mediaMeta = JSON.stringify(results);
    });
};

async function uploadFile(formData, file, previewContainer) {
    const card = document.createElement('div');
    card.className = 'upload-preview';
    card.textContent = `上传中：${file.name}`;
    previewContainer.appendChild(card);

    const response = await fetch(`${window.APP_CTX || ''}/api/uploads`, {
        method: 'POST',
        body: formData,
        credentials: 'include'
    });
    const payload = await response.json();
    if (!response.ok || payload.success === false) {
        card.textContent = `失败：${file.name}`;
        throw new Error(payload?.error?.message || '上传失败');
    }
    card.textContent = file.name;
    return payload.data || payload;
}
