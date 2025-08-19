const c = window.config;

const hienthiOrder = async () => {
  const tbody = document.querySelector('#tbody');
  tbody.innerHTML = '';

  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/list_order`);
    const data = await res.json();

    const filteredOrders = data.filter(order => order.status === "Đang giao hàng");

    filteredOrders.forEach((order, index) => {
      const { _id, id_order, id_user, products, total, date, status } = order;

      const tongSoLuong = products.reduce((sum, product) => sum + (product.quantity || 0), 0);
      const statusColor = `<span style="color: green; font-weight: bold;">${status}</span>`;
      const formattedTotal = isNaN(Number(total)) ? '0' : Number(total).toLocaleString('vi-VN');
      const formattedDate = date ? new Date(date).toLocaleDateString('vi-VN') : '';

      const tr = document.createElement('tr');
      tr.setAttribute('data-order-id', _id);
      tr.innerHTML = `
        <td>${index + 1}</td> <!-- STT -->
        <td>${id_order}</td>
        <td>${id_user?.name || 'Không có'}<br>
            <small>SĐT: ${id_user?.phone_number || '---'}</small>
        </td>
        <td>${tongSoLuong}</td> <!-- Số lượng -->
        <td>${formattedDate}</td> <!-- Ngày -->
        <td>${formattedTotal} ₫</td> <!-- Tổng tiền -->
        <td>${statusColor}</td> <!-- Trạng thái -->
        <td>
            <button class="btn btn-info btn-sm mt-1" onclick="xemChiTietDon('${_id}')">Chi tiết</button>
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
      <p><strong>Thời gian cập nhật:</strong> ${new Date(checkedAt).toLocaleString("vi-VN")}</p>
      <p><strong>Người xác nhận:</strong> ${checkedBy}</p>
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
