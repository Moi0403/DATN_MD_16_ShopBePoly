package com.example.shopbepoly.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.R;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    private List<String> searchHistory;
    private OnSearchHistoryClickListener listener;
    private OnDeleteItemClickListener deleteListener;

    public interface OnSearchHistoryClickListener {
        void onSearchHistoryClick(String query);
    }

    public interface OnDeleteItemClickListener {
        void onDeleteItemClick(String query);
    }

    public SearchHistoryAdapter(List<String> searchHistory, OnSearchHistoryClickListener listener) {
        this.searchHistory = searchHistory;
        this.listener = listener;
    }

    public void setOnDeleteItemClickListener(OnDeleteItemClickListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = searchHistory.get(position);
        holder.tvSearchQuery.setText(query);
        
        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSearchHistoryClick(query);
            }
        });

        // Set click listener for delete button
        holder.btnDeleteItem.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteItemClick(query);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchHistory.size();
    }

    public void updateData(List<String> newHistory) {
        this.searchHistory = newHistory;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchQuery;
        ImageButton btnDeleteItem;

        ViewHolder(View itemView) {
            super(itemView);
            tvSearchQuery = itemView.findViewById(R.id.tv_search_query);
            btnDeleteItem = itemView.findViewById(R.id.btn_delete_item);
        }
    }
} 