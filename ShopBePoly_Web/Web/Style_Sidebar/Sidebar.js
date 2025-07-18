function toggleSidebar() {
  const sidebar = document.getElementById("sidebar");
  const overlay = document.getElementById("overlay");
  const body = document.body;

  sidebar.classList.toggle("active");
  overlay.classList.toggle("active");
  body.classList.toggle("sidebar-open"); // thêm class để điều chỉnh layout
}
const role = localStorage.getItem('userRole');
  if (role !== '2') {
    const menuQLTK = document.getElementById('menu-ql-tk');
      if (menuQLTK) {
        menuQLTK.style.display = 'none';
      }
}

document.addEventListener('DOMContentLoaded', () => {
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


function toggleDropdown(event) {
  event.preventDefault();
  const parent = event.currentTarget.parentElement;
  parent.classList.toggle('open');
}

