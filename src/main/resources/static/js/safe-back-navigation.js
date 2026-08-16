(function () {
  "use strict";

  const BLOCKED_RETURN_PATHS = new Set(["/login", "/signup", "/my-page"]);

  function canReturnToReferrer(referrer) {
    if (!referrer) return false;

    try {
      const referrerUrl = new URL(referrer);
      return (
        referrerUrl.origin === window.location.origin &&
        !BLOCKED_RETURN_PATHS.has(referrerUrl.pathname)
      );
    } catch (error) {
      return false;
    }
  }

  document.querySelectorAll("[data-safe-back-button]").forEach((button) => {
    button.addEventListener("click", () => {
      if (canReturnToReferrer(document.referrer)) {
        window.history.back();
        return;
      }

      window.location.assign(button.dataset.fallbackPath || "/home");
    });
  });
})();
