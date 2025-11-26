const API_BASE = window.APP_CTX ? `${window.APP_CTX}/api` : '/api';

async function apiRequest(path, options = {}) {
    const config = {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        },
        ...options
    };
    const response = await fetch(`${API_BASE}${path}`, config);
    const data = await response.json();
    if (!response.ok || data.success === false) {
        throw new Error(data?.error?.message || response.statusText);
    }
    return data.data;
}
window.apiGet = (path) => apiRequest(path, { method: 'GET' });
window.apiPost = (path, body) => apiRequest(path, { method: 'POST', body: JSON.stringify(body) });
window.apiDelete = (path) => apiRequest(path, { method: 'DELETE' });
