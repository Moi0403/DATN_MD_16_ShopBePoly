const mongoose = require('mongoose');

const orderSchema = new mongoose.Schema({
    id_order: { type: String, required: true },
    id_user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'user',
        required: true
    },
    products: [
        {
            id_product: {
                type: mongoose.Schema.Types.ObjectId,
                ref: 'product',
                required: true
            },
            quantity: {
                type: Number,
                required: true
            },
            color: String,
            size: String,
            price: Number,
            img: String
        }
    ],
    quantity_order: Number,
    date: Date,
    total: String,
    status: String,
    address: String,
    pay: String,
    cancelReason: {
        type: String,
        default: ""
    },
    checkedAt: {
        type: Date,
        default: null // Thời gian chuyển từ "Đang xử lý" sang "Đang giao hàng"
    },
    checkedBy: {
        type: String,
        default: ""
    },
    // ✅ THÊM TRƯỜNG MỚI
    deliveryConfirmedAt: {
        type: Date,
        default: null // Thời gian staff xác nhận đã giao hàng thành công
    },
    deliveryConfirmedBy: {
        type: String,
        default: "" // ID của staff xác nhận giao hàng
    }
}, { timestamps: true });

const orderModel = mongoose.model('order', orderSchema);
module.exports = orderModel;