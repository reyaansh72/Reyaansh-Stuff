package com.reyaansh72.reyaanshsstuff;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ConverterActivity extends AppCompatActivity {

    private TextInputEditText etConvInput;
    private RadioGroup convTypeRadioGroup;
    private TextView tvConvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_converter);

        ImageButton backButton = findViewById(R.id.convBackButton);
        backButton.setOnClickListener(v -> finish());

        etConvInput = findViewById(R.id.etConvInput);
        convTypeRadioGroup = findViewById(R.id.convTypeRadioGroup);
        tvConvResult = findViewById(R.id.tvConvResult);
        MaterialButton btnConvert = findViewById(R.id.btnConvert);

        btnConvert.setOnClickListener(v -> performConversion());
    }

    private void performConversion() {
        String inputStr = etConvInput.getText() != null ? etConvInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(inputStr)) {
            Toast.makeText(this, "Enter a number first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double val = Double.parseDouble(inputStr);
            int selectedId = convTypeRadioGroup.getCheckedRadioButtonId();
            double result = 0;
            String unit = "";

            if (selectedId == R.id.radioCtoF) {
                result = (val * 9 / 5) + 32;
                unit = " °F";
            } else if (selectedId == R.id.radioFtoC) {
                result = (val - 32) * 5 / 9;
                unit = " °C";
            } else if (selectedId == R.id.radioKmToMiles) {
                result = val * 0.621371;
                unit = " miles";
            } else if (selectedId == R.id.radioKgToLbs) {
                result = val * 2.20462;
                unit = " lbs";
            }

            tvConvResult.setText(String.format("%.2f", result) + unit);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
        }
    }
}