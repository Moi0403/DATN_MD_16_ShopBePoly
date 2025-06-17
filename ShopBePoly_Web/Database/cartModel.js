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
        required: true,
    },
    quantity: Number,
    price: Number,
    size: Number,
    total: Number,
    status: Number
});

const cartModel = mongoose.model('cart', cartSchema);
module.exports = cartModel;