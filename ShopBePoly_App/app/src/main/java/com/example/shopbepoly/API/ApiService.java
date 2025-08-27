package com.example.shopbepoly.API;

import com.example.shopbepoly.DTO.*;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {


    @GET("users/get-admin")
    Call<AdminResponse> getAdminId();

    @GET("notifications/{userId}")
    Call<List<Notification>> getNotifications(@Path("userId") String userId);
    @DELETE("notification/{id}")
    Call<ResponseBody> deleteNotification(@Path("id") String id);
    @PUT("notification/mark-read/{id}")
    Call<ResponseBody> markNotificationAsRead(@Path("id") String notificationId);
    @GET("banners")
    Call<List<Banner>> getBanners();

    @POST("register")
    Call<Void> register(@Body User user);

    @POST("check-email")
    Call<CheckEmailResponse> checkEmailExists(@Body Map<String, String> body);

    @POST("check-username")
    Call<CheckUsernameResponse> checkUsernameExists(@Body Map<String, String> body);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("logout")
    Call<LogoutResponse> logout(@Body Map<String, String> body);

    @PUT("up_password/{userId}")
    Call<ResponseBody> changePassword(@Path("userId") String userId, @Body RequestBody body);


    @GET("list_user")
    Call<List<User>> getUsers();

    @Multipart
    @PUT("up_user/{userId}")
    Call<User> updateUserWithImage(
            @Path("userId") String userId,
            @Part MultipartBody.Part avatar,
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("phone_number") RequestBody phone,
            @Part("birthday") RequestBody birthday,
            @Part("gender") RequestBody gender
    );

    @Multipart
    @POST("upload-avatar/{id}")
    Call<User> uploadAvatar(@Path("id") String userId, @Part MultipartBody.Part avatar);

    @PUT("up_user/{userId}")
    Call<List<User>> updateUser(@Path("userId") String userId, @Body User user);
    @POST("auth/reset-password-by-email")
    Call<ResponseBody> resetPasswordByEmail(@Body RequestBody body);


    @GET("list_product")
    Call<List<Product>> getProducts();

    @GET("products_by_category/{categoryId}")
    Call<List<Product>> getProductsByCategory(@Path("categoryId") String categoryId);

    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") String productId);

    @GET("search_product")
    Call<List<Product>> searchProduct(@Query("q") String keyword);


    @GET("list_category")
    Call<List<Category>> getCategories();


    @POST("add_cart")
    Call<Cart> addCart(@Body Cart cart);

    @GET("list_cart/{userId}")
    Call<List<Cart>> getCart(@Path("userId") String userId);

    @PUT("up_cart/{cartId}")
    Call<Cart> upCart(@Path("cartId") String cartId, @Body Cart cart);


    @DELETE("del_cart/{cartId}")
    Call<ResponseBody> delCart(@Path("cartId") String cartId);


    @DELETE("delete_all_cart/{userId}")
    Call<ResponseBody> deleteAllCart(@Path("userId") String userId);

    @POST("delete_cart_items")
    Call<ResponseBody> deleteCartItems(@Body Map<String, List<String>> cartIds);

    @PUT("cancel_order/{id}")
    Call<Order> cancelOrder(@Path("id") String orderId);


    @POST("add_order")
    Call<Order> createOrder(@Body Order order);

    @GET("list_order/{userId}")
    Call<List<Order>> getOrderList(@Path("userId") String userId);

    @GET("list_all_orders")
    Call<List<Order>> getAllOrders();

    @GET("orders/delivering")
    Call<DeliveringOrdersResponse> getDeliveringOrders();

    @DELETE("del_order/{id}")
    Call<Void> deleteOrder(@Path("id") String id);

    @PUT("updateOrderStatus/{orderId}")
    Call<Order> upStatus(@Path("orderId") String id, @Body Order order);
    @GET("order/{id}")
    Call<Order> getOrderDetail(@Path("id") String orderId);

    @GET("search_order")
    Call<List<Order>> searchOrdersByCode(@Query("code") String code);

    @POST("add_favorite")
    Call<Favorite> addFavorite(@Body Favorite favorite);

    @DELETE("remove_favorite")
    Call<ResponseBody> removeFavorite(
            @Query("id_user") String userId,
            @Query("id_product") String productId
    );

    @GET("favorites/{userId}")
    Call<List<Favorite>> getFavorites(@Path("userId") String userId);


    @POST("messages")
    Call<SendMessageResponse> sendMessage(@Body SendMessageRequest message);

    @GET("messages")
    Call<List<Message>> getMessages(
            @Query("userId") String userId,
            @Query("adminId") String adminId
    );

    @GET("chat-users")
    Call<List<User>> getChatUsers(@Query("adminId") String adminId);

    // Gửi mã xác thực đến email người dùng
    @POST("send-verification-code")
    Call<Void> sendVerificationCode(@Body Map<String, String> body);

    // Xác minh mã đã gửi
    @POST("verify-code")
    Call<Void> verifyCode(@Body Map<String, String> body);

    // Gửi đánh giá sản phẩm
    @POST("add_review")
    Call<ResponseBody> addReview(@Body Review review);

    @POST("order_reviews/{orderId}")
    Call<ResponseBody> sendOrderReviews(
            @Path("orderId") String orderId,
            @Body List<Review> reviews
    );

    @GET("reviews/{productId}")
    Call<List<ListReview>> getReviews(@Path("productId") String productId);

    @GET("reviews/order/{orderId}")
    Call<List<ListReview>> getReviewsByOrder(@Path("orderId") String orderId);

    @PUT("reviews/{id}")
    Call<ListReview> updateReview(
            @Path("id") String reviewId,
            @Query("rating") int rating,
            @Query("comment") String comment
    );

    @GET("reviews")
    Call<List<ListReview>> getAllReviews();

    @GET("average/{productId}")
    Call<RatingResponse> getAverageRating(@Path("productId") String productId);

    // Voucher endpoints
    @GET("vouchers")
    Call<List<Voucher>> getVouchers();

    @GET("voucher/{id}")
    Call<Voucher> getVoucherById(@Path("id") String voucherId);

    @GET("vouchers/code/{code}")
    Call<VoucherResponse> getVoucherByCode(@Path("code") String code);

    @POST("add_voucher")
    Call<VoucherResponse> addVoucher(@Body Voucher voucher);

    @DELETE("del_voucher/{id}")
    Call<VoucherDeleteResponse> deleteVoucher(@Path("id") String voucherId);

    @PUT("update_voucher_status/{id}")
    Call<VoucherResponse> updateVoucherStatus(
            @Path("id") String voucherId,
            @Body Map<String, Boolean> status
    );

    @PUT("update_usage_limit/{id}")
    Call<VoucherResponse> updateUsageLimit(
            @Path("id") String voucherId,
            @Body Map<String, Integer> usageLimit
    );

    @GET("get-voucher/{voucherId}")
    Call<VoucherResponse> getVoucher(@Path("voucherId") String voucherId);

    @PUT("extend-voucher")
    Call<VoucherResponse> extendVoucher(@Body ExtendVoucherRequest request);

    @POST("apply_voucher")
    Call<VoucherApplicationResponse> applyVoucher(@Body VoucherApplicationRequest request);

    @POST("use_voucher/{id}")
    Call<VoucherUsageResponse> useVoucher(@Path("id") String voucherId);

    @GET("user_vouchers/{userId}")
    Call<List<Voucher>> getUserVouchers(@Path("userId") String userId);

    @POST("voucher/validate")
    Call<VoucherValidationResponse> validateVoucher(@Body VoucherValidationRequest request);

    @POST("voucher/use")
    Call<VoucherUsageResponse> markVoucherAsUsed(@Body VoucherUsageRequest request);

    @GET("vouchers/available/{userId}")
    Call<AvailableVouchersResponse> getAvailableVouchers(
            @Path("userId") String userId,
            @Query("orderTotal") Double orderTotal
    );

    // Response Classes
    class VoucherResponse {
        private boolean success;
        private String message;
        private Voucher voucher;
        private Voucher data; // Cho endpoint get-voucher

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Voucher getVoucher() { return voucher; }
        public void setVoucher(Voucher voucher) { this.voucher = voucher; }
        public Voucher getData() { return data; }
        public void setData(Voucher data) { this.data = data; }
    }

    class VoucherDeleteResponse {
        private String message;
        private List<Voucher> vouchers;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<Voucher> getVouchers() { return vouchers; }
        public void setVouchers(List<Voucher> vouchers) { this.vouchers = vouchers; }
    }

    class ExtendVoucherRequest {
        private String voucherId;
        private String startDate;
        private String endDate;

        public ExtendVoucherRequest(String voucherId, String startDate, String endDate) {
            this.voucherId = voucherId;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Getters and setters
        public String getVoucherId() { return voucherId; }
        public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }

    class VoucherApplicationRequest {
        private String voucherCode;
        private Double orderTotal;
        private String userId;

        public VoucherApplicationRequest(String voucherCode, Double orderTotal, String userId) {
            this.voucherCode = voucherCode;
            this.orderTotal = orderTotal;
            this.userId = userId;
        }

        // Getters and setters
        public String getVoucherCode() { return voucherCode; }
        public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
        public Double getOrderTotal() { return orderTotal; }
        public void setOrderTotal(Double orderTotal) { this.orderTotal = orderTotal; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    class VoucherApplicationResponse {
        private boolean success;
        private String message;
        private Voucher voucher;
        private Double discountAmount;
        private Double finalTotal;
        private Double savings;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Voucher getVoucher() { return voucher; }
        public void setVoucher(Voucher voucher) { this.voucher = voucher; }
        public Double getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
        public Double getFinalTotal() { return finalTotal; }
        public void setFinalTotal(Double finalTotal) { this.finalTotal = finalTotal; }
        public Double getSavings() { return savings; }
        public void setSavings(Double savings) { this.savings = savings; }
    }

    class VoucherValidationRequest {
        private String voucherCode;
        private Double orderTotal;
        private String userId;

        // Constructor and getters/setters
        public VoucherValidationRequest(String voucherCode, Double orderTotal, String userId) {
            this.voucherCode = voucherCode;
            this.orderTotal = orderTotal;
            this.userId = userId;
        }

        public String getVoucherCode() { return voucherCode; }
        public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
        public Double getOrderTotal() { return orderTotal; }
        public void setOrderTotal(Double orderTotal) { this.orderTotal = orderTotal; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    class VoucherValidationResponse {
        private boolean success;
        private String message;
        private Voucher voucher;
        private Double discountAmount;
        private Double finalTotal;
        private Double savings;

        // Getters and setters tương tự VoucherApplicationResponse
    }

    class VoucherUsageRequest {
        private String voucherId;
        private String orderId;
        private String userId;

        public VoucherUsageRequest(String voucherId, String orderId, String userId) {
            this.voucherId = voucherId;
            this.orderId = orderId;
            this.userId = userId;
        }

        // Getters and setters
        public String getVoucherId() { return voucherId; }
        public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    class VoucherUsageResponse {
        private boolean success;
        private String message;
        private Voucher voucher;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Voucher getVoucher() { return voucher; }
        public void setVoucher(Voucher voucher) { this.voucher = voucher; }
    }

    class AvailableVouchersResponse {
        private boolean success;
        private List<Voucher> vouchers;
        private int total;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public List<Voucher> getVouchers() { return vouchers; }
        public void setVouchers(List<Voucher> vouchers) { this.vouchers = vouchers; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }
}