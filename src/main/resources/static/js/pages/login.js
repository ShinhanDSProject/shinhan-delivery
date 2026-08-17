document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const loginButton = document.getElementById('loginButton');
    const message = document.getElementById('message');

    function showMessage(text, isSuccess = false) {
        if (!message) return;
        message.textContent = text;
        message.className = isSuccess ? 'message show success' : 'message show';
    }

    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            if (message) message.classList.remove('show');

            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            if (!email || !password) {
                showMessage('이메일 주소와 비밀번호를 모두 입력해주세요.');
                return;
            }

            if (loginButton) {
                loginButton.disabled = true;
                loginButton.textContent = '로그인 중...';
            }

            try {
                const response = await fetch('/api/v1/members/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });
                const data = await response.json();
                if (!response.ok) {
                    showMessage(data.message || '이메일 또는 비밀번호가 올바르지 않습니다.');
                    return;
                }

                localStorage.setItem('accessToken', data.accessToken);
                localStorage.setItem('tokenType', data.tokenType || 'Bearer');
                showMessage('로그인에 성공했습니다.', true);
                location.assign('/home');
            } catch (error) {
                showMessage('서버 통신 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            } finally {
                if (loginButton) {
                    loginButton.disabled = false;
                    loginButton.textContent = '로그인';
                }
            }
        });
    }

    const forgotPassword = document.getElementById('forgotPassword');
    if (forgotPassword) {
        forgotPassword.addEventListener('click', (event) => {
            event.preventDefault();
            showMessage('비밀번호 찾기 기능은 아직 준비 중입니다.');
        });
    }
});
