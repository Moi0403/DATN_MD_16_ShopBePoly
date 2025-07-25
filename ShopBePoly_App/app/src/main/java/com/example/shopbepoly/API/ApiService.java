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


    @POST("register")
    Call<Void> register(@Body User user);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

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

    @DELETE("del_order/{id}")
    Call<Void> deleteOrder(@Path("id") String id);

    @PUT("updateOrderStatus/{orderId}")
    Call<Order> upStatus(@Path("orderId") String id, @Body Order order);
    @GET("order/{id}")
    Call<Order> getOrderDetail(@Path("id") String orderId);


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
    Call<ResponseBody> sendMessage(@Body Message message);

    @GET("messages")
    Call<List<Message>> getMessages(
            @Query("from") String from,
            @Query("to") String to
    );

    @GET("messages")
    Call<List<Message>> getMessagesPaged(
            @Query("from") String from,
            @Query("to") String to,
            @Query("page") int page,
            @Query("limit") int limit
    );

}