fetch('../Style_Sidebar/Sidebar.html')
  .then(res => res.text())
  .then(data => {
    document.getElementById('sidebar-container').innerHTML = data;
  });

const buttons = document.querySelectorAll('.tab-btn');
const content = document.getElementById('content');

  buttons.forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.preventDefault();
      const url = btn.dataset.frag;

      try {
        const res = await fetch(url);
        if (!res.ok) throw new Error('Không tìm thấy file ' + url);
        const html = await res.text();
        content.innerHTML = html;
      } catch (err) {
        content.innerHTML = `<p style="color: red;">${err.message}</p>`;
      }
    });
  });

  // Tự động load tab đầu tiên khi vào
  if (buttons.length > 0) buttons[0].click();
  