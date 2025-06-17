const mongoose = require('mongoose');

const cartSchema = new mongoose.Schema({
    id_user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'user',
        required: true
    },
    id_product: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'product',
        required: true
    },
    nameproduct: String,
    image_product: [String], // ✅ sửa lại kiểu mảng chuỗi
    quantity: Number,
    price: Number,
    total: Number
});

const cartModel = mongoose.model('cart', cartSchema);
module.exports = cartModel;
