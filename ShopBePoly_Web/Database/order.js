const mongoose = require('mongoose');

const orderSchema = new mongoose.Schema({
    bill: String,
    Sl: Number,
    date: Date,
    status: String,
    address: String,
    img:[],
    nameproduct: String,
    pay: String
});

const orderModel = mongoose.model('order', orderSchema);
module.exports = orderModel;