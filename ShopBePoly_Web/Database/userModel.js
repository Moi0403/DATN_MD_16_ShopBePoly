const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    username: String,
    password: String,
    name: String,
    email: String,
    birthday: String,
    gender: String,
    phone_number: Number,
    avt_user: String,
    role: Number,
    isOnline: { type: Boolean, default: false }
});

const userModel = mongoose.model('user', userSchema);
module.exports = userModel;