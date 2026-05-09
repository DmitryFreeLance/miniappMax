const state = {
    userId: null,
    authenticated: false,
    isAdmin: false,
    userFullName: null,
    products: [],
    fixPriceProducts: [],
    catalogQuery: "",
    fixPriceQuery: "",
    catalogOnlyAvailable: false,
    fixOnlyAvailable: false,
    selectedProduct: null,
    orderQuantity: null,
    orderUnit: "PCS"
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

    orderBtn: document.getElementById("orderBtn"),
    orderModal: document.getElementById("orderModal"),
    closeOrderModal: document.getElementById("closeOrderModal"),
    orderStep1: document.getElementById("orderStep1"),
    orderStep2: document.getElementById("orderStep2"),
    orderQuantity: document.getElementById("orderQuantity"),
    orderUnit: document.getElementById("orderUnit"),
    goStep2: document.getElementById("goStep2"),
    orderFullName: document.getElementById("orderFullName"),
    orderPhone: document.getElementById("orderPhone"),
    orderAddress: document.getElementById("orderAddress"),
    submitOrder: document.getElementById("submitOrder"),
    orderMessage: document.getElementById("orderMessage"),

    productForm: document.getElementById("productForm"),
    sectionType: document.getElementById("sectionType"),
    oldPriceWrapper: document.getElementById("oldPriceWrapper"),
    oldPriceInput: document.getElementById("oldPriceInput"),
    unitModeSelect: document.getElementById("unitModeSelect"),
    stockPcsWrapper: document.getElementById("stockPcsWrapper"),
    stockPcsInput: document.getElementById("stockPcsInput"),
    stockCubicWrapper: document.getElementById("stockCubicWrapper"),
    stockCubicInput: document.getElementById("stockCubicInput"),

    postForm: document.getElementById("postForm"),
    addAdminForm: document.getElementById("addAdminForm"),
    adminsList: document.getElementById("adminsList"),
    usersList: document.getElementById("usersList")
};

function notify(message) {
    alert(message);
}

function money(value) {
    return `${Number(value).toLocaleString("ru-RU", {minimumFractionDigits: 2, maximumFractionDigits: 2})} ₽`;
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

function unitPriceSuffixByMode(unitMode) {
    if (unitMode === "PCS_ONLY") {
        return " / шт";
    }
    if (unitMode === "CUBIC_ONLY") {
        return " / куб.м";
    }
    return " / шт или куб.м";
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

function formatDateTime(value) {
    if (!value) {
        return "-";
    }
    return new Date(value).toLocaleString("ru-RU");
}

function getDiscountPercent(product) {
    if (!product?.oldPrice || Number(product.oldPrice) <= Number(product.price)) {
        return null;
    }
    return Math.round((1 - Number(product.price) / Number(product.oldPrice)) * 100);
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
            const message = data?.message || data || `Ошибка ${res.status}`;
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
                    <div class="price">${money(product.price)}</div>
                    ${discountHtml}
                </div>
                <button class="card-cta" type="button">Заказать</button>
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
    renderProducts(items, el.catalogGrid, el.catalogEmpty, "catalog");
}

async function loadFixPrice() {
    const items = await api("/api/fix-price");
    state.fixPriceProducts = items;
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

function openProduct(product) {
    state.selectedProduct = product;
    el.modalImage.src = product.imageUrl;
    el.modalTitle.textContent = product.name;
    el.modalDescription.textContent = product.description;
    const unitSuffix = unitPriceSuffixByMode(product.unitMode);
    el.modalPrice.textContent = `${money(product.price)}${unitSuffix}`;

    if (product.fixPrice && product.oldPrice) {
        el.modalOldPriceLine.classList.remove("hidden");
        el.modalOldPrice.textContent = `${money(product.oldPrice)}${unitSuffix}`;
    } else {
        el.modalOldPriceLine.classList.add("hidden");
        el.modalOldPrice.textContent = "";
    }

    el.productModal.classList.remove("hidden");
}

function closeProduct() {
    el.productModal.classList.add("hidden");
}

function configureOrderUnitSelect(product) {
    const allowed = getAllowedUnits(product);
    el.orderUnit.innerHTML = allowed
        .map((unit) => `<option value="${unit}">${unitLabel(unit)}</option>`)
        .join("");

    el.orderUnit.value = allowed[0];
    el.orderUnit.disabled = allowed.length === 1;
}

function openOrder() {
    if (!state.authenticated || !state.userId) {
        notify("Оформление заказа доступно только при открытии mini app из MAX.");
        return;
    }

    if (!state.selectedProduct) {
        return;
    }

    configureOrderUnitSelect(state.selectedProduct);
    el.orderModal.classList.remove("hidden");
    el.orderStep1.classList.remove("hidden");
    el.orderStep2.classList.add("hidden");
    el.orderMessage.textContent = "";
    el.orderQuantity.value = "";
}

function closeOrder() {
    el.orderModal.classList.add("hidden");
}

function initOrderFlow() {
    el.orderBtn.addEventListener("click", () => {
        closeProduct();
        openOrder();
    });

    el.goStep2.addEventListener("click", () => {
        const qty = Number(el.orderQuantity.value);
        if (!Number.isFinite(qty) || qty <= 0) {
            notify("Введите корректное количество");
            return;
        }

        state.orderQuantity = qty;
        state.orderUnit = el.orderUnit.value;
        el.orderStep1.classList.add("hidden");
        el.orderStep2.classList.remove("hidden");
    });

    el.submitOrder.addEventListener("click", async () => {
        if (!state.authenticated || !state.userId) {
            notify("Откройте mini app через MAX.");
            return;
        }

        const fullName = el.orderFullName.value.trim();
        const phone = el.orderPhone.value.trim();
        const address = el.orderAddress.value.trim();

        if (!fullName || !phone || !address) {
            notify("Заполните ФИО, телефон и адрес доставки");
            return;
        }

        try {
            const response = await api("/api/orders", {
                method: "POST",
                body: JSON.stringify({
                    productId: state.selectedProduct.id,
                    quantity: state.orderQuantity,
                    quantityUnit: state.orderUnit,
                    fullName,
                    phone,
                    address
                })
            });

            if (response.paymentUrl) {
                el.orderMessage.textContent = `Сумма: ${money(response.totalPrice)}. Открываем оплату...`;
                if (window.WebApp?.openLink) {
                    window.WebApp.openLink(response.paymentUrl);
                } else {
                    window.open(response.paymentUrl, "_blank", "noopener,noreferrer");
                }
            } else {
                el.orderMessage.textContent = "Заказ создан. С вами свяжется менеджер.";
            }
        } catch (error) {
            el.orderMessage.textContent = error.message;
        }
    });

    el.closeOrderModal.addEventListener("click", closeOrder);
    el.closeProductModal.addEventListener("click", closeProduct);

    window.addEventListener("click", (event) => {
        if (event.target === el.productModal) {
            closeProduct();
        }
        if (event.target === el.orderModal) {
            closeOrder();
        }
    });
}

function renderSimpleList(node, rows) {
    node.innerHTML = rows.length
        ? rows.map((row) => `<div class=\"list-row\">${escapeHtml(row)}</div>`).join("")
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
            return `
                <article class="manage-item">
                    <div class="manage-meta">
                        <p class="manage-title">${escapeHtml(item.name)}</p>
                        <p class="manage-sub">${money(item.price)}${escapeHtml(oldPriceText)}</p>
                        <div class="manage-badges">
                            ${typeTag}
                            <span class="tag catalog">${escapeHtml(unitModeLabel(item.unitMode))}</span>
                        </div>
                    </div>
                    <button class="btn-inline btn-danger js-delete-product" data-id="${item.id}" data-name="${escapeHtml(item.name)}">
                        Удалить
                    </button>
                </article>
            `;
        }).join("");
    };

    renderGroup(catalogProducts, el.adminCatalogProducts);
    renderGroup(fixPriceProducts, el.adminFixPriceProducts);

    document.querySelectorAll(".js-delete-product").forEach((button) => {
        button.addEventListener("click", async () => {
            const productId = Number(button.dataset.id);
            const productName = button.dataset.name || "товар";
            const confirmed = window.confirm(`Удалить «${productName}»?`);
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
            const confirmed = window.confirm(`Удалить пост «${postTitle}»?`);
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
        const [admins, users, products, posts] = await Promise.all([
            api("/api/admin/admins"),
            api("/api/admin/users"),
            api("/api/admin/products"),
            api("/api/admin/info-posts")
        ]);

        renderSimpleList(
            el.adminsList,
            admins.map((item) => `ID ${item.maxUserId} | ${item.fullName || "без имени"}`)
        );

        renderSimpleList(
            el.usersList,
            users.map((item) => `ID ${item.maxUserId} | ${item.phone || "телефон не указан"} | ${item.admin ? "admin" : "user"}`)
        );

        renderAdminProducts(products);
        renderAdminPosts(posts);
    } catch (error) {
        renderSimpleList(el.adminsList, [`Ошибка: ${error.message}`]);
        renderSimpleList(el.usersList, []);
        el.adminCatalogProducts.innerHTML = '<div class="empty">Ошибка загрузки товаров</div>';
        el.adminFixPriceProducts.innerHTML = '<div class="empty">Ошибка загрузки товаров</div>';
        el.adminPostsList.innerHTML = '<div class="empty">Ошибка загрузки постов</div>';
    }
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
        const statusClass = orderStatusClass(order.status);
        return `
            <article class="order-card">
                <div class="order-head">
                    <h3 class="order-title">#${order.id} · ${escapeHtml(order.productName)}</h3>
                    <span class="order-status ${statusClass}">${escapeHtml(orderStatusLabel(order.status))}</span>
                </div>
                <div class="order-head">
                    <span class="order-time">Создан: ${escapeHtml(formatDateTime(order.createdAt))}</span>
                    <span class="order-time">MAX ID: ${escapeHtml(order.maxUserId)}</span>
                </div>
                <div class="order-grid">
                    <div class="order-cell">
                        <p class="order-label">Количество</p>
                        <p class="order-value">${escapeHtml(order.quantity)} ${escapeHtml(unitLabel(order.quantityUnit))}</p>
                    </div>
                    <div class="order-cell">
                        <p class="order-label">Сумма</p>
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
                        <p class="order-value">${escapeHtml(order.paymentId || "ожидается")}</p>
                    </div>
                </div>
            </article>
        `;
    }).join("");

    el.adminOrdersGrid.innerHTML = markup;
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

function syncStockInputsByUnitMode() {
    const mode = el.unitModeSelect.value;

    const pcsVisible = mode === "PCS_ONLY" || mode === "BOTH";
    const cubicVisible = mode === "CUBIC_ONLY" || mode === "BOTH";

    el.stockPcsWrapper.classList.toggle("hidden", !pcsVisible);
    el.stockCubicWrapper.classList.toggle("hidden", !cubicVisible);

    el.stockPcsInput.required = pcsVisible;
    el.stockCubicInput.required = cubicVisible;

    if (!pcsVisible) {
        el.stockPcsInput.value = "";
    }
    if (!cubicVisible) {
        el.stockCubicInput.value = "";
    }
}

function syncFixPriceFields() {
    const isFixPrice = el.sectionType.value === "FIX_PRICE";
    el.oldPriceWrapper.classList.toggle("hidden", !isFixPrice);
    el.oldPriceInput.required = isFixPrice;

    if (!isFixPrice) {
        el.oldPriceInput.value = "";
    }
}

function initProductFormControls() {
    el.unitModeSelect.addEventListener("change", syncStockInputsByUnitMode);
    el.sectionType.addEventListener("change", syncFixPriceFields);
    syncStockInputsByUnitMode();
    syncFixPriceFields();
}

function parseOptionalNumber(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }

    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : null;
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
            const unitMode = formData.get("unitMode");
            const sectionType = formData.get("sectionType");
            const isFixPrice = sectionType === "FIX_PRICE";

            const file = document.getElementById("productImage").files[0];
            if (!file) {
                throw new Error("Загрузите фото товара");
            }

            const imageUrl = await uploadImage(file);

            const stockPcs = (unitMode === "PCS_ONLY" || unitMode === "BOTH")
                ? parseOptionalNumber(formData.get("stockPcs"))
                : null;

            const stockCubicMeters = (unitMode === "CUBIC_ONLY" || unitMode === "BOTH")
                ? parseOptionalNumber(formData.get("stockCubicMeters"))
                : null;

            if ((unitMode === "PCS_ONLY" || unitMode === "BOTH") && stockPcs === null) {
                throw new Error("Укажите остаток в штуках");
            }

            if ((unitMode === "CUBIC_ONLY" || unitMode === "BOTH") && stockCubicMeters === null) {
                throw new Error("Укажите остаток в кубометрах");
            }

            const price = Number(formData.get("price"));
            const oldPrice = parseOptionalNumber(formData.get("oldPrice"));

            if (isFixPrice && (oldPrice === null || oldPrice <= price)) {
                throw new Error("Для Fix Price старая цена должна быть выше текущей");
            }

            await api("/api/admin/products", {
                method: "POST",
                body: JSON.stringify({
                    name: formData.get("name"),
                    description: formData.get("description"),
                    imageUrl,
                    price,
                    oldPrice: isFixPrice ? oldPrice : null,
                    unitMode,
                    stockPcs,
                    stockCubicMeters,
                    fixPrice: isFixPrice,
                    active: true
                })
            });

            notify("Товар добавлен");
            el.productForm.reset();
            syncStockInputsByUnitMode();
            syncFixPriceFields();
            await Promise.all([loadCatalog(), loadFixPrice(), loadAdminData()]);
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

    initTabs();
    initShopControls();
    initOrderFlow();
    initProductFormControls();
    initAdminForms();

    await bootstrapUser();
    await Promise.all([loadCatalog(), loadFixPrice(), loadInfo()]);
}

init().catch((error) => {
    console.error(error);
    notify("Ошибка загрузки mini app");
});
