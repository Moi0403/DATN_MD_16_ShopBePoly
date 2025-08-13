// reviewModel.js
const mongoose = require("mongoose");

const reviewSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "user", // phải khớp với tên model user
      required: [true, "Thiếu userId"],
    },
    productId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "product", // đúng tên model
      required: [true, "Thiếu productId"],
    },
    orderId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "orders",
      required: [true, "Thiếu orderId"],
    },
    rating: {
      type: Number,
      required: [true, "Thiếu số sao"],
      min: [1, "Số sao tối thiểu là 1"],
      max: [5, "Số sao tối đa là 5"],
    },
    comment: {
      type: String,
      default: "",
      trim: true,
    },
    images: {
      type: [String],
      default: [],
    },
  },
  { timestamps: true }
);

module.exports = mongoose.model("reviews", reviewSchema);
