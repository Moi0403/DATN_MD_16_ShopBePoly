package com.example.shopbepoly.Screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Notification;
import com.example.shopbepoly.R;
import com.example.shopbepoly.Adapter.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThongBao extends AppCompatActivity {
    RecyclerView rcNotify;
    private NotificationAdapter adapter;
    List<Notification> list = new ArrayList<>();
    String userId;
    private ImageView img_Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);
        img_Back = findViewById(R.id.btnBack);
        img_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        rcNotify = findViewById(R.id.rcNotify);
        adapter = new NotificationAdapter(this,list);
        rcNotify.setLayoutManager(new LinearLayoutManager(this));
        rcNotify.setAdapter(adapter);

        loadNotifications();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Notification notification = list.get(position);

                        ApiService apiService = ApiClient.getApiService();
                        apiService.deleteNotification(notification.get_id()).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    list.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    setResult(RESULT_OK);
                                } else {
                                    adapter.notifyItemRemoved(position);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                adapter.notifyItemChanged(position);
                            }
                        });


                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {

                        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY,
                                actionState, isCurrentlyActive)
                                .addSwipeLeftLabel("XÓA")
                                .setSwipeLeftLabelColor(Color.WHITE)
                                .addBackgroundColor(Color.RED)
                                .create()
                                .decorate();

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                });
        itemTouchHelper.attachToRecyclerView(rcNotify);
    }

    private void loadNotifications() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getNotifications(userId).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    list.clear();
                    list.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("Notification", "Không có dữ liệu hoặc lỗi phản hồi");
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Log.e("Notification", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}