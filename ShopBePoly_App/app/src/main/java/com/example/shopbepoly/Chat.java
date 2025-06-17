package com.example.shopbepoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.Adapter.MessageAdapter;
import com.example.shopbepoly.DTO.Message;
import com.example.shopbepoly.DTO.User;
import com.example.shopbepoly.Screen.LoginScreen;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat extends AppCompatActivity {

    private RecyclerView recyclerMessages;
    private EditText edtMessage;
    private Button btnSend;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    private ApiService apiService;
    private String currentUserId;
    private String adminId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Ánh xạ view
        recyclerMessages = findViewById(R.id.recyclerMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        apiService = ApiClient.getApiService();

        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("userId", null);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để nhắn tin", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
        }

        // Gọi hàm lấy adminId và khởi tạo adapter
        loadAdminId();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadAdminId() {
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (User user : response.body()) {
                        if (user.getRole() == 1) {
                            adminId = user.getId();
                            Log.d("Chat", "adminId: " + adminId);

                            adapter = new MessageAdapter(Chat.this, messageList, currentUserId);
                            recyclerMessages.setAdapter(adapter);


                            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                            boolean hasChatted = prefs.getBoolean("hasChatted", false);
                            if (hasChatted) {
                                loadMessages();
                            }

                            return;
                        }
                    }
                    Toast.makeText(Chat.this, "Không tìm thấy tài khoản admin", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Chat.this, "Lỗi khi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(Chat.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        apiService.getMessages(currentUserId, adminId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.clear();
                    messageList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    recyclerMessages.scrollToPosition(messageList.size() - 1);
                } else {
                    Toast.makeText(Chat.this, "Không thể tải tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(Chat.this, "Lỗi tải tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = edtMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        btnSend.setEnabled(false);


        SharedPreferences.Editor editor = getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("hasChatted", true);
        editor.apply();

        Message message = new Message(currentUserId, adminId, content);

        apiService.sendMessage(message).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnSend.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseStr = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONObject data = jsonObject.getJSONObject("data");

                        Message newMessage = new Message(
                                data.getString("from"),
                                data.getString("to"),
                                data.getString("content")
                        );
                        newMessage.setId(data.getString("_id"));
                        newMessage.setTimestamp(data.getString("timestamp"));

                        edtMessage.setText("");
                        messageList.add(newMessage);
                        adapter.notifyItemInserted(messageList.size() - 1);
                        recyclerMessages.scrollToPosition(messageList.size() - 1);


                        loadMessages();

                    } catch (Exception e) {
                        Toast.makeText(Chat.this, "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                        Log.e("Chat", "JSON parse error: " + e.getMessage());
                    }
                } else {
                    Toast.makeText(Chat.this, "Không gửi được tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSend.setEnabled(true);
                Toast.makeText(Chat.this, "Lỗi gửi tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
