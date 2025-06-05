const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    username: String,
    password: String,
    name: String,
    email: String,
    phone_number: Number,
    avt: [],
    role: Number
});

const userModel = mongoose.model('user', userSchema);
module.exports = userModel;