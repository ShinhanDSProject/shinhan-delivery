document.addEventListener('DOMContentLoaded', () => {
    // 비밀번호 확인 일치 실시간 검사
    const pwInput = document.getElementById('password');
    const confirmPwInput = document.getElementById('confirmPassword');
    const pwMatchStatus = document.getElementById('pwMatchStatus');

    function checkPasswordMatch() {
        if (!confirmPwInput || !pwMatchStatus) return;
        if (!confirmPwInput.value) {
            pwMatchStatus.textContent = '';
            return;
        }
        if (pwInput.value === confirmPwInput.value) {
            pwMatchStatus.className = 'status-badge success';
            pwMatchStatus.textContent = '✓ 비밀번호가 일치합니다.';
        } else {
            pwMatchStatus.className = 'status-badge error';
            pwMatchStatus.textContent = '✕ 비밀번호가 일치하지 않습니다.';
        }
    }

    if (pwInput) pwInput.addEventListener('input', checkPasswordMatch);
    if (confirmPwInput) confirmPwInput.addEventListener('input', checkPasswordMatch);

    // 이용동의 전체 선택 / 해제
    const checkAll = document.getElementById('checkAll');
    const termChecks = document.querySelectorAll('.term-check');

    if (checkAll) {
        checkAll.addEventListener('change', function() {
            termChecks.forEach(cb => cb.checked = checkAll.checked);
        });
    }

    termChecks.forEach(cb => {
        cb.addEventListener('change', function() {
            if (checkAll) {
                const allChecked = Array.from(termChecks).every(c => c.checked);
                checkAll.checked = allChecked;
            }
        });
    });

    // 가입 완료 제출
    const signupForm = document.getElementById('signupForm');
    if (signupForm) {
        signupForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const resultToast = document.getElementById('resultToast');

            const name = document.getElementById('name').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const phoneNumber = document.getElementById('phoneNumber').value.trim();

            if (password !== confirmPassword) {
                if (resultToast) {
                    resultToast.className = 'result-toast error';
                    resultToast.textContent = '비밀번호와 비밀번호 확인이 일치하지 않습니다.';
                }
                return;
            }

            try {
                const res = await fetch('/api/v1/members', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        name,
                        email,
                        password,
                        phoneNumber,
                        role: 'CUSTOMER'
                    })
                });

                const data = await res.json();

                if (res.ok) {
                    if (resultToast) {
                        resultToast.className = 'result-toast success';
                        resultToast.textContent = `🎉 가입 완료! 축하합니다, ${data.name}님 (${data.email})`;
                    }
                    setTimeout(() => {
                        location.href = '/login';
                    }, 2000);
                } else {
                    if (resultToast) {
                        resultToast.className = 'result-toast error';
                        resultToast.textContent = data.message || '가입 처리 중 오류가 발생했습니다.';
                    }
                }
            } catch (err) {
                if (resultToast) {
                    resultToast.className = 'result-toast error';
                    resultToast.textContent = '서버 통신 오류가 발생했습니다.';
                }
            }
        });
    }
});
