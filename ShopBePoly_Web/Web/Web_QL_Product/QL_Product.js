const host = window.config;

window.addEventListener('DOMContentLoaded', async () =>{
    const cate_selet = document.getElementById('category_pro');
    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/list_category`);
        const categories = await response.json();

        // Xoá các option cũ (nếu cần)
        cate_selet.innerHTML = '<option value="">-- Chọn hãng giày --</option>';

        categories.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat._id; // hoặc cat.id nếu bạn dùng SQL
            option.textContent = cat.title; // tuỳ theo cấu trúc DB
            cate_selet.appendChild(option);
        });
    } catch (error) {
        console.error('Lỗi khi lấy danh sách category:', error);
    }
})
const ThemPro = () => {
    document.getElementById('themPro').addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);

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
}
ThemPro();

const hienThiPro = async () => {
    const tbody = document.querySelector('#tbody');

    try {
        const api = await fetch(`http://${config.host}:${config.port}/api/list_product`);
        if (!api.ok) {
            throw new Error(`HTTP error! Status: ${api.status}`);
        }
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

            const tdName = document.createElement('td');
            tdName.textContent = item.nameproduct;
            tdName.style.textAlign = 'center';

            const tdCate = document.createElement('td');
            tdCate.textContent = item.id_category ? item.id_category.title : 'N/A';
            tdCate.style.textAlign = 'center';

            const tdSlg = document.createElement('td');
            tdSlg.textContent = item.quantity;
            tdSlg.style.textAlign = 'center';

            const tdSize = document.createElement('td');
            tdSize.textContent = item.size;
            tdSize.style.textAlign = 'center';

            const tdColor = document.createElement('td');
            tdColor.textContent = item.color;
            tdColor.style.textAlign = 'center';

            const tdStock = document.createElement('td');
            tdStock.textContent = item.stock;
            tdStock.style.textAlign = 'center';

            const tdSold = document.createElement('td');
            tdSold.textContent = item.sold;
            tdSold.style.textAlign = 'center';

            const tdPrice = document.createElement('td');
            tdPrice.textContent = item.price;
            tdPrice.style.textAlign = 'center';

            tr.appendChild(tdSTT);
            tr.appendChild(tdIMG);
            tr.appendChild(tdName);
            tr.appendChild(tdCate);
            tr.appendChild(tdSlg);
            tr.appendChild(tdSize);
            tr.appendChild(tdColor);
            tr.appendChild(tdStock);
            tr.appendChild(tdSold);
            tr.appendChild(tdPrice);

            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('Fetch error:', error);
    }
};

hienThiPro();
