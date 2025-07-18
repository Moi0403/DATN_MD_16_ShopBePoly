const host = window.config;

const hienthi_DSXN = async () => {
  const tbody = document.querySelector('#tbody');

  try {
    const api = await fetch(`http://${config.host}:${config.port}/api/list_order`);
    const data = await api.json();
    tbody.innerHTML = '';

    data.forEach((item) => {
      item.products.forEach((product) => {
        const tr = document.createElement('tr');

        const tdName_user = document.createElement('td');
        tdName_user.textContent = item.id_user?.name || 'Không có';

        const tdImg = document.createElement('td');
        const img = document.createElement('img');
        img.src = `../../uploads/${product.img || product.id_product?.avt_imgproduct}`;
        img.alt = 'Ảnh SP';
        img.style.width = '50px';
        tdImg.appendChild(img);

        const tdName_pro = document.createElement('td');
        tdName_pro.textContent = product.id_product?.nameproduct || '---';

        const tdSlg = document.createElement('td');
        tdSlg.textContent = product.quantity;

        const tdPrice = document.createElement('td');
        tdPrice.textContent = product.price
          ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(product.price)
          : '0 ₫';

        const tdTotal = document.createElement('td');
        tdTotal.textContent = item.total
          ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(item.total))
          : '0 ₫';

        const tdStatus = document.createElement('td');
        tdStatus.textContent = item.status || '---';

        tr.appendChild(tdName_user);
        tr.appendChild(tdImg);
        tr.appendChild(tdName_pro);
        tr.appendChild(tdSlg);
        tr.appendChild(tdPrice);
        tr.appendChild(tdTotal);
        tr.appendChild(tdStatus);

        tbody.appendChild(tr);
      });
    });

  } catch (error) {
    console.error('❌ Lỗi khi gọi API:', error);
  }
};

hienthi_DSXN();
