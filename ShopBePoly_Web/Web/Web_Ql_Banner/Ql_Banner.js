const API_BASE = `http://${config.host}:${config.port}/api`;
const UPLOADS_BASE = `http://${config.host}:${config.port}/uploads`;

fetch('../Style_Sidebar/Sidebar.html')
  .then(res => res.text())
  .then(data => {
    document.getElementById('sidebar-container').innerHTML = data;
    const dangxuat = document.getElementById('dangxuat');
    if (dangxuat) {
      dangxuat.addEventListener('click', () => {
        const confirmLogout = confirm('Bạn có chắc chắn muốn đăng xuất không?');
        if (confirmLogout) {
          window.location.href = '../Web_DangNhap/Web_DangNhap.html';
        }
      });
    }
  });
// Fetch and display banners
async function fetchBanners() {
    try {
        const response = await fetch(`${API_BASE}/banners`);
        if (!response.ok) {
            throw new Error(`Failed to fetch banners: ${response.statusText}`);
        }
        const banners = await response.json();
        renderBanners(banners);
    } catch (error) {
        console.error('Error fetching banners:', error);
        showError('Không thể tải danh sách banner. Vui lòng thử lại.');
    }
}

function renderBanners(banners) {
    const bannerList = document.getElementById('banner-list');
    bannerList.innerHTML = '';
    banners.forEach(banner => {
        const imageUrl = `${UPLOADS_BASE}/${banner.imageUrl.split('/').pop()}`;
        const bannerCard = document.createElement('div');
        bannerCard.className = 'bg-white rounded-lg shadow-md overflow-hidden';
        bannerCard.innerHTML = `
            <img 
                src="${imageUrl}" 
                alt="${banner.name}" 
                class="w-full h-48 object-cover" 
                onerror="this.src='https://via.placeholder.com/300x200?text=Image+Not+Found'"
            >
            <div class="p-4">
                <p class="text-lg font-semibold truncate">${banner.name}</p>
                <p class="text-gray-500 text-sm">Ngày tạo: ${new Date(banner.createdAt).toLocaleDateString('vi-VN')}</p>
                <div class="mt-2 flex space-x-2">
                    <button
                        onclick="openUpdateModal('${banner._id}', '${banner.name}', '${imageUrl}')"
                        class="bg-green-500 text-white py-1 px-3 rounded hover:bg-green-600"
                    >
                        Sửa
                    </button>
                    <button
                        onclick="deleteBanner('${banner._id}')"
                        class="bg-red-500 text-white py-1 px-3 rounded hover:bg-red-600"
                    >
                        Xóa
                    </button>
                </div>
            </div>
        `;
        bannerList.appendChild(bannerCard);
    });
}

// Add a new banner
async function addBanner() {
    const bannerName = document.getElementById('bannerName').value;
    const imageFile = document.getElementById('imageFile').files[0];
    const errorMessage = document.getElementById('error-message');

    if (!bannerName.trim()) {
        showError('Vui lòng nhập tên banner.');
        return;
    }

    if (!imageFile) {
        showError('Vui lòng chọn một file ảnh.');
        return;
    }

    const formData = new FormData();
    formData.append('name', bannerName);
    formData.append('image', imageFile);

    try {
        const response = await fetch(`${API_BASE}/banners`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Không thể thêm banner.');
        }

        document.getElementById('bannerName').value = '';
        document.getElementById('imageFile').value = '';
        errorMessage.classList.add('hidden');
        await fetchBanners();
    } catch (error) {
        console.error('Error adding banner:', error);
        showError(error.message || 'Không thể thêm banner. Vui lòng thử lại.');
    }
}

// Delete a banner
async function deleteBanner(bannerId) {
    if (!confirm('Bạn có chắc chắn muốn xóa banner này không?')) return;

    try {
        const response = await fetch(`${API_BASE}/banners/${bannerId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Không thể xóa banner.');
        }

        await fetchBanners();
    } catch (error) {
        console.error('Error deleting banner:', error);
        showError(error.message || 'Không thể xóa banner. Vui lòng thử lại.');
    }
}

// Update functions
function openUpdateModal(bannerId, bannerName, imageUrl) {
    document.getElementById('updateBannerId').value = bannerId;
    document.getElementById('updateBannerName').value = bannerName;
    document.getElementById('currentBannerImage').src = imageUrl;
    document.getElementById('updateImageFile').value = '';
    document.getElementById('update-error-message').classList.add('hidden');
    document.getElementById('update-modal').classList.remove('hidden');
    document.getElementById('update-modal').classList.add('flex');
}

function closeUpdateModal() {
    document.getElementById('update-modal').classList.add('hidden');
    document.getElementById('update-modal').classList.remove('flex');
}

async function submitUpdate() {
    const bannerId = document.getElementById('updateBannerId').value;
    const newName = document.getElementById('updateBannerName').value;
    const newImageFile = document.getElementById('updateImageFile').files[0];
    const errorMessage = document.getElementById('update-error-message');

    if (!newName.trim() && !newImageFile) {
        showUpdateError('Vui lòng nhập tên mới hoặc chọn một file ảnh mới.');
        return;
    }

    const formData = new FormData();
    formData.append('name', newName);
    if (newImageFile) {
        formData.append('image', newImageFile);
    }

    try {
        const response = await fetch(`${API_BASE}/banners/${bannerId}`, {
            method: 'PUT',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Không thể cập nhật banner.');
        }

        closeUpdateModal();
        await fetchBanners();
    } catch (error) {
        console.error('Error updating banner:', error);
        showUpdateError(error.message || 'Không thể cập nhật banner. Vui lòng thử lại.');
    }
}

// Show error messages
function showError(message) {
    const errorMessage = document.getElementById('error-message');
    errorMessage.textContent = message;
    errorMessage.classList.remove('hidden');
}

function showUpdateError(message) {
    const errorMessage = document.getElementById('update-error-message');
    errorMessage.textContent = message;
    errorMessage.classList.remove('hidden');
}

// Load banners on page load
document.addEventListener('DOMContentLoaded', fetchBanners);