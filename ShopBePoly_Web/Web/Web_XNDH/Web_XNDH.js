const c = window.config;

const hienthiOrder = async () => {
  const tbody = document.querySelector('#tbody');
  tbody.innerHTML = '';

  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/list_order`);
    const data = await res.json();

    data.forEach(order => {
      const { id_user, products, total, address, date, pay, status } = order;
      const tongSoLuong = products.reduce((sum, product) => sum + (product.quantity || 0), 0);

      const productInfo = products.map(product => {
  const productData = product.id_product || {};
  const color = product.color || '';
  const category = productData.category || 'Không có thể loại';
  let imageUrl = '';

  if (product.img) {
    imageUrl = `http://${config.host}:${config.port}/uploads/${product.img}`;
  } 
  else if (productData.variations && Array.isArray(productData.variations)) {
    const matchedVariation = productData.variations.find(v =>
      v.color?.name?.toLowerCase() === color.toLowerCase()
    );
    if (matchedVariation?.image) {
      imageUrl = `http://${config.host}:${config.port}/uploads/${matchedVariation.image}`;
    } else if (productData.avt_imgproduct) {
      imageUrl = `http://${config.host}:${config.port}/uploads/${productData.avt_imgproduct}`;
    }
  } 
  else if (productData.avt_imgproduct) {
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
      tr.innerHTML = `
        <td>${id_user?.name || 'Không có'}</td>
        <td>${productInfo}</td>
        <td>${tongSoLuong}</td>
        <td>${formattedTotal} ₫</td>
        <td>${address}</td>
        <td>${formattedDate}</td>
        <td>${pay}</td>
        <td>${statusColor}</td>
      `;
      tbody.appendChild(tr);
    });

  } catch (error) {
    console.error('Lỗi khi lấy đơn hàng:', error);
  }
};

hienthiOrder();

document.getElementById('btnDeleteAll').addEventListener('click', async () => {
  if (!confirm('Bạn có chắc chắn muốn xóa tất cả đơn hàng không?')) return;

  try {
    const res = await fetch(`http://${config.host}:${config.port}/api/delete_all_orders`, {
      method: 'DELETE',
    });

    const data = await res.json();

    if (res.ok) {
      alert(data.message);
      hienthiOrder();  // gọi lại hàm load lại danh sách đơn hàng
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
  });
