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

public class ThongBao extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {
    RecyclerView rcNotify;
    private NotificationAdapter adapter;
    List<Notification> list = new ArrayList<>();
    String userId;
    private ImageView img_Back;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);

        img_Back = findViewById(R.id.btnBack);
        img_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trả result về để HomeFragment cập nhật count
                setResult(RESULT_OK);
                finish();
            }
        });

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        apiService = ApiClient.getApiService();

        rcNotify = findViewById(R.id.rcNotify);
        // Truyền thêm callback listener
        adapter = new NotificationAdapter(this, list, this);
        rcNotify.setLayoutManager(new LinearLayoutManager(this));
        rcNotify.setAdapter(adapter);

        loadNotifications();

        setupSwipeToDelete();
    }

    @Override
    public void onNotificationRead(String notificationId) {
        // Gọi API đánh dấu đã đọc
        markAsRead(notificationId);
    }

    private void markAsRead(String notificationId) {
        apiService.markNotificationAsRead(notificationId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Cập nhật local list
                    for (Notification n : list) {
                        if (n.get_id().equals(notificationId)) {
                            n.setRead(true);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Trả result về HomeFragment để update count
                    setResult(RESULT_OK);

                    Log.d("ThongBao", "Đã đánh dấu thông báo đã đọc");
                } else {
                    Log.e("ThongBao", "Lỗi đánh dấu đã đọc: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ThongBao", "Lỗi kết nối khi đánh dấu đã đọc: " + t.getMessage());
            }
        });
    }

    private void setupSwipeToDelete() {
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

                        apiService.deleteNotification(notification.get_id()).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    list.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    setResult(RESULT_OK);
                                    Log.d("ThongBao", "Đã xóa thông báo");
                                } else {
                                    adapter.notifyItemChanged(position);
                                    Log.e("ThongBao", "Lỗi xóa thông báo: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                adapter.notifyItemChanged(position);
                                Log.e("ThongBao", "Lỗi kết nối khi xóa: " + t.getMessage());
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
        apiService.getNotifications(userId).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    list.clear();
                    list.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d("ThongBao", "Đã tải " + list.size() + " thông báo");
                } else {
                    Log.e("ThongBao", "Không có dữ liệu hoặc lỗi phản hồi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Log.e("ThongBao", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đảm bảo trả result khi activity bị destroy
        setResult(RESULT_OK);
    }
}