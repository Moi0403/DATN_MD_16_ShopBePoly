const host = window.config;

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

document.getElementById('add-variation').addEventListener('click', () => {
    const wrapper = document.getElementById('variation-wrapper');
    const newRow = document.createElement('div');
    newRow.classList.add('variation-row', 'd-flex', 'gap-2', 'mb-3');
    newRow.innerHTML = `
    <select class="form-select var-size" required>
        <option value="">-- Chọn size --</option>
        <option value="37">37</option>
        <option value="38">38</option>
        <option value="39">39</option>
        <option value="40">40</option>
        <option value="41">41</option>
    </select>
    <input type="number" class="form-control var-stock" placeholder="Tồn kho" required>
    <input type="text" class="form-control var-color" placeholder="Màu sắc" list="colors" required>
    <input type="file" class="form-control var-image" accept="image/*" required>
    <button type="button" class="btn btn-danger btn-remove-size">X</button>
`;

    wrapper.appendChild(newRow);
});

// Xử lý nút xóa
document.getElementById('variation-wrapper').addEventListener('click', (e) => {
    if (e.target.classList.contains('btn-remove-size')) {
        e.target.closest('.variation-row').remove();
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

        const variationElements = document.querySelectorAll('.variation-row');
        const variations = [];

        variationElements.forEach((row, index) => {
            const size = row.querySelector('select.var-size').value;
            const stock = row.querySelector('.var-stock').value;
            const color = row.querySelector('.var-color').value;
            const imageFiles = row.querySelector('.var-image').files;

            if (size && stock && color && imageFiles.length > 0) {
                const listImg = [];

                for (let i = 0; i < imageFiles.length; i++) {
                    const file = imageFiles[i];
                    formData.append(`variationImages-${index}`, file);
                    listImg.push(file.name);
                }

                variations.push({
                    size,
                    stock: Number(stock),
                    sold: 0,
                    color: {
                        name: color,
                        code: convertToColorCode(color)
                    },
                    list_imgproduct: listImg
                });
            }
        });

        formData.append("variations", JSON.stringify(variations));

        try {
            const response = await fetch(`http://${config.host}:${config.port}/api/add_product`, {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

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

            const tdIMG = document.createElement('td');
            const img = document.createElement('img');
            img.src = `http://localhost:3000/uploads/${item.avt_imgproduct}`;
            img.width = 100;
            img.height = 50;
            img.style.objectFit = 'contain';
            tdIMG.appendChild(img);
            tdIMG.style.textAlign = 'center';

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
                tdSize.innerHTML = item.variations.map(v => v.size).join('<br>');
                tdColor.innerHTML = item.variations.map(v => v.color?.name || 'N/A').join('<br>');
                tdStock.innerHTML = item.variations.map(v => v.stock).join('<br>');
                tdSold.innerHTML = item.variations.map(v => v.sold).join('<br>');

            } else {
                tdSize.textContent = tdColor.textContent = tdStock.textContent = tdSold.textContent = 'N/A';
            }

            [tdSize, tdColor, tdStock, tdSold].forEach(td => td.style.textAlign = 'center');

            const tdPrice = document.createElement('td');
            tdPrice.textContent = item.price + ".000 đ";
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
            tr.appendChild(tdSize);
            tr.appendChild(tdColor);
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
