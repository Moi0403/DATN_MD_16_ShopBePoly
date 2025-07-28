const mongoose = require('mongoose');

const verifyCodeSchema = new mongoose.Schema({
  email: String,
  code: String,
  createdAt: { type: Date, default: Date.now, expires: 300 } // TTL: xóa sau 5 phút
});

const VerifyCode = mongoose.model('VerifyCode', verifyCodeSchema);
module.exports = VerifyCode;
//abc