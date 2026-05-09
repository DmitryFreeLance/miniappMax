const state = {
    userId: null,
    authenticated: false,
    isAdmin: false,
    userFullName: null,
    products: [],
    fixPriceProducts: [],
    selectedProduct: null,
    orderQuantity: null,
    orderUnit: "PCS"
};

const el = {
    tabs: document.querySelectorAll(".tab"),
    tabContents: document.querySelectorAll(".tab-content"),
    adminTabBtn: document.getElementById("adminTabBtn"),
    userStatusBadge: document.getElementById("userStatusBadge"),
    userNameText: document.getElementById("userNameText"),
    userHint: document.getElementById("userHint"),

    catalogGrid: document.getElementById("catalogGrid"),
    catalogEmpty: document.getElementById("catalogEmpty"),
    fixPriceGrid: document.getElementById("fixPriceGrid"),
    fixPriceEmpty: document.getElementById("fixPriceEmpty"),
    infoPosts: document.getElementById("infoPosts"),

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
    usersList: document.getElementById("usersList"),
    ordersList: document.getElementById("ordersList")
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
    if (!state.authenticated) {
        el.userStatusBadge.className = "status-pill warn";
        el.userStatusBadge.textContent = "Открыт вне MAX";
        el.userNameText.textContent = "Гостевой режим";
        el.userHint.textContent = "Откройте mini app через бот в MAX, чтобы оформлять заказы и видеть персональные данные.";
        return;
    }

    el.userStatusBadge.className = "status-pill ok";
    el.userStatusBadge.textContent = state.isAdmin ? "Вы админ" : "Авторизовано в MAX";
    el.userNameText.textContent = state.userFullName ? state.userFullName : "Профиль MAX подключен";
    el.userHint.textContent = state.isAdmin
        ? "Доступны клиентские и админские функции." : "Доступно оформление заказа и просмотр каталога.";
}

function setAdminVisibility() {
    el.adminTabBtn.classList.toggle("hidden", !state.isAdmin);

    const activeAdmin = document.querySelector(".tab.active")?.dataset?.tab === "admin";
    if (!state.isAdmin && activeAdmin) {
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
}

function initTabs() {
    el.tabs.forEach((btn) => {
        btn.addEventListener("click", () => {
            const target = btn.dataset.tab;
            if (target === "admin" && !state.isAdmin) {
                notify("Раздел доступен только администраторам.");
                return;
            }
            switchTab(target);
        });
    });
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

function renderProducts(items, targetGrid, targetEmpty) {
    targetGrid.innerHTML = "";

    if (!items.length) {
        targetEmpty.classList.remove("hidden");
        return;
    }

    targetEmpty.classList.add("hidden");

    items.forEach((product) => {
        const card = document.createElement("article");
        card.className = "card";

        const oldPriceHtml = product.fixPrice && product.oldPrice
            ? `<div class=\"price-old\">${money(product.oldPrice)}</div>`
            : "";

        const badgeHtml = product.fixPrice
            ? '<div class="fix-badge">Fix Price 🔥</div>'
            : "";

        card.innerHTML = `
            <img src="${product.imageUrl}" alt="${product.name}">
            <div class="card-body">
                ${badgeHtml}
                <h4>${product.name}</h4>
                ${oldPriceHtml}
                <div class="price">${money(product.price)}</div>
            </div>
        `;

        card.addEventListener("click", () => openProduct(product));
        targetGrid.appendChild(card);
    });
}

async function loadCatalog() {
    const items = await api("/api/catalog");
    state.products = items;
    renderProducts(items, el.catalogGrid, el.catalogEmpty);
}

async function loadFixPrice() {
    const items = await api("/api/fix-price");
    state.fixPriceProducts = items;
    renderProducts(items, el.fixPriceGrid, el.fixPriceEmpty);
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
        node.innerHTML = `
            <h4>${post.title}</h4>
            <p>${post.content}</p>
            <small>${new Date(post.createdAt).toLocaleString("ru-RU")}</small>
        `;
        el.infoPosts.appendChild(node);
    });
}

function openProduct(product) {
    state.selectedProduct = product;
    el.modalImage.src = product.imageUrl;
    el.modalTitle.textContent = product.name;
    el.modalDescription.textContent = product.description;
    el.modalPrice.textContent = money(product.price);

    if (product.fixPrice && product.oldPrice) {
        el.modalOldPriceLine.classList.remove("hidden");
        el.modalOldPrice.textContent = money(product.oldPrice);
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
        ? rows.map((row) => `<div class=\"list-row\">${row}</div>`).join("")
        : '<div class="list-row">Нет данных</div>';
}

async function loadAdminData() {
    if (!state.isAdmin) {
        return;
    }

    try {
        const [admins, users, orders] = await Promise.all([
            api("/api/admin/admins"),
            api("/api/admin/users"),
            api("/api/admin/orders")
        ]);

        renderSimpleList(
            el.adminsList,
            admins.map((item) => `ID ${item.maxUserId} | ${item.fullName || "без имени"}`)
        );

        renderSimpleList(
            el.usersList,
            users.map((item) => `ID ${item.maxUserId} | ${item.phone || "телефон не указан"} | ${item.admin ? "admin" : "user"}`)
        );

        renderSimpleList(
            el.ordersList,
            orders.map((item) => `#${item.id} | ${item.productName} | ${item.quantity} ${unitLabel(item.quantityUnit)} | ${money(item.totalPrice)} | ${item.status}`)
        );
    } catch (error) {
        renderSimpleList(el.adminsList, [`Ошибка: ${error.message}`]);
        renderSimpleList(el.usersList, []);
        renderSimpleList(el.ordersList, []);
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
            await loadInfo();
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
