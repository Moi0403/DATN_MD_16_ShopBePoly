const mongoose = require('mongoose');

const verifyCodeSchema = new mongoose.Schema({
  email: { 
    type: String, 
    required: true, 
    lowercase: true,   // lưu email dạng chữ thường
    trim: true 
  },
  code: { 
    type: String, 
    required: true, 
    trim: true 
  },
  createdAt: { 
    type: Date, 
    default: Date.now, 
    expires: 300 // TTL: record tự xóa sau 5 phút
  }
});

// Tạo index để mỗi email chỉ có 1 code còn sống (tránh trùng lặp nhiều code)
verifyCodeSchema.index({ email: 1 }, { unique: true });

const VerifyCode = mongoose.model('VerifyCode', verifyCodeSchema);

module.exports = VerifyCode;