const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  title: String,
  content: String,
  type: String,
  isRead: { type: Boolean, default: false },
  createdAt: { type: Date, default: Date.now }, 
   products: [
    {
      id_product: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'product'
      },
      productName: String,
      img: String
    }
  ]
});

module.exports = mongoose.model('Notification', notificationSchema);
 