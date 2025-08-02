package com.example.shopbepoly.API;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.util.concurrent.TimeUnit;

public class WebSocketManager {
    private static WebSocket webSocket;
    private static final String TAG = "WebSocket";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    private static final int HEARTBEAT_INTERVAL = 3000;
    private static Thread heartbeatThread;
    private static boolean isConnected = false;

    // ✅ Biến lưu userId hiện tại
    private static String currentUserId;

    public static void connect(String userId) {
        currentUserId = userId; // ✅ Lưu lại userId để reconnect sau này

        if (webSocket != null) {
            Log.d(TAG, "Closing existing WebSocket connection before reconnecting");
            disconnect();
        }

        String wsUrl = "ws://" + ApiClient.IPV4 + ":3000/ws?userId=" + userId;
        Request request = new Request.Builder().url(wsUrl).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, okhttp3.Response response) {
                Log.d(TAG, "WebSocket connected for userId: " + userId);
                isConnected = true;
                startHeartbeat();
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d(TAG, "Received message: " + text);
                if ("ping".equals(text)) {
                    ws.send("pong");
                }
            }

            @Override
            public void onMessage(WebSocket ws, ByteString bytes) {
                Log.d(TAG, "Received binary message: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
                sendOffline();
                stopHeartbeat();
                isConnected = false;
                webSocket = null;
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket error: " + t.getMessage());
                sendOffline();
                stopHeartbeat();
                isConnected = false;
                webSocket = null;
            }
        });
    }

    // ✅ Dùng lại userId cũ để reconnect khi app foreground
    public static void reconnect() {
        if (currentUserId != null) {
            Log.d(TAG, "Reconnecting WebSocket with userId: " + currentUserId);
            connect(currentUserId);
        } else {
            Log.w(TAG, "Cannot reconnect: currentUserId is null");
        }
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

    private static void sendOffline() {
        if (webSocket != null && isConnected) {
            try {
                boolean sent = webSocket.send("offline");
                Log.d(TAG, "Sent 'offline' message to server: " + sent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send 'offline': " + e.getMessage());
            }
        }
    }

    public static void disconnect() {
        if (webSocket != null && isConnected) {
            stopHeartbeat();
            sendOffline();
            try {
                boolean closed = webSocket.close(1000, "App closed");
                Log.d(TAG, "WebSocket close() called, success: " + closed);
            } catch (Exception e) {
                Log.e(TAG, "Error during WebSocket disconnect: " + e.getMessage());
            }

            isConnected = false;
            webSocket = null;
        } else {
            Log.w(TAG, "disconnect() called but WebSocket is already closed or null");
        }
    }

    public static void onAppClose() {
        disconnect();
    }

    // ✅ Dùng để gọi từ AppLifecycleListener
    public static boolean isConnected() {
        return isConnected;
    }

    public static String getCurrentUserId() {
        return currentUserId;
    }
}
