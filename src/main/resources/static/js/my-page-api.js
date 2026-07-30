(function () {
  const LOGIN_PATH = "/login.html";

  function getToken() {
    return localStorage.getItem("accessToken");
  }

  function redirectToLogin() {
    window.location.replace(LOGIN_PATH);
  }

  function requireToken() {
    const token = getToken();
    if (!token) {
      redirectToLogin();
      return null;
    }
    return token;
  }

  async function parseResponse(response) {
    if (response.status === 401 || response.status === 403) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("tokenType");
      redirectToLogin();
      throw new Error("로그인이 필요합니다.");
    }

    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get("content-type") || "";
    const body = contentType.includes("application/json") ? await response.json() : null;
    if (!response.ok) {
      const validationMessage = body?.errors?.[0]?.reason;
      throw new Error(validationMessage || body?.message || "요청을 처리하지 못했습니다.");
    }
    return body;
  }

  async function request(path, options = {}) {
    const token = requireToken();
    if (!token) {
      throw new Error("로그인이 필요합니다.");
    }

    const headers = new Headers(options.headers || {});
    headers.set("Authorization", `${localStorage.getItem("tokenType") || "Bearer"} ${token}`);
    if (options.body && !headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }

    const response = await fetch(path, { ...options, headers });
    return parseResponse(response);
  }

  function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("tokenType");
    redirectToLogin();
  }

  function roleLabel(role) {
    const labels = {
      CUSTOMER: "일반 회원",
      COURIER: "배송 파트너",
      ADMIN: "관리자"
    };
    return labels[role] || "회원";
  }

  function showToast(message) {
    const toast = document.getElementById("toast");
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add("is-visible");
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => toast.classList.remove("is-visible"), 2200);
  }

  window.MyPageApi = { request, requireToken, logout, roleLabel, showToast };
})();
