(function () {
  "use strict";

  const BLOCKED_RETURN_PATHS = new Set(["/login", "/signup", "/my-page"]);

  function getAllowedReferrer(referrer) {
    if (!referrer) return null;

    try {
      const referrerUrl = new URL(referrer);
      if (
        referrerUrl.origin === window.location.origin &&
        !BLOCKED_RETURN_PATHS.has(referrerUrl.pathname)
      ) {
        return referrerUrl;
      }
    } catch (error) {
      // 잘못된 referrer는 아래의 명시적 fallback 경로로 처리한다.
    }

    return null;
  }

  document.querySelectorAll("[data-safe-back-button]").forEach((button) => {
    button.addEventListener("click", () => {
      const referrerUrl = getAllowedReferrer(document.referrer);
      if (referrerUrl) {
        window.location.assign(referrerUrl.href);
        return;
      }

      window.location.assign(button.dataset.fallbackPath || "/home");
    });
  });
})();
