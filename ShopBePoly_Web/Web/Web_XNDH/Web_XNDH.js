const c = window.config;

const hienthiOrder = async () => {
  const tbody = document.querySelector('#tbody');
  tbody.innerHTML = '';

  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/list_order`);
    const data = await res.json();

    // Lọc các đơn hàng có status là "Đang xử lý"
    const filteredOrders = data.filter(order => order.status === "Đang xử lý");

    filteredOrders.forEach(order => {
      const { _id, id_user, products, total, address, date, pay, status } = order;
      const tongSoLuong = products.reduce((sum, product) => sum + (product.quantity || 0), 0);

      const productInfo = products.map(product => {
        const productData = product.id_product || {};
        const color = product.color || '';
        const category = productData.id_category?.title || 'Không có thể loại';
        let imageUrl = '';

        // Tìm variation theo tên màu
        if (productData.variations && Array.isArray(productData.variations)) {
          const matchedVariation = productData.variations.find(v =>
            v.color?.name?.toLowerCase() === color.toLowerCase()
          );

          if (matchedVariation?.image) {
            imageUrl = `http://${config.host}:${config.port}/uploads/${matchedVariation.image}`;
          }
        }

        // Nếu không có ảnh theo màu, fallback sang avt_imgproduct
        if (!imageUrl && productData.avt_imgproduct) {
          imageUrl = `http://${config.host}:${config.port}/uploads/${productData.avt_imgproduct}`;
        }

        return `
          <div style="display: flex; align-items: center; margin-bottom: 6px;">
            ${imageUrl ? `<img src="${imageUrl}" alt="ảnh" style="width: 100px; height: 100px; object-fit: cover; margin-right: 8px; border-radius: 4px;">` : ''}
            <div>
              <strong>${productData.nameproduct || '---'}</strong><br>
              <small>Thể loại: ${category}</small><br>
              <small>Màu: ${color}, Size: ${product.size}, SL: ${product.quantity}</small>
            </div>
          </div>
        `;
      }).join('');

      const statusColor = `<span style="color: green; font-weight: bold;">${status}</span>`;

      const formattedTotal = isNaN(Number(total)) ? '0' : Number(total).toLocaleString('vi-VN');
      const formattedDate = date ? new Date(date).toLocaleDateString('vi-VN') : '';

      const tr = document.createElement('tr');
      tr.setAttribute('data-order-id', _id); // Thêm orderId vào tr để sử dụng sau
      tr.innerHTML = `
        <td>${id_user?.name || 'Không có'}</td>
        <td>${productInfo}</td>
        <td>${tongSoLuong}</td>
        <td>${formattedTotal} ₫</td>
        <td>${address}</td>
        <td>${formattedDate}</td>
        <td>${pay}</td>
        <td>${statusColor}</td>
        <td><button class="btn btn-primary btn-add-size mt-2" onclick="confirmOrder('${_id}')">Xác nhận</button></td>
      `;
      tbody.appendChild(tr);
    });

  } catch (error) {
    console.error('Lỗi khi lấy đơn hàng:', error);
  }
};

hienthiOrder();

// Hàm xác nhận và cập nhật trạng thái
async function confirmOrder(orderId) {
  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/list_order`);
    const data = await res.json();
    const order = data.find(o => o._id === orderId);

    if (!order) {
      alert('Không tìm thấy đơn hàng');
      return;
    }

    if (order.status !== "Đang xử lý") {
      alert('Chỉ có thể xác nhận khi trạng thái là Đang xử lý');
      return;
    }

    const response = await fetch(`http://${config.host}:${config.port}/api/updateOrderStatus/${orderId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        status: "Đang lấy hàng"
      })
    });

    const result = await response.json();

    if (response.ok) {
      alert(result.message);
      hienthiOrder(); // Tải lại danh sách để cập nhật giao diện
    } else {
      alert('Lỗi: ' + result.message);
    }
  } catch (error) {
    console.error('Lỗi khi xác nhận đơn hàng:', error);
    alert('Lỗi khi xác nhận đơn hàng');
  }
}

document.getElementById('btnDeleteAll').addEventListener('click', async () => {
  if (!confirm('Bạn có chắc chắn muốn xóa tất cả đơn hàng không?')) return;

  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/delete_all_orders`, {
      method: 'DELETE',
    });

    const data = await res.json();

    if (res.ok) {
      alert(data.message);
      hienthiOrder(); // Gọi lại hàm load lại danh sách đơn hàng
    } else {
      alert('Lỗi: ' + data.message);
    }
  } catch (error) {
    console.error('Lỗi khi xóa đơn hàng:', error);
    alert('Lỗi khi xóa đơn hàng');
  }
});

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