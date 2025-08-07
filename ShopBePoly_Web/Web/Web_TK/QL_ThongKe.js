const API_BASE = `http://${config.host}:${config.port}/api`;
const UPLOADS_BASE = `http://${config.host}:${config.port}/Uploads`;
const chartURL = `${API_BASE}/statistics`;
const overviewURL = `${API_BASE}/statistics-overview`;
const ordersURL = `${API_BASE}/orders/by-range`;
const topProductsURL = `${API_BASE}/top-products`;

let myChart = null;

document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.getElementById("myAreaChart");
    if (!canvas) return showError("Không tìm thấy canvas biểu đồ.");

    const end = getTodayInVietnam();
    const start = new Date(end);
    start.setDate(end.getDate() - 29);
    const formatDate = date => date.toISOString().split("T")[0];

    document.getElementById("startDate").value = formatDate(start);
    document.getElementById("endDate").value = formatDate(end);

    document.getElementById("updateChartBtn")?.addEventListener("click", () => {
        if (validateDates()) {
            updateRevenueChart();
            fetchOverview();
            fetchOrderList();
            fetchTopProducts();
        }
    });

    document.getElementById("totalOrdersCard")?.addEventListener("click", () => {
        const orderModal = new bootstrap.Modal(document.getElementById("orderModal"));
        orderModal.show();
        fetchOrderList();
    });

    document.getElementById("closeModalBtn")?.addEventListener("click", () => {
        const orderModal = bootstrap.Modal.getInstance(document.getElementById("orderModal"));
        orderModal.hide();
    });

    document.addEventListener("click", function (e) {
        const orderModal = document.getElementById("orderModal");
        const modalContent = document.querySelector("#orderModal .modal-content");
        const bsModal = bootstrap.Modal.getInstance(orderModal);
        if (!bsModal || orderModal.classList.contains("hidden")) return;
        if (orderModal.contains(e.target) && !modalContent.contains(e.target)) {
            bsModal.hide();
        }
    });

    document.addEventListener("keydown", e => {
        const orderModal = document.getElementById("orderModal");
        const bsModal = bootstrap.Modal.getInstance(orderModal);
        if (e.key === "Escape" && bsModal) {
            bsModal.hide();
        }
    });

    updateRevenueChart();
    fetchOverview();
    fetchOrderList();
    fetchTopProducts();
});

document.getElementById("searchOrderBtn")?.addEventListener("click", () => {
    const keyword = document.getElementById("searchOrderInput").value.toLowerCase();
    document.querySelectorAll("#orderTableBody tr").forEach(row => {
        const text = row.textContent.toLowerCase();
        row.style.display = text.includes(keyword) ? "" : "none";
    });
});

function getTodayInVietnam() {
    const nowVN = new Date().toLocaleString("en-US", { timeZone: "Asia/Ho_Chi_Minh" });
    const [month, day, year] = nowVN.split(",")[0].split("/");
    return new Date(`${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`);
}

function validateDates() {
    const startDate = document.getElementById("startDate").value;
    const endDate = document.getElementById("endDate").value;

    if (!startDate || !endDate) return showError("Vui lòng chọn đầy đủ ngày.");

    if (new Date(startDate) > new Date(endDate))
        return showError("Ngày bắt đầu không thể sau ngày kết thúc.");

    const todayVN = getTodayInVietnam();
    if (new Date(endDate).setHours(0, 0, 0, 0) > todayVN.setHours(0, 0, 0, 0))
        return showError("Ngày kết thúc không thể sau ngày hiện tại.");

    return true;
}

function showError(message) {
    Toastify({
        text: message,
        duration: 3000,
        style: { background: "linear-gradient(to right, #ff5e62, #e74c3c)" },
        className: "toast-error",
    }).showToast();
}

async function updateRevenueChart() {
    const start = document.getElementById("startDate").value;
    const end = document.getElementById("endDate").value;

    try {
        const res = await fetch(`${chartURL}?start=${start}&end=${end}`);
        const data = await res.json();
        const labels = data.map(item => new Date(item.label).toLocaleDateString('vi-VN', { day: 'numeric', month: 'short' }));
        const revenues = data.map(item => item.revenue);
        renderSingleChart(labels, revenues);
    } catch (err) {
        console.error("Lỗi biểu đồ:", err);
        showError("Không thể tải biểu đồ.");
    }
}

function renderSingleChart(labels, data) {
    const ctx = document.getElementById("myAreaChart").getContext("2d");
    if (myChart) myChart.destroy();

    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(37, 99, 235, 0.5)');
    gradient.addColorStop(0.5, 'rgba(37, 99, 235, 0.25)');
    gradient.addColorStop(1, 'rgba(37, 99, 235, 0)');

    myChart = new Chart(ctx, {
        type: "line",
        data: {
            labels,
            datasets: [{
                label: "Doanh thu (₫)",
                data,
                borderColor: "#2563eb",
                backgroundColor: gradient,
                fill: true,
                tension: 0.4,
                pointRadius: 0,
                pointHitRadius: 10,
                pointHoverRadius: 6,
                pointBackgroundColor: "#2563eb",
                pointBorderColor: "#ffffff",
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "top",
                    labels: {
                        font: { size: 14, family: "Nunito" },
                        color: "#1f2937"
                    }
                },
                tooltip: {
                    backgroundColor: "#1f2937",
                    callbacks: {
                        label: ctx => `${ctx.dataset.label}: ${ctx.parsed.y.toLocaleString('vi-VN')} ₫`,
                        title: ctx => new Date(ctx[0].label).toLocaleDateString('vi-VN', {
                            day: 'numeric', month: 'long', year: 'numeric'
                        })
                    }
                }
            },
            scales: {
                x: {
                    ticks: { font: { size: 12, family: "Nunito" }, color: "#4b5563" },
                    grid: { display: false }
                },
            y: {
                beginAtZero: true,
                ticks: {
                    font: { size: 12, family: "Nunito" },
                    color: "#4b5563",
                    callback: value => value.toLocaleString('vi-VN') + " ₫"
                },
                grid: { color: "#e5e7eb", borderDash: [5, 5] }
            }
        }
    }
});
}

async function fetchOverview() {
    const start = document.getElementById("startDate").value;
    const end = document.getElementById("endDate").value;

    try {
        const res = await fetch(`${overviewURL}?startDate=${start}&endDate=${end}`);
        const data = await res.json();

        const toCurrency = v => v.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });

        document.getElementById("totalRevenue").textContent = toCurrency(data.totalRevenue || 0);
        document.getElementById("totalOrders").textContent = data.totalOrders || 0;
        document.getElementById("totalUsers").textContent = data.totalUsers || 0;
        document.getElementById("totalProducts").textContent = data.totalProducts || 0;
    } catch (err) {
        console.error("Lỗi overview:", err);
        showError("Không thể tải thống kê tổng quan.");
    }
}

async function fetchOrderList() {
    const startDate = document.getElementById("startDate").value;
    const endDate = document.getElementById("endDate").value;

    try {
        const res = await fetch(`${ordersURL}?start=${startDate}&end=${endDate}`);
        const orders = await res.json();
        const tbody = document.getElementById("orderTableBody");
        tbody.innerHTML = orders.length > 0 ? orders.map(order => `
            <tr>
                <td class="px-4 py-2">${order.id_order || order._id}</td>
                <td class="px-4 py-2">${Number(order.total).toLocaleString('vi-VN')} ₫</td>
                <td class="px-4 py-2">${order.status}</td>
                <td class="px-4 py-2">${new Date(order.date).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}</td>
                <td class="px-4 py-2">${new Date(order.date).toLocaleDateString('vi-VN')}</td>
                <td class="px-4 py-2">
                    <button class="text-blue-600 hover:underline detail-btn" data-id="${order._id}">Chi tiết</button>
                </td>
            </tr>
        `).join("") : "<tr><td colspan='6' class='text-center'>Không có đơn hàng</td></tr>";

        document.querySelectorAll(".detail-btn").forEach(btn => {
            btn.addEventListener("click", () => showOrderDetails(btn.dataset.id));
        });

        const formattedStartDate = new Date(startDate).toLocaleDateString('vi-VN');
        const formattedEndDate = new Date(endDate).toLocaleDateString('vi-VN');
        document.getElementById("modalDate").textContent = startDate === endDate ? formattedStartDate : `${formattedStartDate} đến ${formattedEndDate}`;
    } catch (err) {
        console.error("Lỗi danh sách đơn:", err);
        showError("Không thể tải danh sách đơn hàng.");
    }
}

async function fetchTopProducts() {
    const start = document.getElementById("startDate").value;
    const end = document.getElementById("endDate").value;

    try {
        const res = await fetch(`${topProductsURL}?start=${start}&end=${end}`);
        const products = await res.json();
        const topProductsList = document.getElementById("topProductsList");
        topProductsList.innerHTML = products.length > 0 ? products.map((product, index) => `
            <li class="flex items-center">
                <span class="inline-flex items-center justify-center w-6 h-6 mr-3 text-sm font-semibold text-white bg-blue-500 rounded-full">${index + 1}</span>
                <img src="${product.image ? `${UPLOADS_BASE}/${product.image}` : 'https://via.placeholder.com/50'}" 
                     class="w-12 h-12 object-cover rounded mr-3" 
                     onerror="this.src='https://via.placeholder.com/50'">
                <div>
                    <p class="font-semibold text-gray-800">${product.name || 'Không xác định'}</p>
                    <p class="text-sm text-gray-500">Số lượng bán: ${product.totalQuantity}</p>
                </div>
            </li>
        `).join("") : "<li class='text-center text-gray-500'>Không có sản phẩm nào</li>";
    } catch (err) {
        console.error("Lỗi top sản phẩm:", err);
        showError("Không thể tải danh sách top sản phẩm.");
    }
}

async function showOrderDetails(orderId) {
    try {
        const res = await fetch(`${API_BASE}/list_order`);
        const data = await res.json();
        const order = data.find(o => o._id === orderId);
        if (!order) {
            showError("Không tìm thấy đơn hàng.");
            return;
        }

        const { id_user, products, total, address, date, pay, status } = order;
        const productHtml = Array.isArray(products) ? products.map(product => {
            const productData = product.id_product || {};
            const color = product.color || '';
            const category = productData.id_category?.title || 'Không có thể loại';
            let imageUrl = '';

            if (productData.variations && Array.isArray(productData.variations)) {
                const matchedVariation = productData.variations.find(v => {
                    const colorName = v.color?.name?.toLowerCase();
                    return colorName === color.toLowerCase();
                });
                if (matchedVariation?.image) {
                    imageUrl = `${UPLOADS_BASE}/${matchedVariation.image}`;
                }
            }

            if (!imageUrl && productData.avt_imgproduct) {
                imageUrl = `${UPLOADS_BASE}/${productData.avt_imgproduct}`;
            }

            if (!imageUrl) {
                imageUrl = 'https://via.placeholder.com/90';
            }

            return `
                <div class="flex items-center mb-4">
                    <img src="${imageUrl}" onerror="this.src='https://via.placeholder.com/90'" class="w-16 h-16 object-cover rounded mr-3">
                    <div>
                        <p class="font-semibold text-gray-800">${productData.nameproduct || '---'}</p>
                        <p class="text-sm text-gray-500">Thể loại: ${category}</p>
                        <p class="text-sm text-gray-500">Màu: ${color} - Size: ${product.size || ''} - SL: ${product.quantity || 0}</p>
                    </div>
                </div>
            `;
        }).join('') : '<p class="text-gray-500">Không có sản phẩm</p>';

        const bodyHtml = `
            <p><strong>Mã đơn hàng:</strong> ${order.id_order || order._id}</p>
            <p><strong>Người đặt:</strong> ${id_user?.name || '---'}</p>
            <p><strong>SĐT:</strong> ${id_user?.phone_number || ''}</p>
            <p><strong>Địa chỉ:</strong> ${address || 'Không có'}</p>
            <p><strong>Ngày đặt:</strong> ${date ? new Date(date).toLocaleDateString('vi-VN') : 'Không xác định'}</p>
            <p><strong>Thời gian đặt:</strong> ${date ? new Date(date).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'Không xác định'}</p>
            <p><strong>Thanh toán:</strong> ${pay || 'Không xác định'}</p>
            <p><strong>Tổng tiền:</strong> ${Number(total || 0).toLocaleString('vi-VN')} ₫</p>
            <p><strong>Trạng thái:</strong> ${status || 'Không xác định'}</p>
            <hr class="my-4">
            <h6 class="font-bold text-gray-800">Sản phẩm:</h6>
            ${productHtml}
        `;

        document.getElementById('order-detail-body').innerHTML = bodyHtml;
        const modal = new bootstrap.Modal(document.getElementById("orderDetailModal"));
        modal.show();
    } catch (err) {
        console.error("Lỗi chi tiết đơn:", err);
        showError("Không thể tải chi tiết đơn hàng.");
    }
}