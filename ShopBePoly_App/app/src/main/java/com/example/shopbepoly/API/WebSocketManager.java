package com.example.shopbepoly.API;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;
import okio.ByteString;

import java.util.concurrent.TimeUnit;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private static WebSocket webSocket;
    private static boolean isConnected = false;
    private static Thread heartbeatThread;
    private static final int HEARTBEAT_INTERVAL = 3000;
    private static String currentUserId;

    // Listener để nhận message từ server
    public interface MessageListener {
        void onMessageReceived(String message);
    }
    private static MessageListener listener;

    public static void setMessageListener(MessageListener l) {
        listener = l;
    }

    /**
     * Kết nối WebSocket với userId
     */
    public static void connect(String userId) {
        currentUserId = userId;
        disconnect(); // Ngắt WS cũ nếu còn

        String wsUrl = "ws://" + ApiClient.IPV4 + ":3000/ws?userId=" + userId;
        Request request = new Request.Builder().url(wsUrl).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "WebSocket connected for " + userId);
                isConnected = true;
                startHeartbeat();
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d(TAG, "WS Received: " + text);
                if (listener != null) listener.onMessageReceived(text);
            }

            @Override
            public void onMessage(WebSocket ws, ByteString bytes) {
                Log.d(TAG, "WS Binary: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WS Closing: " + reason);
                stopHeartbeat();
                isConnected = false;
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket error: " + t.getMessage());
                stopHeartbeat();
                isConnected = false;
            }
        });
    }

    /**
     * Gửi tin nhắn đến server
     */
    public static void sendMessage(String message) {
        if (webSocket != null && isConnected) {
            webSocket.send(message);
        } else {
            Log.w(TAG, "Cannot send message. WS not connected.");
        }
    }

    /**
     * Ngắt kết nối WebSocket
     */
    public static void disconnect() {
        if (webSocket != null) {
            stopHeartbeat();
            try {
                webSocket.close(1000, "App closed");
            } catch (Exception e) {
                Log.e(TAG, "Error closing WS: " + e.getMessage());
            }
        }
        isConnected = false;
        webSocket = null;
    }

    /**
     * Khi app tắt hoặc background
     */
    public static void onAppClose() {
        disconnect();
    }

    /**
     * Reconnect WS khi app resume hoặc cần reconnect
     */
    public static void reconnect() {
        if (currentUserId != null && !isConnected) {
            Log.d(TAG, "WebSocket reconnect called for " + currentUserId);
            connect(currentUserId);
        }
    }

    /**
     * Heartbeat giữ kết nối
     */
    private static void startHeartbeat() {
        if (heartbeatThread != null && heartbeatThread.isAlive()) return;

        heartbeatThread = new Thread(() -> {
            while (webSocket != null && isConnected) {
                try {
                    webSocket.send("ping");
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (Exception e) {
                    Log.e(TAG, "Heartbeat stopped: " + e.getMessage());
                    break;
                }
            }
        });
        heartbeatThread.start();
    }

    private static void stopHeartbeat() {
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
            heartbeatThread = null;
        }
    }

    public static boolean isConnected() {
        return isConnected;
    }

    public static String getCurrentUserId() {
        return currentUserId;
    }
}