function authHeader() {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';
    if (!token) return null;
    return `${tokenType} ${token}`;
}

function clearAuthToken() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('tokenType');
}
