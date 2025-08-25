document.addEventListener('DOMContentLoaded', () => {
  // Lấy dữ liệu người dùng từ localStorage
  let userData = {};
  try {
    const storedData = localStorage.getItem('userData');
    userData = storedData ? JSON.parse(storedData) : {};
  } catch (e) {
    console.error('Lỗi khi phân tích dữ liệu userData:', e);
    userData = {};
  }

  const timestamp = new Date().getTime();

  // Cập nhật tên người dùng
  const nameElement = document.getElementById('name');
  if (nameElement) {
    nameElement.textContent = userData.name || 'Họ và tên';
  }

  // Cập nhật ảnh đại diện
  const img = document.getElementById('avt');
  if (img) {
    img.src = userData.avatar && userData.avatar.trim() !== ''
      ? `http://localhost:3000/Uploads/${userData.avatar}?t=${timestamp}`
      : '../../Images/default-avatar.png';
    img.style.display = 'block';
    img.onerror = () => {
      img.src = '../../Images/default-avatar.png'; // Backup nếu ảnh không tải được
    };
  }

  const menuHome = document.getElementById('menu-home');
  const menuBanner = document.getElementById('menu-banner');
  const menuCategory = document.getElementById('menu-category');
  const menuProduct = document.getElementById('menu-product');
  const menuQlTk = document.getElementById('menu-ql-tk');
  const menuVoucher = document.getElementById('menu-voucher');
  const menuXndh = document.getElementById('menu-xndh');
  const menuGh = document.getElementById('menu-gh');
  const menuGhtc = document.getElementById('menu-ghtc');
  const menuDh = document.getElementById('menu-dh');
  const menuStats = document.getElementById('menu-stats');
  const menuTop10 = document.getElementById('menu-top10');
  const menuContact = document.getElementById('menu-contact');
  const quantri = document.getElementById('quantri');
  const donhang = document.getElementById('donhang');
  const thongke = document.getElementById('thongke');
  const lienhe = document.getElementById('lienhe');

  const hideElement = (element) => {
      if (element) element.style.display = 'none';
  }
    hideElement(menuHome);
    hideElement(menuBanner);
    hideElement(menuCategory);
    hideElement(menuProduct);
    hideElement(menuQlTk);
    hideElement(menuVoucher);
    hideElement(menuXndh);
    hideElement(menuGh);
    hideElement(menuGhtc);
    hideElement(menuDh);
    hideElement(menuStats);
    hideElement(menuTop10);
    hideElement(menuContact);
    hideElement(quantri);
    hideElement(donhang);
    hideElement(thongke);
    hideElement(lienhe);

  if (userData.role === 1){
      document.getElementById('menu-home').style.display = 'block';
      document.getElementById('quantri').style.display = 'block';
      document.getElementById('menu-banner').style.display = 'block';
      document.getElementById('menu-category').style.display = 'block';
      document.getElementById('menu-product').style.display = 'block';
      document.getElementById('menu-ql-tk').style.display = 'block';
      document.getElementById('menu-voucher').style.display = 'block';
      document.getElementById('donhang').style.display = 'block';
      document.getElementById('menu-xndh').style.display = 'block';
      document.getElementById('menu-gh').style.display = 'block';
      document.getElementById('menu-ghtc').style.display = 'block';
      document.getElementById('menu-dh').style.display = 'block';
      document.getElementById('lienhe').style.display = 'block';
      document.getElementById('menu-contact').style.display = 'block';
  } else if (userData.role === 2){
      document.getElementById('menu-home').style.display = 'block';
      document.getElementById('thongke').style.display = 'block';
      document.getElementById('menu-stats').style.display = 'block';
      document.getElementById('menu-top10').style.display = 'block';
  }

  // Xử lý trạng thái active cho link navigation
  const navLinks = document.querySelectorAll('.sidebar-nav a');
  function setActiveLink(activeLink) {
    navLinks.forEach(link => link.classList.remove('active'));
    if (activeLink) {
      activeLink.classList.add('active');
    }
  }

  navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
      const href = link.getAttribute('href');
      if (href && href !== '#') {
        setActiveLink(link);
        // Có thể thêm logic tải nội dung nếu cần
      }
    });
  });

  // Đặt active link dựa trên đường dẫn hiện tại
  const currentPath = window.location.pathname.split('/').pop();
  navLinks.forEach(link => {
    const href = link.getAttribute('href');
    if (href && href.includes(currentPath)) {
      setActiveLink(link);
    }
  });


 const avatar = document.getElementById('avt');
    const userModal = document.getElementById('userModal');
    const userModalDialog = userModal?.querySelector('.modal-dialog');

    if (avatar && userModal) {
        let userModalInstance = null;

        avatar.addEventListener('click', () => {
            // Tạo hoặc tái sử dụng instance modal
            if (!userModalInstance || userModalInstance._isShown === false) {
                userModalInstance = new bootstrap.Modal(userModal, { backdrop: false });
            }
            userModalInstance.show();

            const avatarRect = avatar.getBoundingClientRect();
            const sidebarRect = document.querySelector('.sidebar').getBoundingClientRect();

            userModalDialog.style.position = 'absolute';
            userModalDialog.style.top = `${avatarRect.top + window.scrollY}px`;
            userModalDialog.style.left = `${sidebarRect.right + 10}px`;
            userModalDialog.style.margin = '0';
            userModalDialog.style.zIndex = '1050';
        });

        userModal.addEventListener('show.bs.modal', () => {
            userModalDialog.style.display = 'block';
            // Xóa backdrop nếu tồn tại
            const backdrop = document.querySelector('.modal-backdrop');
            if (backdrop) backdrop.remove();
        });

        userModal.addEventListener('hidden.bs.modal', () => {
            userModalDialog.style.position = '';
            userModalDialog.style.top = '';
            userModalDialog.style.left = '';
            userModalDialog.style.margin = '';
            userModalDialog.style.zIndex = '';
            // Đảm bảo không còn backdrop
            const backdrop = document.querySelector('.modal-backdrop');
            if (backdrop) backdrop.remove();
        });
    }

    const changePasswordLink = userModal.querySelector('a[href="#"]:nth-child(2)');
    const changePasswordModal = document.getElementById('changePasswordModal');
    const changeModalDialog = changePasswordModal?.querySelector('.modal-dialog');

    if (changePasswordLink && changePasswordModal) {
        changePasswordLink.addEventListener('click', (e) => {
            e.preventDefault();
            const modalInstance = new bootstrap.Modal(changePasswordModal, { backdrop: false });
            modalInstance.show();

            const avatarRect = avatar.getBoundingClientRect();
            const sidebarRect = document.querySelector('.sidebar').getBoundingClientRect();

            changeModalDialog.style.position = 'absolute';
            changeModalDialog.style.top = `${avatarRect.top + window.scrollY}px`;
            changeModalDialog.style.left = `${sidebarRect.right + 10}px`;
            changeModalDialog.style.margin = '0';
            changeModalDialog.style.zIndex = '1050';
        });

        changePasswordModal.addEventListener('show.bs.modal', () => {
            changeModalDialog.style.display = 'block';
            const backdrop = document.querySelector('.modal-backdrop');
            if (backdrop) backdrop.remove();
        });

        changePasswordModal.addEventListener('hidden.bs.modal', () => {
            changeModalDialog.style.position = '';
            changeModalDialog.style.top = '';
            changeModalDialog.style.left = '';
            changeModalDialog.style.margin = '';
            changeModalDialog.style.zIndex = '';
            const backdrop = document.querySelector('.modal-backdrop');
            if (backdrop) backdrop.remove();
        });
    }

    const changePasswordForm = document.getElementById('changePasswordForm');
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (newPassword !== confirmPassword) {
                alert('Mật khẩu mới và xác nhận mật khẩu không khớp!');
                return;
            }

            console.log('Đổi mật khẩu:', { currentPassword, newPassword });
            alert('Đổi mật khẩu thành công! (Đây là mô phỏng)');
            bootstrap.Modal.getInstance(changePasswordModal).hide();
        });
    }

  // Xử lý đăng xuất trong modal
  const dangxuat = document.getElementById('dangxuat');
  if (dangxuat) {
    dangxuat.addEventListener('click', (e) => {
      e.preventDefault();
      DangXuat();
    });
  }
});

function DangXuat() {
  const confirmLogout = confirm('Bạn có chắc chắn muốn đăng xuất không?');
  if (confirmLogout) {
    try {
      localStorage.removeItem('userData');
      localStorage.removeItem('userRole');
      window.location.href = '../Web_DangNhap/Web_DangNhap.html';
    } catch (e) {
      console.error('Lỗi khi xóa dữ liệu localStorage:', e);
    }
  }
}