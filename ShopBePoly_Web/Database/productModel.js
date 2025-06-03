const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
    nameproduct: String,
    id_category: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'category',
        required: true,
    },
    price: Number,
    quantity: Number,
    description: String,
    avt_imgproduct: String,
    list_imgproduct: [String],
    size: Number,
    color: String,
    stock: Number,
    sold: Number,
});

const productModel = mongoose.model('product', productSchema);
module.exports = productModel;