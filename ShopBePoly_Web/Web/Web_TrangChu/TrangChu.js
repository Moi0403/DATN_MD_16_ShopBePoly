const API_BASE = `http://${config.host}:${config.port}/api`;
const UPLOADS_BASE = `http://${config.host}:${config.port}/Uploads`;

let previousLowStockData = [];
let previousStagnantData = [];
let previousOnlineUsers = [];
let todayOrdersData = [];
let pendingOrdersData = [];
let previousTodayStats = { totalOrders: 0, totalRevenue: 0 };
let currentStagnantPage = 1;
let totalStagnantPages = 1;
let onlineUsersIntervalId = null; 
let userModalInstance = null; 

function setTextIfExists(id, text) {
    const elem = document.getElementById(id);
    if (elem) {
        elem.textContent = text || '0';
        console.log(`Updated ${id} with value: ${text}`);
    } else {
        console.error(`Element with id ${id} not found in DOM`);
    }
}

function formatCurrency(value) {
    return value ? Number(value).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' }) : '0 ₫';
}

async function fetchWithRetry(url, retries = 3, delay = 1000, timeout = 20000) {
    if (!url || url.includes('undefined')) {
        console.error('Invalid URL:', url);
        throw new Error('Invalid API base URL');
    }
    for (let i = 0; i < retries; i++) {
        try {
            console.log(`Fetching URL: ${url}, attempt ${i + 1}`);
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), timeout);
            const res = await fetch(url, { signal: controller.signal });
            clearTimeout(timeoutId);
            if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
            const data = await res.json();
            console.log(`Response from ${url}:`, data);
            return data;
        } catch (error) {
            console.error(`Attempt ${i + 1} failed for ${url}:`, error);
            if (i === retries - 1) throw error;
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }
}

function sanitizeHtml(html) {
    if (window.DOMPurify) {
        return DOMPurify.sanitize(html);
    } else {
        console.warn('DOMPurify not loaded, rendering unsanitized HTML');
        return html;
    }
}

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

async function fetchOnlineCount() {
    console.log('Fetching online user count...');
    try {
        if (!API_BASE) {
            console.error('API_BASE is not defined');
            setTextIfExists('user_online', 'Error');
            return;
        }
        const data = await fetchWithRetry(`${API_BASE}/list/users_online`);
        const users = Array.isArray(data.users) ? data.users : [];
        const role0Users = users.filter(user => user.role === 0);
        setTextIfExists('user_online', role0Users.length);
    } catch (error) {
        console.error('Error fetching online user count:', error);
        setTextIfExists('user_online', 'Error');
        Toastify({
            text: "Lỗi tải số lượng người dùng online: " + (error.message || 'Unknown error'),
            duration: 3000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
    }
}

async function fetchAndDisplayOnlineUsers() {
    const modalBody = document.querySelector('#userModal .modal-body');
    if (!modalBody) {
        console.error('Modal body not found for #userModal');
        return;
    }

    modalBody.innerHTML = '<div class="text-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';

    try {
        const data = await fetchWithRetry(`${API_BASE}/list/users_online`, 3, 1000, 20000);
        const users = Array.isArray(data.users) ? data.users.filter(user => user.role === 0) : [];
        const currentUsernames = users.map(user => user.username || '').sort();
        const previousUsernames = previousOnlineUsers.map(user => user.username || '').sort();
        if (JSON.stringify(currentUsernames) === JSON.stringify(previousUsernames)) {
            console.log('No changes in online users, skipping DOM update');
            return;
        }
        previousOnlineUsers = users;

        let html = users.length
            ? users.map((user, index) => `
                <tr class="${index % 2 === 0 ? 'row-white' : 'row-black'}">
                    <td>${user.username || '---'}</td>
                    <td>${user.name || '---'}</td>
                    <td>${user.email || '---'}</td>
                    <td>${user.phone_number || '---'}</td>
                    <td>
                        <img src="${UPLOADS_BASE}/${user.avt_user || 'default.png'}" width="50" height="50" style="object-fit:cover;" 
                             onerror="this.src='https://via.placeholder.com/50'"/>
                    </td>
                </tr>
            `).join('')
            : '<tr><td colspan="5" class="text-center">Không có người dùng online (role = 0)</td></tr>';

        modalBody.innerHTML = sanitizeHtml(`
            <div class="table-responsive">
                <table class="table table-bordered table-hover">
                    <thead>
                        <tr>
                            <th>Username</th>
                            <th>Họ tên</th>
                            <th>Email</th>
                            <th>SĐT</th>
                            <th>Avatar</th>
                        </tr>
                    </thead>
                    <tbody>${html}</tbody>
                </table>
            </div>
        `);
    } catch (error) {
        console.error('Error fetching online users:', error);
        modalBody.innerHTML = '<p class="text-center text-danger">Lỗi tải dữ liệu người dùng online: ' + (error.message || 'Unknown error') + '</p>';
        Toastify({
            text: "Lỗi tải danh sách người dùng online: " + (error.message || 'Unknown error'),
            duration: 3000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
    }
}

async function fetchTodayStatistics() {
    try {
        const data = await fetchWithRetry(`${API_BASE}/statistics-today`);
        if (previousTodayStats.totalOrders === data.totalOrders && previousTodayStats.totalRevenue === data.totalRevenue) return;
        previousTodayStats = { totalOrders: data.totalOrders, totalRevenue: data.totalRevenue };

        setTextIfExists('todayOrdersCount', data.totalOrders ?? 0);
        setTextIfExists('todayRevenue', formatCurrency(data.totalRevenue));
    } catch (error) {
        console.error('Error fetching today statistics:', error);
        setTextIfExists('todayOrdersCount', 'Error');
        setTextIfExists('todayRevenue', 'Error');
        Toastify({
            text: "Lỗi tải thống kê hôm nay!",
            duration: 3000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
    }
}

async function fetchLowStockProducts() {
    const tableBody = document.getElementById('lowStockTable');
    if (!tableBody) return;

    tableBody.innerHTML = '<tr><td colspan="8" class="text-center"><div class="spinner-border text-primary" role="status"></div></td></tr>';

    try {
        const threshold = localStorage.getItem('lowStockThreshold') || 20;
        const data = await fetchWithRetry(`${API_BASE}/products/low-stock?threshold=${threshold}`);

        if (JSON.stringify(data.data) === JSON.stringify(previousLowStockData)) return;
        previousLowStockData = data.data;

        tableBody.innerHTML = '';
        if (!data.data?.length) {
            tableBody.innerHTML = '<tr><td colspan="8" class="text-center">Không có sản phẩm gần hết hàng</td></tr>';
            return;
        }

        const html = data.data
            .filter(item => item.variations.some(v => v.stock <= threshold))
            .map((item, index) => {
                const lowStockVariations = item.variations.filter(v => v.stock <= threshold);
                const sizesHtml = lowStockVariations.map(v => v.size || '---').join('<br>');
                const stockHtml = lowStockVariations.map(v => v.stock || 0).join('<br>');
                const colorHtml = item.color || '-';
                const rowClass = index % 2 === 0 ? 'row-white' : 'row-black'; // Xen kẽ màu trắng đen

                return `
                    <tr class="${rowClass}">
                        <td>${item.name || '---'}</td>
                        <td>${item.category || '---'}</td>
                        <td>${colorHtml}</td>
                        <td>${Number(item.price || 0).toLocaleString('vi-VN')} ₫</td>
                        <td>${item.sale_price ? Number(item.sale_price).toLocaleString('vi-VN') + ' ₫' : '-'}</td>
                        <td>${sizesHtml}</td>
                        <td>${stockHtml}</td>
                        <td>
                            <img src="${UPLOADS_BASE}/${item.avt_imgproduct || 'default.png'}" width="100" height="100" style="object-fit:cover;" 
                                 onerror="this.src='https://via.placeholder.com/100'"/>
                        </td>
                    </tr>
                `;
            }).join('');

        tableBody.innerHTML = sanitizeHtml(html);
    } catch (error) {
        console.error('Error fetching low-stock products:', error);
        tableBody.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Lỗi tải dữ liệu sản phẩm gần hết hàng</td></tr>';
        Toastify({
            text: "Lỗi tải danh sách sản phẩm gần hết hàng!",
            duration: 3000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
    }
}

const debouncedFetchStagnantProducts = debounce(async (page = 1) => {
    const tableBody = document.getElementById('stagnantTable');
    if (!tableBody) return;

    tableBody.innerHTML = '<tr><td colspan="8" class="text-center"><div class="spinner-border text-primary" role="status"></div></td></tr>';

    try {
        const days = localStorage.getItem('stagnantDays') || 7;
        const soldLimit = localStorage.getItem('stagnantSoldLimit') || 50;
        console.log(`Fetching stagnant products: days=${days}, soldLimit=${soldLimit}, page=${page}`);
        const res = await fetchWithRetry(`${API_BASE}/products/stagnant?days=${days}&soldLimit=${soldLimit}&page=${page}&limit=10`);
        const { data, pagination } = res;

        if (JSON.stringify(data) === JSON.stringify(previousStagnantData)) {
            console.log('No changes in stagnant products data, skipping DOM update');
            return;
        }
        previousStagnantData = data;
        currentStagnantPage = pagination.page;
        totalStagnantPages = pagination.totalPages;

        tableBody.innerHTML = '';
        if (!data.length) {
            tableBody.innerHTML = '<tr><td colspan="8" class="text-center">Không có sản phẩm tồn kho lâu. Vui lòng kiểm tra dữ liệu hoặc điều chỉnh tham số (ngày: ' + days + ', giới hạn bán: ' + soldLimit + ').</td></tr>';
            Toastify({
                text: `Không tìm thấy sản phẩm tồn kho lâu (days=${days}, soldLimit=${soldLimit})`,
                duration: 5000,
                gravity: "top",
                position: "right",
                backgroundColor: "#ffc107"
            }).showToast();
            return;
        }

        const fragment = document.createDocumentFragment();
        data.forEach((product, index) => {
            const tr = document.createElement('tr');
            tr.className = index % 2 === 0 ? 'row-white' : 'row-black'; // Xen kẽ màu trắng đen
            const variationsHtml = Object.entries(product.variationsByColor || {}).map(([color, { variations }]) =>
                variations.map(v =>
                    `Size: ${v.size || '---'}, Tồn: ${v.stock || 0}, Đã bán: ${v.sold || 0} <br>
                     <img src="${UPLOADS_BASE}/${v.image || 'default.png'}" width="50" height="50" style="object-fit:cover;" 
                          onerror="this.src='https://via.placeholder.com/50'"/>`
                ).join('<hr>')
            ).join('<hr>');

            tr.innerHTML = `
                <td>${product.name || '---'}</td>
                <td>${product.category || '---'}</td>
                <td>${Object.keys(product.variationsByColor || {}).join(', ') || '-'}</td>
                <td>${Number(product.price || 0).toLocaleString('vi-VN')} ₫</td>
                <td>${product.sale_price ? Number(product.sale_price).toLocaleString('vi-VN') + ' ₫' : '-'}</td>
                <td>${variationsHtml || '-'}</td>
                <td>${product.totalStock || 0}</td>
                <td>
                    <img src="${UPLOADS_BASE}/${product.avt_imgproduct || 'default.png'}" width="100" height="100" style="object-fit:cover;" 
                         onerror="this.src='https://via.placeholder.com/100'"/>
                </td>
            `;
            fragment.appendChild(tr);
        });

        tableBody.appendChild(fragment);

        const wrapper = document.getElementById('stagnantTableWrapper');
        const existingPagination = wrapper.nextElementSibling;
        if (existingPagination && existingPagination.classList.contains('pagination-info')) {
            existingPagination.remove();
        }
    } catch (error) {
        console.error('Error fetching stagnant products:', error);
        tableBody.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Lỗi tải dữ liệu sản phẩm tồn kho lâu</td></tr>';
        Toastify({
            text: "Lỗi tải danh sách sản phẩm tồn kho lâu: " + error.message,
            duration: 5000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
    }
}, 300);

function displayOrderDetail(order) {
    const modalBody = document.querySelector('#orderModal .modal-body');
    const modalTitle = document.querySelector('#orderModal .modal-title');
    if (!modalBody || !modalTitle) return;

    modalTitle.textContent = `Chi tiết đơn hàng: ${order.id_order || order._id || 'N/A'}`;

    const productHtml = Array.isArray(order.products) ? order.products.map(product => {
        const productData = product.id_product || {};
        const category = productData.id_category?.title || 'Không có thể loại';
        const imageUrl = productData.avt_imgproduct ? `${UPLOADS_BASE}/${productData.avt_imgproduct}` : 'https://via.placeholder.com/90';

        return `
            <div class="d-flex align-items-center mb-3">
                <img src="${imageUrl}" onerror="this.src='https://via.placeholder.com/90'" class="img-thumbnail" style="width: 60px; height: 60px; object-fit: cover; margin-right: 15px;">
                <div>
                    <h6 class="mb-0 text-gray-800">${productData.nameproduct || '---'}</h6>
                    <small class="text-muted d-block">Thể loại: ${category}</small>
                    <small class="text-muted d-block">Màu: ${product.color || '---'} - Size: ${product.size || '---'} - Số lượng: ${product.quantity || 0}</small>
                </div>
            </div>
            <hr>
        `;
    }).join('') : '<p class="text-gray-500">Không có sản phẩm</p>';

    const cancelReasonHtml = order.status === 'Đã hủy' && order.cancelReason
        ? `<p class="text-danger"><strong>Lý do hủy:</strong> ${order.cancelReason || 'Không có lý do cụ thể'}</p>`
        : '';

    // 👉 Thêm checkedAt + checkedBy
    const checkedHtml = order.checkedAt || order.checkedBy ? `
        <p><strong>Thời gian cập nhật:</strong> 
            ${order.checkedAt 
                ? new Date(order.checkedAt).toLocaleString('vi-VN', { 
                    day: '2-digit', month: '2-digit', year: 'numeric', 
                    hour: '2-digit', minute: '2-digit', second: '2-digit' 
                  }) 
                : '---'}
        </p>
        <p><strong>Người xác nhận:</strong> ${order.checkedBy || '---'}</p>
    ` : '';

    modalBody.innerHTML = sanitizeHtml(`
        <div class="row">
            <div class="col-lg-6">
                <h5>Thông tin đơn hàng</h5>
                <p><strong>Mã đơn hàng:</strong> ${order.id_order || order._id || 'N/A'}</p>
                <p><strong>Người đặt:</strong> ${order.id_user?.name || '---'}</p>
                <p><strong>SĐT:</strong> ${order.id_user?.phone_number || '---'}</p>
                <p><strong>Địa chỉ:</strong> ${order.address || 'Không có'}</p>
                <p><strong>Ngày đặt:</strong> ${order.date 
                    ? new Date(order.date).toLocaleDateString('vi-VN') 
                    : 'Không xác định'}</p>
                <p><strong>Thời gian đặt:</strong> ${order.date 
                    ? new Date(order.date).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }) 
                    : 'Không xác định'}</p>
                <p><strong>Thanh toán:</strong> ${order.pay || 'Không xác định'}</p>
                <p><strong>Tổng tiền:</strong> ${Number(order.total || 0).toLocaleString('vi-VN')} ₫</p>
                <p><strong>Trạng thái:</strong> ${order.status || 'Không xác định'}</p>
                ${cancelReasonHtml}
                ${checkedHtml}
            </div>
            <div class="col-lg-6">
                <h5 class="font-weight-bold text-gray-800">Sản phẩm:</h5>
                <div class="products-list-container">
                    ${productHtml}
                </div>
            </div>
        </div>
        <button class="btn btn-secondary mt-3" id="backToOrdersListBtn">Quay lại danh sách</button>
    `);

    document.getElementById('backToOrdersListBtn')?.addEventListener('click', () => {
        fetchAndDisplayOrdersToday();
    });
}


async function fetchAndDisplayOrdersToday() {
    const modalBody = document.querySelector('#orderModal .modal-body');
    const modalTitle = document.querySelector('#orderModal .modal-title');
    if (!modalBody || !modalTitle) return;

    modalBody.innerHTML = '<div class="text-center"><div class="spinner-border text-primary" role="status"></div></div>';
    modalTitle.textContent = 'Danh sách đơn hàng hôm nay';

    try {
        const allOrders = await fetchWithRetry(`${API_BASE}/list_order`);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        todayOrdersData = allOrders.filter(order => new Date(order.date) >= today);

        const html = todayOrdersData.length
            ? todayOrdersData.map((order, index) => `
                <tr class="${index % 2 === 0 ? 'row-white' : 'row-black'}">
                    <td>${order.id_order || order._id || 'N/A'}</td>
                    <td>${order.id_user?.name || 'N/A'}</td>
                    <td>${Number(order.total || 0).toLocaleString('vi-VN')} ₫</td>
                    <td>${order.status || '-'}</td>
                    <td>${order.date ? new Date(order.date).toLocaleTimeString('vi-VN') : '-'}</td>
                    <td>${order.date ? new Date(order.date).toLocaleDateString('vi-VN') : '-'}</td>
                    <td>
                        <button class="btn btn-sm btn-primary view-detail-btn" data-order-id="${order.id_order || order._id}">Xem chi tiết</button>
                    </td>
                </tr>
            `).join('')
            : '<tr><td colspan="7" class="text-center">Không có đơn hàng hôm nay</td></tr>';

        modalBody.innerHTML = sanitizeHtml(`
            <div class="table-responsive">
                <table class="table table-bordered table-hover">
                    <thead>
                        <tr>
                            <th>Mã đơn</th>
                            <th>Người đặt</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th>Giờ đặt</th>
                            <th>Ngày đặt</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>${html}</tbody>
                </table>
            </div>
        `);

        document.querySelectorAll('.view-detail-btn').forEach(button => {
            button.addEventListener('click', () => {
                const orderId = button.dataset.orderId;
                const order = todayOrdersData.find(o => o.id_order === orderId || o._id === orderId);
                if (order) displayOrderDetail(order);
            });
        });
    } catch (error) {
        console.error('Error fetching today\'s orders:', error);
        modalBody.innerHTML = '<p class="text-center text-danger">Lỗi tải dữ liệu đơn hàng.</p>';
        Toastify({
            text: "Lỗi tải danh sách đơn hàng hôm nay!",
            duration: 3000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
    }
}

async function fetchPendingOrders() {
    try {
        const data = await fetchWithRetry(`${API_BASE}/orders/pending`);
        pendingOrdersData = Array.isArray(data.orders) ? data.orders : [];
        setTextIfExists('pendingOrders', pendingOrdersData.length);
        renderPendingOrdersTable();
    } catch (error) {
        console.error('Error fetching pending orders:', error);
        setTextIfExists('pendingOrders', 'Error');
        Toastify({
            text: "Lỗi tải danh sách đơn hàng chờ xác nhận!",
            duration: 3000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
    }
}

function renderPendingOrdersTable() {
    const modalBody = document.querySelector('#pendingOrdersModal .modal-body');
    if (!modalBody) return;

    modalBody.innerHTML = '<div class="text-center"><div class="spinner-border text-primary" role="status"></div></div>';

    if (!pendingOrdersData.length) {
        modalBody.innerHTML = '<p>Không có đơn hàng chờ xác nhận.</p>';
        return;
    }

    const html = pendingOrdersData.map((order, index) => {
        const totalQuantity = order.products.reduce((acc, p) => acc + (p.quantity || 0), 0);
        return `
            <tr class="${index % 2 === 0 ? 'row-white' : 'row-black'}">
                <td>${order.id_order || 'N/A'}</td>
                <td>${order.id_user?.name || 'N/A'}</td>
                <td>${totalQuantity}</td>
                <td>${order.date ? new Date(order.date).toLocaleString('vi-VN') : '-'}</td>
                <td>${Number(order.total || 0).toLocaleString('vi-VN')} ₫</td>
                <td>${order.status || '-'}</td>
                <td>
                    <button class="btn btn-sm btn-primary process-order-btn" data-order-id="${order.id_order}">Xử lý đơn hàng</button>
                </td>
            </tr>
        `;
    }).join('');

    modalBody.innerHTML = sanitizeHtml(`
        <div class="table-responsive">
            <table class="table table-bordered table-hover">
                <thead>
                    <tr>
                        <th>Mã đơn</th>
                        <th>Người đặt</th>
                        <th>Số lượng</th>
                        <th>Ngày đặt</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                        <th>Xử lý</th>
                    </tr>
                </thead>
                <tbody>${html}</tbody>
            </table>
        </div>
    `);

    document.querySelectorAll('.process-order-btn').forEach(button => {
        button.addEventListener('click', (e) => {
            e.stopPropagation();
            const orderId = button.dataset.orderId;
            localStorage.setItem('pendingOrderId', orderId);
            window.location.href = '../Web_XNDH/Web_XNDH.html';
        });
    });
}

document.querySelector('.border-left-danger')?.addEventListener('click', async () => {
    const pendingModalElement = document.getElementById('pendingOrdersModal');
    if (!pendingModalElement) return;
    const pendingModal = new bootstrap.Modal(pendingModalElement);
    pendingModal.show();
    await fetchPendingOrders();
});

const debouncedClickOnlineUsers = debounce(async () => {
    console.log('Opening user modal');
    const userModalElement = document.getElementById('userModal');
    if (!userModalElement) {
        console.error('User modal element not found');
        return;
    }

    if (!API_BASE || API_BASE.includes('undefined')) {
        console.error('API_BASE is not defined or invalid');
        Toastify({
            text: "Lỗi: API base URL không hợp lệ",
            duration: 3000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
        return;
    }

    if (onlineUsersIntervalId) {
        clearInterval(onlineUsersIntervalId);
        onlineUsersIntervalId = null;
        console.log('Cleared previous interval');
    }

    if (userModalInstance) {
        userModalInstance.dispose();
        userModalInstance = null;
        console.log('Previous modal instance disposed');
    }

    userModalInstance = new bootstrap.Modal(userModalElement, {
        backdrop: 'static',
        keyboard: true
    });

    const modalBody = document.querySelector('#userModal .modal-body');
    if (modalBody) {
        modalBody.innerHTML = '<div class="text-center"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    } else {
        console.error('Modal body not found');
        return;
    }

    userModalInstance.show();

    try {
        await fetchAndDisplayOnlineUsers();
        onlineUsersIntervalId = setInterval(async () => {
            if (userModalInstance && userModalInstance._isShown) {
                await fetchAndDisplayOnlineUsers();
            } else {
                console.log('Modal is not shown, clearing interval');
                clearInterval(onlineUsersIntervalId);
                onlineUsersIntervalId = null;
            }
        }, 120000);
        console.log('New interval set:', onlineUsersIntervalId);
    } catch (error) {
        console.error('Initial fetch failed, not setting interval:', error);
        modalBody.innerHTML = '<p class="text-center text-danger">Lỗi tải dữ liệu người dùng online: ' + (error.message || 'Unknown error') + '</p>';
    }

    userModalElement.addEventListener('hidden.bs.modal', () => {
        console.log('Modal hidden, cleaning up');
        if (onlineUsersIntervalId) {
            clearInterval(onlineUsersIntervalId);
            onlineUsersIntervalId = null;
            console.log('Interval cleared on modal close');
        }
        if (userModalInstance) {
            userModalInstance.dispose();
            userModalInstance = null;
            console.log('Modal instance disposed on close');
        }
        if (modalBody) {
            modalBody.innerHTML = '';
            previousOnlineUsers = [];
        }
        document.querySelectorAll('.modal-backdrop').forEach(backdrop => backdrop.remove());
        document.body.classList.remove('modal-open');
        document.body.style.overflow = '';
    }, { once: true });
}, 300);

document.getElementById('user_online')?.addEventListener('click', debouncedClickOnlineUsers);

document.getElementById('todayOrdersCard')?.addEventListener('click', async () => {
    const orderModalElement = document.getElementById('orderModal');
    if (!orderModalElement) return;
    const orderModal = new bootstrap.Modal(orderModalElement);
    orderModal.show();
    await fetchAndDisplayOrdersToday();
});

document.getElementById('pendingOrdersCard')?.addEventListener('click', async () => {
    const pendingModalElement = document.getElementById('pendingOrdersModal');
    if (!pendingModalElement) return;
    const pendingModal = new bootstrap.Modal(pendingModalElement);
    pendingModal.show();
    await fetchPendingOrders();
});

document.addEventListener('hidden.bs.modal', () => {
    console.log('Cleaning up modal backdrops');
    document.body.classList.remove('modal-open');
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => backdrop.remove());
});

fetch('../Style_Sidebar/Sidebar.html')
    .then(res => res.text())
    .then(data => {
        const sidebarContainer = document.getElementById('sidebar-container');
        if (sidebarContainer) {
            sidebarContainer.innerHTML = sanitizeHtml(data);
            const dangxuat = document.getElementById('dangxuat');
            if (dangxuat) {
                dangxuat.addEventListener('click', () => {
                    if (confirm('Bạn có chắc chắn muốn đăng xuất không?')) {
                        window.location.href = '../Web_TrangChu/TrangChu.html';
                    }
                });
            }
        }
    })
    .catch(error => console.error('Error loading sidebar:', error));

async function refreshDashboard() {
    try {
        await debouncedFetchStagnantProducts(currentStagnantPage);
        await Promise.all([
            fetchOnlineCount(),
            fetchTodayStatistics(),
            fetchLowStockProducts(),
            fetchPendingOrders()
        ]);
    } catch (error) {
        console.error('Error refreshing dashboard:', error);
        Toastify({
            text: "Lỗi làm mới dashboard: " + error.message,
            duration: 5000,
            gravity: "top",
            position: "right",
            backgroundColor: "#dc3545"
        }).showToast();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM fully loaded, initializing dashboard');
    if (!localStorage.getItem('stagnantDays')) localStorage.setItem('stagnantDays', 7);
    if (!localStorage.getItem('stagnantSoldLimit')) localStorage.setItem('stagnantSoldLimit', 50);
    refreshDashboard();
});