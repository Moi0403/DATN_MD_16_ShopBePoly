package com.example.shopbepoly;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.NumberFormat;
import java.util.Locale;

public class VietQRPaymentHandler {

    private static final String TAG = "VietQRPaymentHandler";

    private static final String BANK_ID = "970422";
    private static final String ACCOUNT_NO = "0346752715";
    private static final String ACCOUNT_NAME = "SHOPBEPOLY";
    private static final String TEMPLATE = "compact2";

    // Payment timeout (15 minutes)
    private static final long PAYMENT_TIMEOUT = 15 * 60 * 1000; // 15 minutes in milliseconds

    private Context context;
    private PaymentCallback callback;
    private AlertDialog paymentDialog;
    private CountDownTimer countDownTimer;
    private String currentOrderId;
    private int currentAmount;

    public interface PaymentCallback {
        void onPaymentConfirmed(String orderId, int amount);
        void onPaymentCancelled();
        void onPaymentTimeout();
    }

    public VietQRPaymentHandler(Context context, PaymentCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    /**
     * Hiển thị dialog thanh toán VietQR
     */
    public void showPaymentDialog(String orderId, int amount, String customerName) {
        try {
            this.currentOrderId = orderId;
            this.currentAmount = amount;


            String transferContent = "ShopBePoly " + orderId;


            String vietQRUrl = createVietQRUrl(amount, transferContent);

            // Tạo dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_vietqr_payment, null);

            setupDialogViews(dialogView, vietQRUrl, amount, transferContent, orderId);

            builder.setView(dialogView);
            builder.setCancelable(false);

            paymentDialog = builder.create();
            paymentDialog.show();


            startPaymentTimer(dialogView);

        } catch (Exception e) {
            Log.e(TAG, "Error showing payment dialog", e);
            Toast.makeText(context, "Lỗi hiển thị thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tạo URL VietQR
     */
    private String createVietQRUrl(int amount, String content) {
        String baseUrl = "https://img.vietqr.io/image/";

        // Format: https://img.vietqr.io/image/{BANK_ID}-{ACCOUNT_NO}-{TEMPLATE}.png?amount={AMOUNT}&addInfo={CONTENT}&accountName={ACCOUNT_NAME}
        String url = baseUrl + BANK_ID + "-" + ACCOUNT_NO + "-" + TEMPLATE + ".png"
                + "?amount=" + amount
                + "&addInfo=" + Uri.encode(content)
                + "&accountName=" + Uri.encode(ACCOUNT_NAME);

        Log.d(TAG, "VietQR URL: " + url);
        return url;
    }

    /**
     * Setup các view trong dialog
     */
    private void setupDialogViews(View dialogView, String qrUrl, int amount, String content, String orderId) {
        // Tìm các view
        ImageView imgQRCode = dialogView.findViewById(R.id.imgQRCode);
        ProgressBar progressQR = dialogView.findViewById(R.id.progressQR);
        TextView txtQRStatus = dialogView.findViewById(R.id.txtQRStatus);
        TextView txtAmount = dialogView.findViewById(R.id.txtAmount);
        TextView txtContent = dialogView.findViewById(R.id.txtContent);
        TextView txtAccountInfo = dialogView.findViewById(R.id.txtAccountInfo);
        Button btnConfirmPayment = dialogView.findViewById(R.id.btnConfirmPayment);
        Button btnCancelPayment = dialogView.findViewById(R.id.btnCancelPayment);
        Button btnRefreshQR = dialogView.findViewById(R.id.btnRefreshQR);
        ImageView btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        // Set thông tin thanh toán
        txtAmount.setText(formatPrice(amount));
        txtContent.setText(content);
        txtAccountInfo.setText(String.format("MB Bank - %s\n%s", ACCOUNT_NO, ACCOUNT_NAME));

        // Load QR code
        loadQRCode(imgQRCode, progressQR, txtQRStatus, btnRefreshQR, qrUrl);

        // Setup button listeners
        btnConfirmPayment.setOnClickListener(v -> {
            // Hiển thị dialog xác nhận
            showConfirmationDialog();
        });

        btnCancelPayment.setOnClickListener(v -> {
            cancelPayment();
        });

        btnRefreshQR.setOnClickListener(v -> {
            // Tạo lại QR code với timestamp mới để tránh cache
            String newQrUrl = qrUrl + "&t=" + System.currentTimeMillis();
            loadQRCode(imgQRCode, progressQR, txtQRStatus, btnRefreshQR, newQrUrl);
        });

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> cancelPayment());
        }
    }

    /**
     * Load QR code image
     */
    private void loadQRCode(ImageView imgQRCode, ProgressBar progressQR, TextView txtQRStatus, Button btnRefreshQR, String qrUrl) {
        progressQR.setVisibility(View.VISIBLE);
        imgQRCode.setVisibility(View.GONE);
        txtQRStatus.setText("Đang tạo mã QR...");
        btnRefreshQR.setVisibility(View.GONE);

        Glide.with(context)
                .load(qrUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Không cache để luôn lấy QR mới
                .skipMemoryCache(true)
                .placeholder(null)
                .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    @Override
                    public void onResourceReady(android.graphics.drawable.Drawable resource,
                                                com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                        progressQR.setVisibility(View.GONE);
                        imgQRCode.setVisibility(View.VISIBLE);
                        imgQRCode.setImageDrawable(resource);
                        txtQRStatus.setText("Quét mã QR để thanh toán");
                    }

                    @Override
                    public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
                        // Do nothing
                    }

                    @Override
                    public void onLoadFailed(android.graphics.drawable.Drawable errorDrawable) {
                        progressQR.setVisibility(View.GONE);
                        txtQRStatus.setText("Không thể tạo mã QR. Nhấn để thử lại.");
                        btnRefreshQR.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xác nhận thanh toán");
        builder.setMessage("Bạn có chắc chắn đã chuyển khoản thành công?\n\n" +
                "Số tiền: " + formatPrice(currentAmount) + "\n" +
                "Nội dung: ShopBePoly " + currentOrderId);

        builder.setPositiveButton("Đã thanh toán", (dialog, which) -> {
            confirmPayment();
        });

        builder.setNegativeButton("Chưa thanh toán", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }


    private void confirmPayment() {
        if (paymentDialog != null) {
            paymentDialog.dismiss();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (callback != null) {
            callback.onPaymentConfirmed(currentOrderId, currentAmount);
        }

        Toast.makeText(context, "Đơn hàng của bạn đang được xử lý!", Toast.LENGTH_LONG).show();
    }

    private void cancelPayment() {
        if (paymentDialog != null) {
            paymentDialog.dismiss();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (callback != null) {
            callback.onPaymentCancelled();
        }
    }


    private void startPaymentTimer(View dialogView) {
        TextView txtTimer = dialogView.findViewById(R.id.txtTimer);

        countDownTimer = new CountDownTimer(PAYMENT_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;

                String timeText = String.format("Thời gian còn lại: %02d:%02d", minutes, seconds);
                txtTimer.setText(timeText);

                // Đổi màu khi sắp hết thời gian (dưới 2 phút)
                if (millisUntilFinished < 2 * 60 * 1000) {
                    txtTimer.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFinish() {
                txtTimer.setText("Hết thời gian!");
                txtTimer.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));

                // Tự động hủy thanh toán khi hết thời gian
                if (paymentDialog != null) {
                    paymentDialog.dismiss();
                }

                if (callback != null) {
                    callback.onPaymentTimeout();
                }

                Toast.makeText(context, "Phiên thanh toán đã hết hạn!", Toast.LENGTH_LONG).show();
            }
        };

        countDownTimer.start();
    }


    private String formatPrice(int price) {
        return NumberFormat.getNumberInstance(Locale.US).format(price) + "₫";
    }

    public void cleanup() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        if (paymentDialog != null && paymentDialog.isShowing()) {
            paymentDialog.dismiss();
            paymentDialog = null;
        }
    }


    public boolean isShowing() {
        return paymentDialog != null && paymentDialog.isShowing();
    }
}