package com.example.shopbepoly;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ThanhToan extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thanh_toan);

        // Ánh xạ view
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageView imgProduct = findViewById(R.id.imgProduct);
        TextView txtProductName = findViewById(R.id.txtProductName);
        TextView txtProductQuantity = findViewById(R.id.txtProductQuantity);
        View viewProductColor = findViewById(R.id.viewProductColor);
        TextView txtProductSize = findViewById(R.id.txtProductSize);
        TextView txtProductPrice = findViewById(R.id.txtProductPrice);
        TextView txtProductTotal = findViewById(R.id.txtProductTotal);
        TextView txtShippingFee = findViewById(R.id.txtShippingFee);
        TextView txtTotalPayment = findViewById(R.id.txtTotalPayment);
        // EditText cho thông tin khách hàng
        // ...
        RadioGroup radioGroupPaymentMain = findViewById(R.id.radioGroupPaymentMain);
        RadioButton radioCOD = findViewById(R.id.radioCOD);
        RadioButton radioAppBank = findViewById(R.id.radioAppBank);
        View layoutBankOptions = findViewById(R.id.layoutBankOptions);
        RadioGroup radioGroupBank = findViewById(R.id.radioGroupBank);
        Button btnDatHang = findViewById(R.id.btnDatHang);

        // Ẩn/hiện lựa chọn ngân hàng
        radioGroupPaymentMain.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAppBank) {
                layoutBankOptions.setVisibility(View.VISIBLE);
            } else {
                layoutBankOptions.setVisibility(View.GONE);
                radioGroupBank.clearCheck();
            }
        });

        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // Xử lý nút Đặt hàng (ví dụ lấy phương thức thanh toán)
        btnDatHang.setOnClickListener(v -> {
            String paymentMethod = "";
            if (radioCOD.isChecked()) {
                paymentMethod = "Thanh toán khi nhận hàng";
            } else if (radioAppBank.isChecked()) {
                int checkedBankId = radioGroupBank.getCheckedRadioButtonId();
                if (checkedBankId == R.id.radioMomo) {
                    paymentMethod = "Momo";
                } else if (checkedBankId == R.id.radioAgribank) {
                    paymentMethod = "Agribank";
                } else {
                    paymentMethod = "Chưa chọn ngân hàng";
                }
            }
            // Chuyển sang màn hình đặt hàng thành công
            startActivity(new android.content.Intent(this, Dathangthanhcong.class));
            // Nếu muốn đóng luôn màn thanh toán sau khi chuyển, bỏ comment dòng dưới:
            // finish();
        });
    }
}