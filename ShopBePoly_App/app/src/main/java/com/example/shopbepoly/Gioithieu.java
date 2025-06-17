package com.example.shopbepoly;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class Gioithieu extends AppCompatActivity {
    ImageButton btnBack;
    Button btnClearCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gioithieu);
        btnClearCache = findViewById(R.id.btnClearCache);
        btnClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAppCache(Gioithieu.this);
            }
        });
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    private void clearAppCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            long sizeBefore = getDirSize(cacheDir);
            Log.d("CACHE", "Kích thước cache trước khi xóa: " + sizeBefore + " bytes");
            deleteDir(cacheDir);
            long sizeAfter = getDirSize(cacheDir);
            Log.d("CACHE", "Kích thước cache sau khi xóa: " + sizeAfter + " bytes");

            Toast.makeText(context, "Đã xóa bộ nhớ đệm", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi xóa bộ nhớ đệm", Toast.LENGTH_SHORT).show();
        }
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getDirSize(file);
                }
            }
        }
        return size;
    }
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}