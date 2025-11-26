document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const logoutButtons = document.querySelectorAll('[data-action="logout"]');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const identifierField = loginForm.username || loginForm.email || loginForm.querySelector('[name="identifier"]');
            const payload = {
                identifier: identifierField ? identifierField.value : '',
                password: loginForm.password.value
            };
            await window.apiPost('/auth/login', payload);
            window.location.href = `${window.APP_CTX || ''}/app/feed`;
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                username: registerForm.username.value,
                password: registerForm.password.value,
                email: registerForm.email.value,
                displayName: registerForm.displayName.value
            };
            await window.apiPost('/auth/register', payload);
            window.location.href = `${window.APP_CTX || ''}/app/feed`;
        });
    }

    logoutButtons.forEach((btn) => {
        btn.addEventListener('click', async () => {
            await window.apiPost('/auth/logout', {});
            window.location.href = `${window.APP_CTX || ''}/app/login`;
        });
    });
});
