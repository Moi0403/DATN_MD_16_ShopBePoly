
const role = localStorage.getItem('userRole');
  if (role !== '2') {
    const menuQLTK = document.getElementById('menu-ql-tk');
      if (menuQLTK) {
        menuQLTK.style.display = 'none';
      }
}

// Sidebar.js
document.addEventListener('DOMContentLoaded', () => {
    const sidebarContainer = document.getElementById('sidebar-container');
    if (sidebarContainer) {
        sidebarContainer.innerHTML = `
            <div class="sidebar">
                <div class="header-menu d-flex justify-content-between align-items-center px-3 py-2 border-bottom border-light">
                    <h5 class="mb-0 text-white">Quản trị</h5>
                </div>
                <ul class="list-unstyled sidebar-nav px-2 mt-3">
                    <li><a href="../Web_TrangChu/TrangChu.html" class="nav-link"><i class="bi bi-house me-2"></i>Trang Chủ</a></li>
                    <li class="nav-title">Quản trị</li>
                    <li class="nav-item"><a class="nav-link" href="../Web_Ql_Banner/QL_Banner.html"><i class="bi bi-image me-2"></i> Banner</a></li>
                    <li class="nav-item"><a class="nav-link" href="../Web_QL_Category/QL_Category.html"><i class="bi bi-tags me-2"></i> Thương hiệu</a></li>
                    <li class="nav-item"><a class="nav-link" href="../Web_QL_Product/QL_Product.html"><i class="bi bi-box-seam me-2"></i> Sản phẩm</a></li>
                    <li class="nav-item"><a class="nav-link" href="../Web_QL_User/QL_User.html"><i class="bi bi-people me-2"></i> Tài khoản</a></li>
                    <li class="nav-title">Đơn hàng</li>
                    <li class="nav-item"><a class="nav-link" href="../Web_XNDH/Web_XNDH.html"><i class="bi bi-clipboard-check me-2"></i>Xác nhận đơn</a></li>
                    <li class="nav-item"><a class="nav-link" href="../Web_GH/Web_GH.html"><i class="bi bi-truck me-2"></i>Giao hàng</a></li>
                    <li class="nav-item"><a class="nav-link" href="../Web_GHTC/Web_GHTC.html"><i class="bi bi-check2-circle me-2"></i>Đã giao</a></li>
                    <li class="nav-item"><a class="nav-link" href="../Web_DH/Web_DH.html"><i class="bi bi-x-circle me-2"></i>Đơn hủy</a></li>
                    <li class="nav-title">Thống kê</li>
                    <li><a class="nav-link" href="../Web_TK/QL_ThongKe.html"><i class="bi bi-bar-chart me-2"></i>Doanh thu</a></li>
                    <li class="nav-title">Khác</li>
                    <li><a class="nav-link" href="#" onclick="DangXuat()"><i class="bi bi-box-arrow-right me-2"></i>Đăng xuất</a></li>
                </ul>
            </div>
        `;
    }

    const navLinks = document.querySelectorAll('.sidebar-nav a');
    function setActiveLink(activeLink) {
        navLinks.forEach(link => link.classList.remove('active'));
        activeLink.classList.add('active');
    }

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const href = link.getAttribute('href');
            if (href && href !== '#') {
                setActiveLink(link);
                // Có thể thêm logic tải nội dung nếu cần
            }
        });
    });

    const currentPath = window.location.pathname.split('/').pop();
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href && href.includes(currentPath)) {
            setActiveLink(link);
        }
    });
});


function DangXuat() {
  const confirmLogout = confirm('Bạn có chắc chắn muốn đăng xuất không?');
    if (confirmLogout) {
      window.location.href = '../Web_DangNhap/Web_DangNhap.html';
    }
}
