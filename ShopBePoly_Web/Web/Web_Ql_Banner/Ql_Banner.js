const API_BASE = `http://${config.host}:${config.port}/api`;
const UPLOADS_BASE = `http://${config.host}:${config.port}/uploads`;

// Load Sidebar
fetch('../Style_Sidebar/Sidebar.html')
  .then(res => res.text())
  .then(data => {
    document.getElementById('sidebar-container').innerHTML = data;
    const dangxuat = document.getElementById('dangxuat');
    if (dangxuat) {
      dangxuat.addEventListener('click', () => {
        if (confirm('Bạn có chắc chắn muốn đăng xuất không?')) {
          window.location.href = '../Web_DangNhap/Web_DangNhap.html';
        }
      });
    }
  });

// Lấy và hiển thị danh sách banner
async function fetchBanners() {
    try {
        const response = await fetch(`${API_BASE}/banners`);
        if (!response.ok) throw new Error(`Lỗi tải banner: ${response.statusText}`);
        const banners = await response.json();
        renderBanners(banners);
    } catch (error) {
        console.error('Error fetching banners:', error);
        alert('Không thể tải danh sách banner. Vui lòng thử lại.');
    }
}

// Render bảng
function renderBanners(banners) {
    const tbody = document.getElementById('tbody');
    tbody.innerHTML = '';
    banners.forEach((banner, index) => {
        const imageUrl = `${UPLOADS_BASE}/${banner.imageUrl.split('/').pop()}`;
        const row = `
            <tr>
                <td>${index + 1}</td>
                <td><img src="${imageUrl}" alt="${banner.name}" style="width: 800px; height: auto; max-width: 100%;"></td>
                <td>${banner.name}</td>
                <td>
                    <button class="btn btn-sm btn-success" onclick="openUpdateModal('${banner._id}', '${banner.name}', '${imageUrl}')">Sửa</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteBanner('${banner._id}')">Xóa</button>
                </td>
            </tr>
        `;
        tbody.insertAdjacentHTML('beforeend', row);
    });
}

// Thêm banner
document.getElementById('themBanner').addEventListener('submit', async function (e) {
    e.preventDefault();
    const bannerName = document.getElementById('titleBanner').value;
    const imageFile = document.getElementById('imgBanner').files[0];

    if (!bannerName.trim() || !imageFile) {
        alert('Vui lòng nhập đầy đủ thông tin.');
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
        if (!response.ok) throw new Error('Không thể thêm banner.');
        document.getElementById('themBanner').reset();
        await fetchBanners();
    } catch (error) {
        console.error('Error adding banner:', error);
        alert(error.message);
    }
});

// Xóa banner
async function deleteBanner(bannerId) {
    if (!confirm('Bạn có chắc chắn muốn xóa banner này không?')) return;
    try {
        const response = await fetch(`${API_BASE}/banners/${bannerId}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Không thể xóa banner.');
        await fetchBanners();
    } catch (error) {
        console.error('Error deleting banner:', error);
        alert(error.message);
    }
}

// Mở modal cập nhật (có đổi ảnh)
function openUpdateModal(id, name, imageUrl) {
    const formHtml = `
        <label>Tên banner mới:</label>
        <input type="text" id="updateName" class="swal2-input" value="${name}">
        <label>Chọn ảnh mới (nếu muốn đổi):</label>
        <input type="file" id="updateImage" class="swal2-file" accept="image/*">
        <br>
        <img id="previewImage" src="${imageUrl}" alt="Ảnh hiện tại" style="width: 300px; margin-top: 10px; border: 1px solid #ccc; border-radius: 5px;">
    `;

    Swal.fire({
        title: 'Cập nhật banner',
        html: formHtml,
        focusConfirm: false,
        showCancelButton: true,
        confirmButtonText: 'Cập nhật',
        cancelButtonText: 'Hủy',
        didOpen: () => {
            const fileInput = document.getElementById("updateImage");
            const previewImg = document.getElementById("previewImage");

            fileInput.addEventListener("change", (event) => {
                const file = event.target.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = (e) => {
                        previewImg.src = e.target.result;
                    };
                    reader.readAsDataURL(file);
                }
            });
        },
        preConfirm: () => {
            const newName = document.getElementById("updateName").value.trim();
            const newImage = document.getElementById("updateImage").files[0];
            return { newName, newImage };
        }
    }).then(result => {
        if (result.isConfirmed) {
            updateBanner(id, result.value.newName, result.value.newImage);
        }
    });
}


// Cập nhật banner (tên + ảnh)
async function updateBanner(id, newName, newImageFile) {
    const formData = new FormData();
    formData.append('name', newName);
    if (newImageFile) {
        formData.append('image', newImageFile);
    }

    try {
        const response = await fetch(`${API_BASE}/banners/${id}`, {
            method: 'PUT',
            body: formData
        });
        if (!response.ok) throw new Error('Không thể cập nhật banner.');
        await fetchBanners();
        Swal.fire('Thành công!', 'Banner đã được cập nhật.', 'success');
    } catch (error) {
        console.error('Error updating banner:', error);
        Swal.fire('Lỗi!', error.message, 'error');
    }
}

// Load dữ liệu khi trang mở
document.addEventListener('DOMContentLoaded', fetchBanners);