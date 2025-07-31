// Đảm bảo config được định nghĩa (giả sử đã có trong window.config)
const { host, port } = window.config || { host: 'localhost', port: 3000 };

// Load Sidebar
fetch('../Style_Sidebar/Sidebar.html')
  .then(res => res.text())
  .then(data => {
    document.getElementById('sidebar-container').innerHTML = data;
    const dangxuat = document.getElementById('dangxuat');
    if (dangxuat) {
      dangxuat.addEventListener('click', () => {
        if (confirm('Bạn có chắc chắn muốn đăng xuất không?')) {
          window.location.href = '../Web_TrangChu/TrangChu.html';
        }
      });
    }
  });

window.addEventListener('DOMContentLoaded', async () => {
  const cate_selet = document.getElementById('category_pro');
  const plSelect = document.getElementById('pl_pro');
  try {
    const response = await fetch(`http://${host}:${port}/api/list_category`);
    const categories = await response.json();

    cate_selet.innerHTML = '<option value="">-- Chọn hãng giày --</option>';
    plSelect.innerHTML = '<option value="0">-- Tất cả --</option>';

    categories.forEach(cat => {
      const option = document.createElement('option');
      option.value = cat._id;
      option.textContent = cat.title;
      cate_selet.appendChild(option);

      const opt = document.createElement('option');
      opt.value = cat._id;
      opt.textContent = "-- " + cat.title + " --";
      plSelect.appendChild(opt);
    });
  } catch (error) {
    console.error('Lỗi khi lấy danh sách category:', error);
  }
});

function convertToColorCode(color) {
  const colorMap = {
    'đen': 'black',
    'trắng': 'white',
    'đỏ': 'red',
    'xanh': 'blue',
    'vàng': 'yellow',
    default: '#ccc'
  };
  return colorMap[color.toLowerCase()] || colorMap.default;
}

function updateColorSpinner() {
  const colorSpinner = document.getElementById('colorSpinner');
  const existingColors = [...document.querySelectorAll('.var-color-name')].map(el => el.value.toLowerCase());
  const allColors = ['Đen', 'Trắng', 'Đỏ', 'Xanh', 'Vàng', 'Hồng'];

  // Lấy các màu chưa tồn tại
  const availableColors = allColors.filter(color => !existingColors.includes(color.toLowerCase()));

  // Cập nhật spinner
  colorSpinner.innerHTML = '<option value="">-- Chọn màu --</option>';
  availableColors.forEach(color => {
    const option = document.createElement('option');
    option.value = color;
    option.textContent = color.charAt(0).toUpperCase() + color.slice(1); // Viết hoa chữ cái đầu
    colorSpinner.appendChild(option);
  });
}

document.getElementById('addColorBlock').addEventListener('click', () => {
  const colorName = document.getElementById('colorSpinner').value;
  if (!colorName) {
    alert("Vui lòng chọn một màu!");
    return;
  }
  const colorCode = convertToColorCode(colorName);
  const container = document.getElementById('color-block-container');

  if ([...document.querySelectorAll('.var-color-name')].some(el => el.value.toLowerCase() === colorName.toLowerCase())) {
    alert("Màu này đã được thêm.");
    return;
  }

  const colorBlock = document.createElement('div');
  colorBlock.classList.add('color-block', 'border', 'p-3', 'mb-3');
  container.appendChild(document.createElement('hr'));
  colorBlock.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-2">
      <h5>${colorName}</h5>
      <button type="button" class="btn btn-danger btn-remove-color">X</button>
    </div>
    <input type="hidden" class="var-color-name" value="${colorName}">
    <input type="hidden" class="var-color-code" value="${colorCode}">
    <div class="row mb-3">
      <div class="col-md-6">
        <label class="form-label">Chọn ảnh cho màu này:</label>
        <input type="file" class="form-control var-image" accept="image/*" multiple required>
      </div>
      <div class="col-md-6">
        <img class="color-preview" src="" alt="Preview Image" style="max-width: 150px; max-height: 150px; margin-top: 10px; display: none;">
      </div>
    </div>
    <div class="variation-wrapper"></div>
    <button type="button" class="btn btn-primary btn-add-size mt-2">Thêm size</button>
  `;
  container.appendChild(colorBlock);

  // Sự kiện xem trước ảnh
  const imageInput = colorBlock.querySelector('.var-image');
  const preview = colorBlock.querySelector('.color-preview');
  imageInput.addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        preview.src = event.target.result;
        preview.style.display = 'block';
      };
      reader.onerror = () => {
        preview.style.display = 'none';
        alert('Lỗi khi đọc file ảnh cho màu!');
      };
      reader.readAsDataURL(file);
    } else {
      preview.style.display = 'none';
      preview.src = '';
    }
  });

  // Thêm sự kiện cho btn-add-size sau khi tạo
  attachSizeEvents(colorBlock);

  // Cập nhật spinner sau khi thêm màu
  updateColorSpinner();
});

// Hàm gắn sự kiện cho btn-add-size, btn-remove-size và xử lý thay đổi size
function attachSizeEvents(colorBlock) {
  const btnAddSize = colorBlock.querySelector('.btn-add-size');
  const variationWrapper = colorBlock.querySelector('.variation-wrapper');

  btnAddSize.addEventListener('click', () => {
    const sizeRow = document.createElement('div');
    sizeRow.classList.add('variation-row', 'd-flex', 'gap-2', 'mb-2');
    updateSizeOptions(sizeRow, variationWrapper);
    variationWrapper.appendChild(sizeRow);

    // Gắn sự kiện xóa size
    sizeRow.querySelector('.btn-remove-size').addEventListener('click', () => {
      sizeRow.remove();
      updateAllSizeOptions(variationWrapper);
    });
  });

  // Gắn sự kiện xóa màu
  colorBlock.querySelector('.btn-remove-color').addEventListener('click', () => {
    colorBlock.remove();
    updateColorSpinner(); // Cập nhật spinner khi xóa màu
  });

  // Cập nhật tất cả select ban đầu
  updateAllSizeOptions(variationWrapper);
}

// Hàm cập nhật option của một select
function updateSizeOptions(sizeRow, wrapper, initialSize = null) {
  const currentSizes = Array.from(wrapper.querySelectorAll('.var-size'))
    .filter(select => select !== sizeRow.querySelector('.var-size'))
    .map(select => Number(select.value))
    .filter(size => !isNaN(size));
  sizeRow.innerHTML = `
    <select class="form-select var-size" required>
      <option value="">-- Chọn size --</option>
      ${[37, 38, 39, 40, 41]
        .filter(size => !currentSizes.includes(size) || (initialSize && size === initialSize))
        .map(size => `<option value="${size}" ${initialSize === size ? 'selected' : ''}>${size}</option>`).join('')}
    </select>
    <input type="number" class="form-control var-stock" placeholder="Tồn kho" required>
    <button type="button" class="btn btn-danger btn-remove-size">X</button>
  `;
  const select = sizeRow.querySelector('.var-size');
  if (initialSize && !currentSizes.includes(initialSize)) {
    select.value = initialSize;
  }
  select.addEventListener('change', () => updateAllSizeOptions(wrapper));
}

// Hàm cập nhật tất cả select trong cùng wrapper
function updateAllSizeOptions(wrapper) {
  const rows = wrapper.querySelectorAll('.variation-row');
  rows.forEach(row => {
    const currentSize = Number(row.querySelector('.var-size').value);
    const currentSizes = Array.from(wrapper.querySelectorAll('.var-size'))
      .filter(select => select !== row.querySelector('.var-size'))
      .map(select => Number(select.value))
      .filter(size => !isNaN(size) && size !== currentSize);
    const select = row.querySelector('.var-size');
    const selectedValue = select.value;
    select.innerHTML = `
      <option value="">-- Chọn size --</option>
      ${[37, 38, 39, 40, 41]
        .filter(size => !currentSizes.includes(size))
        .map(size => `<option value="${size}" ${size === Number(selectedValue) ? 'selected' : ''}>${size}</option>`).join('')}
    `;
    if (selectedValue && !currentSizes.includes(Number(selectedValue))) {
      select.value = selectedValue;
    }
  });
}

// Xử lý submit form thêm sản phẩm
const ThemPro = () => {
  document.getElementById('themPro').addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = new FormData(event.target);
    const avtInput = document.getElementById('avt_imgpro');
    if (avtInput && avtInput.files && avtInput.files.length > 0) {
      formData.append('avt_imgpro', avtInput.files[0]);
    }

    const colorBlocks = document.querySelectorAll('.color-block');
    const variations = [];

    colorBlocks.forEach((block, colorIndex) => {
      const colorName = block.querySelector('.var-color-name').value;
      const colorCode = block.querySelector('.var-color-code').value;
      const fileInput = block.querySelector('.var-image');
      const files = fileInput?.files || null;
      let listImg = [];

      console.log(`Color: ${colorName}, FileInput:`, fileInput, 'Files:', files);

      if (files && files.length > 0) {
        Array.from(files).forEach(file => {
          if (file) {
            formData.append(`variationImages-${colorIndex}`, file);
            listImg.push(file.name);
          }
        });
      } else {
        alert(`Vui lòng chọn ảnh cho màu ${colorName}!`);
        throw new Error(`No image selected for color ${colorName}`);
      }

      const rows = block.querySelectorAll('.variation-row');
      rows.forEach(row => {
        const size = row.querySelector('.var-size').value;
        const stock = row.querySelector('.var-stock').value;
        if (!size || !stock) {
          alert('Vui lòng điền đầy đủ size và tồn kho!');
          throw new Error('Dữ liệu không hợp lệ');
        }
        variations.push({
          size: Number(size),
          stock: Number(stock),
          sold: 0,
          color: { name: colorName, code: colorCode },
          list_imgproduct: listImg
        });
      });
    });

    if (variations.length === 0) {
      alert('Vui lòng thêm ít nhất một biến thể!');
      return;
    }

    formData.append("variations", JSON.stringify(variations));

    try {
      const response = await fetch(`http://${host}:${port}/api/add_product`, {
        method: 'POST',
        body: formData
      });
      if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
      const data = await response.json();
      alert('Thêm sản phẩm thành công');
      document.getElementById('themPro').reset();
      document.getElementById('color-block-container').innerHTML = '';
      document.getElementById('preview-image').style.display = 'none';
      updateColorSpinner(); // Cập nhật spinner sau khi thêm thành công
    } catch (error) {
      console.error('Lỗi khi thêm sản phẩm:', error);
      alert('Lỗi: ' + error.message);
    }
  });
};

ThemPro();

// Lọc sản phẩm theo danh mục
document.getElementById('pl_pro').addEventListener('change', function () {
  const selectedCate = this.value;
  const rows = document.querySelectorAll('#tbody tr');
  rows.forEach(row => {
    const rowCate = row.getAttribute('data-category');
    row.style.display = (selectedCate === "0" || !selectedCate) || rowCate === selectedCate ? '' : 'none';
  });
});

// Hiển thị danh sách sản phẩm
const hienThiPro = async () => {
  const tbody = document.querySelector('#tbody');
  try {
    const api = await fetch(`http://${host}:${port}/api/list_product`);
    if (!api.ok) throw new Error(`HTTP error! Status: ${api.status}`);
    const data = await api.json();
    console.log('Raw data from server:', data);
    tbody.innerHTML = '';

    data.forEach((item, index) => {
      const tr = document.createElement('tr');
      tr.setAttribute('data-category', item.id_category?._id || '');

      const tdSTT = document.createElement('td');
      tdSTT.textContent = index + 1;
      tdSTT.style.textAlign = 'center';

      const tdIMG = document.createElement('td');
      tdIMG.style.textAlign = 'center';
      const slideshowContainer = document.createElement('div');
      slideshowContainer.classList.add('slideshow-container');
      Object.assign(slideshowContainer.style, {
        position: 'relative',
        width: '150px',
        height: '150px',
        overflow: 'hidden',
        borderRadius: '8px',
        border: '1px solid #ccc'
      });

      let imgIndex = 0;
      const allImages = [
        item.avt_imgproduct ? [item.avt_imgproduct] : [],
        ...(item.variations?.map(v => v.list_imgproduct || []).flat() || [])
      ].flat().filter(img => img);
      console.log('All images collected:', allImages);
      console.log('Item variations:', item.variations);

      const img = document.createElement('img');
      img.src = allImages.length ? `http://${host}:${port}/uploads/${allImages[imgIndex]}` : '';
      if (!img.src) console.log('No valid image source for item:', item.nameproduct);
      Object.assign(img.style, { width: '100%', height: '100%', objectFit: 'contain' });
      slideshowContainer.appendChild(img);

      const btnPrev = document.createElement('button');
      btnPrev.innerHTML = '&#10094;';
      Object.assign(btnPrev.style, {
        position: 'absolute',
        top: '50%',
        left: '0',
        transform: 'translateY(-50%)',
        background: 'rgba(0,0,0,0.5)',
        color: 'white',
        border: 'none',
        cursor: 'pointer',
        padding: '5px',
        zIndex: '1'
      });
      btnPrev.addEventListener('click', () => {
        imgIndex = (imgIndex - 1 + allImages.length) % allImages.length;
        img.src = `http://${host}:${port}/uploads/${allImages[imgIndex]}?t=${Date.now()}`;
      });

      const btnNext = document.createElement('button');
      btnNext.innerHTML = '&#10095;';
      Object.assign(btnNext.style, {
        position: 'absolute',
        top: '50%',
        right: '0',
        transform: 'translateY(-50%)',
        background: 'rgba(0,0,0,0.5)',
        color: 'white',
        border: 'none',
        cursor: 'pointer',
        padding: '5px',
        zIndex: '1'
      });
      btnNext.addEventListener('click', () => {
        imgIndex = (imgIndex + 1) % allImages.length;
        img.src = `http://${host}:${port}/uploads/${allImages[imgIndex]}?t=${Date.now()}`;
      });

      slideshowContainer.appendChild(btnPrev);
      slideshowContainer.appendChild(btnNext);
      tdIMG.appendChild(slideshowContainer);

      const tdName = document.createElement('td');
      tdName.textContent = item.nameproduct;
      tdName.style.textAlign = 'center';

      const tdCate = document.createElement('td');
      tdCate.textContent = item.id_category?.title || 'N/A';
      tdCate.style.textAlign = 'center';

      const tdColor = document.createElement('td');
      const tdSize = document.createElement('td');
      const tdStock = document.createElement('td');
      const tdSold = document.createElement('td');

      if (item.variations?.length > 0) {
        let groupedHTML = { size: '', color: '', stock: '', sold: '' };
        const grouped = {};

        item.variations.forEach(v => {
          const colorName = v.color?.name || 'N/A';
          if (!grouped[colorName]) grouped[colorName] = [];
          grouped[colorName].push(v);
        });

        for (const color in grouped) {
          const variations = grouped[color];
          groupedHTML.color += variations.map(() => `
            <div style="height: 30px; display: flex; align-items: center; margin-top: 10px;">${color}</div>
          `).join('') + '<hr>';
          groupedHTML.size += variations.map(v => `
            <div style="height: 30px; display: flex; align-items: center; margin-top: 10px;">${v.size}</div>
          `).join('') + '<hr>';
          groupedHTML.stock += variations.map(v => `
            <div style="height: 30px; margin-top: 10px;" class="d-flex justify-content-between align-items-center">
              <span class="stock-value">${v.stock}</span>
              <button class="btn btn-sm btn-outline-primary p-1 edit-stock-btn"
                      data-product-id="${item._id}"
                      data-color="${v.color?.name}"
                      data-size="${v.size}"
                      title="Chỉnh sửa tồn kho">
                <i class="bi bi-pencil-square me-1" style="font-size: 0.8rem;"></i>Sửa
              </button>
            </div>
          `).join('') + '<hr>';
          groupedHTML.sold += variations.map(v => `
            <div style="height: 30px; display: flex; align-items: center; margin-top: 10px;">${v.sold}</div>
          `).join('') + '<hr>';
        }

        tdColor.innerHTML = groupedHTML.color;
        tdSize.innerHTML = groupedHTML.size;
        tdStock.innerHTML = groupedHTML.stock;
        tdSold.innerHTML = groupedHTML.sold;
      }
      [tdSize, tdColor, tdStock, tdSold].forEach(td => td.style.textAlign = 'center');

      const tdDis = document.createElement('td');
      tdDis.textContent = item.discount;
      tdDis.style.textAlign = 'center';

      const tdPriceE = document.createElement('td');
      tdPriceE.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.price_enter);
      tdPriceE.style.textAlign = 'center';

      const tdPrice = document.createElement('td');
      tdPrice.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.price);
      tdPrice.style.textAlign = 'center';

      const tdXL = document.createElement('td');
      tdXL.style.textAlign = 'center';

      const btnGroup = document.createElement('div');
      btnGroup.classList.add('d-flex', 'flex-column', 'gap-1', 'align-items-center');

      const btnDel = document.createElement('button');
      btnDel.textContent = 'Xóa';
      btnDel.classList.add('btn', 'btn-outline-danger');
      btnDel.addEventListener('click', async () => {
        if (confirm('Bạn muốn xóa sản phẩm này?')) {
          try {
            const response = await fetch(`http://${host}:${port}/api/del_product/${item._id}`, { method: 'DELETE' });
            if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
            tr.remove();
            alert('Xóa thành công');
            hienThiPro();
          } catch (error) {
            console.error('Lỗi khi xóa sản phẩm:', error);
          }
        }
      });

      const btnSua = document.createElement('button');
      btnSua.textContent = 'Sửa';
      btnSua.classList.add('btn', 'btn-outline-primary');
      btnSua.style.marginTop = '10px';
      btnSua.addEventListener('click', () => {
        editingProductId = item._id;
        window.currentEditingVariations = item.variations || [];
        document.getElementById("ThemPro").style.display = "none";

        document.getElementById('name_pro').value = item.nameproduct || '';
        document.getElementById('price_enter').value = item.price_enter || '';
        document.getElementById('price_pro').value = item.price || '';
        document.getElementById('category_pro').value = item.id_category?._id || '';
        document.getElementById('mota_pro').value = item.description || '';

        const avtPreview = document.getElementById('preview-image');
        if (item.avt_imgproduct) {
          avtPreview.src = `http://${host}:${port}/uploads/${item.avt_imgproduct}`;
          avtPreview.style.display = 'block';
        } else {
          avtPreview.style.display = 'none';
        }

        const colorBlockContainer = document.getElementById('color-block-container');
        colorBlockContainer.innerHTML = '';

        const grouped = {};
        item.variations?.forEach(variation => {
          const colorName = variation.color?.name || '';
          if (!grouped[colorName]) grouped[colorName] = [];
          grouped[colorName].push(variation);
        }) || {};

        Object.entries(grouped).forEach(([colorName, variations], colorIndex) => {
          const colorCode = convertToColorCode(colorName);
          const colorBlock = document.createElement('div');
          colorBlock.classList.add('color-block', 'border', 'p-3', 'mb-3');
          colorBlockContainer.appendChild(document.createElement('hr'));
          colorBlockContainer.appendChild(colorBlock);

          let listImg = [];
          variations.forEach(v => {
            if (Array.isArray(v.list_imgproduct)) {
              v.list_imgproduct.forEach(img => {
                if (img && !listImg.includes(img)) listImg.push(img);
              });
            }
          });
          colorBlock.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h5>${colorName}</h5>
              <button type="button" class="btn btn-danger btn-remove-color">X</button>
            </div>
            <input type="hidden" class="var-color-name" value="${colorName}">
            <input type="hidden" class="var-color-code" value="${colorCode}">
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label">Chọn ảnh cho màu này:</label>
                <input type="file" class="form-control var-image" accept="image/*" multiple>
              </div>
              <div class="col-md-6">
                <img class="color-preview" src="${listImg.length > 0 ? `http://${host}:${port}/uploads/${listImg[0]}` : ''}" alt="Preview Image" style="max-width: 150px; max-height: 150px; margin-top: 10px; ${listImg.length > 0 ? 'display:block;' : 'display:none;'}">
              </div>
            </div>
            <div class="variation-wrapper"></div>
            <button type="button" class="btn btn-primary btn-add-size mt-2">Thêm size</button>
          `;

          const varImageInput = colorBlock.querySelector('.var-image');
          const colorPreview = colorBlock.querySelector('.color-preview');
          varImageInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
              const reader = new FileReader();
              reader.onload = (event) => {
                colorPreview.src = event.target.result;
                colorPreview.style.display = 'block';
              };
              reader.onerror = () => {
                colorPreview.style.display = 'none';
                alert('Lỗi khi đọc file ảnh cho màu!');
              };
              reader.readAsDataURL(file);
            } else {
              colorPreview.style.display = 'none';
              colorPreview.src = '';
            }
          });

          const variationWrapper = colorBlock.querySelector('.variation-wrapper');
          variations.forEach(variation => {
            const sizeRow = document.createElement('div');
            sizeRow.classList.add('variation-row', 'd-flex', 'gap-2', 'mb-2');
            updateSizeOptions(sizeRow, variationWrapper, variation.size);
            variationWrapper.appendChild(sizeRow);
            const stockInput = sizeRow.querySelector('.var-stock');
            stockInput.value = variation.stock || '';
          });

          attachSizeEvents(colorBlock);
        });

        // Cập nhật spinner sau khi tạo các color-block trong chế độ sửa
        updateColorSpinner();

        let btnLuu = document.getElementById('btnLuuSua');
        if (!btnLuu) {
          btnLuu = document.createElement('button');
          btnLuu.id = 'btnLuuSua';
          btnLuu.textContent = 'Lưu';
          btnLuu.className = 'btn btn-success mt-3';
          document.getElementById('themPro').appendChild(btnLuu);
          btnLuu.addEventListener('click', async (e) => {
            e.preventDefault();
            const formData = new FormData(document.getElementById('themPro'));

            const colorBlocks = document.querySelectorAll('.color-block');
            const variations = [];

            if (colorBlocks.length === 0) {
              alert('Vui lòng thêm ít nhất một màu!');
              return;
            }

            let hasError = false;
            colorBlocks.forEach((block, colorIndex) => {
              const colorName = block.querySelector('.var-color-name').value;
              const colorCode = block.querySelector('.var-color-code').value;
              const fileInput = block.querySelector('.var-image');
              const files = fileInput?.files || null;
              const colorPreview = block.querySelector('.color-preview');
              let listImg = [];

              console.log(`Processing color: ${colorName}, files:`, files);

              if (files && files.length > 0) {
                Array.from(files).forEach(file => {
                  if (file) {
                    formData.append(`variationImages-${colorIndex}`, file);
                    listImg.push(file.name);
                  }
                });
              } else {
                const oldVariation = (window.currentEditingVariations || []).find(v => v.color?.name === colorName);
                if (oldVariation && oldVariation.list_imgproduct?.length > 0) {
                  listImg = oldVariation.list_imgproduct;
                  formData.append(`variationImages-${colorIndex}_old`, oldVariation.list_imgproduct[0]);
                } else if (!window.currentEditingVariations.some(v => v.color?.name === colorName)) {
                  alert(`Bạn phải chọn ảnh cho màu mới: ${colorName}`);
                  hasError = true;
                  return;
                }
              }

              const rows = block.querySelectorAll('.variation-row');
              if (rows.length === 0) {
                alert(`Vui lòng thêm ít nhất một size cho màu ${colorName}!`);
                hasError = true;
                return;
              }

              rows.forEach(row => {
                const size = row.querySelector('.var-size').value;
                const stock = row.querySelector('.var-stock').value;
                if (!size || !stock || stock.trim() === '') {
                  alert(`Vui lòng điền đầy đủ size và tồn kho cho màu ${colorName}!`);
                  hasError = true;
                  return;
                }
                variations.push({
                  size: Number(size),
                  stock: Number(stock),
                  color: { name: colorName, code: colorCode },
                  list_imgproduct: listImg
                });
              });
            });

            if (hasError || variations.length === 0) {
              return;
            }

            formData.append("variations", JSON.stringify(variations));
            const avtInput = document.getElementById('avt_imgpro');
            if (avtInput && avtInput.files.length > 0) {
              formData.append('avt_imgproduct', avtInput.files[0]);
            }

            try {
              const response = await fetch(`http://${host}:${port}/api/update_product/${editingProductId}`, {
                method: 'PUT',
                body: formData
              });
              if (!response.ok) {
                const result = await response.json();
                alert('Lỗi cập nhật: ' + (result.message || ''));
                return;
              }
              alert('Cập nhật thành công');
              hienThiPro();
              btnLuu.remove();
              document.getElementById("ThemPro").style.display = "block";
              document.getElementById('themPro').reset();
              document.getElementById('preview-image').style.display = 'none';
              document.getElementById('preview-image').src = '';
              document.getElementById('color-block-container').innerHTML = '';
            } catch (err) {
              alert('Lỗi khi cập nhật sản phẩm');
            }
          });
        }
        let btnHuy = document.createElement('button');
        btnHuy.id = 'btnHuy';
        btnHuy.textContent = 'Hủy';
        btnHuy.className = 'btn btn-danger mt-3';
        btnHuy.style.marginLeft = '10px';
        document.getElementById('themPro').appendChild(btnHuy);
        btnHuy.addEventListener('click', () => {
          document.getElementById("ThemPro").style.display = "block";
          document.getElementById('themPro').reset();
          document.getElementById('preview-image').style.display = 'none';
          document.getElementById('preview-image').src = '';
          document.getElementById('color-block-container').innerHTML = '';
          if (btnLuu.parentNode) btnLuu.parentNode.removeChild(btnLuu);
          if (btnHuy.parentNode) btnHuy.parentNode.removeChild(btnHuy);
          hienThiPro();
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
      });

      const btnGG = document.createElement('button');
      btnGG.textContent = 'Giảm giá';
      btnGG.classList.add('btn', 'btn-outline-warning');
      btnGG.style.marginTop = '10px';

      btnGroup.appendChild(btnDel);
      btnGroup.appendChild(btnSua);
      btnGroup.appendChild(btnGG);
      tdXL.appendChild(btnGroup);

      tr.appendChild(tdSTT);
      tr.appendChild(tdIMG);
      tr.appendChild(tdName);
      tr.appendChild(tdCate);
      tr.appendChild(tdColor);
      tr.appendChild(tdSize);
      tr.appendChild(tdStock);
      tr.appendChild(tdSold);
      tr.appendChild(tdDis);
      tr.appendChild(tdPriceE);
      tr.appendChild(tdPrice);
      tr.appendChild(tdXL);
      tbody.appendChild(tr);

      tr.querySelectorAll('.edit-stock-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
          const btn = e.target.closest('.edit-stock-btn');
          const wrapper = btn.parentElement;
          const stockSpan = wrapper.querySelector('.stock-value');
          const currentStock = stockSpan.textContent;
          const input = prompt('Nhập số lượng mới:', currentStock);
          const newStock = parseInt(input);

          if (input.trim() === '' || !/^\d+$/.test(input) || isNaN(newStock) || newStock < 0) {
            alert('Vui lòng nhập số hợp lệ!');
            return;
          }

          try {
            const productId = btn.getAttribute('data-product-id');
            const color = btn.getAttribute('data-color');
            const size = btn.getAttribute('data-size');
            const response = await fetch(`http://${host}:${port}/api/update_stock`, {
              method: 'PUT',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ productId, color, size, stock: newStock })
            });
            const result = await response.json();
            if (response.ok) {
              stockSpan.textContent = newStock;
              alert('Cập nhật số lượng thành công');
            } else {
              alert('Cập nhật thất bại: ' + result.message);
            }
          } catch (err) {
            console.error(err);
            alert('Lỗi khi cập nhật số lượng');
          }
        });
      });
    });
  } catch (error) {
    console.error('Fetch error:', error);
  }
};
hienThiPro();

document.getElementById('avt_imgpro').addEventListener('change', function(e) {
  const file = e.target.files[0];
  const preview = document.getElementById('preview-image');
  if (file) {
    const reader = new FileReader();
    reader.onload = (event) => {
      preview.src = event.target.result;
      preview.style.display = 'block';
    };
    reader.onerror = () => {
      preview.style.display = 'none';
      alert('Lỗi khi đọc file ảnh!');
    };
    reader.readAsDataURL(file);
  } else {
    preview.style.display = 'none';
    preview.src = '';
  }
});