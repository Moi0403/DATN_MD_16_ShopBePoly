package com.example.shopbepoly;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class VietQRValidator {

    private static final String TAG = "VietQRValidator";
    private static final String PREFS_NAME = "VietQRPrefs";

    // Danh sách các ngân hàng hỗ trợ VietQR
    private static final Map<String, String> SUPPORTED_BANKS = new HashMap<String, String>() {{
        put("970415", "Vietinbank");
        put("970418", "BIDV");
        put("970422", "MB Bank");
        put("970407", "Techcombank");
        put("970416", "ACB");
        put("970432", "VPBank");
        put("970403", "Sacombank");
        put("970448", "OCB");
        put("970423", "TPBank");
        put("970414", "SHB");
        put("970431", "Eximbank");
        put("970426", "MSB");
        put("970419", "NCB");
        put("970405", "Agribank");
        put("970409", "BacABank");
        put("970428", "Nam A Bank");
        put("970434", "VIB");
        put("970436", "Vietcombank");
        put("970438", "BVB");
        put("970440", "SeABank");
    }};

    /**
     * Kiểm tra bank ID có hợp lệ không
     */
    public static boolean isValidBankId(String bankId) {
        return SUPPORTED_BANKS.containsKey(bankId);
    }

    /**
     * Lấy tên ngân hàng từ bank ID
     */
    public static String getBankName(String bankId) {
        return SUPPORTED_BANKS.getOrDefault(bankId, "Unknown Bank");
    }

    /**
     * Kiểm tra số tài khoản có hợp lệ không (cơ bản)
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }

        accountNumber = accountNumber.trim();

        // Kiểm tra độ dài (6-20 ký tự)
        if (accountNumber.length() < 6 || accountNumber.length() > 20) {
            return false;
        }

        // Chỉ chứa số
        return accountNumber.matches("^[0-9]+$");
    }

    /**
     * Kiểm tra số tiền có hợp lệ không
     */
    public static boolean isValidAmount(int amount) {
        return amount > 0 && amount <= 500000000; // Tối đa 500 triệu
    }

    /**
     * Validate nội dung chuyển khoản
     */
    public static boolean isValidTransferContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        content = content.trim();

        // Độ dài từ 3-500 ký tự
        if (content.length() < 3 || content.length() > 500) {
            return false;
        }

        // Không chứa ký tự đặc biệt nguy hiểm
        String dangerousChars = "<>\"'&";
        for (char c : dangerousChars.toCharArray()) {
            if (content.indexOf(c) != -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tạo VietQR URL với validation
     */
    public static String createValidatedVietQRUrl(String bankId, String accountNumber,
                                                  String accountName, int amount, String content) {
        try {
            // Validate các tham số
            if (!isValidBankId(bankId)) {
                throw new IllegalArgumentException("Bank ID không hợp lệ: " + bankId);
            }

            if (!isValidAccountNumber(accountNumber)) {
                throw new IllegalArgumentException("Số tài khoản không hợp lệ");
            }

            if (!isValidAmount(amount)) {
                throw new IllegalArgumentException("Số tiền không hợp lệ");
            }

            if (!isValidTransferContent(content)) {
                throw new IllegalArgumentException("Nội dung chuyển khoản không hợp lệ");
            }

            if (accountName == null || accountName.trim().isEmpty()) {
                throw new IllegalArgumentException("Tên tài khoản không được để trống");
            }

            // Tạo URL
            String baseUrl = "https://img.vietqr.io/image/";
            String template = "compact2";

            String url = baseUrl + bankId + "-" + accountNumber + "-" + template + ".png"
                    + "?amount=" + amount
                    + "&addInfo=" + android.net.Uri.encode(content.trim())
                    + "&accountName=" + android.net.Uri.encode(accountName.trim());

            Log.d(TAG, "Generated VietQR URL: " + url);
            return url;

        } catch (Exception e) {
            Log.e(TAG, "Error creating VietQR URL", e);
            throw new RuntimeException("Không thể tạo mã QR: " + e.getMessage());
        }
    }

    /**
     * Lưu thông tin QR code đã tạo để debug
     */
    public static void saveQRCodeInfo(Context context, String orderId, String url, int amount) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String key = "qr_" + orderId;
            String info = url + "|" + amount + "|" + System.currentTimeMillis();

            editor.putString(key, info);
            editor.apply();

            Log.d(TAG, "Saved QR info for order: " + orderId);

        } catch (Exception e) {
            Log.e(TAG, "Error saving QR info", e);
        }
    }

    /**
     * Lấy thông tin QR code đã lưu
     */
    public static String getSavedQRInfo(Context context, String orderId) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String key = "qr_" + orderId;
            return prefs.getString(key, null);
        } catch (Exception e) {
            Log.e(TAG, "Error getting saved QR info", e);
            return null;
        }
    }

    /**
     * Xóa thông tin QR code cũ (cleanup)
     */
    public static void cleanupOldQRInfo(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            long currentTime = System.currentTimeMillis();
            long oneDayAgo = currentTime - (24 * 60 * 60 * 1000); // 1 ngày trước

            Map<String, ?> allEntries = prefs.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("qr_")) {
                    String value = (String) entry.getValue();
                    if (value != null) {
                        String[] parts = value.split("\\|");
                        if (parts.length >= 3) {
                            try {
                                long timestamp = Long.parseLong(parts[2]);
                                if (timestamp < oneDayAgo) {
                                    editor.remove(key);
                                    Log.d(TAG, "Removed old QR info: " + key);
                                }
                            } catch (NumberFormatException e) {
                                // Remove invalid entries
                                editor.remove(key);
                            }
                        }
                    }
                }
            }

            editor.apply();

        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up old QR info", e);
        }
    }

    /**
     * Format số tiền cho display
     */
    public static String formatCurrency(int amount) {
        return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(amount) + "₫";
    }

    /**
     * Kiểm tra kết nối internet
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            android.net.ConnectivityManager connectivityManager =
                    (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking network", e);
            return false;
        }
    }

    /**
     * Tạo order ID duy nhất
     */
    public static String generateOrderId() {
        return "ORD" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * Validate order ID format
     */
    public static boolean isValidOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return false;
        }

        // Format: ORD + timestamp + _ + random number
        return orderId.matches("^ORD\\d+_\\d{1,3}$");
    }
}
