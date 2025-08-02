const host = window.config;

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

async function fetchOnlineCount() {
    try {
        const res = await fetch(`http://${config.host}:${config.port}/api/users_online`);
        if (!res.ok) {
            throw new Error(`HTTP error! Status: ${res.status}`);
        }
        const data = await res.json();
        if (data.online === undefined) {
            console.warn('Invalid data format, online count missing');
            document.getElementById('user_online').innerText = 'N/A';
        } else {
            document.getElementById('user_online').innerText = data.online;
        }
    } catch (error) {
        console.error('Lỗi lấy số user online:', error.message);
        document.getElementById('user_online').innerText = 'N/A';
    }
}

async function fetchProcessingOrderCount() {
  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/getStatusOder`);
      if (!res.ok) throw new Error(`HTTP error! Status: ${res.status}`);
      const data = await res.json();
      document.getElementById('getStatusOder').innerText = data.count ?? 'N/A';
    } catch (error) {
      console.error('Lỗi lấy số đơn hàng đang xử lý:', error.message);
      document.getElementById('getStatusOder').innerText = 'N/A';
    }
  }

async function fetchTodayStatistics() {
  const today = new Date();
  const y = today.getFullYear();
  const m = `${today.getMonth() + 1}`.padStart(2, '0');
  const d = `${today.getDate()}`.padStart(2, '0');

  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/statistics?type=day&year=${y}&month=${m}&day=${d}`);
    const data = await res.json();

    const todayOrders = data.totalOrders || 0;
    const todayRevenue = data.totalRevenue || 0;

    // Gán vào các phần tử HTML tương ứng nếu có
    document.getElementById("todayOder").textContent = todayOrders;
    document.getElementById("todayReve").textContent = todayRevenue.toLocaleString('vi-VN') + ' ₫';
  } catch (err) {
    console.error("Lỗi khi lấy thống kê hôm nay:", err);
  }
}

function refreshDashboard() {
    fetchOnlineCount();
    fetchProcessingOrderCount();
    fetchTodayStatistics();
  }

  refreshDashboard();
  setInterval(refreshDashboard, 5000);

