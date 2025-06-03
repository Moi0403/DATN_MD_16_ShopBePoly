function toggleSidebar() {
  const sidebar = document.getElementById("sidebar");
  const overlay = document.getElementById("overlay");
  const body = document.body;

  sidebar.classList.toggle("active");
  overlay.classList.toggle("active");
  body.classList.toggle("sidebar-open"); // thêm class để điều chỉnh layout
}
