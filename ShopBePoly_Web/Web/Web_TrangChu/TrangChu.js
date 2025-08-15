const API_BASE = `http://${config.host}:${config.port}/api`;
const UPLOADS_BASE = `http://${config.host}:${config.port}/uploads`;

let previousLowStockData = [];
let previousStagnantData = [];
let previousOnlineUsers = [];
let todayOrdersData = [];
let pendingOrdersData = [];
let previousTodayStats = { totalOrders: 0, totalRevenue: 0 };

function setTextIfExists(id, text) {
  const elem = document.getElementById(id);
  if (elem) elem.innerText = text;
}

function formatCurrency(value) {
  return value ? Number(value).toLocaleString('vi-VN') + ' ₫' : '0 ₫';
}

async function fetchOnlineCount() {
  try {
    const res = await fetch(`${API_BASE}/users_online`);
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const data = await res.json();
    setTextIfExists('user_online', data.online ?? '0');
  } catch (error) {
    console.error('Lỗi lấy số user online:', error);
  }
}

async function fetchAndDisplayOnlineUsers() {
  const modalBody = document.querySelector('#userModal .modal-body');
  if (!modalBody) return;

  try {
    const res = await fetch(`${API_BASE}/list/users_online`);
    const data = await res.json();
    const users = data.users || [];

    if (JSON.stringify(users) === JSON.stringify(previousOnlineUsers)) return;
    previousOnlineUsers = users;

    let html = '';
    if (!users.length) {
      html = '<tr><td colspan="5" class="text-center">Không có người dùng online</td></tr>';
    } else {
      html = users.map(user => `
                <tr>
                    <td>${user.username || '---'}</td>
                    <td>${user.name || '---'}</td>
                    <td>${user.email || '---'}</td>
                    <td>${user.phone_number || '---'}</td>
                    <td>
                        <img src="${UPLOADS_BASE}/${user.avt_user || 'default.png'}" width="50" height="50" style="object-fit:cover;" 
                        onerror="this.src='https://via.placeholder.com/50'"/>
                    </td>
                </tr>
            `).join('');
    }

    modalBody.innerHTML = `
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
        `;

  } catch (err) {
    console.error('Lỗi khi lấy danh sách người dùng online:', err);
    modalBody.innerHTML = '<p class="text-center text-danger">Lỗi tải dữ liệu.</p>';
  }
}

async function fetchTodayStatistics() {
  try {
    const res = await fetch(`${API_BASE}/statistics-today`);
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const data = await res.json();

    if (previousTodayStats.totalOrders === data.totalOrders && previousTodayStats.totalRevenue === data.totalRevenue) return;
    previousTodayStats = { totalOrders: data.totalOrders, totalRevenue: data.totalRevenue };

    setTextIfExists("todayOrdersCount", data.totalOrders ?? 0);
    setTextIfExists("todayRevenue", formatCurrency(data.totalRevenue));
  } catch (error) {
    console.error("Lỗi lấy doanh thu hôm nay:", error);
  }
}

async function fetchLowStockProducts() {
  const tableBody = document.getElementById('lowStockTable');
  if (!tableBody) return;

  try {
    const res = await fetch(`${API_BASE}/products/low-stock?threshold=20`);
    const data = await res.json();

    if (JSON.stringify(data.data) === JSON.stringify(previousLowStockData)) return;
    previousLowStockData = data.data;

    tableBody.innerHTML = '';
    if (!data.data?.length) {
      tableBody.innerHTML = '<tr><td colspan="8" class="text-center">Không có sản phẩm gần hết hàng</td></tr>';
      return;
    }

    data.data.forEach(item => {
      const lowStockVariations = item.variations.filter(v => v.stock <= 20);
      if (!lowStockVariations.length) return;

      const sizesHtml = lowStockVariations.map(v => v.size).join('<br>');
      const stockHtml = lowStockVariations.map(v => v.stock).join('<br>');
      const colorHtml = item.color ?? '-';

      tableBody.innerHTML += `
                <tr class="table-danger">
                    <td>${item.name}</td>
                    <td>${item.category}</td>
                    <td>${colorHtml}</td>
                    <td>${item.price?.toLocaleString('vi-VN') ?? 0} ₫</td>
                    <td>${item.sale_price ? item.sale_price.toLocaleString('vi-VN') + ' ₫' : '-'}</td>
                    <td>${sizesHtml}</td>
                    <td>${stockHtml}</td>
                    <td>
                        <img src="${UPLOADS_BASE}/${item.avt_imgproduct}" width="100" height="100" style="object-fit:cover;">
                    </td>
                </tr>
            `;
    });

  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm gần hết hàng:', error);
    tableBody.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Lỗi tải dữ liệu</td></tr>';
  }
}

async function fetchStagnantProducts() {
  const tableBody = document.getElementById('stagnantTable');
  if (!tableBody) return;

  try {
    const res = await fetch(`${API_BASE}/products/stagnant`);
    const data = await res.json();

    if (JSON.stringify(data.data) === JSON.stringify(previousStagnantData)) return;
    previousStagnantData = data.data;

    tableBody.innerHTML = '';
    if (!data?.data?.length) {
      tableBody.innerHTML = '<tr><td colspan="8" class="text-center">Không có sản phẩm tồn kho</td></tr>';
      return;
    }

    data.data.forEach(product => {
      const variationsHtml = product.variations.map(v =>
        `Size: ${v.size}, Tồn: ${v.stock}, Đã bán: ${v.sold} <br>
                 <img src="${UPLOADS_BASE}/${v.image}" width="50" height="50" style="object-fit:cover;">`
      ).join('<hr>');

      tableBody.innerHTML += `
                <tr class="table-warning">
                    <td>${product.name}</td>
                    <td>${product.category}</td>
                    <td>${product.color ?? '-'}</td>
                    <td>${product.price?.toLocaleString('vi-VN') ?? 0} ₫</td>
                    <td>${product.sale_price ? product.sale_price.toLocaleString('vi-VN') + ' ₫' : '-'}</td>
                    <td>${variationsHtml}</td>
                    <td>${product.totalStock}</td>
                    <td><img src="${UPLOADS_BASE}/${product.avt_imgproduct}" width="100" height="100" style="object-fit:cover;"></td>
                </tr>
            `;
    });

  } catch (error) {
    console.error('Lỗi khi lấy danh sách sản phẩm tồn kho:', error);
    tableBody.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Lỗi tải dữ liệu</td></tr>';
  }
}

function displayOrderDetail(order) {
  const modalBody = document.querySelector('#orderModal .modal-body');
  const modalTitle = document.querySelector('#orderModal .modal-title');
  modalTitle.innerText = `Chi tiết đơn hàng: ${order.id_order || order._id}`;

  const productHtml = Array.isArray(order.products) ? order.products.map(product => {
    const productData = product.id_product || {};
    const color = product.color || '';
    const category = productData.id_category?.title || 'Không có thể loại';
    let imageUrl = productData.avt_imgproduct ? `${UPLOADS_BASE}/${productData.avt_imgproduct}` : 'https://via.placeholder.com/90';

    return `
            <div class="d-flex align-items-center mb-3">
                <img src="${imageUrl}" onerror="this.src='https://via.placeholder.com/90'" class="img-thumbnail" style="width: 60px; height: 60px; object-fit: cover; margin-right: 15px;">
                <div>
                    <h6 class="mb-0 text-gray-800">${productData.nameproduct || '---'}</h6>
                    <small class="text-muted d-block">Thể loại: ${category}</small>
                    <small class="text-muted d-block">Màu: ${color || '---'} - Size: ${product.size || '---'} - Số lượng: ${product.quantity || 0}</small>
                </div>
            </div>
            <hr>
        `;
  }).join('') : '<p class="text-gray-500">Không có sản phẩm</p>';

  const cancelReasonHtml = order.status === 'Đã hủy' && order.cancelReason
    ? `<p class="text-danger"><strong>Lý do hủy:</strong> ${order.cancelReason || 'Không có lý do cụ thể'}</p>` : '';

  modalBody.innerHTML = `
        <div class="row">
            <div class="col-lg-6">
                <h5>Thông tin đơn hàng</h5>
                <p><strong>Mã đơn hàng:</strong> ${order.id_order || order._id}</p>
                <p><strong>Người đặt:</strong> ${order.id_user?.name || '---'}</p>
                <p><strong>SĐT:</strong> ${order.id_user?.phone_number || '---'}</p>
                <p><strong>Địa chỉ:</strong> ${order.address || 'Không có'}</p>
                <p><strong>Ngày đặt:</strong> ${order.date ? new Date(order.date).toLocaleDateString('vi-VN') : 'Không xác định'}</p>
                <p><strong>Thời gian đặt:</strong> ${order.date ? new Date(order.date).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'Không xác định'}</p>
                <p><strong>Thanh toán:</strong> ${order.pay || 'Không xác định'}</p>
                <p><strong>Tổng tiền:</strong> ${Number(order.total || 0).toLocaleString('vi-VN')} ₫</p>
                <p><strong>Trạng thái:</strong> ${order.status || 'Không xác định'}</p>
                ${cancelReasonHtml}
            </div>
            <div class="col-lg-6">
                <h5 class="font-weight-bold text-gray-800">Sản phẩm:</h5>
                <div class="products-list-container">
                    ${productHtml}
                </div>
            </div>
        </div>
        <button class="btn btn-secondary mt-3" id="backToOrdersListBtn">Quay lại danh sách</button>
    `;

  document.getElementById('backToOrdersListBtn').addEventListener('click', () => {
    fetchAndDisplayOrdersToday();
  });
}

async function fetchAndDisplayOrdersToday() {
  const modalBody = document.querySelector('#orderModal .modal-body');
  const modalTitle = document.querySelector('#orderModal .modal-title');
  modalBody.innerHTML = '<div class="text-center"><div class="spinner-border text-primary" role="status"></div></div>';
  modalTitle.innerText = 'Danh sách đơn hàng hôm nay';

  try {
    const res = await fetch(`${API_BASE}/list_order`);
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const allOrders = await res.json();

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    todayOrdersData = allOrders.filter(order => new Date(order.date) >= today);

    let ordersTableHtml = '';
    if (!todayOrdersData.length) {
      ordersTableHtml = '<tr><td colspan="7" class="text-center">Không có đơn hàng hôm nay</td></tr>';
    } else {
      ordersTableHtml = todayOrdersData.map(order => `
                <tr>
                    <td>${order.id_order || order._id}</td>
                    <td>${order.id_user?.name ?? 'N/A'}</td>
                    <td>${Number(order.total ?? 0).toLocaleString('vi-VN')} ₫</td>
                    <td>${order.status ?? '-'}</td>
                    <td>${order.date ? new Date(order.date).toLocaleTimeString('vi-VN') : '-'}</td>
                    <td>${order.date ? new Date(order.date).toLocaleDateString('vi-VN') : '-'}</td>
                    <td>
                        <button class="btn btn-sm btn-primary view-detail-btn" data-order-id="${order.id_order || order._id}">Xem chi tiết</button>
                    </td>
                </tr>
            `).join('');
    }

    modalBody.innerHTML = `
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
                    <tbody>${ordersTableHtml}</tbody>
                </table>
            </div>
        `;

    document.querySelectorAll('.view-detail-btn').forEach(button => {
      button.addEventListener('click', () => {
        const orderId = button.dataset.orderId;
        const order = todayOrdersData.find(o => o.id_order === orderId);
        if (order) {
          displayOrderDetail(order);
        }
      });
    });

  } catch (error) {
    console.error('Lỗi khi lấy đơn hàng hôm nay:', error);
    modalBody.innerHTML = '<p class="text-center text-danger">Lỗi tải dữ liệu đơn hàng.</p>';
  }
}


async function fetchPendingOrders() {
  try {
    const res = await fetch(`${API_BASE}/orders/pending`);
    const data = await res.json();
    pendingOrdersData = data.orders || [];

    const pendingCountElem = document.getElementById('pendingOrders');
    if (pendingCountElem) pendingCountElem.innerText = pendingOrdersData.length;

    renderPendingOrdersTable();
  } catch (err) {
    console.error('Lỗi fetch pending orders:', err);
  }
}

function renderPendingOrdersTable() {
  const modalBody = document.querySelector('#pendingOrdersModal .modal-body');
  if (!modalBody) return;

  if (!pendingOrdersData.length) {
    modalBody.innerHTML = '<p>Không có đơn hàng chờ xác nhận.</p>';
    return;
  }

  let tableHtml = `
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
            <tbody>
  `;

  pendingOrdersData.forEach(order => {
    const totalQuantity = order.products.reduce((acc, p) => acc + (p.quantity || 0), 0);

    tableHtml += `
            <tr>
                <td>${order.id_order}</td>
                <td>${order.id_user?.name || 'N/A'}</td>
                <td>${totalQuantity}</td>
                <td>${order.date ? new Date(order.date).toLocaleString('vi-VN') : '-'}</td>
                <td>${Number(order.total || 0).toLocaleString('vi-VN')} ₫</td>
                <td>${order.status}</td>
                <td>
                    <button class="btn btn-sm btn-primary process-order-btn" data-order-id="${order.id_order}">Xử lý đơn hàng</button>
                </td>
            </tr>
        `;
  });

  tableHtml += `
            </tbody>
        </table>
    </div>
  `;

  modalBody.innerHTML = tableHtml;

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
  const pendingModal = new bootstrap.Modal(document.getElementById('pendingOrdersModal'));
  pendingModal.show();
  await fetchPendingOrders();
});

document.getElementById('user_online')?.addEventListener('click', () => {
  const userModal = new bootstrap.Modal(document.getElementById('userModal'));
  userModal.show();
  fetchAndDisplayOnlineUsers();

  const intervalId = setInterval(fetchAndDisplayOnlineUsers, 5000);
  document.getElementById('userModal').addEventListener('hidden.bs.modal', () => clearInterval(intervalId), { once: true });
});

document.getElementById('todayOrdersCard')?.addEventListener('click', async () => {
  const orderModal = new bootstrap.Modal(document.getElementById('orderModal'));
  orderModal.show();
  await fetchAndDisplayOrdersToday();
});

document.getElementById('pendingOrdersCard')?.addEventListener('click', async () => {
  const pendingModal = new bootstrap.Modal(document.getElementById('pendingOrdersModal'));
  pendingModal.show();
  await fetchPendingOrders();
});


document.addEventListener('hidden.bs.modal', function (event) {
  document.body.classList.remove('modal-open');
  const backdrops = document.querySelectorAll('.modal-backdrop');
  backdrops.forEach(backdrop => backdrop.remove());
});


fetch('../Style_Sidebar/Sidebar.html')
  .then(res => res.text())
  .then(data => {
    document.getElementById('sidebar-container').innerHTML = data;
    const dangxuat = document.getElementById('dangxuat');
    if (dangxuat) {
      dangxuat.addEventListener('click', () => {
        const confirmLogout = confirm('Bạn có chắc chắn muốn đăng xuất không?');
        if (confirmLogout) {
          window.location.href = '../Web_TrangChu/TrangChu.html';
        }
      });
    }
  });

async function refreshDashboard() {
  fetchOnlineCount();
  fetchTodayStatistics();
  fetchLowStockProducts();
  fetchStagnantProducts();
  fetchPendingOrders();
  fetch();
}

document.addEventListener('DOMContentLoaded', () => {
  refreshDashboard();
  setInterval(refreshDashboard, 5000);
});
document.addEventListener('hidden.bs.modal', function (event) {
  document.body.classList.remove('modal-open');
  const backdrops = document.querySelectorAll('.modal-backdrop');
  backdrops.forEach(backdrop => backdrop.remove());
});