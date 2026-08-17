function showToast(message, duration = 2000) {
    const existing = document.getElementById('toast');
    if (existing) existing.remove();

    const toast = document.createElement('div');
    toast.id = 'toast';
    toast.className = 'toast';
    toast.textContent = message;
    toast.style.cssText =
        'position:fixed;left:50%;bottom:90px;transform:translateX(-50%);' +
        'background:#1e293b;color:#fff;padding:10px 20px;border-radius:999px;' +
        'font-size:13px;z-index:100;';
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), duration);
}
