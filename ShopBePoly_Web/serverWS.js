const WebSocket = require('ws');
const mongoose = require('mongoose');
const messageModel = require('./Database/messageModel');
const userModel = require('./Database/userModel');


const userSockets = new Map();


async function updateUserStatus(userId, isOnline) {
    try {
        await userModel.findByIdAndUpdate(userId, { isOnline });
        console.log(`User ${userId} is now ${isOnline ? 'online' : 'offline'}`);

        // Gửi thông báo trạng thái đến tất cả client
        userSockets.forEach((socket) => {
            if (socket.readyState === WebSocket.OPEN) {
                socket.send(JSON.stringify({
                    type: 'user_status',
                    userId,
                    isOnline
                }));
            }
        });
    } catch (err) {
        console.error(`Error updating user status: ${err.message}`);
    }
}


function initWebSocket(server) {
    const wss = new WebSocket.Server({ server, path: '/ws' });

    wss.on('connection', (socket, req) => {
        const params = new URLSearchParams(req.url.replace('/ws?', ''));
        const userId = params.get('userId');
        socket.userId = userId;

        // Xác thực userId hợp lệ
        if (!mongoose.Types.ObjectId.isValid(userId)) {
            socket.send(JSON.stringify({ error: 'ID user không hợp lệ' }));
            socket.close();
            return;
        }

        // Đóng kết nối cũ nếu user đã kết nối
        const oldSocket = userSockets.get(userId);
        if (oldSocket && oldSocket.readyState === WebSocket.OPEN) {
            oldSocket.close(1000, 'New connection established');
        }

        // Lưu socket vào Map
        userSockets.set(userId, socket);
        updateUserStatus(userId, true);

        console.log(`User ${userId} connected via WebSocket`);

       socket.on('message', async (data) => {
            const messageString = data.toString();

         
            if (messageString === 'ping') {
               
               
                return;
            }

            try {
                const msg = JSON.parse(messageString);

                if (msg.type === 'message') {
                    const { from, to, content } = msg;

                    if (!mongoose.Types.ObjectId.isValid(from) || !mongoose.Types.ObjectId.isValid(to)) {
                        return socket.send(JSON.stringify({ error: 'ID from hoặc to không hợp lệ' }));
                    }

                    // Lưu tin nhắn vào DB
                    const newMessage = new messageModel({
                        from: new mongoose.Types.ObjectId(from),
                        to: new mongoose.Types.ObjectId(to),
                        content,
                        timestamp: new Date()
                    });
                    await newMessage.save();

                    const populatedMessage = await messageModel
                        .findById(newMessage._id)
                        .populate('from', 'name avt_user isOnline') 
                        .populate('to', 'name avt_user isOnline'); 

                    // Gửi tin nhắn cho người nhận nếu đang online
                    const recipientSocket = userSockets.get(to);
                    if (recipientSocket && recipientSocket.readyState === WebSocket.OPEN) {
                        recipientSocket.send(JSON.stringify({
                            type: 'new_message',
                            data: populatedMessage
                        }));
                    }

                    // Xác nhận lại cho người gửi
                    socket.send(JSON.stringify({
                        type: 'message_sent',
                        data: populatedMessage
                    }));
                }
            } catch (err) {
                console.error(`Error handling message: ${err.message}`);
                // socket.send(JSON.stringify({ error: 'Lỗi server khi xử lý tin nhắn' }));
            }
        });

        socket.on('close', () => {
            if (socket.userId) {
                updateUserStatus(socket.userId, false);
                userSockets.delete(socket.userId);
                console.log(`User ${socket.userId} disconnected`);
            }
        });

        socket.on('error', (err) => {
            console.error(`Socket error for user ${socket.userId}: ${err.message}`);
            if (socket.userId) {
                updateUserStatus(socket.userId, false);
                userSockets.delete(socket.userId);
            }
        });
    });
}

module.exports = {
    initWebSocket,
    userSockets
};