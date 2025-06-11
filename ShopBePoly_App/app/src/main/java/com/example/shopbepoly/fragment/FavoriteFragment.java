package com.example.shopbepoly.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.Adapter.ProductAdapter;
import com.example.shopbepoly.DTO.Product;
import com.example.shopbepoly.R;

import java.util.ArrayList;
import java.util.List;


public class FavoriteFragment extends Fragment {

    private RecyclerView rvFavorites;
    private ProductAdapter adapter;

    public static List<Product> favoriteProducts = new ArrayList<>();


    public static void add(Product product) {
        if (!favoriteProducts.contains(product)) {
            favoriteProducts.add(product);
        }
    }

    public static void remove(Product product) {
        favoriteProducts.remove(product);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new GridLayoutManager(getContext(),2));
        adapter = new ProductAdapter(getContext(), getFavorites());
        rvFavorites.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setData(getFavorites());
        }
    }
}