// forgot_password.js

// Biến toàn cục để lưu email tạm thời
let currentEmail = '';

// Khởi tạo khi DOM load xong
document.addEventListener('DOMContentLoaded', function() {
    // Lắng nghe sự kiện click vào link "Bạn quên mật khẩu ?"
    const forgotPasswordLink = document.getElementById('forgotPasswordLink');
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', function(e) {
            e.preventDefault();
            showEmailDialog();
        });
    }
});

// Hiển thị dialog nhập email
function showEmailDialog() {
    // Tạo backdrop
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop fade show';
    backdrop.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1040;
        width: 100vw;
        height: 100vh;
        background-color: rgba(0, 0, 0, 0.5);
    `;

    // Tạo dialog
    const dialog = document.createElement('div');
    dialog.className = 'modal fade show';
    dialog.style.cssText = `
        display: block;
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1050;
        width: 100%;
        height: 100%;
        overflow: hidden;
        outline: 0;
    `;

    dialog.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Quên mật khẩu</h5>
                    <button type="button" class="btn-close" onclick="closeDialog()"></button>
                </div>
                <div class="modal-body">
                    <p>Vui lòng nhập email để nhận mã xác thực:</p>
                    <div class="mb-3">
                        <input type="email" class="form-control" id="resetEmail" 
                               placeholder="Nhập địa chỉ email của bạn..." required>
                    </div>
                    <div id="emailError" class="text-danger" style="display: none;"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeDialog()">Hủy</button>
                    <button type="button" class="btn btn-primary" onclick="sendVerificationCode()">
                        <span class="btn-text">Gửi mã xác thực</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </div>
        </div>
    `;

    // Thêm vào body
    document.body.appendChild(backdrop);
    document.body.appendChild(dialog);

    // Focus vào input email
    setTimeout(() => {
        document.getElementById('resetEmail').focus();
    }, 100);

    // Lắng nghe Enter key
    document.getElementById('resetEmail').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendVerificationCode();
        }
    });
}

// Gửi mã xác thực
async function sendVerificationCode() {
    const emailInput = document.getElementById('resetEmail');
    const email = emailInput.value.trim();
    const errorDiv = document.getElementById('emailError');
    const button = document.querySelector('.modal-footer .btn-primary');
    const buttonText = button.querySelector('.btn-text');
    const spinner = button.querySelector('.spinner-border');

    // Validate email
    if (!email) {
        showError('Vui lòng nhập email!', errorDiv);
        return;
    }

    if (!isValidEmail(email)) {
        showError('Email không đúng định dạng!', errorDiv);
        return;
    }

    // Hiển thị loading
    button.disabled = true;
    buttonText.textContent = 'Đang gửi...';
    spinner.classList.remove('d-none');
    errorDiv.style.display = 'none';

    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/send-verification-code`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email })
        });

        if (response.ok) {
            currentEmail = email;
            closeDialog();
            showVerificationDialog();
        } else {
            const errorData = await response.json();
            showError(errorData.message || 'Không thể gửi mã xác thực. Vui lòng thử lại!', errorDiv);
        }
    } catch (error) {
        console.error('Lỗi khi gửi mã xác thực:', error);
        showError('Lỗi kết nối. Vui lòng kiểm tra internet và thử lại!', errorDiv);
    } finally {
        // Reset button
        button.disabled = false;
        buttonText.textContent = 'Gửi mã xác thực';
        spinner.classList.add('d-none');
    }
}

// Hiển thị dialog nhập mã xác thực
function showVerificationDialog() {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop fade show';
    backdrop.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1040;
        width: 100vw;
        height: 100vh;
        background-color: rgba(0, 0, 0, 0.5);
    `;

    const dialog = document.createElement('div');
    dialog.className = 'modal fade show';
    dialog.style.cssText = `
        display: block;
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1050;
        width: 100%;
        height: 100%;
        overflow: hidden;
        outline: 0;
    `;

    dialog.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Xác thực mã</h5>
                    <button type="button" class="btn-close" onclick="closeDialog()"></button>
                </div>
                <div class="modal-body">
                    <p>Mã xác thực đã được gửi đến email: <strong>${currentEmail}</strong></p>
                    <p>Vui lòng nhập mã xác thực (6 số):</p>
                    <div class="mb-3">
                        <input type="text" class="form-control text-center" id="verificationCode" 
                               placeholder="Nhập mã 6 số..." maxlength="6" required
                               style="font-size: 1.2em; letter-spacing: 0.3em;">
                    </div>
                    <div id="codeError" class="text-danger" style="display: none;"></div>
                    <div class="text-center">
                        <small class="text-muted">
                            Không nhận được mã? 
                            <a href="#" onclick="resendCode()" class="text-decoration-none">Gửi lại</a>
                        </small>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeDialog()">Hủy</button>
                    <button type="button" class="btn btn-primary" onclick="verifyCode()">
                        <span class="btn-text">Xác thực</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(backdrop);
    document.body.appendChild(dialog);

    // Focus và format input
    const codeInput = document.getElementById('verificationCode');
    setTimeout(() => codeInput.focus(), 100);

    // Chỉ cho phép nhập số
    codeInput.addEventListener('input', function(e) {
        this.value = this.value.replace(/[^0-9]/g, '');
    });

    // Lắng nghe Enter key
    codeInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            verifyCode();
        }
    });
}

// Xác thực mã
async function verifyCode() {
    const codeInput = document.getElementById('verificationCode');
    const code = codeInput.value.trim();
    const errorDiv = document.getElementById('codeError');
    const button = document.querySelector('.modal-footer .btn-primary');
    const buttonText = button.querySelector('.btn-text');
    const spinner = button.querySelector('.spinner-border');

    if (!code) {
        showError('Vui lòng nhập mã xác thực!', errorDiv);
        return;
    }

    if (code.length !== 6) {
        showError('Mã xác thực phải có 6 số!', errorDiv);
        return;
    }

    // Hiển thị loading
    button.disabled = true;
    buttonText.textContent = 'Đang xác thực...';
    spinner.classList.remove('d-none');
    errorDiv.style.display = 'none';

    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/verify-code`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                email: currentEmail, 
                code: code 
            })
        });

        const data = await response.json();

        if (response.ok) {
            closeDialog();
            showNewPasswordDialog();
        } else {
            showError(data.message || 'Mã xác thực không đúng!', errorDiv);
            codeInput.select(); // Chọn toàn bộ text để dễ nhập lại
        }
    } catch (error) {
        console.error('Lỗi khi xác thực mã:', error);
        showError('Lỗi kết nối. Vui lòng thử lại!', errorDiv);
    } finally {
        // Reset button
        button.disabled = false;
        buttonText.textContent = 'Xác thực';
        spinner.classList.add('d-none');
    }
}

// Gửi lại mã
async function resendCode() {
    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/send-verification-code`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email: currentEmail })
        });

        if (response.ok) {
            alert('Mã xác thực mới đã được gửi!');
            document.getElementById('verificationCode').value = '';
            document.getElementById('verificationCode').focus();
        } else {
            alert('Không thể gửi lại mã. Vui lòng thử lại!');
        }
    } catch (error) {
        console.error('Lỗi khi gửi lại mã:', error);
        alert('Lỗi kết nối. Vui lòng thử lại!');
    }
}

// Hiển thị dialog đổi mật khẩu mới
function showNewPasswordDialog() {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop fade show';
    backdrop.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1040;
        width: 100vw;
        height: 100vh;
        background-color: rgba(0, 0, 0, 0.5);
    `;

    const dialog = document.createElement('div');
    dialog.className = 'modal fade show';
    dialog.style.cssText = `
        display: block;
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1050;
        width: 100%;
        height: 100%;
        overflow: hidden;
        outline: 0;
    `;

    dialog.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Đặt mật khẩu mới</h5>
                </div>
                <div class="modal-body">
                    <p>Vui lòng nhập mật khẩu mới cho tài khoản: <strong>${currentEmail}</strong></p>
                    <div class="mb-3">
                        <label for="newPassword" class="form-label">Mật khẩu mới:</label>
                        <div class="input-group">
                            <input type="password" class="form-control" id="newPassword" 
                                   placeholder="Nhập mật khẩu mới..." required>
                            <span class="input-group-text" onclick="togglePasswordVisibility('newPassword', this)">
                                <i class="bi bi-eye"></i>
                            </span>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="confirmPassword" class="form-label">Xác nhận mật khẩu:</label>
                        <div class="input-group">
                            <input type="password" class="form-control" id="confirmPassword" 
                                   placeholder="Nhập lại mật khẩu mới..." required>
                            <span class="input-group-text" onclick="togglePasswordVisibility('confirmPassword', this)">
                                <i class="bi bi-eye"></i>
                            </span>
                        </div>
                    </div>
                    <div id="passwordError" class="text-danger" style="display: none;"></div>
                    <div class="alert alert-info">
                        <small>
                            <i class="bi bi-info-circle"></i> 
                            Mật khẩu nên có ít nhất 6 ký tự để đảm bảo an toàn
                        </small>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary w-100" onclick="updatePassword()">
                        <span class="btn-text">Cập nhật mật khẩu</span>
                        <span class="spinner-border spinner-border-sm d-none" role="status"></span>
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(backdrop);
    document.body.appendChild(dialog);

    // Focus vào input đầu tiên
    setTimeout(() => {
        document.getElementById('newPassword').focus();
    }, 100);

    // Lắng nghe Enter key
    ['newPassword', 'confirmPassword'].forEach(id => {
        document.getElementById(id).addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                updatePassword();
            }
        });
    });
}

// Toggle hiển thị mật khẩu
function togglePasswordVisibility(inputId, toggleButton) {
    const input = document.getElementById(inputId);
    const icon = toggleButton.querySelector('i');
    
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'bi bi-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'bi bi-eye';
    }
}

// Cập nhật mật khẩu mới
async function updatePassword() {
    const newPassword = document.getElementById('newPassword').value.trim();
    const confirmPassword = document.getElementById('confirmPassword').value.trim();
    const errorDiv = document.getElementById('passwordError');
    const button = document.querySelector('.modal-footer .btn-primary');
    const buttonText = button.querySelector('.btn-text');
    const spinner = button.querySelector('.spinner-border');

    // Validate
    if (!newPassword || !confirmPassword) {
        showError('Vui lòng nhập đầy đủ thông tin!', errorDiv);
        return;
    }

    if (newPassword.length < 6) {
        showError('Mật khẩu phải có ít nhất 6 ký tự!', errorDiv);
        return;
    }

    if (newPassword !== confirmPassword) {
        showError('Mật khẩu xác nhận không khớp!', errorDiv);
        return;
    }

    // Hiển thị loading
    button.disabled = true;
    buttonText.textContent = 'Đang cập nhật...';
    spinner.classList.remove('d-none');
    errorDiv.style.display = 'none';

    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/auth/reset-password-by-email`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ 
                email: currentEmail, 
                newPassword: newPassword 
            })
        });

        const data = await response.json();

        if (response.ok) {
            closeDialog();
            showSuccessMessage();
        } else {
            showError(data.message || 'Không thể cập nhật mật khẩu!', errorDiv);
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật mật khẩu:', error);
        showError('Lỗi kết nối. Vui lòng thử lại!', errorDiv);
    } finally {
        // Reset button
        button.disabled = false;
        buttonText.textContent = 'Cập nhật mật khẩu';
        spinner.classList.add('d-none');
    }
}

// Hiển thị thông báo thành công và quay lại đăng nhập
function showSuccessMessage() {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop fade show';
    backdrop.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1040;
        width: 100vw;
        height: 100vh;
        background-color: rgba(0, 0, 0, 0.5);
    `;

    const dialog = document.createElement('div');
    dialog.className = 'modal fade show';
    dialog.style.cssText = `
        display: block;
        position: fixed;
        top: 0;
        left: 0;
        z-index: 1050;
        width: 100%;
        height: 100%;
        overflow: hidden;
        outline: 0;
    `;

    dialog.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-body text-center p-4">
                    <div class="mb-3">
                        <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
                    </div>
                    <h5 class="mb-3">Cập nhật mật khẩu thành công!</h5>
                    <p class="text-muted mb-4">
                        Mật khẩu của bạn đã được thay đổi thành công.<br>
                        Bạn có thể đăng nhập bằng mật khẩu mới.
                    </p>
                    <button type="button" class="btn btn-primary" onclick="backToLogin()">
                        Quay lại đăng nhập
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(backdrop);
    document.body.appendChild(dialog);

    // Auto close after 5 seconds
    setTimeout(() => {
        backToLogin();
    }, 5000);
}

// Quay lại trang đăng nhập
function backToLogin() {
    closeDialog();
    currentEmail = ''; // Reset email
    // Focus vào username input nếu có
    const usernameInput = document.getElementById('username');
    if (usernameInput) {
        usernameInput.focus();
    }
}

// Đóng dialog
function closeDialog() {
    const modals = document.querySelectorAll('.modal, .modal-backdrop');
    modals.forEach(modal => {
        if (modal && modal.parentNode) {
            modal.parentNode.removeChild(modal);
        }
    });
}

// Hiển thị lỗi
function showError(message, errorDiv) {
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    
    // Tự động ẩn sau 5 giây
    setTimeout(() => {
        if (errorDiv) {
            errorDiv.style.display = 'none';
        }
    }, 5000);
}

// Validate email format
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Đóng dialog khi click outside (optional)
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-backdrop')) {
        closeDialog();
    }
});

// Đóng dialog khi nhấn Escape (optional)
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeDialog();
    }
});