const API_BASE = `http://${config.host}:${config.port}/api`;
const serverURL = `${API_BASE}/statistics`;

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

let charts = { day: null, month: null, year: null };

async function fetchStatistics(type) {
  const today = new Date();
  const year = today.getFullYear();

  if (type === 'day') {
    const y = today.getFullYear();
    const m = `${today.getMonth() + 1}`.padStart(2, '0');
    const d = `${today.getDate()}`.padStart(2, '0');

    const res = await fetch(`${serverURL}?type=day&year=${y}&month=${m}&day=${d}`);
    const data = await res.json();

    const results = [{
      label: `${d}/${m}/${y}`,
      revenue: data.totalRevenue || 0,
      orders: data.totalOrders || 0
    }];

    renderChart('chartDay', results.map(r => r.label), results.map(r => r.revenue), 'Doanh thu hôm nay', 'green', 'day');
    renderTable('dayTable', results);
    return;
  }

  if (type === 'month') {
    const months = [...Array(12).keys()].map(i => i + 1);
    const results = await Promise.all(months.map(async m => {
      const monthStr = `${m}`.padStart(2, '0');
      const res = await fetch(`${serverURL}?type=month&year=${year}&month=${monthStr}`);
      const data = await res.json();
      return {
        label: `Tháng ${m}`,
        revenue: data.totalRevenue || 0,
        orders: data.totalOrders || 0
      };
    }));
    renderChart('chartMonth', results.map(r => r.label), results.map(r => r.revenue), `Doanh thu theo tháng - ${year}`, 'blue', 'month');
    renderTable('monthTable', results);
    return;
  }

  if (type === 'year') {
    const years = [year - 2, year - 1, year, year + 1];
    const results = await Promise.all(years.map(async y => {
      const res = await fetch(`${serverURL}?type=year&year=${y}`);
      const data = await res.json();
      return {
        label: `Năm ${y}`,
        revenue: data.totalRevenue || 0,
        orders: data.totalOrders || 0
      };
    }));
    renderChart('chartYear', results.map(r => r.label), results.map(r => r.revenue), 'Doanh thu theo năm', 'black', 'year');
    renderTable('yearTable', results);
    return;
  }
}

function renderChart(canvasId, labels, data, title, color, type) {
  const ctx = document.getElementById(canvasId).getContext('2d');
  if (charts[type]) charts[type].destroy();

  charts[type] = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: title,
        data,
        backgroundColor: color,
        borderRadius: 5
      }]
    },
    options: {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: value => value.toLocaleString('vi-VN') + ' ₫'
          }
        }
      },
      plugins: {
        tooltip: {
          callbacks: {
            label: context =>
              context.dataset.label + ': ' +
              context.parsed.y.toLocaleString('vi-VN') + ' ₫'
          }
        }
      }
    }
  });
}

function renderTable(tableId, data) {
  const table = document.getElementById(tableId);
  table.innerHTML = `
    <thead class="table-light">
      <tr>
        <th>Thời gian</th>
        <th>Số đơn hàng</th>
        <th>Tổng doanh thu</th>
      </tr>
    </thead>
    <tbody>
      ${data.map(row => `
        <tr>
          <td>${row.label}</td>
          <td>${row.orders}</td>
          <td>${row.revenue.toLocaleString('vi-VN')} ₫</td>
        </tr>`).join('')}
    </tbody>
  `;
}

async function fetchTopProducts() {
  try {
    const response = await fetch(`${API_BASE}/top-products`);
    const topProducts = await response.json();

    const container = document.getElementById("topProducts");
    container.innerHTML = "";

    topProducts.forEach(product => {
      const productCard = `
        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100 shadow-sm">
            <img src="${API_BASE.replace('/api', '')}/uploads/${product.image}" 
                 class="card-img-top" 
                 style="height: 180px; object-fit: cover;" 
                 alt="${product.name}">
            <div class="card-body">
              <h6 class="card-title text-truncate">${product.name}</h6>
              <p class="card-text">Số lượng đã bán: <strong>${product.totalQuantity}</strong></p>
            </div>
          </div>
        </div>
      `;
      container.innerHTML += productCard;
    });
  } catch (error) {
    console.error("Lỗi khi lấy top sản phẩm:", error);
  }
}

async function fetchOverview() {
  try {
    const start = document.getElementById("startOverview")?.value;
    const end = document.getElementById("endOverview")?.value;

    let url = `${API_BASE}/statistics/overview`;
    if (start || end) {
      const params = new URLSearchParams();
      if (start) params.append("start", start);
      if (end) params.append("end", end);
      url += `?${params.toString()}`;
    }

    const response = await fetch(url);
    const data = await response.json();

    const formatCurrency = (number) =>
      number.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });

    document.getElementById("totalRevenue").textContent = formatCurrency(data.totalRevenue || 0);
    document.getElementById("totalUsers").textContent = data.totalUsers || 0;
    document.getElementById("totalOrders").textContent = data.totalOrders || 0;
    
    if (data.totalProducts !== undefined) {
      document.getElementById("totalProducts").textContent = data.totalProducts;
    }

  } catch (error) {
    console.error("Lỗi khi fetch overview:", error);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  // Load biểu đồ theo ngày, tháng, năm khi trang mở
  fetchStatistics("day");
  fetchStatistics("month");
  fetchStatistics("year");

  // Load tổng quan
  fetchOverview();

  // Load top sản phẩm
  fetchTopProducts();

  // Khi bấm nút "Thống kê" chỉ cập nhật lại thống kê tổng quan (không đổi biểu đồ)
  document.getElementById("filterOverviewBtn").addEventListener("click", () => {
    fetchOverview();
  });
});
