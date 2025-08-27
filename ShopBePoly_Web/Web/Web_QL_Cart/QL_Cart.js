const c = window.config;

const hienthiOrder = async () => {
  const tbody = document.querySelector('#tbody');
  tbody.innerHTML = '';

  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/list_order`);
    const data = await res.json();

    // Sử dụng toàn bộ data thay vì filteredOrders
    data.forEach((order, index) => {
      const { _id, id_order, id_user, products, total, date, pay, status } = order;

      const tongSoLuong = products.reduce((sum, product) => sum + (product.quantity || 0), 0);
      const statusColor = `<span style="color: green; font-weight: bold;">${status}</span>`;
      const formattedTotal = pay === "ZaloPay - Đã thanh toán"
        ? '0'
        : (isNaN(Number(total)) ? '0' : Number(total).toLocaleString('vi-VN'));
      const formattedDate = date ? new Date(date).toLocaleDateString('vi-VN') : '';

      const tr = document.createElement('tr');
      tr.setAttribute('data-order-id', _id);
      tr.innerHTML = `
        <td>${index + 1}</td> <!-- STT -->
        <td>${id_order}</td>
        <td>${id_user?.name || 'Không có'}</td>
        <td>${tongSoLuong}</td> <!-- Số lượng -->
        <td>${formattedDate}</td> <!-- Ngày -->
        <td>${formattedTotal} ₫</td> <!-- Tổng tiền -->
        <td>${pay}</td> <!-- Phương thức thanh toán -->
        <td>${statusColor}</td> <!-- Trạng thái -->
        <td>
            <button class="btn btn-info btn-sm mt-1" onclick="xemChiTietDon('${_id}')">Chi tiết</button>
            ${status !== 'Đã hủy' ? `<button class="btn btn-primary btn-sm mt-1" onclick="confirmOrder('${_id}')">Hủy đơn</button>` : ''}
        </td>
      `;
      tbody.appendChild(tr);
    });

  } catch (error) {
    console.error('Lỗi khi lấy đơn hàng:', error);
  }
};

hienthiOrder();


const xemChiTietDon = async (orderId) => {
  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/list_order`);
    const data = await res.json();

    const order = data.find(o => o._id === orderId);
    if (!order) return;

    const {id_order, id_user, products, total, address, date, pay, status, checkedAt, checkedBy } = order;

    const productHtml = products.map(product => {
      const productData = product.id_product || {};
      const color = product.color || '';
      const category = productData.id_category?.title || 'Không có thể loại';
      let imageUrl = '';

      if (productData.variations && Array.isArray(productData.variations)) {
        const matchedVariation = productData.variations.find(v =>
          v.color?.name?.toLowerCase() === color.toLowerCase()
        );
        if (matchedVariation?.image) {
          imageUrl = `http://${config.host}:${config.port}/uploads/${matchedVariation.image}`;
        }
      }

      if (!imageUrl && productData.avt_imgproduct) {
        imageUrl = `http://${config.host}:${config.port}/uploads/${productData.avt_imgproduct}`;
      }

      return `
        <div style="display: flex; align-items: center; margin-bottom: 10px;">
          <img src="${imageUrl}" style="width: 90px; height: 90px; object-fit: cover; border-radius: 6px; margin-right: 10px;">
          <div>
            <strong>${productData.nameproduct || '---'}</strong><br>
            <small>Thể loại: ${category}</small><br>
            <small>Màu: ${color} - Size: ${product.size} - SL: ${product.quantity}</small>
          </div>
        </div>
      `;
    }).join('');

    const bodyHtml = `
      <p><strong>Mã đơn: </strong>${id_order}</p>
      <p><strong>Người đặt:</strong> ${id_user?.name || '---'}</p>
      <p><strong>SĐT:</strong> ${id_user?.phone_number || ''}</p>
      <p><strong>Địa chỉ:</strong> ${address}</p>
      <p><strong>Ngày đặt:</strong> ${new Date(date).toLocaleDateString('vi-VN')}</p>
      <p><strong>Thanh toán:</strong> ${pay}</p>
      <p><strong>Tổng tiền:</strong> ${Number(total).toLocaleString('vi-VN')} ₫</p>
      <p><strong>Trạng thái:</strong> ${status}</p>
      <p><strong>Thời gian xác nhận:</strong> ${new Date(checkedAt).toLocaleString("vi-VN") || "Chưa cập nhật"}</p>
      <p><strong>Người xác nhận:</strong> ${checkedBy || "Chưa cập nhật"}</p>
      <p><strong>Thời gian giao:</strong> ${new Date(checkedAt).toLocaleString("vi-VN") || "Chưa cập nhật"}</p>
      <p><strong>Người giao:</strong> ${checkedBy || "Chưa cập nhật"}</p>
      <hr>
      <h6>Sản phẩm:</h6>
      ${productHtml}
    `;

    document.getElementById('order-detail-body').innerHTML = bodyHtml;

    // Mở modal Bootstrap
    const modal = new bootstrap.Modal(document.getElementById('orderDetailModal'));
    modal.show();

  } catch (err) {
    console.error('Lỗi khi hiển thị chi tiết đơn:', err);
  }
};

async function confirmOrder(orderId) {
  const reason = prompt('Lý do hủy đơn hàng:');
  if (reason === null || reason.trim() === '') {
    alert('Vui lòng nhập lý do hủy đơn hàng.');
    return;
  }

  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/cancel_order/${orderId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ reason }),
    });

    const data = await res.json();
    if (res.ok) {
      alert('Đơn hàng đã được hủy.');
      hienthiOrder(); // Cập nhật danh sách đơn hàng
    } else {
      alert(`Có lỗi xảy ra: ${data.message || 'Không thể hủy đơn hàng.'}`);
    }
  } catch (error) {
    console.error('Lỗi khi hủy đơn hàng:', error);
    if (error instanceof SyntaxError) {
      console.log('Phản hồi không phải JSON:', await res.text());
    }
    alert('Có lỗi xảy ra khi hủy đơn hàng. Kiểm tra console để biết chi tiết.');
  }
}

const searchOrder = async () => {
  const searchInput = document.getElementById('searchInput').value.trim();
  const searchResults = document.getElementById('searchResults');
  searchResults.innerHTML = '<h4>Kết quả tìm kiếm</h4><table class="table"><thead><tr><th>STT</th><th>Mã đơn</th><th>Người đặt</th><th>Số lượng</th><th>Ngày</th><th>Tổng tiền</th><th>Phương thức thanh toán</th><th>Trạng thái</th><th>Hành động</th></tr></thead><tbody id="searchTbody"></tbody></table>';

  if (!searchInput && !searchNameInput) {
    searchResults.innerHTML = '';
    return;
  }

  try {
    const url = new URL(`http://${config.host}:${config.port}/api/search_order/id_order`);
    if (searchInput) url.searchParams.append('id_order', searchInput);

    const res = await fetch(url);
    const data = await res.json();

    const searchTbody = document.getElementById('searchTbody');
    searchTbody.innerHTML = '';

    if (Array.isArray(data) && data.length > 0) {
      data.forEach((order, index) => {
        const { _id, id_order, id_user, products, total, date, pay, status } = order;

        const tongSoLuong = products.reduce((sum, product) => sum + (product.quantity || 0), 0);
        const statusColor = `<span style="color: green; font-weight: bold;">${status}</span>`;
        const formattedTotal = isNaN(Number(total)) ? '0' : Number(total).toLocaleString('vi-VN');
        const formattedDate = date ? new Date(date).toLocaleDateString('vi-VN') : '';

        const tr = document.createElement('tr');
        tr.setAttribute('data-order-id', _id);
        tr.innerHTML = `
          <td>${index + 1}</td> <!-- STT -->
          <td>${id_order}</td>
          <td>${id_user?.name || 'Không có'}</td>
          <td>${tongSoLuong}</td> <!-- Số lượng -->
          <td>${formattedDate}</td> <!-- Ngày -->
          <td>${formattedTotal} ₫</td> <!-- Tổng tiền -->
          <td>${pay}</td> <!-- Phương thức thanh toán -->
          <td>${statusColor}</td> <!-- Trạng thái -->
          <td>
              <button class="btn btn-info btn-sm mt-1" onclick="xemChiTietDon('${_id}')">Chi tiết</button>
              ${status !== 'Đã hủy' ? `<button class="btn btn-primary btn-sm mt-1" onclick="confirmOrder('${_id}')">Hủy đơn</button>` : ''}
          </td>
        `;
        searchTbody.appendChild(tr);
      });
    } else if (data.message) {
      searchResults.innerHTML = `<p>${data.message}</p>`;
    } else {
      searchResults.innerHTML = '<p>Không tìm thấy đơn hàng.</p>';
    }

  } catch (error) {
    console.error('Lỗi khi tìm kiếm đơn hàng:', error);
    searchResults.innerHTML = '<p>Lỗi khi tìm kiếm. Kiểm tra console.</p>';
  }
};

// Gắn sự kiện cho nút tìm kiếm
document.getElementById('searchButton').addEventListener('click', searchOrder);

// Sự kiện Enter trên input
document.getElementById('searchInput').addEventListener('keypress', (e) => {
  if (e.key === 'Enter') searchOrder();
});
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

