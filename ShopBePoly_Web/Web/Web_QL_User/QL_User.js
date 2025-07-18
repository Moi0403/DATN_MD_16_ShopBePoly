const host = window.config;

fetch('../Style_Sidebar/Sidebar.html')
  .then(res => res.text())
  .then(data => {
    document.getElementById('sidebar-container').innerHTML = data;
  });

document.addEventListener("DOMContentLoaded", () => {
    const observer = new MutationObserver(() => {
        document.querySelectorAll("img").forEach(img => {
            img.onerror = () => img.src = "../../Images/default-avatar.png";
        });
    });
    observer.observe(document.getElementById("tbody"), { childList: true, subtree: true });
});

const hienThiUser = async () => {
    const tbody = document.querySelector('#tbody');

    try {
        const api = await fetch(`http://${config.host}:${config.port}/api/list_user`);
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
            tdSTT.style.alignContent = 'center';

            const tdIMG = document.createElement('td');
            const img = document.createElement('img'); 
            const timestamp = Date.now();

            // ✅ Kiểm tra có avatar không
            if (item.avt_user && item.avt_user.trim() !== "") {
                img.src = `http://${config.host}:${config.port}/uploads/${item.avt_user}?t=${timestamp}`;
            } else {
                img.src = "../../Images/default-avatar.png";
            }

            // ✅ Fallback nếu ảnh không tồn tại hoặc lỗi
            img.onerror = () => {
                img.onerror = null; // tránh lặp vô hạn
                img.src = "../../Images/default-avatar.png";
            };

            img.width = 100;
            img.height = 100;
            img.style.objectFit = 'contain';
            tdIMG.appendChild(img);
            tdIMG.style.textAlign = 'center';


            const tdName = document.createElement('td');
            tdName.textContent = item.name;
            tdName.style.textAlign = 'center';
            tdName.style.alignContent = 'center';

            const tdSDT = document.createElement('td');
            tdSDT.textContent = item.phone_number;
            tdSDT.style.textAlign = 'center';
            tdSDT.style.alignContent = 'center';

            const tdEmail = document.createElement('td');
            tdEmail.textContent = item.email;
            tdEmail.style.textAlign = 'center';
            tdEmail.style.alignContent = 'center';

            const tdTDN = document.createElement('td');
            tdTDN.textContent = item.username;
            tdTDN.style.textAlign = 'center';
            tdTDN.style.alignContent = 'center';

            const tdMK = document.createElement('td');
            tdMK.textContent = item.password;
            tdMK.style.textAlign = 'center';
            tdMK.style.alignContent = 'center';

            const tdPL = document.createElement('td');
            if (item.role === 2) {
                tdPL.textContent = 'Admin';
            } else if (item.role === 1) {
                tdPL.textContent = 'Nhân viên';
            } else if (item.role === 0) {
                tdPL.textContent = 'Người dùng';
            }
            tdPL.style.textAlign = 'center';
            tdPL.style.alignContent = 'center';
            tdPL.style.color = 'red';

            const tdXL = document.createElement('td');
            tdXL.classList.add('tdXL');
            tdXL.style.alignContent = 'center';

            const btnDel = document.createElement('button');
            btnDel.textContent = 'Xóa';
            btnDel.classList.add('btn', 'btn-outline-primary');
            btnDel.addEventListener('click', async () => {
                try {
                    const conf = confirm(`Bạn muốn xóa thể loại ${item.name} này ?`);
                    if (conf) {
                        const response = await fetch(`http://${config.host}:${config.port}/api/del_user/${item._id}`, {
                            method: 'DELETE'
                        });

                        if (!response.ok) {
                            throw new Error(`HTTP error! Status: ${response.status}`);
                        }
                        tr.remove();
                        alert('Xóa thành công');
                        hienThiUser();
                    }
                } catch (error) {
                    console.error('Lỗi khi xóa thể loại:', error);
                }
            })
            tdXL.appendChild(btnDel);


            tr.appendChild(tdSTT);
            tr.appendChild(tdIMG);
            tr.appendChild(tdName);
            tr.appendChild(tdSDT);
            tr.appendChild(tdEmail);
            tr.appendChild(tdTDN);
            tr.appendChild(tdMK);
            tr.appendChild(tdPL);
            tr.appendChild(tdXL);
            tr.setAttribute('data-role', item.role);

            tbody.appendChild(tr);
        })
    } catch (error) {
        console.error('Fetch error:', error);
    }
}
hienThiUser();

const ThemUser = () => {
    document.getElementById('themUser').addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);

        try {
            const response = await fetch(`http://${config.host}:${config.port}/api/add_user`, {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            alert('Thêm tài khoản thành công');
            hienThiUser();
        } catch (error) {
            console.error('Lỗi khi thêm sản phẩm:', error);
        }
    });
}
ThemUser();

document.getElementById('pl_role').addEventListener('change', function () {
    const selectedRole = this.value;
    const rows = document.querySelectorAll('#tbody tr');

    rows.forEach(row => {
        const role = row.getAttribute('data-role');

        if (selectedRole === "3") {
            row.style.display = ""; // Hiện tất cả
        } else {
            row.style.display = role === selectedRole ? "" : "none";
        }
    });
});
