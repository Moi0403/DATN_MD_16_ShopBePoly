const API_BASE = `http://${config.host}:${config.port}/api`;
const WS_BASE = `ws://${config.host}:${config.port}/ws`;
const UPLOADS_BASE = `http://${config.host}:${config.port}/Uploads`;

let userId = null;
let myAvatarUrl = null;
let selectedUserId = null;
let selectedUserAvatar = null;
let chatSocket = null;

let userStatus = new Map();

async function getAdminId() {
    try {
        const response = await fetch(`${API_BASE}/get-admin`);
        if (!response.ok) {
            throw new Error('Failed to fetch admin');
        }
        const admin = await response.json();
        return admin._id;
    } catch (err) {
        console.error('Error fetching admin ID:', err);
        alert('Không thể lấy thông tin admin. Vui lòng thử lại sau.');
        return null;
    }
}

async function initChat() {
    userId = await getAdminId();
    if (!userId) return;

    myAvatarUrl = `${UPLOADS_BASE}/${userId}.jpg`;

    chatSocket = new WebSocket(`${WS_BASE}?userId=${userId}`);

    chatSocket.onopen = () => {
        console.log('WebSocket connected');
        loadChatUsers();
    };

    chatSocket.onmessage = (event) => {
        console.log('Received WebSocket message:', event.data);
        const msg = JSON.parse(event.data);
        if (msg.type === 'new_message' || msg.type === 'message_sent') {
            const isMe = msg.data.from._id === userId;
            const otherId = isMe ? msg.data.to._id : msg.data.from._id;
            const senderAvatarUrl = isMe ? myAvatarUrl : `${UPLOADS_BASE}/${msg.data.from.avt_user}`;
            const senderName = isMe ? 'Tôi' : msg.data.from.name;

            if (msg.type === 'message_sent' && isMe) {
                return;
            }

            const userItem = document.querySelector(`li[data-user-id="${otherId}"]`);
            if (!userItem && !isMe) {
                loadChatUsers().then(() => {
                    if (!selectedUserId) {
                        selectUser(otherId, senderName, senderAvatarUrl);
                        appendMessage(false, senderName, senderAvatarUrl, msg.data.content);
                    }
                });
            }

            if (selectedUserId && otherId === selectedUserId) {
                appendMessage(isMe, senderName, senderAvatarUrl, msg.data.content);
            } else if (!isMe) {
                if (!selectedUserId) {
                    selectUser(otherId, senderName, senderAvatarUrl);
                    appendMessage(false, senderName, senderAvatarUrl, msg.data.content);
                } else {
                    markUnreadMessage(otherId, senderName);
                    const event = new CustomEvent('messageReceived');
                    window.dispatchEvent(event);
                }
            }
        } else if (msg.type === 'conversation_update') {
            const messages = msg.data;
            if (messages.length > 0) {
                const lastMessage = messages[messages.length - 1];
                const otherId = lastMessage.from._id === userId ? lastMessage.to._id : lastMessage.from._id;
                const senderName = lastMessage.from._id === userId ? 'Tôi' : lastMessage.from.name;
                const senderAvatarUrl = lastMessage.from._id === userId ? myAvatarUrl : `${UPLOADS_BASE}/${lastMessage.from.avt_user}`;
                const isOnline = userStatus.get(otherId) || false;

                console.log('Processing conversation_update with otherId:', otherId);
                if (!selectedUserId || selectedUserId === otherId) {
                    selectedUserId = otherId;
                    selectedUserAvatar = senderAvatarUrl;
                    
                    const chatHeader = document.getElementById('chat-header-title');
                    chatHeader.textContent = senderName;

                    const messagesDiv = document.getElementById('messages');
                    messagesDiv.innerHTML = '';
                    messages.forEach(msg => {
                        const isMe = msg.from._id === userId;
                        const senderName = isMe ? 'Tôi' : msg.from.name;
                        const avatar = isMe ? myAvatarUrl : `${UPLOADS_BASE}/${msg.from.avt_user}`;
                        appendMessage(isMe, senderName, avatar, msg.content);
                    });
                    console.log('Updated UI for conversation with', otherId);
                    const event = new CustomEvent('userSelected', { detail: { userId: otherId, userName: senderName, avatarUrl: senderAvatarUrl, isOnline } });
                    window.dispatchEvent(event);
                } else {
                    markUnreadMessage(otherId, senderName);
                    console.log('Marked unread for', otherId, 'since selectedUserId is', selectedUserId);
                    const event = new CustomEvent('messageReceived');
                    window.dispatchEvent(event);
                }
            }
        } else if (msg.type === 'user_status') {
            updateUserStatus(msg.userId, msg.isOnline);
            if (selectedUserId === msg.userId) {
                const headerStatusDot = document.getElementById('header-status-dot');
                if (headerStatusDot) {
                    headerStatusDot.classList.toggle('online', msg.isOnline);
                }
            }
        }
    };

    chatSocket.onclose = () => {
        console.log('WebSocket closed');
        reconnectWebSocket();
    };

    chatSocket.onerror = (error) => console.error('WebSocket error:', error);
}

function reconnectWebSocket() {
    setTimeout(() => {
        console.log('Attempting to reconnect WebSocket...');
        initChat(); // Gọi lại hàm initChat để khởi tạo lại WebSocket
    }, 5000);
}

document.getElementById('send-button').addEventListener('click', sendMessage);
document.getElementById('message-input').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendMessage();
});

function sendMessage() {
    const input = document.getElementById('message-input');
    const content = input.value.trim();
    if (content && selectedUserId) {
        if (chatSocket.readyState === WebSocket.OPEN) {
            appendMessage(true, 'Tôi', myAvatarUrl, content);
            chatSocket.send(JSON.stringify({
                type: 'message',
                from: userId,
                to: selectedUserId,
                content
            }));
        } else {
            console.error('WebSocket is not open');
        }
        input.value = '';
    }
}

function appendMessage(isMe, senderName, avatar, content) {
    const messagesDiv = document.getElementById('messages');
    const div = document.createElement('div');

    div.classList.add('message-container');
    div.classList.add(isMe ? 'me' : 'other');

    div.innerHTML = `
        <img class="user-avatar" src="${avatar}" alt="${senderName}">
        <div class="message">
            <span class="sender-name">${senderName}</span>
            <p>${content}</p>
        </div>
    `;

    messagesDiv.appendChild(div);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function updateUserStatus(userIdParam, isOnline) {
    const statusElement = document.querySelector(`#user-list li[data-user-id="${userIdParam}"] .user-status-dot`);
    userStatus.set(userIdParam, isOnline); // Cập nhật trạng thái vào Map
    if (statusElement) {
        if (isOnline) {
            statusElement.classList.add('online');
        } else {
            statusElement.classList.remove('online');
        }
    }
}

function markUnreadMessage(userIdParam, senderName) {
    const userItem = document.querySelector(`li[data-user-id="${userIdParam}"]`);
    if (userItem) {
        if (!userItem.querySelector('.unread-indicator')) {
            const unreadIndicator = document.createElement('span');
            unreadIndicator.classList.add('unread-indicator');
            unreadIndicator.textContent = '●';
            userItem.appendChild(unreadIndicator);
        }
    }
}

function selectUser(selectId, userName, userAvatarUrl) {
    selectedUserId = selectId;
    selectedUserAvatar = userAvatarUrl;
    const isOnline = userStatus.get(selectId) || false; // Lấy trạng thái từ Map
    updateChatHeader(selectId, userName, userAvatarUrl, isOnline);
    const messagesDiv = document.getElementById('messages');
    messagesDiv.innerHTML = '';
    loadMessages(selectId, false);
    const userItems = document.querySelectorAll('.user-item');
    userItems.forEach(item => item.classList.remove('active'));
    const selectedItem = document.querySelector(`li[data-user-id="${selectId}"]`);
    if (selectedItem) {
        selectedItem.classList.add('active');
        const unreadIndicator = selectedItem.querySelector('.unread-indicator');
        if (unreadIndicator) unreadIndicator.remove();
    }
}

async function loadChatUsers() {
    try {
        const response = await fetch(`${API_BASE}/chat-users?adminId=${userId}`);
        const users = await response.json();
        const userList = document.getElementById('user-list');
        userList.innerHTML = '';
        users.forEach(user => {
            const userAvatarUrl = `${UPLOADS_BASE}/${user.avt_user}`;
            const li = document.createElement('li');
            li.classList.add('user-item');
            li.dataset.userId = user._id; // Thêm data attribute để dễ dàng truy vấn
            
            // Cập nhật cấu trúc HTML để có chấm tròn trạng thái
            li.innerHTML = `
                <div class="user-item-content">
                    <img class="user-avatar" src="${userAvatarUrl}" alt="${user.name}">
                    <span id="status-dot-${user._id}" class="user-status-dot"></span>
                </div>
                <span class="user-name">${user.name}</span>
            `;
            li.onclick = () => {
                selectUser(user._id, user.name, userAvatarUrl);
            };
            userList.appendChild(li);
            updateUserStatus(user._id, user.isOnline); // Cập nhật trạng thái ban đầu
        });

        if (users.length > 0 && !selectedUserId) {
            selectUser(users[0]._id, users[0].name, `${UPLOADS_BASE}/${users[0].avt_user}`);
        }
    } catch (err) {
        console.error('Error loading chat users:', err);
    }
}

async function loadMessages(toUserId, clear = true) {
    try {
        const response = await fetch(`${API_BASE}/messages?userId=${toUserId}&adminId=${userId}`);
        const messages = await response.json();
        const messagesDiv = document.getElementById('messages');
        if (clear) messagesDiv.innerHTML = '';
        messages.forEach(msg => {
            const isMe = msg.from._id === userId;
            const senderName = isMe ? 'Tôi' : msg.from.name;
            const avatar = isMe ? myAvatarUrl : `${UPLOADS_BASE}/${msg.from.avt_user}`;
            appendMessage(isMe, senderName, avatar, msg.content);
        });
    } catch (err) {
        console.error('Error loading messages:', err);
    }
}

function showNotification() {
    const notification = document.getElementById('notification');
    notification.classList.add('show');
    setTimeout(() => {
        notification.classList.remove('show');
    }, 3500);
}

window.addEventListener('messageReceived', showNotification);

function updateChatHeader(userIdParam, userName, avatarUrl, isOnline) {
    let header = document.getElementById('chat-header');
    if (!header) return;

    header.innerHTML = `
        <img id="header-avatar" class="header-avatar" src="${avatarUrl}" alt="${userName}">
        <h2 id="chat-header-title">${userName}</h2>
        <span id="header-status-dot" class="status-dot ${isOnline ? 'online' : ''}"></span>
    `;
}

function updateChatHeader(userIdParam, userName, avatarUrl, isOnline) {
    const headerTitle = document.getElementById('chat-header-title');
    const headerAvatar = document.getElementById('header-avatar');
    const headerStatusDot = document.getElementById('header-status-dot');

    if (!headerTitle || !headerAvatar || !headerStatusDot) {
        // Nếu các phần tử chưa tồn tại, hãy tạo chúng lần đầu
        const chatHeader = document.getElementById('chat-header');
        chatHeader.innerHTML = `
            <div class="header-avatar-container">
                <img id="header-avatar" class="header-avatar" src="${avatarUrl}" alt="${userName}">
                <span id="header-status-dot" class="status-dot"></span>
            </div>
            <h2 id="chat-header-title">${userName}</h2>
        `;
        // Cập nhật lại các biến sau khi tạo
        headerAvatar = document.getElementById('header-avatar');
        headerStatusDot = document.getElementById('header-status-dot');
    }
    
    // Cập nhật giá trị
    headerTitle.textContent = userName || 'Chọn một cuộc trò chuyện';
    headerAvatar.src = avatarUrl || ''; // Đặt src rỗng nếu không có avatar
    headerStatusDot.classList.toggle('online', isOnline);
}

initChat();