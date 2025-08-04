const API_BASE = `http://${config.host}:${config.port}/api`;
const UPLOADS_BASE = `http://${config.host}:${config.port}/Uploads`; // Đường dẫn cho hình ảnh
const chartURL = `${API_BASE}/statistics`;
const overviewURL = `${API_BASE}/statistics-overview`;
const ordersURL = `${API_BASE}/orders/by-range`;

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
                        title: ctx => new Date(ctx[0].raw.label).toLocaleDateString('vi-VN', {
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
                <td class="px-4 py-2">${order._id}</td>
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

        // Định dạng ngày cho modalDate
        const formattedStartDate = new Date(startDate).toLocaleDateString('vi-VN');
        const formattedEndDate = new Date(endDate).toLocaleDateString('vi-VN');
        document.getElementById("modalDate").textContent = `${formattedStartDate} đến ${formattedEndDate}`;
    } catch (err) {
        console.error("Lỗi danh sách đơn:", err);
        showError("Không thể tải danh sách đơn hàng.");
    }
}

async function showOrderDetails(orderId) {
    try {
        const res = await fetch(`${API_BASE}/list_order`);
        const data = await res.json();
        console.log('API response:', data); // Debug: Log toàn bộ dữ liệu API

        const order = data.find(o => o._id === orderId);
        if (!order) {
            showError("Không tìm thấy đơn hàng.");
            return;
        }
        console.log('Selected order:', order); // Debug: Log đơn hàng được chọn

        const { id_user, products, total, address, date, pay, status } = order;
        console.log('Products:', products); // Debug: Log danh sách sản phẩm

        const productHtml = Array.isArray(products) ? products.map(product => {
            const productData = product.id_product || {};
            console.log('Product data:', productData); // Debug: Log dữ liệu sản phẩm
            const color = product.color || '';
            const category = productData.id_category?.title || 'Không có thể loại';
            let imageUrl = '';

            // Kiểm tra variations và tìm ảnh theo màu
            if (productData.variations && Array.isArray(productData.variations)) {
                const matchedVariation = productData.variations.find(v => {
                    const colorName = v.color?.name?.toLowerCase();
                    console.log('Checking variation:', v, 'color:', color, 'colorName:', colorName); // Debug
                    return colorName === color.toLowerCase();
                });
                if (matchedVariation?.image) {
                    imageUrl = `${UPLOADS_BASE}/${matchedVariation.image}`;
                    console.log('Variation image URL:', imageUrl); // Debug
                }
            }

            // Fallback về avt_imgproduct nếu không tìm thấy ảnh trong variations
            if (!imageUrl && productData.avt_imgproduct) {
                imageUrl = `${UPLOADS_BASE}/${productData.avt_imgproduct}`;
                console.log('Avatar image URL:', imageUrl); // Debug
            }

            // Fallback về placeholder nếu không có ảnh
            if (!imageUrl) {
                imageUrl = 'https://via.placeholder.com/90';
                console.log('Using placeholder image'); // Debug
            }

            // Thêm sự kiện onerror để xử lý lỗi tải ảnh
            return `
                <div style="display: flex; align-items: center; margin-bottom: 10px;">
                    <img src="${imageUrl}" onerror="this.src='https://via.placeholder.com/90'; console.log('Image load failed:', '${imageUrl}')" style="width: 90px; height: 90px; object-fit: cover; border-radius: 6px; margin-right: 10px;">
                    <div>
                        <strong>${productData.nameproduct || '---'}</strong><br>
                        <small>Thể loại: ${category}</small><br>
                        <small>Màu: ${color} - Size: ${product.size || ''} - SL: ${product.quantity || 0}</small>
                    </div>
                </div>
            `;
        }).join('') : '<p>Không có sản phẩm</p>';

        const bodyHtml = `
            <p><strong>Người đặt:</strong> ${id_user?.name || '---'}</p>
            <p><strong>SĐT:</strong> ${id_user?.phone_number || ''}</p>
            <p><strong>Địa chỉ:</strong> ${address || 'Không có'}</p>
            <p><strong>Ngày đặt:</strong> ${date ? new Date(date).toLocaleDateString('vi-VN') : 'Không xác định'}</p>
            <p><strong>Thời gian đặt:</strong> ${date ? new Date(date).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'Không xác định'}</p>
            <p><strong>Thanh toán:</strong> ${pay || 'Không xác định'}</p>
            <p><strong>Tổng tiền:</strong> ${Number(total || 0).toLocaleString('vi-VN')} ₫</p>
            <p><strong>Trạng thái:</strong> ${status || 'Không xác định'}</p>
            <hr>
            <h6>Sản phẩm:</h6>
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