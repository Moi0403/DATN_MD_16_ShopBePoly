const host = window.config;
// checkpass
const toggle = document.getElementById('togglePassword');
  const password = document.getElementById('password');
  const icon = document.getElementById('toggleIcon');

  toggle.addEventListener('click', () => {
    const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
    password.setAttribute('type', type);

    icon.classList.toggle('bi-eye');
    icon.classList.toggle('bi-eye-slash');
});

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;

    try {
        const res = await fetch(`http://${config.host}:${config.port}/api/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const text = await res.text();
        let data;
        try {
            data = JSON.parse(text);
        } catch (err) {
            console.error('Phản hồi không phải JSON:', text);
            alert('Lỗi không xác định từ server.');
            return;
        }

        if (!res.ok) {
            alert(data.message || 'Đăng nhập thất bại.');
            return;
        }

        // Kiểm tra role
        if (data.user.role === 1 || data.user.role === 2) {
            if (rememberMe) {
                localStorage.setItem('rememberedUsername', username);
                localStorage.setItem('rememberedPassword', password);
            } else {
                localStorage.removeItem('rememberedUsername');
                localStorage.removeItem('rememberedPassword');
            }
            localStorage.setItem('userRole', data.user.role);
            alert(`Chào mừng ${data.user.name}, bạn đã đăng nhập thành công.`);
            window.location.href = '../Web_QL_Product/QL_Product.html';
        } else {
            alert('Bạn không có quyền truy cập.');
        }

    } catch (error) {
        console.error('Lỗi khi gửi yêu cầu:', error);
        alert('Lỗi kết nối đến server.');
    }
});

window.addEventListener('DOMContentLoaded', () => {
    const rememberedUsername = localStorage.getItem('rememberedUsername');
    const rememberedPassword = localStorage.getItem('rememberedPassword');

    if (rememberedUsername && rememberedPassword) {
        document.getElementById('username').value = rememberedUsername;
        document.getElementById('password').value = rememberedPassword;
        document.getElementById('rememberMe').checked = true;
    }
});

// ngăn quay lại trang
history.pushState(null, null, location.href);
    window.onpopstate = function () {
        history.go(1);
};





