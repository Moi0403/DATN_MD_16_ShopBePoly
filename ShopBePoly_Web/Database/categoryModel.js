const mongoose = require('mongoose');

const categorySchema = new mongoose.Schema({
    title: String,
    cateImg:String
});


const categoryModel = mongoose.models.category || mongoose.model('category', categorySchema);
module.exports = categoryModel;