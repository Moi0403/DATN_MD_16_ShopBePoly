package com.example.shopbepoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.API.WebSocketManager;
import com.example.shopbepoly.Adapter.MessageAdapter;
import com.example.shopbepoly.DTO.AdminResponse;
import com.example.shopbepoly.DTO.Message;
import com.example.shopbepoly.DTO.SendMessageRequest;
import com.example.shopbepoly.DTO.SendMessageResponse;
import com.example.shopbepoly.DTO.User;
import com.example.shopbepoly.Screen.LoginScreen;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat extends AppCompatActivity {

    private RecyclerView recyclerMessages;
    private EditText edtMessage;
    private Button btnSend;
    private ImageButton img_back;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    private ApiService apiService;
    private String currentUserId;
    private String adminId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerMessages = findViewById(R.id.recyclerMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        img_back = findViewById(R.id.btnBack_chat);
        img_back.setOnClickListener(v -> finish());

        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        apiService = ApiClient.getApiService();

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("userId", null);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để nhắn tin", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginScreen.class));
            finish();
            return;
        }

        adapter = new MessageAdapter(this, messageList, currentUserId);
        recyclerMessages.setAdapter(adapter);

        loadAdminId();

        btnSend.setEnabled(false);
        btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectSocket();
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebSocketManager.disconnect();
    }

    private void connectSocket() {
        WebSocketManager.setMessageListener(text -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(text);
                String messageType = json.optString("type");

                if ("new_message".equals(messageType)) {
                    JSONObject data = json.getJSONObject("data");
                    Log.d("Chat", "Received WS message: " + data.toString());

                    JSONObject fromJson = data.getJSONObject("from");
                    User fromUser = new User();
                    fromUser.setId(fromJson.optString("_id"));
                    fromUser.setName(fromJson.optString("name"));
                    fromUser.setAvatar(fromJson.optString("avt_user"));
                    fromUser.setOnline(fromJson.optBoolean("isOnline")); // Thêm dòng này

                    JSONObject toJson = data.getJSONObject("to");
                    User toUser = new User();
                    toUser.setId(toJson.optString("_id"));
                    toUser.setName(toJson.optString("name"));
                    toUser.setAvatar(toJson.optString("avt_user"));

                    Message newMsg = new Message(fromUser, toUser, data.getString("content"));
                    newMsg.set_id(data.optString("_id"));
                    newMsg.setTimestamp(data.optString("timestamp"));

                    boolean isDuplicate = false;
                    for (Message existingMsg : messageList) {
                        if (newMsg.get_id() != null && newMsg.get_id().equals(existingMsg.get_id())) {
                            isDuplicate = true;
                            break;
                        }
                    }

                    if (!isDuplicate) {
                        messageList.add(newMsg);
                        adapter.notifyItemInserted(messageList.size() - 1);
                        recyclerMessages.scrollToPosition(messageList.size() - 1);
                    }
                } else if ("user_status".equals(messageType)) {
                    // Xử lý thông báo trạng thái online/offline
                    String userIdToUpdate = json.optString("userId");
                    boolean isOnline = json.optBoolean("isOnline");

                    // Cập nhật trạng thái trong danh sách tin nhắn
                    for (Message message : messageList) {
                        if (message.getFrom().getId().equals(userIdToUpdate)) {
                            message.getFrom().setOnline(isOnline);
                        }
                    }
                    // Thông báo cho adapter để cập nhật UI
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                Log.e("Chat", "Parse WS message error: " + e.getMessage());
            }
        }));
        WebSocketManager.connect(currentUserId);
    }

    private void loadAdminId() {
        apiService.getAdminId().enqueue(new Callback<AdminResponse>() {
            @Override
            public void onResponse(Call<AdminResponse> call, Response<AdminResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adminId = response.body().get_id();
                    btnSend.setEnabled(true);
                    loadMessages();
                } else {
                    Toast.makeText(Chat.this, "Không tìm thấy tài khoản admin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminResponse> call, Throwable t) {
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
                    Toast.makeText(Chat.this, "Không tải được tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(Chat.this, "Lỗi tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = edtMessage.getText().toString().trim();
        if (content.isEmpty() || adminId == null) {
            return;
        }

        // Tạo đối tượng User tạm thời chỉ với ID
        User currentUser = new User();
        currentUser.setId(currentUserId);
        User adminUser = new User();
        adminUser.setId(adminId);


        Message tempMessage = new Message(currentUser, adminUser, content);
        messageList.add(tempMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerMessages.scrollToPosition(messageList.size() - 1);
        edtMessage.setText("");


        btnSend.setEnabled(false);

        SendMessageRequest request = new SendMessageRequest(currentUserId, adminId, content);

        apiService.sendMessage(request).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                        messageList.remove(tempMessage);

                        messageList.add(response.body().getData());

                        adapter.notifyDataSetChanged();
                        recyclerMessages.scrollToPosition(messageList.size() - 1);
                    } else {

                        messageList.remove(tempMessage);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(Chat.this, "Không gửi được tin nhắn", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    // Nếu thất bại, xóa tin nhắn tạm thời
                    messageList.remove(tempMessage);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(Chat.this, "Lỗi gửi tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}