const mongoose = require("mongoose");

const voucherSchema = new mongoose.Schema({
  code: { type: String},
  description: { type: String }, 
  discountType: { type: String,}, 
  discountValue: { type: Number, required: true },
  minOrderValue: { type: Number, default: 0 }, 
  usageLimit: { type: Number, default: 1 }, 
  usedCount: { type: Number, default: 0 }, 
  startDate: { type: Date, required: true }, 
  endDate: { type: Date, required: true }, 
  isActive: { type: Boolean, default: true }, 
}, { timestamps: true });

const Voucher = mongoose.model("voucher", voucherSchema);
module.exports = Voucher;
