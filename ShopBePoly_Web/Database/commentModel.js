const mongoose = require('mongoose');

const commentSchema = new mongoose.Schema({
    id_user: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'user',
            required: true
        },
    name: String,
    title: String,
    time: Date,
});

const commentModel = mongoose.model('comment', commentSchema);
module.exports = commentModel;