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
  const khac = document.getElementById('khac');
  const menudangxuat = document.getElementById('menu-dangxuat');

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
    hideElement(khac);
    hideElement(menudangxuat);

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
      document.getElementById('khac').style.display = 'block';
      document.getElementById('menu-dangxuat').style.display = 'block';
  } else if (userData.role === 2){
      document.getElementById('menu-home').style.display = 'block';
      document.getElementById('quantri').style.display = 'block';
      document.getElementById('menu-ql-tk').style.display = 'block';
      document.getElementById('menu-voucher').style.display = 'block';
      document.getElementById('thongke').style.display = 'block';
      document.getElementById('menu-stats').style.display = 'block';
      document.getElementById('menu-top10').style.display = 'block';
      document.getElementById('khac').style.display = 'block';
      document.getElementById('menu-dangxuat').style.display = 'block';
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


  // Xử lý đăng xuất trong modal
  const dangxuat = document.getElementById('menu-dangxuat');
if (dangxuat) {
  dangxuat.addEventListener('click', (e) => {
    e.preventDefault();
    const confirmLogout = confirm('Bạn có chắc chắn muốn đăng xuất không?');
    if (confirmLogout) {
      try {
        // Xóa dữ liệu đăng nhập
        localStorage.removeItem('userData');
        localStorage.removeItem('userRole');
        
        // Chuyển hướng đến trang đăng nhập
        const redirectUrl = '../Web_DangNhap/Web_DangNhap.html';
        window.location.href = redirectUrl;
        
        // Thông báo thành công (nếu cần, nhưng có thể không hiển thị do chuyển hướng ngay)
        console.log('Đăng xuất thành công, chuyển hướng đến:', redirectUrl);
      } catch (error) {
        console.error('Lỗi khi xóa dữ liệu localStorage:', error);
        alert('Đã xảy ra lỗi khi đăng xuất. Vui lòng thử lại.');
      }
    }
  });
}
});
