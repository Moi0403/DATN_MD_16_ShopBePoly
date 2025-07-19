const host = window.config;

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

window.addEventListener('DOMContentLoaded', async () => {
    const cate_selet = document.getElementById('category_pro');
    const plSelect = document.getElementById('pl_pro');
    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/list_category`);
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
    switch (color.toLowerCase()) {
        case 'đen': return 'black';
        case 'trắng': return 'white';
        case 'đỏ': return 'red';
        case 'xanh': return 'blue';
        case 'vàng': return 'yellow';
        default: return '#ccc';
    }
}

// Xử lý thêm size trong từng khối màu
document.getElementById('addColorBlock').addEventListener('click', () => {
    const colorName = document.getElementById('colorSpinner').value;
    const colorCode = convertToColorCode(colorName);
    const container = document.getElementById('color-block-container');

    // Kiểm tra trùng màu
    const existing = [...document.querySelectorAll('.var-color-name')].some(el => el.value === colorName);
    if (existing) {
        alert("Màu này đã được thêm.");
        return;
    }

    const colorBlock = document.createElement('div');
    colorBlock.classList.add('color-block', 'border', 'p-3', 'mb-3');
    const divider = document.createElement('hr');
    container.appendChild(divider);


    colorBlock.innerHTML = `
        <div class="d-flex justify-content-between align-items-center mb-2">
            <h5>${colorName}</h5>
            <button type="button" class="btn btn-danger btn-remove-color">X</button>
        </div>

        <input type="hidden" class="var-color-name" value="${colorName}">
        <input type="hidden" class="var-color-code" value="${colorCode}">

        <div class="mb-2">
            <label>Chọn ảnh cho màu này:</label>
            <input type="file" class="form-control var-image" accept="image/*" multiple required>
        </div>

        <div class="variation-wrapper"></div>
        <button type="button" class="btn btn-primary btn-add-size mt-2">Thêm size</button>
    `;

    container.appendChild(colorBlock);
});



document.addEventListener('click', (e) => {
    if (e.target.classList.contains('btn-add-size')) {
        const wrapper = e.target.previousElementSibling;
        const sizeRow = document.createElement('div');
        sizeRow.classList.add('variation-row', 'd-flex', 'gap-2', 'mb-2');
        sizeRow.innerHTML = `
            <select class="form-select var-size" required>
                <option value="">-- Chọn size --</option>
                <option value="37">37</option>
                <option value="38">38</option>
                <option value="39">39</option>
                <option value="40">40</option>
                <option value="41">41</option>
            </select>
            <input type="number" class="form-control var-stock" placeholder="Tồn kho" required>
            <button type="button" class="btn btn-danger btn-remove-size">X</button>
        `;
        wrapper.appendChild(sizeRow);
    }

    if (e.target.classList.contains('btn-remove-size')) {
        e.target.closest('.variation-row').remove();
    }

    if (e.target.classList.contains('btn-remove-color')) {
        e.target.closest('.color-block').remove();
    }
});

const ThemPro = () => {
    document.getElementById('themPro').addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);
        const avtInput = document.getElementById('avt_imgpro');
        if (avtInput && avtInput.files.length > 0) {
            formData.append('avt_imgpro', avtInput.files[0]);
        }

        const colorBlocks = document.querySelectorAll('.color-block');
        const variations = [];
        let imgIndex = 0;

        colorBlocks.forEach((block, colorIndex) => {
            const colorName = block.querySelector('.var-color-name').value;
            const colorCode = block.querySelector('.var-color-code').value;
            const files = block.querySelector('.var-image')?.files || [];

            const listImg = [];

            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                // ❗ Sửa lại index theo màu
                formData.append(`variationImages-${colorIndex}`, file);
                listImg.push(file.name);
            }

            const rows = block.querySelectorAll('.variation-row');
            rows.forEach(row => {
                const size = row.querySelector('.var-size').value;
                const stock = row.querySelector('.var-stock').value;

                variations.push({
                    size,
                    stock: Number(stock),
                    sold: 0,
                    color: { name: colorName, code: colorCode },
                    list_imgproduct: listImg
                });
            });
        });



        formData.append("variations", JSON.stringify(variations));

        try {
            const response = await fetch(`http://${config.host}:${config.port}/api/add_product`, {
                method: 'POST',
                body: formData
            });

            if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
            const data = await response.json();
            alert('Thêm sản phẩm thành công');
        } catch (error) {
            console.error('Lỗi khi thêm sản phẩm:', error);
        }
    });
};

ThemPro();


document.getElementById('pl_pro').addEventListener('change', function () {
    const selectedCate = this.value;
    const rows = document.querySelectorAll('#tbody tr');

    rows.forEach(row => {
        const rowCate = row.getAttribute('data-category');
        row.style.display = (selectedCate === "0" || selectedCate === "") || rowCate === selectedCate ? "" : "none";
    });
});

const hienThiPro = async () => {
    const tbody = document.querySelector('#tbody');

    try {
        const api = await fetch(`http://${config.host}:${config.port}/api/list_product`);
        if (!api.ok) throw new Error(`HTTP error! Status: ${api.status}`);

        const data = await api.json();
        tbody.innerHTML = '';

        data.forEach((item, index) => {
            const tr = document.createElement('tr');

            const tdSTT = document.createElement('td');
            tdSTT.textContent = index + 1;
            tdSTT.style.textAlign = 'center';

            // const tdIMG = document.createElement('td');
            // const img = document.createElement('img');
            // img.src = `http://localhost:3000/uploads/${item.avt_imgproduct}`;
            // img.width = 100;
            // img.height = 50;
            // img.style.objectFit = 'contain';
            // tdIMG.appendChild(img);
            // tdIMG.style.textAlign = 'center';


            const tdIMG = document.createElement('td');
            tdIMG.style.textAlign = 'center';

            // Tạo container slideshow
            const slideshowContainer = document.createElement('div');
            slideshowContainer.classList.add('slideshow-container');
            slideshowContainer.style.position = 'relative';
            slideshowContainer.style.width = '150px';
            slideshowContainer.style.height = '150px';
            slideshowContainer.style.overflow = 'hidden';
            slideshowContainer.style.borderRadius = '8px';
            slideshowContainer.style.border = '1px solid #ccc';

            let imgIndex = 0;
            const images = [
                item.avt_imgproduct,
                ...(item.variations?.flatMap(v => v.list_imgproduct || []) || [])
            ];

            // Tạo thẻ <img>
            const img = document.createElement('img');
            img.src = `http://localhost:3000/uploads/${images[imgIndex]}`;
            img.style.width = '100%';
            img.style.height = '100%';
            img.style.objectFit = 'contain';
            slideshowContainer.appendChild(img);

            // Nút prev
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

            // Nút next
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

            // Sự kiện chuyển ảnh
            btnPrev.addEventListener('click', () => {
                imgIndex = (imgIndex - 1 + images.length) % images.length;
                img.src = `http://localhost:3000/uploads/${images[imgIndex]}?t=${Date.now()}`;
            });
            btnNext.addEventListener('click', () => {
                imgIndex = (imgIndex + 1) % images.length;
                img.src = `http://localhost:3000/uploads/${images[imgIndex]}?t=${Date.now()}`;
            });

            slideshowContainer.appendChild(btnPrev);
            slideshowContainer.appendChild(btnNext);
            tdIMG.appendChild(slideshowContainer);



            const colorDotsDiv = document.createElement('div');
            colorDotsDiv.classList.add('color-dots');

            const imageMap = {};

            item.variations?.forEach((variation, i) => {
                if (variation.color?.code) {
                    const dot = document.createElement('span');
                    dot.classList.add('color-dot');
                    dot.style.backgroundColor = variation.color.code;


                    const imgForColor = item.list_imgproduct[i] || item.avt_imgproduct;

                    dot.addEventListener('click', () => {
                        const imgList = variation.list_imgproduct || [];
                        let index = 0;

                        const updateImage = () => {
                            img.src = `http://localhost:3000/uploads/${imgList[index]}?t=${Date.now()}`;
                            index = (index + 1) % imgList.length;
                        };

                        updateImage();
                        if (imgList.length > 1) {
                            setInterval(updateImage, 3000); // slider tự động lướt ảnh mỗi 3s
                        }
                    });


                    colorDotsDiv.appendChild(dot);
                }
            });


            // tdIMG.appendChild(document.createElement('br'));
            // tdIMG.appendChild(colorDotsDiv);


            const tdName = document.createElement('td');
            tdName.textContent = item.nameproduct;
            tdName.style.textAlign = 'center';

            const tdCate = document.createElement('td');
            tdCate.textContent = item.id_category?.title || 'N/A';
            tdCate.style.textAlign = 'center';

            const tdSize = document.createElement('td');
            const tdColor = document.createElement('td');
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

                    groupedHTML.size += variations.map(v => v.size).join('<br>') + '<hr>';
                    groupedHTML.color += variations.map(() => color).join('<br>') + '<hr>';
                    groupedHTML.stock += variations.map(v => v.stock).join('<br>') + '<hr>';
                    groupedHTML.sold += variations.map(v => v.sold).join('<br>') + '<hr>';
                }

                tdSize.innerHTML = groupedHTML.size;
                tdColor.innerHTML = groupedHTML.color;
                tdStock.innerHTML = groupedHTML.stock;
                tdSold.innerHTML = groupedHTML.sold;
            }
            [tdSize, tdColor, tdStock, tdSold].forEach(td => td.style.textAlign = 'center');


            const tdPrice = document.createElement('td');
            tdPrice.textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.price);
            tdPrice.style.textAlign = 'center';

            const tdXL = document.createElement('td');
            tdXL.classList.add('tdXL');
            tdXL.style.textAlign = 'center';

            const btnDel = document.createElement('button');
            btnDel.textContent = 'Xóa';
            btnDel.classList.add('btn', 'btn-outline-primary');
            btnDel.addEventListener('click', async () => {
                try {
                    const conf = confirm(`Bạn muốn xóa sản phẩm này?`);
                    if (conf) {
                        const response = await fetch(`http://${config.host}:${config.port}/api/del_product/${item._id}`, {
                            method: 'DELETE'
                        });

                        if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);

                        tr.remove();
                        alert('Xóa thành công');
                        hienThiPro();
                    }
                } catch (error) {
                    console.error('Lỗi khi xóa sản phẩm:', error);
                }
            });

            tdXL.appendChild(btnDel);

            tr.appendChild(tdSTT);
            tr.appendChild(tdIMG);
            tr.appendChild(tdName);
            tr.appendChild(tdCate);
            tr.appendChild(tdColor);
            tr.appendChild(tdSize);
            tr.appendChild(tdStock);
            tr.appendChild(tdSold);
            tr.appendChild(tdPrice);
            tr.appendChild(tdXL);

            tr.setAttribute('data-category', item.id_category?._id || '');

            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('Fetch error:', error);
    }
};
hienThiPro();
