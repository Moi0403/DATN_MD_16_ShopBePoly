const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
    nameproduct: String,
    id_category: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'category',
        required: true,
    },
    price: Number,
    description: String,
    avt_imgproduct: String,
    list_imgproduct: [String],
    variations: [{
        size: Number,
        stock: Number,
        sold: Number   
    }]
});

const productModel = mongoose.models.product || mongoose.model('product', productSchema);
module.exports = productModel;