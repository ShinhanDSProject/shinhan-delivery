const API_PATH = "/api/v1/addresses";
let editingId = null;
let addresses = [];

function renderAddresses() {
  const addressList = document.getElementById("addressList");
  if (!addressList) return;
  if (!addresses.length) {
    addressList.innerHTML = '<div class="empty-state">등록된 배송 주소가 없습니다.<br>새 주소를 추가해보세요.</div>';
    return;
  }

  addressList.innerHTML = addresses.map((item) => `
    <article class="address-card">
      <div>
        <span class="address-badge">${escapeHtml(item.alias)}</span>
        <p class="address-main">${escapeHtml(item.address)}</p>
        <p class="address-detail">${escapeHtml(item.detailAddress || "상세 주소 없음")}</p>
      </div>
      <div class="address-actions">
        <button class="address-action" type="button" data-action="edit" data-id="${item.id}" aria-label="${escapeHtml(item.alias)} 주소 수정">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="m4 16-.7 4.7L8 20l11-11-4-4L4 16Z" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="m13.5 6.5 4 4" stroke="currentColor" stroke-width="1.8"/></svg>
        </button>
        <button class="address-action address-action--delete" type="button" data-action="delete" data-id="${item.id}" aria-label="${escapeHtml(item.alias)} 주소 삭제">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M5 7h14M9 7V4h6v3m2 0-1 13H8L7 7" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </button>
      </div>
    </article>
  `).join("");
}

async function loadAddresses() {
  const addressList = document.getElementById("addressList");
  try {
    addresses = await MyPageApi.request(API_PATH);
    renderAddresses();
  } catch (error) {
    if (addressList) {
      addressList.innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
    }
  }
}

function openForm(item = null) {
  const contentView = document.getElementById("contentView");
  const formPanel = document.getElementById("formPanel");
  const form = document.getElementById("addressForm");
  const formTitle = document.getElementById("formTitle");
  const headerTitle = document.getElementById("headerTitle");
  const statusMessage = document.getElementById("statusMessage");

  editingId = item?.id || null;
  form.reset();
  if (item) {
    document.getElementById("alias").value = item.alias || "";
    document.getElementById("address").value = item.address || "";
    document.getElementById("detailAddress").value = item.detailAddress || "";
    document.getElementById("pickupGuide").value = item.pickupGuide || "";
  }
  formTitle.textContent = editingId ? "주소 수정" : "새 주소 추가";
  headerTitle.textContent = editingId ? "주소 수정" : "새 주소 추가";
  statusMessage.textContent = "";
  contentView.classList.add("is-hidden");
  formPanel.classList.add("is-open");
  document.getElementById("alias").focus();
}

function closeForm() {
  const contentView = document.getElementById("contentView");
  const formPanel = document.getElementById("formPanel");
  const form = document.getElementById("addressForm");
  const headerTitle = document.getElementById("headerTitle");

  editingId = null;
  form.reset();
  formPanel.classList.remove("is-open");
  contentView.classList.remove("is-hidden");
  headerTitle.textContent = "배송 주소 관리";
}

async function deleteAddress(id) {
  const item = addresses.find((address) => address.id === id);
  if (!window.confirm(`'${item?.alias || "선택한"}' 주소를 삭제할까요?`)) return;

  try {
    await MyPageApi.request(`${API_PATH}/${id}`, { method: "DELETE" });
    addresses = addresses.filter((address) => address.id !== id);
    renderAddresses();
    MyPageApi.showToast("주소가 삭제되었습니다.");
  } catch (error) {
    MyPageApi.showToast(error.message);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const addButton = document.getElementById("addButton");
  const cancelButton = document.getElementById("cancelButton");
  const backButton = document.getElementById("backButton");
  const addressList = document.getElementById("addressList");
  const formPanel = document.getElementById("formPanel");
  const form = document.getElementById("addressForm");
  const saveButton = document.getElementById("saveButton");
  const statusMessage = document.getElementById("statusMessage");

  if (addButton) addButton.addEventListener("click", () => openForm());
  if (cancelButton) cancelButton.addEventListener("click", closeForm);
  if (backButton) {
    backButton.addEventListener("click", () => {
      if (formPanel && formPanel.classList.contains("is-open")) closeForm();
      else location.href = "/my-page";
    });
  }

  if (addressList) {
    addressList.addEventListener("click", (event) => {
      const button = event.target.closest("[data-action]");
      if (!button) return;
      const id = Number(button.dataset.id);
      if (button.dataset.action === "edit") openForm(addresses.find((item) => item.id === id));
      if (button.dataset.action === "delete") deleteAddress(id);
    });
  }

  if (form) {
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const payload = {
        alias: document.getElementById("alias").value.trim(),
        address: document.getElementById("address").value.trim(),
        detailAddress: document.getElementById("detailAddress").value.trim(),
        pickupGuide: document.getElementById("pickupGuide").value.trim()
      };

      if (!payload.alias || !payload.address) {
        statusMessage.textContent = "주소 별칭과 기본 주소를 입력해주세요.";
        statusMessage.className = "status-message is-error";
        return;
      }

      saveButton.disabled = true;
      saveButton.textContent = "저장 중...";
      statusMessage.textContent = "";
      try {
        const saved = await MyPageApi.request(editingId ? `${API_PATH}/${editingId}` : API_PATH, {
          method: editingId ? "PATCH" : "POST",
          body: JSON.stringify(payload)
        });
        if (editingId) {
          addresses = addresses.map((item) => item.id === editingId ? saved : item);
        } else {
          addresses.push(saved);
        }
        closeForm();
        renderAddresses();
        MyPageApi.showToast("주소가 저장되었습니다.");
      } catch (error) {
        statusMessage.textContent = error.message;
        statusMessage.className = "status-message is-error";
      } finally {
        saveButton.disabled = false;
        saveButton.textContent = "저장하기";
      }
    });
  }

  if (typeof MyPageApi !== "undefined" && MyPageApi.requireToken()) {
    loadAddresses();
  }
});
