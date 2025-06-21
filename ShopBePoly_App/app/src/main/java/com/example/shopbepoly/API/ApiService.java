package com.example.shopbepoly.API;

import com.example.shopbepoly.DTO.Cart;
import com.example.shopbepoly.DTO.Category;
import com.example.shopbepoly.DTO.Favorite;
import com.example.shopbepoly.DTO.LoginRequest;
import com.example.shopbepoly.DTO.LoginResponse;
import com.example.shopbepoly.DTO.Message;
import com.example.shopbepoly.DTO.Order;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.DTO.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Order
    @GET("list_order")
    Call<List<Order>> getOrderList();

    @DELETE("order/{id}")
    Call<Void> deleteOrder(@Path("id") String id);

    // Product
    @GET("list_product")
    Call<List<Product>> getProducts();

    @GET("products_by_category/{categoryId}")
    Call<List<Product>> getProductsByCategory(@Path("categoryId") String categoryId);

    @GET("search_product")
    Call<List<Product>> searchProduct(@Query("q") String keyword);

    // Category
    @GET("list_category")
    Call<List<Category>> getCategories();

    // Auth
    @POST("register")
    Call<Void> register(@Body User user);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);


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

    // User
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
    @POST("upload-avatar/{id}")  // ✅ đúng với đường dẫn backend
    Call<User> uploadAvatar(@Path("id") String userId, @Part MultipartBody.Part avatar);


    @PUT("up_user/{userId}")
    Call<List<User>> updateUser(@Path("userId") String userId, @Body User user);

    // cart
    @POST("add_cart")
    Call<Cart> addCart(@Body Cart cart);
    @GET("list_cart/{userId}")
    Call<List<Cart>> getCart(@Path("userId") String userId);
    @PUT("up_cart/{cartId}")
    Call<List<Cart>> upCart(@Path("cartId") String cartId, @Body Cart cart);
    @DELETE("del_cart/{cartId}")
    Call<List<Cart>> delCart(@Path("cartId") String cartId);

    @PUT("up_password/{userId}")
    Call<ResponseBody> changePassword(@Path("userId") String userId, @Body RequestBody body);

    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") String productId);

    @POST("add_favorite")
    Call<Favorite> addFavorite(@Body Favorite favorite);

    @DELETE("remove_favorite")
    Call<ResponseBody> removeFavorite(@Query("id_user") String userId, @Query("id_product") String productId);

    @GET("favorites/{userId}")
    Call<List<Favorite>> getFavorites(@Path("userId") String userId);
}
