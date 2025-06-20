const mongoose = require('mongoose');

const favoriteSchema = new mongoose.Schema({
    id_user: { type: mongoose.Schema.Types.ObjectId, ref: 'user' },
    id_product: { type: mongoose.Schema.Types.ObjectId, ref: 'product' },
}, { timestamps: true });

module.exports = mongoose.model('favorite', favoriteSchema);
