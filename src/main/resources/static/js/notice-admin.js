(function () {
  const API_PATH = "/api/v1/notices";
  const PROFILE_PATH = "/api/v1/members/me";
  const state = { notices: [], selected: null, editing: false, isAdmin: false };

  const elements = {
    listView: document.getElementById("listView"),
    noticeList: document.getElementById("noticeList"),
    detailView: document.getElementById("detailView"),
    detailTitle: document.getElementById("detailTitle"),
    detailCategory: document.getElementById("detailCategory"),
    detailMeta: document.getElementById("detailMeta"),
    detailContent: document.getElementById("detailContent"),
    formView: document.getElementById("formView"),
    formTitle: document.getElementById("formTitle"),
    form: document.getElementById("noticeForm"),
    title: document.getElementById("noticeTitle"),
    category: document.getElementById("noticeCategory"),
    content: document.getElementById("noticeContent"),
    pinned: document.getElementById("noticePinned"),
    titleError: document.getElementById("titleError"),
    contentError: document.getElementById("contentError"),
    status: document.getElementById("statusMessage"),
    createButton: document.getElementById("createButton"),
    editButton: document.getElementById("editButton"),
    deleteButton: document.getElementById("deleteButton"),
    closeDetailButton: document.getElementById("closeDetailButton"),
    deleteDialog: document.getElementById("deleteDialog"),
    deleteNoticeTitle: document.getElementById("deleteNoticeTitle"),
    confirmDeleteButton: document.getElementById("confirmDeleteButton"),
    cancelDeleteButton: document.getElementById("cancelDeleteButton")
  };

  function authHeaders() {
    const token = localStorage.getItem("accessToken");
    if (!token) return {};
    return { Authorization: `${localStorage.getItem("tokenType") || "Bearer"} ${token}` };
  }

  async function parseResponse(response) {
    if (response.status === 204) return null;
    const body = response.headers.get("content-type")?.includes("application/json")
      ? await response.json()
      : null;
    if (!response.ok) {
      throw new Error(body?.errors?.[0]?.reason || body?.message || "요청을 처리하지 못했습니다.");
    }
    return body;
  }

  function showStatus(message) {
    elements.status.textContent = message;
  }

  function formatDate(value) {
    if (!value) return "";
    return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeStyle: "short" })
      .format(new Date(value));
  }

  function showOnly(view) {
    elements.listView.hidden = view !== elements.listView;
    elements.detailView.hidden = view !== elements.detailView;
    elements.formView.hidden = view !== elements.formView;
  }

  function createNoticeCard(notice) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "card-box notice-card";
    button.setAttribute("aria-label", `${notice.title} 공지사항 상세 보기`);

    const heading = document.createElement("span");
    heading.className = "notice-card__heading";
    const title = document.createElement("strong");
    title.textContent = notice.title;
    const badge = document.createElement("span");
    badge.className = "badge badge-primary";
    badge.textContent = notice.isPinned ? "상단 고정" : notice.category;
    heading.append(title, badge);

    const date = document.createElement("span");
    date.className = "notice-meta";
    date.textContent = formatDate(notice.createdAt);
    button.append(heading, date);
    button.addEventListener("click", () => openDetail(notice.id));
    return button;
  }

  function renderList() {
    elements.noticeList.replaceChildren();
    if (!state.notices.length) {
      const empty = document.createElement("div");
      empty.className = "card-box";
      empty.textContent = "등록된 공지사항이 없습니다.";
      elements.noticeList.appendChild(empty);
      return;
    }
    state.notices.forEach((notice) => elements.noticeList.appendChild(createNoticeCard(notice)));
  }

  async function loadNotices() {
    try {
      const page = await parseResponse(await fetch(`${API_PATH}?size=30`));
      state.notices = page.content || [];
      renderList();
      showStatus("");
    } catch (error) {
      showStatus(error.message);
    }
  }

  async function resolveAdmin() {
    if (!localStorage.getItem("accessToken")) return;
    try {
      const profile = await parseResponse(await fetch(PROFILE_PATH, { headers: authHeaders() }));
      state.isAdmin = profile.role === "ADMIN";
      document.querySelectorAll(".admin-only").forEach((element) => {
        element.hidden = !state.isAdmin;
      });
    } catch (error) {
      state.isAdmin = false;
    }
  }

  async function openDetail(id) {
    try {
      state.selected = await parseResponse(await fetch(`${API_PATH}/${id}`));
      elements.detailTitle.textContent = state.selected.title;
      elements.detailCategory.textContent = state.selected.category;
      elements.detailMeta.textContent = formatDate(state.selected.createdAt);
      elements.detailContent.textContent = state.selected.content;
      showOnly(elements.detailView);
      showStatus("");
    } catch (error) {
      showStatus(error.message);
    }
  }

  function openForm(editing) {
    state.editing = editing;
    elements.formTitle.textContent = editing ? "공지 수정" : "공지 작성";
    elements.title.value = editing ? state.selected.title : "";
    elements.category.value = editing ? state.selected.category : "SYSTEM";
    elements.content.value = editing ? state.selected.content : "";
    elements.pinned.checked = editing ? state.selected.isPinned : false;
    elements.titleError.textContent = "";
    elements.contentError.textContent = "";
    showOnly(elements.formView);
    elements.title.focus();
  }

  function validateForm() {
    const title = elements.title.value.trim();
    const content = elements.content.value.trim();
    elements.titleError.textContent = !title
      ? "공지사항 제목은 필수입니다."
      : title.length > 150 ? "공지사항 제목은 150자 이하여야 합니다." : "";
    elements.contentError.textContent = content ? "" : "공지사항 본문은 필수입니다.";
    return !elements.titleError.textContent && !elements.contentError.textContent;
  }

  async function submitForm(event) {
    event.preventDefault();
    if (!validateForm()) return;
    const submitButton = elements.form.querySelector("button[type='submit']");
    submitButton.disabled = true;
    const path = state.editing ? `${API_PATH}/${state.selected.id}` : API_PATH;
    const method = state.editing ? "PUT" : "POST";
    const payload = {
      title: elements.title.value.trim(),
      content: elements.content.value.trim(),
      category: elements.category.value,
      isPinned: elements.pinned.checked
    };
    try {
      const saved = await parseResponse(await fetch(path, {
        method,
        headers: { ...authHeaders(), "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      }));
      await loadNotices();
      await openDetail(saved.id);
      showStatus(state.editing ? "공지사항을 수정했습니다." : "공지사항을 등록했습니다.");
    } catch (error) {
      showStatus(error.message);
    } finally {
      submitButton.disabled = false;
    }
  }

  async function deleteNotice() {
    elements.confirmDeleteButton.disabled = true;
    try {
      await parseResponse(await fetch(`${API_PATH}/${state.selected.id}`, {
        method: "DELETE",
        headers: authHeaders()
      }));
      elements.deleteDialog.close();
      await loadNotices();
      showOnly(elements.listView);
      showStatus("공지사항을 삭제했습니다.");
    } catch (error) {
      elements.deleteDialog.close();
      showStatus(error.message);
    } finally {
      elements.confirmDeleteButton.disabled = false;
    }
  }

  elements.createButton.addEventListener("click", () => openForm(false));
  elements.editButton.addEventListener("click", () => openForm(true));
  elements.deleteButton.addEventListener("click", () => {
    elements.deleteNoticeTitle.textContent = state.selected.title;
    elements.deleteDialog.showModal();
  });
  elements.closeDetailButton.addEventListener("click", () => showOnly(elements.listView));
  elements.form.addEventListener("submit", submitForm);
  elements.form.querySelector("button[type='button']").addEventListener("click", () => {
    showOnly(state.editing ? elements.detailView : elements.listView);
  });
  elements.confirmDeleteButton.addEventListener("click", deleteNotice);
  elements.cancelDeleteButton.addEventListener("click", () => elements.deleteDialog.close());

  Promise.all([loadNotices(), resolveAdmin()]);
})();
