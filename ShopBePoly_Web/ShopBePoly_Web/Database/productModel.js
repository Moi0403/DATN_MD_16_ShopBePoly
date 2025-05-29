const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
    nameproduct: String,
    category: String,
    price: Number,
    quantity: Number,
    description: String,
    image_product: [],
    size: Number,
    color: String,
    stock: Number,
    sold: Number,
});

const productModel = mongoose.model('product', productSchema);
module.exports = productModel;