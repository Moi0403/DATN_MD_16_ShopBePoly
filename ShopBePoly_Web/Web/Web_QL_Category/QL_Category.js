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

const ThemTL = () => {
    document.getElementById('themTL').addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = new FormData(event.target);

        try {
            const response = await fetch(`http://${config.host}:${config.port}/api/add_category`, {
                method: 'POST',
                body: formData
            });

            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }

            const data = await response.json();
            alert('Thêm thể loại thành công');
            hienThiTL();
        } catch (error) {
            console.error('Lỗi khi thêm thể loại:', error);
        }
    });
}
ThemTL();

const hienThiTL = async() => {
    const tbody = document.querySelector('#tbody');

    try{
        const api = await fetch(`http://${config.host}:${config.port}/api/list_category`);
        if (!api.ok) {
            throw new Error(`HTTP error! Status: ${api.status}`);
        }
        const data = await api.json();
        tbody.innerHTML = '';
        data.forEach((item, index)=>{
            const tr = document.createElement('tr');

            const tdSTT = document.createElement('td');
            tdSTT.textContent = index+1;
            tdSTT.style.alignContent ='center';

            const tdIMG = document.createElement('td')
            const img = document.createElement('img');
            img.src = `http://localhost:3000/uploads/${item.cateImg}`;
            img.width = 100;
            img.height = 50;
            img.style.objectFit = 'contain';
            tdIMG.appendChild(img);
            

            const tdTitle = document.createElement('td');
            tdTitle.textContent = item.title;
            tdTitle.style.alignContent ='center';

            const tdXL = document.createElement('td');
            tdXL.classList.add('tdXL');

            const btnDel = document.createElement('button');
            btnDel.textContent = 'Xóa';
            btnDel.classList.add('btn', 'btn-outline-primary');
            btnDel.addEventListener('click', async () => {
                try {
                    const conf = confirm(`Bạn muốn xóa thể loại ${item.title} này ?`);
                    if (conf) {
                        const response = await fetch(`http://${config.host}:${config.port}/api/del_category/${item._id}`, {
                            method: 'DELETE'
                        });

                        const result = await response.json();

                        if (!response.ok) {
                            alert('Bạn không thể xóa vì đang liên kết với sản phẩm !!!');
                            return;
                        }

                        tr.remove();
                        alert('Xóa thành công');
                        hienThiTL();
                    }
                } catch (error) {
                    console.error('Lỗi khi xóa thể loại:', error);
                }
            });

            tdXL.appendChild(btnDel);

            tr.appendChild(tdSTT);
            tr.appendChild(tdIMG);
            tr.appendChild(tdTitle);
            tr.appendChild(tdXL);

            tbody.appendChild(tr);
        });
    } catch (error) {
                console.error('Fetch error:', error);
    }
}
hienThiTL();
