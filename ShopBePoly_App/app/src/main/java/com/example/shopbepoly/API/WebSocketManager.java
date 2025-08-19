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
    private static final String TAG = "WebSocket";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private static WebSocket webSocket;
    private static boolean isConnected = false;
    private static Thread heartbeatThread;
    private static final int HEARTBEAT_INTERVAL = 3000;
    private static String currentUserId;

    // Listener để Chat.java nhận tin mới
    public interface MessageListener {
        void onMessageReceived(String message);
    }
    private static MessageListener listener;

    public static void setMessageListener(MessageListener l) {
        listener = l;
    }

    public static void connect(String userId) {
        currentUserId = userId;
        disconnect();

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

    public static void reconnect() {
        if (currentUserId != null) {
            connect(currentUserId);
        }
    }

    public static void disconnect() {
        if (webSocket != null && isConnected) {
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
