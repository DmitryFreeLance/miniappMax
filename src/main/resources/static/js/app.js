const state = {
    userId: null,
    authenticated: false,
    isAdmin: false,
    userFullName: null,

    products: [],
    fixPriceProducts: [],
    catalogLoaded: false,
    fixPriceLoaded: false,
    catalogQuery: "",
    fixPriceQuery: "",
    catalogOnlyAvailable: false,
    fixOnlyAvailable: false,
    adminProducts: [],

    selectedProduct: null,
    editingProductId: null,

    cartItems: [],
    cartDeliveryMethod: "CITY_DELIVERY",
    cartPaymentMethod: "CARD_NOW",
    cartItemsTotal: 0,
    cartDeliveryFee: 0,
    cartTotal: 0,

    cityDeliveryFee: 1000
};

const el = {
    tabs: document.querySelectorAll(".tab"),
    tabContents: document.querySelectorAll(".tab-content"),
    adminTabBtn: document.getElementById("adminTabBtn"),
    adminOrdersTabBtn: document.getElementById("adminOrdersTabBtn"),

    catalogGrid: document.getElementById("catalogGrid"),
    catalogEmpty: document.getElementById("catalogEmpty"),
    fixPriceGrid: document.getElementById("fixPriceGrid"),
    fixPriceEmpty: document.getElementById("fixPriceEmpty"),
    catalogBackBtn: document.getElementById("catalogBackBtn"),
    catalogSearchInput: document.getElementById("catalogSearchInput"),
    catalogSearchClear: document.getElementById("catalogSearchClear"),
    catalogFilterBtn: document.getElementById("catalogFilterBtn"),
    fixBackBtn: document.getElementById("fixBackBtn"),
    fixSearchInput: document.getElementById("fixSearchInput"),
    fixSearchClear: document.getElementById("fixSearchClear"),
    fixFilterBtn: document.getElementById("fixFilterBtn"),

    cartTabBadge: document.getElementById("cartTabBadge"),
    cartItems: document.getElementById("cartItems"),
    cartEmpty: document.getElementById("cartEmpty"),
    cartCheckoutPanel: document.getElementById("cartCheckoutPanel"),
    cartFullName: document.getElementById("cartFullName"),
    cartPhone: document.getElementById("cartPhone"),
    cartAddress: document.getElementById("cartAddress"),
    cartItemsTotal: document.getElementById("cartItemsTotal"),
    cartDeliveryFee: document.getElementById("cartDeliveryFee"),
    cartTotal: document.getElementById("cartTotal"),
    cartCheckoutBtn: document.getElementById("cartCheckoutBtn"),
    cartPaymentStep: document.getElementById("cartPaymentStep"),
    cartPayTotal: document.getElementById("cartPayTotal"),
    cartPaymentDetailsText: document.getElementById("cartPaymentDetailsText"),
    cartConfirmPaidBtn: document.getElementById("cartConfirmPaidBtn"),
    cartBackFromPaymentBtn: document.getElementById("cartBackFromPaymentBtn"),
    cartMessage: document.getElementById("cartMessage"),

    infoPosts: document.getElementById("infoPosts"),

    adminCatalogProducts: document.getElementById("adminCatalogProducts"),
    adminFixPriceProducts: document.getElementById("adminFixPriceProducts"),
    adminPostsList: document.getElementById("adminPostsList"),
    adminOrdersGrid: document.getElementById("adminOrdersGrid"),
    adminOrdersEmpty: document.getElementById("adminOrdersEmpty"),

    productModal: document.getElementById("productModal"),
    closeProductModal: document.getElementById("closeProductModal"),
    modalImage: document.getElementById("modalImage"),
    modalTitle: document.getElementById("modalTitle"),
    modalDescription: document.getElementById("modalDescription"),
    modalOldPriceLine: document.getElementById("modalOldPriceLine"),
    modalOldPrice: document.getElementById("modalOldPrice"),
    modalPrice: document.getElementById("modalPrice"),
    modalStock: document.getElementById("modalStock"),
    modalQuantity: document.getElementById("modalQuantity"),
    modalUnit: document.getElementById("modalUnit"),
    orderBtn: document.getElementById("orderBtn"),

    productForm: document.getElementById("productForm"),
    sectionType: document.getElementById("sectionType"),
    oldPriceWrapper: document.getElementById("oldPriceWrapper"),
    oldPriceInput: document.getElementById("oldPriceInput"),
    pricePcsWrapper: document.getElementById("pricePcsWrapper"),
    pricePcsInput: document.getElementById("pricePcsInput"),
    priceCubicWrapper: document.getElementById("priceCubicWrapper"),
    priceCubicInput: document.getElementById("priceCubicInput"),
    unitModeSelect: document.getElementById("unitModeSelect"),
    stockPcsWrapper: document.getElementById("stockPcsWrapper"),
    stockPcsInput: document.getElementById("stockPcsInput"),
    stockCubicWrapper: document.getElementById("stockCubicWrapper"),
    stockCubicInput: document.getElementById("stockCubicInput"),

    postForm: document.getElementById("postForm"),
    paymentDetailsForm: document.getElementById("paymentDetailsForm"),
    paymentDetailsInput: document.getElementById("paymentDetailsInput"),
    addAdminForm: document.getElementById("addAdminForm"),
    adminsList: document.getElementById("adminsList"),
    usersList: document.getElementById("usersList"),

    appDialog: document.getElementById("appDialog"),
    dialogTitle: document.getElementById("dialogTitle"),
    dialogMessage: document.getElementById("dialogMessage"),
    dialogInput: document.getElementById("dialogInput"),
    dialogCancelBtn: document.getElementById("dialogCancelBtn"),
    dialogConfirmBtn: document.getElementById("dialogConfirmBtn"),

    editProductModal: document.getElementById("editProductModal"),
    editProductForm: document.getElementById("editProductForm"),
    editProductName: document.getElementById("editProductName"),
    editProductDescription: document.getElementById("editProductDescription"),
    editSectionType: document.getElementById("editSectionType"),
    editOldPriceWrapper: document.getElementById("editOldPriceWrapper"),
    editOldPriceInput: document.getElementById("editOldPriceInput"),
    editUnitModeSelect: document.getElementById("editUnitModeSelect"),
    editPricePcsWrapper: document.getElementById("editPricePcsWrapper"),
    editPricePcsInput: document.getElementById("editPricePcsInput"),
    editPriceCubicWrapper: document.getElementById("editPriceCubicWrapper"),
    editPriceCubicInput: document.getElementById("editPriceCubicInput"),
    editStockPcsWrapper: document.getElementById("editStockPcsWrapper"),
    editStockPcsInput: document.getElementById("editStockPcsInput"),
    editStockCubicWrapper: document.getElementById("editStockCubicWrapper"),
    editStockCubicInput: document.getElementById("editStockCubicInput"),
    editProductActiveSelect: document.getElementById("editProductActiveSelect"),
    cancelEditProductBtn: document.getElementById("cancelEditProductBtn")
};

let dialogResolver = null;
let dialogLastActive = null;

function closeDialog(payload) {
    if (!dialogResolver) {
        return;
    }
    el.appDialog.classList.add("hidden");
    const resolver = dialogResolver;
    dialogResolver = null;
    if (dialogLastActive && typeof dialogLastActive.focus === "function") {
        dialogLastActive.focus();
    }
    resolver(payload);
}

function showDialog(options) {
    const cfg = options || {};
    if (dialogResolver) {
        closeDialog({confirmed: false, value: null});
    }

    dialogLastActive = document.activeElement;

    el.dialogTitle.textContent = cfg.title || "Уведомление";
    el.dialogMessage.textContent = cfg.message || "";

    const hasInput = !!cfg.withInput;
    el.dialogInput.classList.toggle("hidden", !hasInput);
    el.dialogInput.value = hasInput ? (cfg.inputValue || "") : "";
    el.dialogInput.placeholder = hasInput ? (cfg.inputPlaceholder || "") : "";
    el.dialogInput.type = cfg.inputType || "text";

    const hasCancel = !!cfg.withCancel;
    el.dialogCancelBtn.classList.toggle("hidden", !hasCancel);
    el.dialogCancelBtn.textContent = cfg.cancelText || "Отмена";
    el.dialogConfirmBtn.textContent = cfg.confirmText || "ОК";

    el.appDialog.classList.remove("hidden");

    return new Promise((resolve) => {
        dialogResolver = resolve;
        if (hasInput) {
            requestAnimationFrame(() => {
                el.dialogInput.focus();
                el.dialogInput.select();
            });
        } else {
            requestAnimationFrame(() => {
                el.dialogConfirmBtn.focus();
            });
        }
    });
}

function initDialog() {
    el.dialogConfirmBtn.addEventListener("click", () => {
        const value = !el.dialogInput.classList.contains("hidden")
            ? el.dialogInput.value
            : null;
        closeDialog({confirmed: true, value});
    });

    el.dialogCancelBtn.addEventListener("click", () => {
        closeDialog({confirmed: false, value: null});
    });

    el.appDialog.addEventListener("click", (event) => {
        if (event.target === el.appDialog) {
            closeDialog({confirmed: false, value: null});
        }
    });

    document.addEventListener("keydown", (event) => {
        if (el.appDialog.classList.contains("hidden")) {
            return;
        }
        if (event.key === "Escape") {
            event.preventDefault();
            closeDialog({confirmed: false, value: null});
            return;
        }
        if (event.key === "Enter") {
            event.preventDefault();
            const value = !el.dialogInput.classList.contains("hidden")
                ? el.dialogInput.value
                : null;
            closeDialog({confirmed: true, value});
        }
    });
}

async function dialogInfo(message, title = "Уведомление", confirmText = "ОК") {
    await showDialog({title, message, confirmText});
}

async function dialogConfirm(message, title = "Подтверждение", confirmText = "Подтвердить", cancelText = "Отмена") {
    const result = await showDialog({
        title,
        message,
        withCancel: true,
        confirmText,
        cancelText
    });
    return !!result?.confirmed;
}

async function dialogPrompt(message, title = "Введите значение", inputPlaceholder = "", inputValue = "", confirmText = "Готово", cancelText = "Отмена") {
    const result = await showDialog({
        title,
        message,
        withInput: true,
        inputPlaceholder,
        inputValue,
        withCancel: true,
        confirmText,
        cancelText
    });
    if (!result?.confirmed) {
        return null;
    }
    return (result.value || "").trim();
}

function notify(message) {
    void dialogInfo(String(message || ""));
}

function money(value) {
    return `${Number(value).toLocaleString("ru-RU", {minimumFractionDigits: 2, maximumFractionDigits: 2})} ₽`;
}

function formatQuantity(value) {
    const numeric = Number(value);
    if (!Number.isFinite(numeric)) {
        return "0";
    }
    return numeric.toLocaleString("ru-RU", {minimumFractionDigits: 0, maximumFractionDigits: 3});
}

function unitLabel(unit) {
    return unit === "PCS" ? "шт" : "куб.м";
}

function unitModeLabel(unitMode) {
    if (unitMode === "PCS_ONLY") {
        return "только шт";
    }
    if (unitMode === "CUBIC_ONLY") {
        return "только куб.м";
    }
    return "шт + куб.м";
}

function getUnitPriceValue(product, unit) {
    if (!product) {
        return null;
    }
    if (unit === "PCS") {
        return product.pricePcs ?? product.price ?? null;
    }
    if (unit === "CUBIC_METERS") {
        return product.priceCubicMeters ?? product.price ?? null;
    }
    return product.price ?? null;
}

function getDisplayPrice(product) {
    if (!product) {
        return null;
    }

    if (product.unitMode === "PCS_ONLY") {
        return getUnitPriceValue(product, "PCS");
    }

    if (product.unitMode === "CUBIC_ONLY") {
        return getUnitPriceValue(product, "CUBIC_METERS");
    }

    const byPcs = getUnitPriceValue(product, "PCS");
    const byCubic = getUnitPriceValue(product, "CUBIC_METERS");
    if (byPcs != null && byCubic != null) {
        return Math.min(Number(byPcs), Number(byCubic));
    }
    return byPcs ?? byCubic ?? product.price ?? null;
}

function getPriceSummaryText(product) {
    const byPcs = getUnitPriceValue(product, "PCS");
    const byCubic = getUnitPriceValue(product, "CUBIC_METERS");

    if (product.unitMode === "PCS_ONLY") {
        return byPcs != null ? `${money(byPcs)} / шт` : "-";
    }
    if (product.unitMode === "CUBIC_ONLY") {
        return byCubic != null ? `${money(byCubic)} / куб.м` : "-";
    }
    const left = byPcs != null ? `${money(byPcs)} / шт` : "— / шт";
    const right = byCubic != null ? `${money(byCubic)} / куб.м` : "— / куб.м";
    return `${left} · ${right}`;
}

function paymentMethodLabel(method) {
    if (method === "CARD_NOW") {
        return "Сейчас (карта)";
    }
    if (method === "ON_DELIVERY") {
        return "При получении";
    }
    return "-";
}

function deliveryMethodLabel(method) {
    if (method === "CITY_DELIVERY") {
        return "Доставка по городу";
    }
    if (method === "PICKUP") {
        return "Самовывоз";
    }
    if (method === "OTHER") {
        return "Другая доставка";
    }
    return "-";
}

function deliveryFeeByMethod(method) {
    return method === "CITY_DELIVERY" ? Number(state.cityDeliveryFee || 1000) : 0;
}

function readRadioValue(name) {
    const node = document.querySelector(`input[name="${name}"]:checked`);
    return node ? node.value : null;
}

function setRadioValue(name, value) {
    const target = document.querySelector(`input[name="${name}"][value="${value}"]`);
    if (target) {
        target.checked = true;
    }
}

function formatAddressForDelivery(method, rawAddress) {
    const trimmed = (rawAddress || "").trim();
    if (method === "PICKUP") {
        return trimmed || "Самовывоз";
    }
    if (method === "OTHER") {
        return trimmed || "Другая доставка (обсуждается)";
    }
    return trimmed;
}

function orderStatusLabel(status) {
    if (status === "PAID") {
        return "Оплачен";
    }
    if (status === "CANCELED") {
        return "Отменен";
    }
    if (status === "FAILED") {
        return "Ошибка оплаты";
    }
    if (status === "PENDING_PAYMENT") {
        return "Ожидает оплату";
    }
    return "Создан";
}

function orderStatusClass(status) {
    if (status === "PAID") {
        return "paid";
    }
    if (status === "CANCELED" || status === "FAILED") {
        return "cancelled";
    }
    return "";
}

function orderDisplayStatus(order) {
    if (order?.accepted) {
        return "Заказ принят";
    }
    if (order?.paymentMethod === "ON_DELIVERY") {
        return "Оплата при получении";
    }
    if (order?.paymentMethod === "CARD_NOW") {
        return "Оплата сейчас (перевод)";
    }
    return orderStatusLabel(order?.status);
}

function orderDisplayStatusClass(order) {
    if (order?.accepted) {
        return "paid";
    }
    if (order?.paymentMethod === "CARD_NOW") {
        return "paid";
    }
    return orderStatusClass(order?.status);
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }
    return new Date(value).toLocaleString("ru-RU");
}

function formatStockValue(value) {
    const numeric = Number(value);
    if (!Number.isFinite(numeric)) {
        return "0";
    }
    return numeric.toLocaleString("ru-RU", {minimumFractionDigits: 0, maximumFractionDigits: 3});
}

function getStockSummaryText(product) {
    const stockPcs = formatStockValue(product.stockPcs);
    const stockCubic = formatStockValue(product.stockCubicMeters);

    if (product.unitMode === "PCS_ONLY") {
        return `${stockPcs} шт`;
    }
    if (product.unitMode === "CUBIC_ONLY") {
        return `${stockCubic} куб.м`;
    }
    return `${stockPcs} шт · ${stockCubic} куб.м`;
}

function getDiscountPercent(product) {
    const displayPrice = getDisplayPrice(product);
    if (displayPrice == null || !product?.oldPrice || Number(product.oldPrice) <= Number(displayPrice)) {
        return null;
    }
    return Math.round((1 - Number(displayPrice) / Number(product.oldPrice)) * 100);
}

function hasAvailableStock(product) {
    const pcs = Number(product.stockPcs || 0);
    const cubic = Number(product.stockCubicMeters || 0);

    if (product.unitMode === "PCS_ONLY") {
        return pcs > 0;
    }

    if (product.unitMode === "CUBIC_ONLY") {
        return cubic > 0;
    }

    return pcs > 0 || cubic > 0;
}

function applyCatalogFilters(items, query, onlyAvailable) {
    const normalized = (query || "").trim().toLowerCase();
    return items.filter((product) => {
        const text = `${product.name || ""} ${product.description || ""}`.toLowerCase();
        const passQuery = !normalized || text.includes(normalized);
        const passAvailable = !onlyAvailable || hasAvailableStock(product);
        return passQuery && passAvailable;
    });
}

function updateFilterButtonsState() {
    el.catalogFilterBtn.classList.toggle("active", state.catalogOnlyAvailable);
    el.fixFilterBtn.classList.toggle("active", state.fixOnlyAvailable);
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (char) => {
        const entities = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        };
        return entities[char] || char;
    });
}

function api(path, options = {}) {
    const headers = {...(options.headers || {})};
    const hasBody = options.body !== undefined;
    const isFormData = typeof FormData !== "undefined" && options.body instanceof FormData;

    if (!isFormData) {
        headers["Content-Type"] = headers["Content-Type"] || "application/json";
    }

    if (state.userId) {
        headers["X-User-Id"] = String(state.userId);
    }

    return fetch(path, {
        ...options,
        headers
    }).then(async (res) => {
        const contentType = res.headers.get("content-type") || "";
        const data = contentType.includes("application/json") ? await res.json() : await res.text();
        if (!res.ok) {
            let message = data?.message || data || `Ошибка ${res.status}`;
            if (res.status === 413) {
                message = "Файл слишком большой. Уменьшите фото и попробуйте снова.";
            } else if (typeof message === "string" && message.includes("<html")) {
                message = `Ошибка ${res.status}`;
            }
            throw new Error(message);
        }

        if (!hasBody && options.method === "HEAD") {
            return null;
        }

        return data;
    });
}

function parseUserIdFromHashWebAppData() {
    const hash = window.location.hash;
    if (!hash || !hash.includes("WebAppData=")) {
        return null;
    }

    try {
        const hashParams = new URLSearchParams(hash.slice(1));
        const webAppData = hashParams.get("WebAppData");
        if (!webAppData) {
            return null;
        }

        const params = new URLSearchParams(webAppData);
        const rawUser = params.get("user");
        if (!rawUser) {
            return null;
        }

        const user = JSON.parse(rawUser);
        const id = Number(user?.id);
        return Number.isFinite(id) && id > 0 ? id : null;
    } catch {
        return null;
    }
}

function parseUserId() {
    const fromBridge = Number(window.WebApp?.initDataUnsafe?.user?.id);
    if (Number.isFinite(fromBridge) && fromBridge > 0) {
        return fromBridge;
    }

    const fromHash = parseUserIdFromHashWebAppData();
    if (Number.isFinite(fromHash) && fromHash > 0) {
        return fromHash;
    }

    const fromQuery = Number(new URL(window.location.href).searchParams.get("userId"));
    if (Number.isFinite(fromQuery) && fromQuery > 0) {
        return fromQuery;
    }

    return null;
}

function updateIdentityUi() {
    document.body.dataset.authenticated = state.authenticated ? "1" : "0";
    document.body.dataset.admin = state.isAdmin ? "1" : "0";
}

function setAdminVisibility() {
    el.adminTabBtn.classList.toggle("hidden", !state.isAdmin);
    el.adminOrdersTabBtn.classList.toggle("hidden", !state.isAdmin);

    const activeTab = document.querySelector(".tab.active")?.dataset?.tab;
    if (!state.isAdmin && (activeTab === "admin" || activeTab === "admin-orders")) {
        switchTab("catalog");
    }
}

function switchTab(tabId) {
    el.tabs.forEach((btn) => {
        const isActive = btn.dataset.tab === tabId;
        btn.classList.toggle("active", isActive);
    });

    el.tabContents.forEach((section) => {
        section.classList.toggle("active", section.id === tabId);
    });

    if (tabId === "admin" && state.isAdmin) {
        loadAdminData();
    }

    if (tabId === "admin-orders" && state.isAdmin) {
        loadAdminOrders();
    }

    if (tabId === "cart") {
        renderCart();
    }
}

function isTabActive(tabId) {
    return document.getElementById(tabId)?.classList.contains("active");
}

function initTabs() {
    el.tabs.forEach((btn) => {
        btn.addEventListener("click", () => {
            const target = btn.dataset.tab;
            if ((target === "admin" || target === "admin-orders") && !state.isAdmin) {
                notify("Раздел доступен только администраторам.");
                return;
            }
            switchTab(target);
        });
    });
}

function rerenderShopCatalogs() {
    renderProducts(state.products, el.catalogGrid, el.catalogEmpty, "catalog");
    renderProducts(state.fixPriceProducts, el.fixPriceGrid, el.fixPriceEmpty, "fix-price");
}

function initShopControls() {
    const goBack = () => {
        if (window.WebApp?.close) {
            window.WebApp.close();
            return;
        }
        if (window.history.length > 1) {
            window.history.back();
        }
    };

    el.catalogBackBtn.addEventListener("click", goBack);
    el.fixBackBtn.addEventListener("click", goBack);

    el.catalogSearchInput.addEventListener("input", () => {
        state.catalogQuery = el.catalogSearchInput.value || "";
        renderProducts(state.products, el.catalogGrid, el.catalogEmpty, "catalog");
    });

    el.fixSearchInput.addEventListener("input", () => {
        state.fixPriceQuery = el.fixSearchInput.value || "";
        renderProducts(state.fixPriceProducts, el.fixPriceGrid, el.fixPriceEmpty, "fix-price");
    });

    el.catalogSearchClear.addEventListener("click", () => {
        state.catalogQuery = "";
        el.catalogSearchInput.value = "";
        renderProducts(state.products, el.catalogGrid, el.catalogEmpty, "catalog");
    });

    el.fixSearchClear.addEventListener("click", () => {
        state.fixPriceQuery = "";
        el.fixSearchInput.value = "";
        renderProducts(state.fixPriceProducts, el.fixPriceGrid, el.fixPriceEmpty, "fix-price");
    });

    el.catalogFilterBtn.addEventListener("click", () => {
        state.catalogOnlyAvailable = !state.catalogOnlyAvailable;
        updateFilterButtonsState();
        renderProducts(state.products, el.catalogGrid, el.catalogEmpty, "catalog");
    });

    el.fixFilterBtn.addEventListener("click", () => {
        state.fixOnlyAvailable = !state.fixOnlyAvailable;
        updateFilterButtonsState();
        renderProducts(state.fixPriceProducts, el.fixPriceGrid, el.fixPriceEmpty, "fix-price");
    });

    updateFilterButtonsState();
}

function getAllowedUnits(product) {
    if (!product || !product.unitMode) {
        return ["PCS", "CUBIC_METERS"];
    }

    if (product.unitMode === "PCS_ONLY") {
        return ["PCS"];
    }

    if (product.unitMode === "CUBIC_ONLY") {
        return ["CUBIC_METERS"];
    }

    return ["PCS", "CUBIC_METERS"];
}

function renderProducts(items, targetGrid, targetEmpty, mode) {
    targetGrid.innerHTML = "";

    const query = mode === "catalog" ? state.catalogQuery : state.fixPriceQuery;
    const onlyAvailable = mode === "catalog" ? state.catalogOnlyAvailable : state.fixOnlyAvailable;
    const filtered = applyCatalogFilters(items, query, onlyAvailable);

    if (!filtered.length) {
        targetEmpty.textContent = query
            ? "Ничего не найдено по запросу."
            : "Товаров пока нет.";
        targetEmpty.classList.remove("hidden");
        return;
    }

    targetEmpty.classList.add("hidden");

    filtered.forEach((product) => {
        const card = document.createElement("article");
        card.className = "card";
        const displayPrice = getDisplayPrice(product);

        const oldPriceHtml = product.oldPrice
            ? `<div class="price-old">${money(product.oldPrice)}</div>`
            : "";

        const discountPercent = getDiscountPercent(product);
        const discountHtml = discountPercent
            ? `<span class="discount-pill">-${discountPercent}%</span>`
            : "";

        card.innerHTML = `
            <div class="card-media">
                <button class="card-fav" type="button" tabindex="-1" aria-hidden="true">♡</button>
                <img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}">
            </div>
            <div class="card-body">
                <h4 class="card-title">${escapeHtml(product.name)}</h4>
                ${oldPriceHtml}
                <div class="card-price-row">
                    <div class="price">${displayPrice == null ? "-" : money(displayPrice)}</div>
                    ${discountHtml}
                </div>
                <button class="card-cta" type="button">В корзину</button>
            </div>
        `;

        card.addEventListener("click", () => openProduct(product));
        const actionBtn = card.querySelector(".card-cta");
        actionBtn.addEventListener("click", (event) => {
            event.stopPropagation();
            openProduct(product);
        });

        targetGrid.appendChild(card);
    });
}

async function loadCatalog() {
    const items = await api("/api/catalog");
    state.products = items;
    state.catalogLoaded = true;
    syncCartWithCatalogData();
    renderProducts(items, el.catalogGrid, el.catalogEmpty, "catalog");
}

async function loadFixPrice() {
    const items = await api("/api/fix-price");
    state.fixPriceProducts = items;
    state.fixPriceLoaded = true;
    syncCartWithCatalogData();
    renderProducts(items, el.fixPriceGrid, el.fixPriceEmpty, "fix-price");
}

async function loadInfo() {
    const posts = await api("/api/info");
    el.infoPosts.innerHTML = "";

    if (!posts.length) {
        el.infoPosts.innerHTML = '<div class="empty">Постов пока нет.</div>';
        return;
    }

    posts.forEach((post) => {
        const node = document.createElement("article");
        node.className = "post";

        const title = document.createElement("h4");
        title.textContent = post.title;

        const content = document.createElement("p");
        content.textContent = post.content;

        const date = document.createElement("small");
        date.textContent = formatDateTime(post.createdAt);

        node.appendChild(title);
        node.appendChild(content);
        node.appendChild(date);
        el.infoPosts.appendChild(node);
    });
}

async function loadCheckoutConfig() {
    try {
        const config = await api("/api/orders/payment-details");
        const fee = Number(config?.cityDeliveryFee);
        if (Number.isFinite(fee) && fee >= 0) {
            state.cityDeliveryFee = fee;
        }
    } catch {
        state.cityDeliveryFee = 1000;
    }
    updateCartSummary();
}

function closeProduct() {
    el.productModal.classList.add("hidden");
}

function availableStockForUnit(product, unit) {
    if (!product) {
        return 0;
    }
    if (unit === "PCS") {
        return Number(product.stockPcs || 0);
    }
    return Number(product.stockCubicMeters || 0);
}

function defaultQtyForUnit(unit) {
    return unit === "PCS" ? 1 : 0.001;
}

function configureModalUnitSelect(product) {
    const allowed = getAllowedUnits(product);
    el.modalUnit.innerHTML = allowed
        .map((unit) => `<option value="${unit}">${unitLabel(unit)}</option>`)
        .join("");

    el.modalUnit.value = allowed[0];
    el.modalUnit.disabled = allowed.length === 1;

    const defaultQty = defaultQtyForUnit(allowed[0]);
    el.modalQuantity.value = defaultQty;
}

function openProduct(product) {
    state.selectedProduct = product;
    el.modalImage.src = product.imageUrl;
    el.modalTitle.textContent = product.name;
    el.modalDescription.textContent = product.description;
    el.modalPrice.textContent = getPriceSummaryText(product);
    el.modalStock.textContent = getStockSummaryText(product);

    if (product.fixPrice && product.oldPrice) {
        el.modalOldPriceLine.classList.remove("hidden");
        el.modalOldPrice.textContent = money(product.oldPrice);
    } else {
        el.modalOldPriceLine.classList.add("hidden");
        el.modalOldPrice.textContent = "";
    }

    configureModalUnitSelect(product);
    el.productModal.classList.remove("hidden");
}

function getAllProductsMap() {
    const map = new Map();
    [...state.products, ...state.fixPriceProducts].forEach((item) => {
        map.set(item.id, item);
    });
    return map;
}

function cartStorageKey() {
    return `miniapp-max-cart-${state.userId || "guest"}`;
}

function saveCartToStorage() {
    try {
        localStorage.setItem(cartStorageKey(), JSON.stringify(state.cartItems));
    } catch {
    }
}

function loadCartFromStorage() {
    try {
        const raw = localStorage.getItem(cartStorageKey());
        if (!raw) {
            state.cartItems = [];
            return;
        }
        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed)) {
            state.cartItems = [];
            return;
        }
        state.cartItems = parsed.map((item) => ({
            key: String(item.key || `${item.productId}:${item.quantityUnit}`),
            productId: Number(item.productId),
            quantity: Number(item.quantity),
            quantityUnit: item.quantityUnit,
            productName: String(item.productName || "Товар"),
            imageUrl: String(item.imageUrl || ""),
            unitPrice: Number(item.unitPrice)
        })).filter((item) => Number.isFinite(item.productId)
            && Number.isFinite(item.quantity)
            && item.quantity > 0
            && Number.isFinite(item.unitPrice)
            && (item.quantityUnit === "PCS" || item.quantityUnit === "CUBIC_METERS"));
    } catch {
        state.cartItems = [];
    }
}

function bumpCartBadge() {
    el.cartTabBadge.classList.remove("bump");
    void el.cartTabBadge.offsetWidth;
    el.cartTabBadge.classList.add("bump");
}

function updateCartBadge(withBump = false) {
    const count = state.cartItems.length;
    if (count <= 0) {
        el.cartTabBadge.classList.add("hidden");
        el.cartTabBadge.textContent = "0";
        return;
    }
    el.cartTabBadge.classList.remove("hidden");
    el.cartTabBadge.textContent = String(count);
    if (withBump) {
        bumpCartBadge();
    }
}

function getCartReservedQuantity(productId, unit, skipKey) {
    return state.cartItems.reduce((sum, item) => {
        if (item.productId !== productId || item.quantityUnit !== unit) {
            return sum;
        }
        if (skipKey && item.key === skipKey) {
            return sum;
        }
        return sum + Number(item.quantity || 0);
    }, 0);
}

function syncCartWithCatalogData() {
    const productsMap = getAllProductsMap();
    const nextItems = [];

    state.cartItems.forEach((item) => {
        const product = productsMap.get(item.productId);
        if (!product) {
            if (!(state.catalogLoaded && state.fixPriceLoaded)) {
                nextItems.push(item);
            }
            return;
        }
        if (!product.active) {
            return;
        }

        const allowedUnits = getAllowedUnits(product);
        if (!allowedUnits.includes(item.quantityUnit)) {
            return;
        }

        const unitPrice = Number(getUnitPriceValue(product, item.quantityUnit));
        if (!Number.isFinite(unitPrice) || unitPrice <= 0) {
            return;
        }

        const maxStock = availableStockForUnit(product, item.quantityUnit);
        let qty = Number(item.quantity || 0);
        if (!Number.isFinite(qty) || qty <= 0) {
            return;
        }
        if (qty > maxStock) {
            qty = maxStock;
        }
        if (qty <= 0) {
            return;
        }

        nextItems.push({
            key: `${product.id}:${item.quantityUnit}`,
            productId: product.id,
            quantityUnit: item.quantityUnit,
            quantity: Number(qty.toFixed(3)),
            unitPrice: unitPrice,
            productName: product.name,
            imageUrl: product.imageUrl
        });
    });

    state.cartItems = nextItems;
    saveCartToStorage();
    updateCartBadge();
    renderCart();
}

function addSelectedProductToCart() {
    if (!state.authenticated || !state.userId) {
        notify("Откройте mini app через MAX.");
        return;
    }

    const product = state.selectedProduct;
    if (!product) {
        return;
    }

    const quantity = Number(el.modalQuantity.value);
    if (!Number.isFinite(quantity) || quantity <= 0) {
        notify("Введите корректное количество");
        return;
    }

    const quantityUnit = el.modalUnit.value;
    const unitPrice = Number(getUnitPriceValue(product, quantityUnit));
    if (!Number.isFinite(unitPrice) || unitPrice <= 0) {
        notify("Для выбранной единицы цена не настроена");
        return;
    }

    const key = `${product.id}:${quantityUnit}`;
    const existing = state.cartItems.find((item) => item.key === key);
    const alreadyReserved = getCartReservedQuantity(product.id, quantityUnit, key);
    const requestedTotal = alreadyReserved + quantity + Number(existing?.quantity || 0);
    const inStock = availableStockForUnit(product, quantityUnit);
    if (requestedTotal > inStock) {
        notify(`Недостаточно остатка. Доступно: ${formatQuantity(inStock)} ${unitLabel(quantityUnit)}`);
        return;
    }

    if (existing) {
        existing.quantity = Number((existing.quantity + quantity).toFixed(3));
        existing.unitPrice = unitPrice;
        existing.productName = product.name;
        existing.imageUrl = product.imageUrl;
    } else {
        state.cartItems.push({
            key,
            productId: product.id,
            quantity: Number(quantity.toFixed(3)),
            quantityUnit,
            unitPrice,
            productName: product.name,
            imageUrl: product.imageUrl
        });
    }

    saveCartToStorage();
    updateCartBadge(true);
    renderCart();
    closeProduct();
    notify("Товар добавлен в корзину");
}

function calculateCartItemsTotal() {
    return state.cartItems.reduce((sum, item) => {
        const line = Number(item.quantity || 0) * Number(item.unitPrice || 0);
        return sum + Number(line.toFixed(2));
    }, 0);
}

function syncCartAddressByDeliveryMethod() {
    const deliveryMethod = readRadioValue("cartDeliveryMethod") || "CITY_DELIVERY";
    if (deliveryMethod === "CITY_DELIVERY") {
        el.cartAddress.required = true;
        el.cartAddress.placeholder = "Укажите адрес доставки";
        return;
    }
    if (deliveryMethod === "PICKUP") {
        el.cartAddress.required = false;
        el.cartAddress.placeholder = "Можно оставить пустым";
        return;
    }
    el.cartAddress.required = false;
    el.cartAddress.placeholder = "Комментарий по доставке (опционально)";
}

function updateCartSummary() {
    const itemsTotal = calculateCartItemsTotal();
    const deliveryMethod = readRadioValue("cartDeliveryMethod") || state.cartDeliveryMethod || "CITY_DELIVERY";
    const paymentMethod = readRadioValue("cartPaymentMethod") || state.cartPaymentMethod || "CARD_NOW";
    const deliveryFee = deliveryFeeByMethod(deliveryMethod);
    const total = Number((itemsTotal + deliveryFee).toFixed(2));

    state.cartDeliveryMethod = deliveryMethod;
    state.cartPaymentMethod = paymentMethod;
    state.cartItemsTotal = Number(itemsTotal.toFixed(2));
    state.cartDeliveryFee = Number(deliveryFee.toFixed(2));
    state.cartTotal = total;

    el.cartItemsTotal.textContent = money(state.cartItemsTotal);
    el.cartDeliveryFee.textContent = deliveryFee > 0 ? money(deliveryFee) : "0 ₽";
    el.cartTotal.textContent = money(state.cartTotal);
    el.cartPayTotal.textContent = money(state.cartTotal);
}

function renderCart() {
    el.cartMessage.textContent = "";
    if (el.cartPaymentStep) {
        el.cartPaymentStep.classList.add("hidden");
    }
    el.cartItems.innerHTML = "";

    if (!state.cartItems.length) {
        el.cartEmpty.classList.remove("hidden");
        el.cartCheckoutPanel.classList.add("hidden");
        updateCartBadge();
        updateCartSummary();
        return;
    }

    el.cartEmpty.classList.add("hidden");
    el.cartCheckoutPanel.classList.remove("hidden");

    const markup = state.cartItems.map((item) => {
        const lineTotal = Number((Number(item.quantity) * Number(item.unitPrice)).toFixed(2));
        return `
            <article class="cart-item">
                <div class="cart-item-media">
                    <img src="${escapeHtml(item.imageUrl)}" alt="${escapeHtml(item.productName)}"/>
                </div>
                <div>
                    <p class="cart-item-title">${escapeHtml(item.productName)}</p>
                    <p class="cart-item-sub">${escapeHtml(formatQuantity(item.quantity))} ${escapeHtml(unitLabel(item.quantityUnit))} × ${escapeHtml(money(item.unitPrice))}</p>
                    <p class="cart-item-total">${escapeHtml(money(lineTotal))}</p>
                </div>
                <div class="cart-item-actions">
                    <button class="btn-inline js-cart-change" data-key="${escapeHtml(item.key)}" type="button">Изменить количество</button>
                    <button class="btn-inline cart-item-remove js-cart-remove" data-key="${escapeHtml(item.key)}" type="button">Удалить</button>
                </div>
            </article>
        `;
    }).join("");

    el.cartItems.innerHTML = markup;

    el.cartItems.querySelectorAll(".js-cart-remove").forEach((button) => {
        button.addEventListener("click", () => {
            const key = button.dataset.key;
            state.cartItems = state.cartItems.filter((item) => item.key !== key);
            saveCartToStorage();
            updateCartBadge();
            renderCart();
        });
    });

    el.cartItems.querySelectorAll(".js-cart-change").forEach((button) => {
        button.addEventListener("click", async () => {
            const key = button.dataset.key;
            const cartItem = state.cartItems.find((item) => item.key === key);
            if (!cartItem) {
                return;
            }

            const raw = await dialogPrompt(
                `Введите новое количество для «${cartItem.productName}» (${unitLabel(cartItem.quantityUnit)}).`,
                "Изменить количество",
                `Например: ${cartItem.quantity}`,
                String(cartItem.quantity),
                "Сохранить",
                "Отмена"
            );
            if (raw === null) {
                return;
            }

            const qty = Number(raw.replace(",", "."));
            if (!Number.isFinite(qty) || qty <= 0) {
                notify("Введите корректное количество");
                return;
            }

            const productsMap = getAllProductsMap();
            const product = productsMap.get(cartItem.productId);
            if (!product) {
                notify("Товар больше недоступен");
                state.cartItems = state.cartItems.filter((item) => item.key !== key);
                saveCartToStorage();
                renderCart();
                return;
            }

            const available = availableStockForUnit(product, cartItem.quantityUnit);
            const otherReserved = getCartReservedQuantity(cartItem.productId, cartItem.quantityUnit, key);
            if (qty + otherReserved > available) {
                notify(`Недостаточно остатка. Доступно: ${formatQuantity(Math.max(0, available - otherReserved))} ${unitLabel(cartItem.quantityUnit)}`);
                return;
            }

            cartItem.quantity = Number(qty.toFixed(3));
            cartItem.unitPrice = Number(getUnitPriceValue(product, cartItem.quantityUnit));
            cartItem.productName = product.name;
            cartItem.imageUrl = product.imageUrl;
            saveCartToStorage();
            renderCart();
        });
    });

    updateCartBadge();
    updateCartSummary();
}

function collectCartCustomerData() {
    const fullName = el.cartFullName.value.trim();
    const phone = el.cartPhone.value.trim();
    const deliveryMethod = state.cartDeliveryMethod || readRadioValue("cartDeliveryMethod") || "CITY_DELIVERY";
    const address = formatAddressForDelivery(deliveryMethod, el.cartAddress.value);

    if (!fullName || !phone) {
        throw new Error("Заполните ФИО и телефон");
    }

    if (deliveryMethod === "CITY_DELIVERY" && !address) {
        throw new Error("Укажите адрес доставки");
    }

    return {fullName, phone, deliveryMethod, address};
}

async function submitCartOrder(paymentMethodOverride) {
    if (!state.authenticated || !state.userId) {
        notify("Откройте mini app через MAX.");
        return;
    }

    if (!state.cartItems.length) {
        el.cartMessage.textContent = "Корзина пуста";
        return;
    }

    try {
        const customerData = collectCartCustomerData();
        const paymentMethod = paymentMethodOverride || state.cartPaymentMethod || readRadioValue("cartPaymentMethod") || "CARD_NOW";

        const response = await api("/api/orders", {
            method: "POST",
            body: JSON.stringify({
                items: state.cartItems.map((item) => ({
                    productId: item.productId,
                    quantity: item.quantity,
                    quantityUnit: item.quantityUnit
                })),
                fullName: customerData.fullName,
                phone: customerData.phone,
                address: customerData.address,
                deliveryMethod: customerData.deliveryMethod,
                paymentMethod
            })
        });

        state.cartItems = [];
        saveCartToStorage();
        updateCartBadge();
        if (el.cartPaymentStep) {
            el.cartPaymentStep.classList.add("hidden");
        }
        renderCart();
        const successMessage = response.message || "Заказ создан. С вами свяжется менеджер.";
        el.cartMessage.textContent = successMessage;
        await dialogInfo(successMessage, "Заказ оформлен");

        await Promise.all([loadCatalog(), loadFixPrice()]);
        if (state.isAdmin) {
            await loadAdminOrders();
        }
    } catch (error) {
        el.cartMessage.textContent = error.message;
    }
}

async function showCardPaymentAlertAndSubmit() {
    try {
        const paymentDetailsResponse = await api("/api/orders/payment-details");
        const fee = Number(paymentDetailsResponse.cityDeliveryFee);
        if (Number.isFinite(fee) && fee >= 0) {
            state.cityDeliveryFee = fee;
            updateCartSummary();
        }

        const details = (paymentDetailsResponse.paymentDetails || "").trim();
        if (!details) {
            throw new Error("Админ еще не заполнил данные для оплаты");
        }

        const confirmed = await dialogConfirm(
            `Оплата сейчас\n\n`
            + `Сумма к переводу: ${money(state.cartTotal)}\n\n`
            + `Реквизиты для оплаты:\n${details}\n\n`
            + `Нажмите «Оплатил», когда перевод выполнен.`,
            "Подтверждение оплаты",
            "Оплатил",
            "Отмена"
        );
        if (!confirmed) {
            el.cartMessage.textContent = "Заказ не отправлен. Можно завершить оплату и нажать «Оформить заказ» снова.";
            return;
        }

        await submitCartOrder("CARD_NOW");
        el.cartMessage.textContent = "";
    } catch (error) {
        el.cartMessage.textContent = error.message;
    }
}

function initCartFlow() {
    document.querySelectorAll('input[name="cartDeliveryMethod"]').forEach((input) => {
        input.addEventListener("change", () => {
            syncCartAddressByDeliveryMethod();
            updateCartSummary();
        });
    });

    document.querySelectorAll('input[name="cartPaymentMethod"]').forEach((input) => {
        input.addEventListener("change", updateCartSummary);
    });

    el.cartCheckoutBtn.addEventListener("click", async () => {
        updateCartSummary();
        try {
            collectCartCustomerData();
        } catch (error) {
            el.cartMessage.textContent = error.message;
            return;
        }

        if (!state.cartItems.length) {
            el.cartMessage.textContent = "Корзина пуста";
            return;
        }

        if ((state.cartPaymentMethod || "CARD_NOW") === "ON_DELIVERY") {
            await submitCartOrder("ON_DELIVERY");
            return;
        }

        await showCardPaymentAlertAndSubmit();
    });

    syncCartAddressByDeliveryMethod();
    updateCartSummary();
}

function initProductModalFlow() {
    el.orderBtn.addEventListener("click", addSelectedProductToCart);

    el.closeProductModal.addEventListener("click", closeProduct);

    el.modalUnit.addEventListener("change", () => {
        const unit = el.modalUnit.value;
        if (!el.modalQuantity.value || Number(el.modalQuantity.value) <= 0) {
            el.modalQuantity.value = defaultQtyForUnit(unit);
        }
    });

    window.addEventListener("click", (event) => {
        if (event.target === el.productModal) {
            closeProduct();
        }
    });
}

function renderSimpleList(node, rows) {
    node.innerHTML = rows.length
        ? rows.map((row) => `<div class="list-row">${escapeHtml(row)}</div>`).join("")
        : '<div class="list-row">Нет данных</div>';
}

function renderAdminProducts(products) {
    const catalogProducts = products.filter((item) => !item.fixPrice && item.active);
    const fixPriceProducts = products.filter((item) => item.fixPrice && item.active);

    const renderGroup = (items, targetNode) => {
        if (!items.length) {
            targetNode.innerHTML = '<div class="empty">Товаров нет.</div>';
            return;
        }

        targetNode.innerHTML = items.map((item) => {
            const oldPriceText = item.fixPrice && item.oldPrice
                ? ` · было ${money(item.oldPrice)}`
                : "";
            const typeTag = item.fixPrice ? '<span class="tag fix">Fix Price</span>' : '<span class="tag catalog">Каталог</span>';
            const priceSummary = getPriceSummaryText(item);
            return `
                <article class="manage-item">
                    <div class="manage-meta">
                        <p class="manage-title">${escapeHtml(item.name)}</p>
                        <p class="manage-sub">${escapeHtml(priceSummary)}${escapeHtml(oldPriceText)}</p>
                        <div class="manage-badges">
                            ${typeTag}
                            <span class="tag catalog">${escapeHtml(unitModeLabel(item.unitMode))}</span>
                        </div>
                    </div>
                    <div class="manage-actions">
                        <button class="btn-inline btn-edit js-edit-product" data-id="${item.id}">
                            Редактировать
                        </button>
                        <button class="btn-inline btn-danger js-delete-product" data-id="${item.id}" data-name="${escapeHtml(item.name)}">
                            Удалить
                        </button>
                    </div>
                </article>
            `;
        }).join("");
    };

    renderGroup(catalogProducts, el.adminCatalogProducts);
    renderGroup(fixPriceProducts, el.adminFixPriceProducts);

    document.querySelectorAll(".js-edit-product").forEach((button) => {
        button.addEventListener("click", () => {
            const productId = Number(button.dataset.id);
            openEditProductModal(productId);
        });
    });

    document.querySelectorAll(".js-delete-product").forEach((button) => {
        button.addEventListener("click", async () => {
            const productId = Number(button.dataset.id);
            const productName = button.dataset.name || "товар";
            const confirmed = await dialogConfirm(
                `Удалить «${productName}»?`,
                "Подтверждение удаления",
                "Удалить",
                "Отмена"
            );
            if (!confirmed) {
                return;
            }

            try {
                const result = await api(`/api/admin/products/${productId}`, {method: "DELETE"});
                notify(result.message || "Товар удален");
                await Promise.all([loadCatalog(), loadFixPrice(), loadAdminData(), loadAdminOrders()]);
            } catch (error) {
                notify(error.message);
            }
        });
    });
}

function renderAdminPosts(posts) {
    if (!posts.length) {
        el.adminPostsList.innerHTML = '<div class="empty">Постов пока нет.</div>';
        return;
    }

    el.adminPostsList.innerHTML = posts.map((post) => `
        <article class="manage-item">
            <div class="manage-meta">
                <p class="manage-title">${escapeHtml(post.title)}</p>
                <p class="manage-sub">${escapeHtml(post.content)}</p>
                <div class="manage-badges">
                    <span class="tag catalog">${escapeHtml(formatDateTime(post.createdAt))}</span>
                </div>
            </div>
            <button class="btn-inline btn-danger js-delete-post" data-id="${post.id}" data-title="${escapeHtml(post.title)}">
                Удалить
            </button>
        </article>
    `).join("");

    document.querySelectorAll(".js-delete-post").forEach((button) => {
        button.addEventListener("click", async () => {
            const postId = Number(button.dataset.id);
            const postTitle = button.dataset.title || "пост";
            const confirmed = await dialogConfirm(
                `Удалить пост «${postTitle}»?`,
                "Подтверждение удаления",
                "Удалить",
                "Отмена"
            );
            if (!confirmed) {
                return;
            }

            try {
                const result = await api(`/api/admin/info-posts/${postId}`, {method: "DELETE"});
                notify(result.message || "Пост удален");
                await Promise.all([loadInfo(), loadAdminData()]);
            } catch (error) {
                notify(error.message);
            }
        });
    });
}

async function loadAdminData() {
    if (!state.isAdmin) {
        return;
    }

    try {
        const [admins, users, products, posts, paymentSettings] = await Promise.all([
            api("/api/admin/admins"),
            api("/api/admin/users"),
            api("/api/admin/products"),
            api("/api/admin/info-posts"),
            api("/api/admin/settings/payment-details")
        ]);

        renderSimpleList(
            el.adminsList,
            admins.map((item) => `ID ${item.maxUserId} | ${item.fullName || "без имени"}`)
        );

        renderSimpleList(
            el.usersList,
            users.map((item) => `ID ${item.maxUserId} | ${item.phone || "телефон не указан"} | ${item.admin ? "admin" : "user"}`)
        );

        state.adminProducts = Array.isArray(products) ? products : [];
        renderAdminProducts(products);
        renderAdminPosts(posts);
        if (el.paymentDetailsInput) {
            el.paymentDetailsInput.value = paymentSettings.paymentDetails || "";
        }
        const fee = Number(paymentSettings.cityDeliveryFee);
        if (Number.isFinite(fee) && fee >= 0) {
            state.cityDeliveryFee = fee;
        }
        updateCartSummary();
    } catch (error) {
        state.adminProducts = [];
        renderSimpleList(el.adminsList, [`Ошибка: ${error.message}`]);
        renderSimpleList(el.usersList, []);
        el.adminCatalogProducts.innerHTML = '<div class="empty">Ошибка загрузки товаров</div>';
        el.adminFixPriceProducts.innerHTML = '<div class="empty">Ошибка загрузки товаров</div>';
        el.adminPostsList.innerHTML = '<div class="empty">Ошибка загрузки постов</div>';
        if (el.paymentDetailsInput) {
            el.paymentDetailsInput.value = "";
        }
    }
}

function orderItemsMarkup(order) {
    const items = Array.isArray(order.items) ? order.items : [];
    if (!items.length) {
        return '<div class="order-item-line">Нет позиций</div>';
    }
    return items.map((item) => {
        const quantity = formatQuantity(item.quantity);
        const lineTotal = Number(item.lineTotal || (Number(item.quantity || 0) * Number(item.unitPrice || 0)));
        return `<div class="order-item-line">• ${escapeHtml(item.productName || "Товар")} — ${escapeHtml(quantity)} ${escapeHtml(unitLabel(item.quantityUnit))} × ${escapeHtml(money(item.unitPrice))} = ${escapeHtml(money(lineTotal))}</div>`;
    }).join("");
}

function renderAdminOrders(orders) {
    el.adminOrdersEmpty.textContent = "Заказов пока нет.";
    el.adminOrdersGrid.innerHTML = "";

    if (!orders.length) {
        el.adminOrdersEmpty.classList.remove("hidden");
        return;
    }

    el.adminOrdersEmpty.classList.add("hidden");

    const markup = orders.map((order) => {
        const statusClass = orderDisplayStatusClass(order);
        return `
            <article class="order-card">
                <div class="order-head">
                    <h3 class="order-title">#${order.id} · ${escapeHtml(order.productName || "Сборный заказ")}</h3>
                    <span class="order-status ${statusClass}">${escapeHtml(orderDisplayStatus(order))}</span>
                </div>
                <div class="order-head">
                    <span class="order-time">Создан: ${escapeHtml(formatDateTime(order.createdAt))}</span>
                    <span class="order-time">MAX ID: ${escapeHtml(order.maxUserId)}</span>
                </div>
                <div class="order-grid">
                    <div class="order-cell">
                        <p class="order-label">Позиции заказа</p>
                        <div class="order-items-lines">${orderItemsMarkup(order)}</div>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">Товары</p>
                        <p class="order-value">${escapeHtml(money(order.itemsTotal ?? order.totalPrice))}</p>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">Доставка</p>
                        <p class="order-value">${escapeHtml(deliveryMethodLabel(order.deliveryMethod))} · ${escapeHtml(money(order.deliveryFee || 0))}</p>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">Итого</p>
                        <p class="order-value">${escapeHtml(money(order.totalPrice))}</p>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">ФИО</p>
                        <p class="order-value">${escapeHtml(order.fullName)}</p>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">Телефон</p>
                        <p class="order-value">${escapeHtml(order.phone)}</p>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">Адрес доставки</p>
                        <p class="order-value">${escapeHtml(order.address)}</p>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">Оплата</p>
                        <p class="order-value">${escapeHtml(paymentMethodLabel(order.paymentMethod))}</p>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">Реквизиты (снимок)</p>
                        <p class="order-value">${escapeHtml((order.paymentDetailsSnapshot || "-").replace(/\n/g, " | "))}</p>
                    </div>
                </div>
                <div class="order-actions">
                    ${order.accepted
            ? `<div class="accept-note">Заказ принят. ETA: ${escapeHtml(order.deliveryEta || "-")}</div>`
            : `
                        <button class="btn-inline js-accept-order" data-order-id="${order.id}">Заказ принят</button>
                    `}
                </div>
            </article>
        `;
    }).join("");

    el.adminOrdersGrid.innerHTML = markup;

    document.querySelectorAll(".js-accept-order").forEach((button) => {
        button.addEventListener("click", async () => {
            const id = Number(button.dataset.orderId);
            const eta = await dialogPrompt(
                "Введите ориентировочную дату и время доставки для клиента.",
                "Заказ принят",
                "Например: 12 мая, 18:00",
                "",
                "Готово",
                "Отмена"
            );
            if (!eta) {
                return;
            }

            try {
                const result = await api(`/api/admin/orders/${id}/accept`, {
                    method: "POST",
                    body: JSON.stringify({eta})
                });
                notify(result.message || "Заказ принят");
                await loadAdminOrders();
            } catch (error) {
                notify(error.message);
            }
        });
    });
}

async function loadAdminOrders() {
    if (!state.isAdmin) {
        return;
    }

    try {
        const orders = await api("/api/admin/orders");
        renderAdminOrders(orders);
    } catch (error) {
        el.adminOrdersEmpty.classList.remove("hidden");
        el.adminOrdersEmpty.textContent = `Ошибка: ${error.message}`;
        el.adminOrdersGrid.innerHTML = "";
    }
}

async function uploadImage(file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await api("/api/admin/uploads", {
        method: "POST",
        body: formData,
        headers: {}
    });

    return response.imageUrl;
}

function parseOptionalNumber(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }

    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
}

function getCreateProductControls() {
    return {
        sectionType: el.sectionType,
        oldPriceWrapper: el.oldPriceWrapper,
        oldPriceInput: el.oldPriceInput,
        unitModeSelect: el.unitModeSelect,
        pricePcsWrapper: el.pricePcsWrapper,
        pricePcsInput: el.pricePcsInput,
        priceCubicWrapper: el.priceCubicWrapper,
        priceCubicInput: el.priceCubicInput,
        stockPcsWrapper: el.stockPcsWrapper,
        stockPcsInput: el.stockPcsInput,
        stockCubicWrapper: el.stockCubicWrapper,
        stockCubicInput: el.stockCubicInput
    };
}

function getEditProductControls() {
    return {
        sectionType: el.editSectionType,
        oldPriceWrapper: el.editOldPriceWrapper,
        oldPriceInput: el.editOldPriceInput,
        unitModeSelect: el.editUnitModeSelect,
        pricePcsWrapper: el.editPricePcsWrapper,
        pricePcsInput: el.editPricePcsInput,
        priceCubicWrapper: el.editPriceCubicWrapper,
        priceCubicInput: el.editPriceCubicInput,
        stockPcsWrapper: el.editStockPcsWrapper,
        stockPcsInput: el.editStockPcsInput,
        stockCubicWrapper: el.editStockCubicWrapper,
        stockCubicInput: el.editStockCubicInput
    };
}

function syncStockInputsByUnitMode(controls, clearHiddenValues = true) {
    const mode = controls.unitModeSelect.value;

    const pcsVisible = mode === "PCS_ONLY" || mode === "BOTH";
    const cubicVisible = mode === "CUBIC_ONLY" || mode === "BOTH";

    controls.pricePcsWrapper.classList.toggle("hidden", !pcsVisible);
    controls.priceCubicWrapper.classList.toggle("hidden", !cubicVisible);
    controls.stockPcsWrapper.classList.toggle("hidden", !pcsVisible);
    controls.stockCubicWrapper.classList.toggle("hidden", !cubicVisible);

    controls.pricePcsInput.required = pcsVisible;
    controls.priceCubicInput.required = cubicVisible;
    controls.stockPcsInput.required = pcsVisible;
    controls.stockCubicInput.required = cubicVisible;

    if (clearHiddenValues && !pcsVisible) {
        controls.pricePcsInput.value = "";
        controls.stockPcsInput.value = "";
    }
    if (clearHiddenValues && !cubicVisible) {
        controls.priceCubicInput.value = "";
        controls.stockCubicInput.value = "";
    }
}

function syncFixPriceFields(controls, clearHiddenValues = true) {
    const isFixPrice = controls.sectionType.value === "FIX_PRICE";
    controls.oldPriceWrapper.classList.toggle("hidden", !isFixPrice);
    controls.oldPriceInput.required = isFixPrice;

    if (clearHiddenValues && !isFixPrice) {
        controls.oldPriceInput.value = "";
    }
}

function initProductFormControls() {
    const createControls = getCreateProductControls();
    el.unitModeSelect.addEventListener("change", () => {
        syncStockInputsByUnitMode(createControls, true);
    });
    el.sectionType.addEventListener("change", () => {
        syncFixPriceFields(createControls, true);
    });
    syncStockInputsByUnitMode(createControls, true);
    syncFixPriceFields(createControls, true);
}

function buildProductPayload(formData, imageUrl, active) {
    const unitMode = formData.get("unitMode");
    const sectionType = formData.get("sectionType");
    const isFixPrice = sectionType === "FIX_PRICE";

    const stockPcs = (unitMode === "PCS_ONLY" || unitMode === "BOTH")
        ? parseOptionalNumber(formData.get("stockPcs"))
        : null;

    const stockCubicMeters = (unitMode === "CUBIC_ONLY" || unitMode === "BOTH")
        ? parseOptionalNumber(formData.get("stockCubicMeters"))
        : null;

    const pricePcs = (unitMode === "PCS_ONLY" || unitMode === "BOTH")
        ? parseOptionalNumber(formData.get("pricePcs"))
        : null;

    const priceCubicMeters = (unitMode === "CUBIC_ONLY" || unitMode === "BOTH")
        ? parseOptionalNumber(formData.get("priceCubicMeters"))
        : null;

    if ((unitMode === "PCS_ONLY" || unitMode === "BOTH") && stockPcs === null) {
        throw new Error("Укажите остаток в штуках");
    }

    if ((unitMode === "CUBIC_ONLY" || unitMode === "BOTH") && stockCubicMeters === null) {
        throw new Error("Укажите остаток в кубометрах");
    }

    if ((unitMode === "PCS_ONLY" || unitMode === "BOTH") && pricePcs === null) {
        throw new Error("Укажите цену за штуку");
    }

    if ((unitMode === "CUBIC_ONLY" || unitMode === "BOTH") && priceCubicMeters === null) {
        throw new Error("Укажите цену за кубометр");
    }

    const oldPrice = parseOptionalNumber(formData.get("oldPrice"));
    const minPriceForValidation = unitMode === "BOTH"
        ? Math.min(Number(pricePcs), Number(priceCubicMeters))
        : Number(pricePcs ?? priceCubicMeters);

    if (isFixPrice && (oldPrice === null || oldPrice <= minPriceForValidation)) {
        throw new Error("Для Fix Price старая цена должна быть выше текущей");
    }

    const name = String(formData.get("name") || "").trim();
    const description = String(formData.get("description") || "").trim();
    if (!name) {
        throw new Error("Укажите название товара");
    }
    if (!description) {
        throw new Error("Укажите описание товара");
    }

    return {
        name,
        description,
        imageUrl,
        pricePcs,
        priceCubicMeters,
        oldPrice: isFixPrice ? oldPrice : null,
        unitMode,
        stockPcs,
        stockCubicMeters,
        fixPrice: isFixPrice,
        active
    };
}

function findAdminProductById(productId) {
    const id = Number(productId);
    return state.adminProducts.find((item) => Number(item.id) === id) || null;
}

function closeEditProductModal() {
    state.editingProductId = null;
    el.editProductModal.classList.add("hidden");
}

function openEditProductModal(productId) {
    const product = findAdminProductById(productId);
    if (!product) {
        notify("Товар не найден");
        return;
    }

    const editControls = getEditProductControls();
    state.editingProductId = Number(product.id);

    el.editProductName.value = product.name || "";
    el.editProductDescription.value = product.description || "";
    el.editSectionType.value = product.fixPrice ? "FIX_PRICE" : "CATALOG";
    el.editOldPriceInput.value = product.oldPrice ?? "";
    el.editUnitModeSelect.value = product.unitMode || "BOTH";
    el.editPricePcsInput.value = product.pricePcs ?? "";
    el.editPriceCubicInput.value = product.priceCubicMeters ?? "";
    el.editStockPcsInput.value = product.stockPcs ?? "";
    el.editStockCubicInput.value = product.stockCubicMeters ?? "";
    el.editProductActiveSelect.value = product.active ? "true" : "false";

    syncFixPriceFields(editControls, true);
    syncStockInputsByUnitMode(editControls, true);
    el.editProductModal.classList.remove("hidden");
}

function initEditProductModalControls() {
    const editControls = getEditProductControls();

    el.editUnitModeSelect.addEventListener("change", () => {
        syncStockInputsByUnitMode(editControls, true);
    });

    el.editSectionType.addEventListener("change", () => {
        syncFixPriceFields(editControls, true);
    });

    el.cancelEditProductBtn.addEventListener("click", closeEditProductModal);
    el.editProductModal.addEventListener("click", (event) => {
        if (event.target === el.editProductModal) {
            closeEditProductModal();
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape" || el.editProductModal.classList.contains("hidden")) {
            return;
        }
        event.preventDefault();
        closeEditProductModal();
    });
}

function initAdminForms() {
    el.productForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!state.isAdmin) {
            notify("Недостаточно прав для добавления товара");
            return;
        }

        try {
            const formData = new FormData(el.productForm);
            const file = document.getElementById("productImage").files[0];
            if (!file) {
                throw new Error("Загрузите фото товара");
            }

            const imageUrl = await uploadImage(file);
            const payload = buildProductPayload(formData, imageUrl, true);

            await api("/api/admin/products", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            notify("Товар добавлен");
            el.productForm.reset();
            const createControls = getCreateProductControls();
            syncStockInputsByUnitMode(createControls, true);
            syncFixPriceFields(createControls, true);
            await Promise.all([loadCatalog(), loadFixPrice(), loadAdminData()]);
        } catch (error) {
            notify(error.message);
        }
    });

    el.editProductForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!state.isAdmin) {
            notify("Недостаточно прав для редактирования товара");
            return;
        }

        const productId = Number(state.editingProductId);
        if (!Number.isFinite(productId) || productId <= 0) {
            notify("Не удалось определить товар для редактирования");
            return;
        }

        const currentProduct = findAdminProductById(productId);
        if (!currentProduct) {
            notify("Товар для редактирования не найден");
            return;
        }

        try {
            const formData = new FormData(el.editProductForm);
            const active = formData.get("active") !== "false";
            const payload = buildProductPayload(formData, currentProduct.imageUrl, active);

            await api(`/api/admin/products/${productId}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            notify("Товар обновлен");
            closeEditProductModal();
            await Promise.all([loadCatalog(), loadFixPrice(), loadAdminData()]);
        } catch (error) {
            notify(error.message);
        }
    });

    el.paymentDetailsForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!state.isAdmin) {
            notify("Недостаточно прав для изменения реквизитов оплаты");
            return;
        }

        const paymentDetails = (el.paymentDetailsInput.value || "").trim();
        if (!paymentDetails) {
            notify("Введите данные для оплаты");
            return;
        }

        try {
            const result = await api("/api/admin/settings/payment-details", {
                method: "PUT",
                body: JSON.stringify({paymentDetails})
            });
            notify(result.message || "Данные для оплаты обновлены");
        } catch (error) {
            notify(error.message);
        }
    });

    el.postForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!state.isAdmin) {
            notify("Недостаточно прав для публикации постов");
            return;
        }

        const formData = new FormData(el.postForm);
        try {
            await api("/api/admin/info-posts", {
                method: "POST",
                body: JSON.stringify({
                    title: formData.get("title"),
                    content: formData.get("content")
                })
            });

            notify("Пост опубликован");
            el.postForm.reset();
            await Promise.all([loadInfo(), loadAdminData()]);
        } catch (error) {
            notify(error.message);
        }
    });

    el.addAdminForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!state.isAdmin) {
            notify("Недостаточно прав для управления админами");
            return;
        }

        const formData = new FormData(el.addAdminForm);
        try {
            await api("/api/admin/admins", {
                method: "POST",
                body: JSON.stringify({maxUserId: Number(formData.get("maxUserId"))})
            });

            notify("Админ добавлен");
            el.addAdminForm.reset();
            await loadAdminData();
        } catch (error) {
            notify(error.message);
        }
    });
}

async function bootstrapUser() {
    state.userId = parseUserId();

    if (!state.userId) {
        state.authenticated = false;
        state.isAdmin = false;
        state.userFullName = null;
        updateIdentityUi();
        setAdminVisibility();
        return;
    }

    try {
        const currentUser = await api("/api/users/me");
        state.authenticated = !!currentUser.authenticated;
        state.isAdmin = !!currentUser.admin;
        state.userFullName = (currentUser.fullName || "").trim() || null;
        state.userId = currentUser.maxUserId || state.userId;
        updateIdentityUi();
        setAdminVisibility();

        if (state.userFullName && !el.cartFullName.value) {
            el.cartFullName.value = state.userFullName;
        }
    } catch (error) {
        console.error(error);
        state.authenticated = false;
        state.isAdmin = false;
        state.userFullName = null;
        updateIdentityUi();
        setAdminVisibility();
    }
}

async function init() {
    if (window.WebApp?.ready) {
        window.WebApp.ready();
    }

    initDialog();
    initTabs();
    initShopControls();
    initCartFlow();
    initProductModalFlow();
    initProductFormControls();
    initEditProductModalControls();
    initAdminForms();

    await bootstrapUser();
    loadCartFromStorage();
    updateCartBadge();

    await Promise.all([loadCatalog(), loadFixPrice(), loadInfo(), loadCheckoutConfig()]);

    renderCart();

    setInterval(() => {
        if (!state.isAdmin || !isTabActive("admin-orders")) {
            return;
        }
        loadAdminOrders().catch(() => {
        });
    }, 15000);
}

init().catch((error) => {
    console.error(error);
    notify("Ошибка загрузки mini app");
});
