const host = window.config;

const ThemVoucher = () => {
    const form = document.getElementById('themVoucher');
    if (!form) {
        console.error('Form với ID "themVoucher" không tồn tại');
        return;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = {
            code: form.code.value.trim(),
            description: form.description.value.trim(),
            discountType: form.discountType.value,
            discountValue: parseFloat(form.discountValue.value),
            minOrderValue: parseFloat(form.minOrderValue.value),
            usageLimit: parseInt(form.usageLimit.value),
            startDate: new Date(form.startDate.value).toISOString(), // Chuyển sang UTC
            endDate: new Date(form.endDate.value).toISOString(),     // Chuyển sang UTC
        };

        // Kiểm tra dữ liệu đầu vào
        if (!formData.code || !formData.description) {
            alert('Vui lòng nhập đầy đủ mã và mô tả voucher');
            return;
        }
        if (isNaN(formData.discountValue) || formData.discountValue <= 0) {
            alert('Giá trị giảm phải là số dương');
            return;
        }
        if (isNaN(formData.minOrderValue) || formData.minOrderValue < 0) {
            alert('Giá trị đơn hàng tối thiểu không hợp lệ');
            return;
        }
        if (isNaN(formData.usageLimit) || formData.usageLimit <= 0) {
            alert('Số lần sử dụng phải là số dương');
            return;
        }
        if (!formData.startDate || !formData.endDate) {
            alert('Vui lòng chọn ngày bắt đầu và ngày hết hạn');
            return;
        }
        if (new Date(formData.startDate) >= new Date(formData.endDate)) {
            alert('Ngày bắt đầu phải trước ngày hết hạn');
            return;
        }

        try {
            const response = await fetch(`http://${config.host}:${config.port}/api/add_voucher`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            alert('Thêm voucher thành công!');
            hienThiVoucher();
        } catch (error) {
            console.error('Lỗi khi thêm voucher:', error);
            alert('Lỗi khi thêm voucher: ' + error.message);
        }
    });
};
// Gọi hàm để gắn sự kiện
ThemVoucher();

const hienThiVoucher = async () => {
    const tbody = document.querySelector('#tbody');
    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/list_voucher`);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const result = await response.json();
        console.log('Dữ liệu từ API:', result); // Debug để kiểm tra dữ liệu
        const data = result.vouchers; // Truy cập mảng vouchers
        if (!Array.isArray(data)) {
            throw new Error('Dữ liệu từ API không phải là mảng');
        }
        tbody.innerHTML = ''; // Xóa nội dung cũ
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" style="text-align: center;">Không có voucher nào</td></tr>';
            return;
        }
        const now = new Date(); // Lấy ngày giờ hiện tại (10:13 PM +07, 17/08/2025)
        for (const item of data) {
            if (new Date(item.endDate) < now && item.isActive) {
                await updateVoucherStatus(item._id, false); // Cập nhật trạng thái thành false nếu hết hạn
            }
        }
        data.forEach((item, index) => {
            const tr = document.createElement('tr');
            tr.setAttribute('data-voucher', item.isActive ? '2' : '1');

            const tdSTT = document.createElement('td');
            tdSTT.textContent = index + 1;
            tdSTT.style.textAlign = 'center';
            tdSTT.style.alignContent = 'center';

            const tdMa = document.createElement('td');
            tdMa.textContent = item.code;
            tdMa.style.textAlign = 'center';
            tdMa.style.alignContent = 'center';

            const tdGTG = document.createElement('td');
            tdGTG.textContent = item.discountValue.toLocaleString('vi-VN') + (item.discountType === 'percent' ? '%' : ' đ');
            tdGTG.style.textAlign = 'center';
            tdGTG.style.alignContent = 'center';

            const tdDTT = document.createElement('td');
            tdDTT.textContent = item.minOrderValue.toLocaleString('vi-VN') + ' đ';
            tdDTT.style.textAlign = 'center';
            tdDTT.style.alignContent = 'center';

            const tdTLD = document.createElement('td');

            const wrapper = document.createElement('div');
            wrapper.style.display = 'flex';
            wrapper.style.justifyContent = 'space-between';
            wrapper.style.alignItems = 'center';  
            wrapper.style.width = '100%';           
            wrapper.style.height = '100%';

            const usageText = document.createElement('span');
            usageText.textContent = item.usageLimit;

            const btnSua = document.createElement('button');
            btnSua.textContent = 'Sửa';
            btnSua.className = 'btn btn-sm btn-outline-primary';
            btnSua.style.minWidth = '50px';

            btnSua.addEventListener('click', () => editUsageLimit(item._id, item.usageLimit()));

            wrapper.appendChild(usageText);
            wrapper.appendChild(btnSua);
            tdTLD.appendChild(wrapper);

            const tdLDD = document.createElement('td');
            tdLDD.textContent = item.usedCount;
            tdLDD.style.textAlign = 'center';
            tdLDD.style.alignContent = 'center';

            const tdMota = document.createElement('td');
            tdMota.textContent = item.description;
            tdMota.style.textAlign = 'center';
            tdMota.style.alignContent = 'center';

            const tdHSD = document.createElement('td');
            // Định dạng ngày giờ
            const startDate = new Date(item.startDate);
            const endDate = new Date(item.endDate);
            const formatDate = (date) => {
                const day = String(date.getDate()).padStart(2, '0');
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const year = date.getFullYear();
                const hours = String(date.getHours()).padStart(2, '0');
                const minutes = String(date.getMinutes()).padStart(2, '0');
                return `${day}/${month}/${year} ${hours}:${minutes}`;
            };
            tdHSD.textContent = `${formatDate(startDate)} - ${formatDate(endDate)}`;
            tdHSD.style.textAlign = 'center';
            tdHSD.style.alignContent = 'center';

            const tdTT = document.createElement('td');
            tdTT.textContent = item.isActive ? 'Hoạt động' : 'Ngừng hoạt động';
            tdTT.style.textAlign = 'center';
            tdTT.style.alignContent = 'center';

            const tdXoa = document.createElement('td');
            const btnXoa = document.createElement('button');
            btnXoa.textContent = 'Xóa';
            btnXoa.className = 'btn btn-danger btn-sm';
            btnXoa.addEventListener('click', async () => {
                try {
                    const conf = confirm(`Bạn muốn xóa voucher ${item.code} này ?`);
                    if (conf) {
                        const response = await fetch(`http://${config.host}:${config.port}/api/del_voucher/${item._id}`, {
                            method: 'DELETE'
                        });

                        if (!response.ok) {
                            throw new Error(`HTTP error! Status: ${response.status}`);
                        }
                        tr.remove();
                        alert('Xóa thành công');
                        hienThiVoucher();
                    }
                } catch (error) {
                    console.error('Lỗi khi xóa thể loại:', error);
                }
            })
            tdXoa.style.textAlign = 'center';
            tdXoa.appendChild(btnXoa);

            tr.appendChild(tdSTT);
            tr.appendChild(tdMa);
            tr.appendChild(tdGTG);
            tr.appendChild(tdDTT);
            tr.appendChild(tdTLD);
            tr.appendChild(tdLDD);
            tr.appendChild(tdMota);
            tr.appendChild(tdHSD);
            tr.appendChild(tdTT);
            tr.appendChild(tdXoa);
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('Lỗi khi lấy danh sách voucher:', error);
        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center;">Lỗi khi tải dữ liệu</td></tr>';
    }
};
hienThiVoucher();

const updateVoucherStatus = async (voucherId, isActive) => {
    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/update_voucher_status/${voucherId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ isActive })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const result = await response.json();
        hienThiVoucher();
        console.log('Cập nhật trạng thái thành công:', result);
    } catch (error) {
        console.error('Lỗi khi cập nhật trạng thái:', error);
    }
};

const updateUsageLimit = async (voucherId, usageLimit) => {
    try {
        const response = await fetch(`http://${config.host}:${config.port}/api/update_usage_limit/${voucherId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ usageLimit })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const result = await response.json();
        console.log('Cập nhật usageLimit thành công:', result);
        alert('Cập nhật số lần sử dụng thành công!');
        await hienThiVoucher(); // Làm mới danh sách
    } catch (error) {
        console.error('Lỗi khi cập nhật usageLimit:', error);
        alert('Lỗi khi cập nhật số lần sử dụng: ' + error.message);
    }
};

const editUsageLimit = (voucherId, currentUsageLimit) => {
    const newUsageLimit = prompt(`Nhập số lần sử dụng mới cho voucher (hiện tại: ${currentUsageLimit}):`, currentUsageLimit);
    if (newUsageLimit === null) return; // Người dùng nhấn Cancel

    const usageLimit = parseInt(newUsageLimit);
    if (isNaN(usageLimit) || usageLimit <= 0) {
        alert('Số lần sử dụng phải là số nguyên dương!');
        return;
    }

    if (usageLimit < currentUsageLimit && confirm('Số lần sử dụng mới nhỏ hơn số lần đã dùng. Bạn có chắc chắn?')) {
        updateUsageLimit(voucherId, usageLimit);
    } else if (usageLimit >= currentUsageLimit) {
        updateUsageLimit(voucherId, usageLimit);
    }
};

document.getElementById('pl_voucher').addEventListener('change', function () {
    const selectedRole = this.value;
    const rows = document.querySelectorAll('#tbody tr');

    rows.forEach(row => {
        const role = row.getAttribute('data-voucher');

        if (selectedRole === "3") {
            row.style.display = ""; // Hiện tất cả
        } else {
            row.style.display = role === selectedRole ? "" : "none";
        }
    });
});

