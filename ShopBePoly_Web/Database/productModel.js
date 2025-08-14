const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
    nameproduct: String,
    id_category: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'category',
        required: true,
    },
    price_enter: Number,
    price: Number,
    price_sale: Number,
    description: String,
    avt_imgproduct: String, 
    list_imgproduct: [String], 
    sale: Number,
    variations: [
        {
            size: Number,
            stock: Number,
            sold: Number,
            color: {
                name: String,
                code: String
            },
            image: String, 
            list_imgproduct: [String] 
        }
    ]
});


const productModel = mongoose.models.product || mongoose.model('product', productSchema);
module.exports = productModel;
