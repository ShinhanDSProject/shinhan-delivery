(() => {
  const screens = [...document.querySelectorAll('.onboarding-screen')];
  const splashAction = document.querySelector('.splash-action');
  const nextButtons = [...document.querySelectorAll('.next-button')];
  const skipButtons = [...document.querySelectorAll('.skip-button')];
  const roleCards = [...document.querySelectorAll('.role-card')];
  const roleButton = document.querySelector('.role-button');
  const roleMessage = document.querySelector('#role-message');
  let activeScreen = 0;
  let selectedRole = null;
  let splashTimer;

  function showScreen(index) {
    activeScreen = index;
    screens.forEach((screen, screenIndex) => {
      const isActive = screenIndex === index;
      screen.classList.toggle('is-active', isActive);
      screen.setAttribute('aria-hidden', String(!isActive));
    });

    if (index !== 0) {
      window.clearTimeout(splashTimer);
    }
  }

  function proceedFromSplash() {
    if (activeScreen === 0) {
      showScreen(1);
    }
  }

  // URL 파라미터 확인 (?screen=4 등)
  const urlParams = new URLSearchParams(window.location.search);
  const targetScreen = urlParams.get('screen');
  if (targetScreen !== null && !isNaN(targetScreen)) {
    const screenIndex = parseInt(targetScreen, 10);
    if (screenIndex >= 0 && screenIndex < screens.length) {
      showScreen(screenIndex);
    }
  } else {
    splashTimer = window.setTimeout(proceedFromSplash, 2400);
  }

  splashAction.addEventListener('click', proceedFromSplash);

  nextButtons.forEach((button) => {
    button.addEventListener('click', () => showScreen(Math.min(activeScreen + 1, 4)));
  });

  skipButtons.forEach((button) => {
    button.addEventListener('click', () => showScreen(4));
  });

  roleCards.forEach((card) => {
    card.addEventListener('click', () => {
      selectedRole = card.dataset.role;
      roleCards.forEach((roleCard) => {
        const isSelected = roleCard === card;
        roleCard.classList.toggle('is-selected', isSelected);
        roleCard.setAttribute('aria-checked', String(isSelected));
      });
      roleButton.disabled = false;
      roleMessage.textContent = '';
    });
  });

  roleButton.addEventListener('click', () => {
    if (!selectedRole) {
      return;
    }

    sessionStorage.setItem('selectedRole', selectedRole);
    if (selectedRole === 'COURIER') {
      window.location.assign('/courier-login.html');
    } else {
      window.location.assign('/login.html');
    }
  });
})();
