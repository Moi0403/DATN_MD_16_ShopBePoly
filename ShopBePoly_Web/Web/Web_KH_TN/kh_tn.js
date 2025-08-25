const host = window.config;

// Lấy và cập nhật ngày hôm nay dưới dạng YYYY-MM-DD theo múi giờ Việt Nam
function getTodayInVietnam() {
    const nowVN = new Date().toLocaleString("en-US", { timeZone: "Asia/Ho_Chi_Minh" });
    const [month, day, year] = nowVN.split(",")[0].split("/");
    return new Date(`${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`).toISOString().split("T")[0];
}
let today = getTodayInVietnam();

const handleTimeFilterChange = () => {
    const timeFilter = document.getElementById('timeFilter').value;
    const dateRange = document.getElementById('dateRange');
    const dateRangeEnd = document.getElementById('dateRangeEnd');

    if (timeFilter === 'custom') {
        dateRange.style.display = 'block';
        dateRangeEnd.style.display = 'block';
    } else {
        dateRange.style.display = 'none';
        dateRangeEnd.style.display = 'none';
    }
};

function validateDates() {
    const timeFilter = document.getElementById('timeFilter')?.value || 'custom';
    const startDate = document.getElementById('startDate')?.value;
    const endDate = document.getElementById('endDate')?.value || today;

    if (timeFilter === 'custom' && (!startDate && !endDate)) {
        alert('Vui lòng chọn ít nhất một ngày bắt đầu hoặc ngày kết thúc!');
        return false;
    }

    if (timeFilter === 'custom' && startDate && endDate && new Date(startDate) > new Date(endDate)) {
        alert('Ngày bắt đầu không thể sau ngày kết thúc!');
        return false;
    }

    const todayVN = getTodayInVietnam();
    if (endDate && new Date(endDate).setHours(0, 0, 0, 0) > new Date(todayVN).setHours(0, 0, 0, 0)) {
        alert('Ngày kết thúc không thể sau ngày hiện tại!');
        return false;
    }

    return true;
}

const hienThiTop = async () => {
    if (!validateDates()) return;

    const tbody = document.querySelector('#tbody');
    const timeFilter = document.getElementById('timeFilter')?.value || 'custom';
    const startDate = document.getElementById('startDate')?.value;
    const endDate = document.getElementById('endDate')?.value || today;

    let url = new URL(`http://${config.host}:${config.port}/api/top-buyers`);
    console.log('Time Filter:', timeFilter); // Debug
    if (timeFilter === 'week' || timeFilter === 'month' || timeFilter === 'year') {
        url.searchParams.append('time', timeFilter);
        console.log('URL with time:', url.toString());
    } else if (timeFilter === 'custom') {
        if (startDate) url.searchParams.append('startDate', startDate);
        if (endDate) url.searchParams.append('endDate', endDate);
        console.log('URL with custom dates:', url.toString());
    }

    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const result = await response.json();
        console.log('API Response:', result);

        if (!result.success || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">Không có dữ liệu khách hàng nào</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        result.data.forEach((item, index) => {
            const tr = document.createElement('tr');

            const tdSTT = document.createElement('td');
            tdSTT.textContent = index + 1;
            tdSTT.style.textAlign = 'center';

            const tdName = document.createElement('td');
            tdName.textContent = item.userName || 'Không có tên';
            tdName.style.textAlign = 'center';

            const tdOrders = document.createElement('td');
            tdOrders.textContent = item.totalOrders;
            tdOrders.style.textAlign = 'center';

            const tdTotal = document.createElement('td');
            tdTotal.textContent = item.totalAmount.toLocaleString('vi-VN') + ' VNĐ';
            tdTotal.style.textAlign = 'center';

            const tdAction = document.createElement('td');
            tdAction.style.textAlign = 'center';

            const giftButton = document.createElement('button');
            giftButton.textContent = 'Tặng voucher';
            giftButton.className = 'btn btn-success btn-sm';
            giftButton.onclick = () => sendVoucher(item.userId); // Gọi hàm gửi voucher với userId
            tdAction.appendChild(giftButton);

            tr.appendChild(tdSTT);
            tr.appendChild(tdName);
            tr.appendChild(tdOrders);
            tr.appendChild(tdTotal);
            tr.appendChild(tdAction);
            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error('Lỗi khi lấy danh sách top khách hàng:', error);
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">Lỗi khi tải dữ liệu</td></tr>';
    }
};

const resetFilters = () => {
    document.getElementById('timeFilter').value = 'custom';
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = today; // Đặt lại endDate là ngày hiện tại
    handleTimeFilterChange();
    hienThiTop();
};

// Cập nhật ngày hiện tại khi trang tải và mỗi phút
document.addEventListener('DOMContentLoaded', () => {
    const endDateInput = document.getElementById('endDate');
    const startDateInput = document.getElementById('startDate');
    endDateInput.value = today;
    startDateInput.max = today;
    endDateInput.max = today;
    handleTimeFilterChange();
    hienThiTop();

    // Cập nhật ngày mỗi phút
    setInterval(() => {
        today = getTodayInVietnam();
        endDateInput.value = today;
        endDateInput.max = today;
        startDateInput.max = today;
    }, 60000); // 60000ms = 1 phút
});

// function sendVoucher(userId) {
//     const modal = new bootstrap.Modal(document.getElementById('giftVoucherModal'));
//     const confirmGiftBtn = document.getElementById('confirmGift');

//     confirmGiftBtn.onclick = async () => {
//         const voucherCode = document.getElementById('voucherCode').value.trim();
//         const discountValue = parseFloat(document.getElementById('discountValue').value);
//         const discountType = document.getElementById('discountType').value;
//         const minOrderValue = parseFloat(document.getElementById('minOrderValue').value) || 0;

//         const startDateRaw = document.getElementById('startDate').value;
//         const endDateRaw = document.getElementById('endDate').value;

//         console.log("Start date raw:", startDateRaw);
//         console.log("End date raw:", endDateRaw);

//         let startDateISO = null;
//         let endDateISO = null;

//         if (startDateRaw) {
//             startDateISO = new Date(startDateRaw.replace(" ", "T")).toISOString();
//         }
//         if (endDateRaw) {
//             endDateISO = new Date(endDateRaw.replace(" ", "T")).toISOString();
//         }

//         try {
//             const response = await fetch(`http://${config.host}:${config.port}/api/send-voucher`, {
//                 method: 'POST',
//                 headers: { 'Content-Type': 'application/json' },
//                 body: JSON.stringify({
//                     userId,
//                     voucherCode,
//                     discountValue,
//                     discountType,
//                     minOrderValue,
//                     startDate: startDateISO,
//                     endDate: endDateISO,
//                 })
//             });

//             if (!response.ok) {
//                 const errorData = await response.json();
//                 throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
//             }

//             const result = await response.json();
//             alert(`${result.message}. Voucher đã được kích hoạt và sẵn sàng sử dụng ngay!`);
//             modal.hide();

//             // Reset form
//             document.getElementById('voucherCode').value = '';
//             document.getElementById('discountValue').value = '';
//             document.getElementById('minOrderValue').value = '';
//             document.getElementById('startDate').value = '';
//             document.getElementById('endDate').value = '';

//         } catch (error) {
//             console.error('Lỗi khi tặng voucher:', error);
//             alert('Đã xảy ra lỗi khi tặng voucher: ' + error.message);
//         }
//     };

//     modal.show();
// }

function sendVoucher(userId) {
    const modal = new bootstrap.Modal(document.getElementById('giftVoucherModal'));
    const form = document.getElementById('tangVoucher');
    if (!form) {
        console.error('Form với ID "tangVoucher" không tồn tại');
        return;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();


        const formData = {
            code: form.voucherCode.value.trim(),
            discountType: form.discountType.value,
            discountValue: parseFloat(form.discountValue.value),
            minOrderValue: parseFloat(form.minOrderValue.value),
            startDate: new Date(form.startDate.value).toISOString(),
            endDate: new Date(form.endDate.value).toISOString(),
        };

        // Kiểm tra trường bắt buộc
        if (!formData.code) {
            alert('Vui lòng nhập mã voucher');
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
        if (!formData.startDate || !formData.endDate) {
            alert('Vui lòng chọn ngày bắt đầu và ngày hết hạn');
            return;
        }
        if (new Date(formData.startDate) >= new Date(formData.endDate)) {
            alert('Ngày bắt đầu phải trước ngày hết hạn');
            return;
        }

        try {
            const submitButton = form.querySelector('button[type="submit"]');
            submitButton.disabled = true;
            submitButton.textContent = 'Đang xử lý...';

            const response = await fetch(`http://${config.host}:${config.port}/api/send-voucher`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...formData, userId, voucherCode: formData.code })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            alert('Tặng voucher thành công!');
            modal.hide();
        } catch (error) {
            console.error('Lỗi khi tặng voucher:', error);
            alert('Lỗi khi tặng voucher: ' + error.message);
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = 'Xác nhận';
        }
    });
    modal.show();
}


