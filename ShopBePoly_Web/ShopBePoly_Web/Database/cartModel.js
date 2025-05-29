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
    image_product: [],
    quantity: Number,
    total: Number,
    price: Number,
});

const cartModel = mongoose.model('cart', cartSchema);
module.exports = cartModel;