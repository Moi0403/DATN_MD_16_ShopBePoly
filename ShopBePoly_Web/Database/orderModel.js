const mongoose = require('mongoose');

const orderSchema = new mongoose.Schema({
    id_user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'user',
        required: true
    },
    id_product: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'product',
        required: true,
    },
    img_oder:[String],
    quantity: Number,
    color: String,
    date: Date,
    price: Number,
    total: String,
    status: String,
    address: String,
    nameproduct: String,
    pay: String
});

const orderModel = mongoose.model('order', orderSchema);
module.exports = orderModel;