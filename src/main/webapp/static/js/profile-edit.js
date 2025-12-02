(function () {
    const form = document.getElementById('profile-form');
    if (!form) {
        return;
    }
    const userId = form.dataset.userId;
    const flash = document.getElementById('profile-flash');
    const avatarInput = document.getElementById('avatarInput');
    const avatarPreview = document.getElementById('avatar-preview');
    const bannerInput = document.getElementById('bannerInput');
    const bannerPreview = document.getElementById('banner-preview');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const payload = {
            displayName: form.displayName.value.trim(),
            bio: form.bio.value.trim()
        };
        try {
            await window.apiPut(`/users/${userId}`, payload);
            showFlash('资料已更新', 'success');
            setTimeout(() => {
                window.location.href = `${window.APP_CTX || ''}/app/profile?id=${userId}`;
            }, 1000);
        } catch (err) {
            showFlash(err.message || '更新失败，请稍后重试', 'error');
        }
    });

    avatarInput?.addEventListener('change', async () => {
        const file = avatarInput.files && avatarInput.files[0];
        if (!file) {
            return;
        }
        const formData = new FormData();
        formData.append('file', file);
        try {
            const response = await fetch(`${window.APP_CTX || ''}/api/users/${userId}/avatar`, {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });
            const payload = await response.json();
            if (!response.ok || payload.success === false) {
                throw new Error(payload?.error?.message || '上传失败');
            }
            if (payload.data?.avatarPath) {
                updateAvatarPreview(payload.data.avatarPath);
            }
            showFlash('头像已更新', 'success');
        } catch (err) {
            showFlash(err.message || '上传失败，请稍后再试', 'error');
        } finally {
            avatarInput.value = '';
        }
    });

    bannerInput?.addEventListener('change', async () => {
        const file = bannerInput.files && bannerInput.files[0];
        if (!file) {
            return;
        }
        const formData = new FormData();
        formData.append('file', file);
        try {
            const response = await fetch(`${window.APP_CTX || ''}/api/users/${userId}/banner`, {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });
            const payload = await response.json();
            if (!response.ok || payload.success === false) {
                throw new Error(payload?.error?.message || '上传失败');
            }
            if (payload.data?.bannerPath) {
                updateBannerPreview(payload.data.bannerPath);
            }
            showFlash('背景图已更新', 'success');
        } catch (err) {
            showFlash(err.message || '上传失败，请稍后再试', 'error');
        } finally {
            bannerInput.value = '';
        }
    });

    function updateAvatarPreview(path) {
        if (!avatarPreview) {
            return;
        }
        avatarPreview.innerHTML = '';
        const img = document.createElement('img');
        img.alt = '头像';
        img.src = `${window.APP_CTX || ''}/static/uploads/${path}`;
        avatarPreview.appendChild(img);
    }

    function updateBannerPreview(path) {
        if (!bannerPreview) {
            return;
        }
        bannerPreview.innerHTML = '';
        const img = document.createElement('img');
        img.alt = '背景图';
        img.src = `${window.APP_CTX || ''}/static/uploads/${path}`;
        img.style.width = '100%';
        img.style.height = '100%';
        img.style.objectFit = 'cover';
        bannerPreview.appendChild(img);
    }

    function showFlash(message, variant) {
        if (!flash) {
            return;
        }
        flash.textContent = message;
        flash.dataset.variant = variant;
    }
})();
