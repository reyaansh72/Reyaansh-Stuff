package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.net.URL;

public class QrCodeActivity extends AppCompatActivity {

    private TextInputEditText etQrInput;
    private ImageView ivQrPreview;
    private String currentQrUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_code);

        ImageButton backButton = findViewById(R.id.qrBackButton);
        backButton.setOnClickListener(v -> finish());

        etQrInput = findViewById(R.id.etQrInput);
        ivQrPreview = findViewById(R.id.ivQrPreview);
        MaterialButton btnGenerateQr = findViewById(R.id.btnGenerateQr);
        MaterialButton btnShareQr = findViewById(R.id.btnShareQr);

        btnGenerateQr.setOnClickListener(v -> {
            String text = etQrInput.getText() != null ? etQrInput.getText().toString().trim() : "";
            if (!TextUtils.isEmpty(text)) {
                generateQrCode(text);
            } else {
                Toast.makeText(this, "Enter text or URL first", Toast.LENGTH_SHORT).show();
            }
        });

        btnShareQr.setOnClickListener(v -> {
            if (!currentQrUrl.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Generated QR Code Image:\n" + currentQrUrl);
                startActivity(Intent.createChooser(shareIntent, "Share QR Link"));
            } else {
                Toast.makeText(this, "Generate a QR code first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Default generate
        etQrInput.setText("https://github.com");
        generateQrCode("https://github.com");
    }

    private void generateQrCode(String data) {
        currentQrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=" + Uri.encode(data);
        new Thread(() -> {
            try {
                InputStream in = new URL(currentQrUrl).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                runOnUiThread(() -> ivQrPreview.setImageBitmap(bitmap));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(QrCodeActivity.this, "Failed to load QR image", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}