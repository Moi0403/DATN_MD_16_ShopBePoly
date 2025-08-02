const WebSocket = require('ws');

const userSockets = new Map();

function updateUserStatus(userId, isOnline) {
  console.log(`User ${userId} is now ${isOnline ? 'online' : 'offline'}`);
  // TODO: update isOnline trong DB ở đây
}

module.exports = function (server) {
  const wss = new WebSocket.Server({ server }); // Gắn WebSocket vào server Express

  wss.on('connection', (socket, req) => {
    const urlParams = new URLSearchParams(req.url.replace('/ws?', ''));
    const userId = urlParams.get('userId');
    socket.userId = userId;

    if (userId) {
      // Đóng kết nối cũ nếu userId đã có socket trước đó
      const oldSocket = userSockets.get(userId);
      if (oldSocket && oldSocket.readyState === WebSocket.OPEN) {
        oldSocket.close(1000, 'New connection established');
      }

      userSockets.set(userId, socket);
      updateUserStatus(userId, true); // Đánh dấu online
    }

    socket.on('message', (data) => {
      const msg = data.toString();
      if (msg === 'offline') {
        updateUserStatus(socket.userId, false); // Đánh dấu offline
      } else if (msg === 'pong') {
        console.log(`Received pong from ${socket.userId}`);
      } else {
        console.log(`Message from ${socket.userId}: ${msg}`);
      }
    });

    socket.on('close', () => {
      if (socket.userId) {
        updateUserStatus(socket.userId, false);
        userSockets.delete(socket.userId);
      }
    });

    socket.on('error', (err) => {
      console.error(`Socket error for user ${socket.userId}:`, err);
      updateUserStatus(socket.userId, false);
      userSockets.delete(socket.userId);
    });
  });
};
