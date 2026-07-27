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

  splashAction.addEventListener('click', proceedFromSplash);
  splashTimer = window.setTimeout(proceedFromSplash, 2400);

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
    window.location.assign('/login.html');
  });
})();
