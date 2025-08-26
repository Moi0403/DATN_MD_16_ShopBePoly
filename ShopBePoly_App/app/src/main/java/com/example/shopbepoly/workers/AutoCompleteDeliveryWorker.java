package com.example.shopbepoly.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.shopbepoly.API.ApiClient;
import com.example.shopbepoly.API.ApiService;
import com.example.shopbepoly.DTO.Order;

import retrofit2.Call;
import retrofit2.Response;

public class AutoCompleteDeliveryWorker extends Worker {

    private static final String TAG = "AutoCompleteDelivery";

    public AutoCompleteDeliveryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data input = getInputData();
            String orderId = input.getString("orderId");
            if (orderId == null || orderId.isEmpty()) {
                Log.e(TAG, "Missing orderId in input data");
                return Result.failure();
            }

            ApiService api = ApiClient.getApiService();

            // Fetch order to check current status
            Response<Order> detailResp = api.getOrderDetail(orderId).execute();
            if (!detailResp.isSuccessful() || detailResp.body() == null) {
                Log.e(TAG, "Failed to fetch order detail before auto-complete, code=" + detailResp.code());
                return Result.retry();
            }

            Order current = detailResp.body();
            String status = current.getStatus() != null ? current.getStatus() : "";

            // Only auto-complete if still delivering
            if (!"Đang giao hàng".equalsIgnoreCase(status)) {
                Log.d(TAG, "Order " + orderId + " is not in delivering status anymore (" + status + ")");
                return Result.success();
            }

            Order update = new Order();
            update.set_id(orderId);
            update.setStatus("Đã giao hàng");

            Call<Order> call = api.upStatus(orderId, update);
            Response<Order> resp = call.execute();
            if (resp.isSuccessful()) {
                Log.d(TAG, "Auto-completed order to Delivered: " + orderId);
                return Result.success();
            } else {
                Log.e(TAG, "Failed to auto-complete delivery, code=" + resp.code());
                return Result.retry();
            }
        } catch (Exception e) {
            Log.e(TAG, "Worker exception", e);
            return Result.retry();
        }
    }
}


