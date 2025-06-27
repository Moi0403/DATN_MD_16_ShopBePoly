package com.example.shopbepoly.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.DTO.Favorite;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class FavoriteFragment extends Fragment {

    private RecyclerView rvFavorites;
    private ProductAdapter adapter;
    private static FavoriteFragment instance;

    public static List<Product> favoriteProducts = new ArrayList<>();

    public static void add(Context context, Product product) {
        if (!favoriteProducts.contains(product)) {
            favoriteProducts.add(product);
            saveFavorites(context);
        }
    }

    public static void remove(Context context, Product product) {
        if (favoriteProducts.contains(product)) {
            favoriteProducts.remove(product);
            saveFavorites(context);

            // Gọi updateAdapter nếu Fragment đang được tạo và adapter tồn tại
            if (instance != null && instance.adapter != null) {
                instance.updateAdapter(); // ✅ Cập nhật lại giao diện
            }
        }
    }


    public static boolean isFavorite(Product product) {
        return favoriteProducts.contains(product);
    }

    public static List<Product> getFavorites() {
        return new ArrayList<>(favoriteProducts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    private void loadFavorites() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) return;

        ApiClient.getApiService()
                .getFavorites(userId)
                .enqueue(new Callback<List<Favorite>>() {
                    @Override
                    public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Favorite> serverFavorites = response.body();
                            favoriteProducts.clear();

                            if (serverFavorites.isEmpty()) {
                                updateAdapter(); // Không có gì thì reset
                                return;
                            }

                            // Biến đếm để biết khi nào fetch xong tất cả sản phẩm
                            final int[] pendingFetches = {serverFavorites.size()};

                            for (Favorite favorite : serverFavorites) {
                                String productId = favorite.getId_product();

                                ApiClient.getApiService().getProductById(productId).enqueue(new Callback<Product>() {
                                    @Override
                                    public void onResponse(Call<Product> call, Response<Product> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            favoriteProducts.add(response.body());
                                        }
                                        checkDone();
                                    }

                                    @Override
                                    public void onFailure(Call<Product> call, Throwable t) {
                                        t.printStackTrace();
                                        checkDone();
                                    }

                                    private void checkDone() {
                                        pendingFetches[0]--;
                                        if (pendingFetches[0] == 0) {
                                            saveFavorites(requireContext());
                                            updateAdapter();
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Favorite>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }


    private void fetchProductById(String productId) {
        ApiClient.getApiService()
                .getProductById(productId)
                .enqueue(new Callback<Product>() {
                    @Override
                    public void onResponse(Call<Product> call, Response<Product> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Product product = response.body();
                            if (!favoriteProducts.contains(product)) {
                                favoriteProducts.add(product);
                                saveFavorites(requireContext());
                                if (adapter != null) {
                                    adapter.setData(getFavorites());
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            // Nếu sản phẩm bị xóa hoặc không tồn tại -> xóa khỏi danh sách yêu thích
                            removeInvalidFavorite(productId);
                        }
                    }

                    @Override
                    public void onFailure(Call<Product> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
    private void removeInvalidFavorite(String productId) {
        // Xóa sản phẩm theo ID khỏi danh sách
        for (int i = 0; i < favoriteProducts.size(); i++) {
            if (favoriteProducts.get(i).get_id().equals(productId)) {
                favoriteProducts.remove(i);
                break;
            }
        }
        saveFavorites(requireContext());
        if (adapter != null) {
            adapter.setData(getFavorites());
            adapter.notifyDataSetChanged();
        }
    }

    private static void saveFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(favoriteProducts);

        // Lấy userId để lưu theo người dùng
        SharedPreferences loginPrefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String userId = loginPrefs.getString("userId", null);
        if (userId != null) {
            editor.putString("favorites_" + userId, json);
            editor.apply();
        }
    }
    public static void loadFavoritesFromPrefs(Context context) {
        SharedPreferences loginPrefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String userId = loginPrefs.getString("userId", null);
        if (userId == null) return;

        SharedPreferences prefs = context.getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("favorites_" + userId, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Product>>() {}.getType();
            List<Product> savedList = gson.fromJson(json, type);
            favoriteProducts.clear();
            favoriteProducts.addAll(savedList);
        } else {
            favoriteProducts.clear(); // Quan trọng: nếu không có gì thì reset
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        instance = this;
        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        loadFavoritesFromPrefs(getContext());
        adapter = new ProductAdapter(getContext(), getFavorites());
        rvFavorites.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
    private void updateAdapter() {
        if (adapter != null) {
            adapter.setData(getFavorites());
            adapter.notifyDataSetChanged();
        }
    }

}
