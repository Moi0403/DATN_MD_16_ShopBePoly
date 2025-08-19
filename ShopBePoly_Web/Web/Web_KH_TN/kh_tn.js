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