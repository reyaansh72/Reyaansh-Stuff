package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton settingsBackButton;
    private TextInputEditText etOllamaHostUrl;
    private RadioGroup modeRadioGroup;
    private RadioButton radioModeSystem, radioModeLight, radioModeDark;
    private RadioGroup themeRadioGroup;
    private RadioButton radioDynamic, radioEmerald, radioBlue, radioPurple, radioAmber;
    private SwitchCompat switchAutoplay, switchMuteDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.settingsRoot), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );

        initializeViews();
        loadCurrentSettings();
    }

    private void initializeViews() {
        settingsBackButton = findViewById(R.id.settingsBackButton);
        etOllamaHostUrl = findViewById(R.id.etOllamaHostUrl);

        modeRadioGroup = findViewById(R.id.modeRadioGroup);
        radioModeSystem = findViewById(R.id.radioModeSystem);
        radioModeLight = findViewById(R.id.radioModeLight);
        radioModeDark = findViewById(R.id.radioModeDark);

        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        radioDynamic = findViewById(R.id.radioDynamic);
        radioEmerald = findViewById(R.id.radioEmerald);
        radioBlue = findViewById(R.id.radioBlue);
        radioPurple = findViewById(R.id.radioPurple);
        radioAmber = findViewById(R.id.radioAmber);

        switchAutoplay = findViewById(R.id.switchAutoplay);
        switchMuteDefault = findViewById(R.id.switchMuteDefault);
        MaterialButton btnSaveSettings = findViewById(R.id.btnSaveSettings);

        settingsBackButton.setOnClickListener(v -> finish());
        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadCurrentSettings() {
        etOllamaHostUrl.setText(ThemeUtils.getOllamaHostUrl(this));

        // Mode
        String currentMode = ThemeUtils.getSelectedMode(this);
        if ("light".equals(currentMode)) {
            radioModeLight.setChecked(true);
        } else if ("dark".equals(currentMode)) {
            radioModeDark.setChecked(true);
        } else {
            radioModeSystem.setChecked(true);
        }

        // Theme Accent
        String currentTheme = ThemeUtils.getSelectedTheme(this);
        switch (currentTheme) {
            case "blue":
                radioBlue.setChecked(true);
                break;
            case "purple":
                radioPurple.setChecked(true);
                break;
            case "amber":
                radioAmber.setChecked(true);
                break;
            case "emerald":
                radioEmerald.setChecked(true);
                break;
            case "dynamic":
            default:
                radioDynamic.setChecked(true);
                break;
        }

        switchAutoplay.setChecked(ThemeUtils.isAutoplayEnabled(this));
        switchMuteDefault.setChecked(ThemeUtils.isMuteDefault(this));
    }

    private void saveSettings() {
        // Save Ollama Host URL
        String hostUrl = etOllamaHostUrl.getText() != null ? etOllamaHostUrl.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(hostUrl)) {
            ThemeUtils.setOllamaHostUrl(this, hostUrl);
        }

        // Save Mode
        int selectedModeId = modeRadioGroup.getCheckedRadioButtonId();
        String modeChoice = "system";
        if (selectedModeId == R.id.radioModeLight) {
            modeChoice = "light";
        } else if (selectedModeId == R.id.radioModeDark) {
            modeChoice = "dark";
        } else if (selectedModeId == R.id.radioModeSystem) {
            modeChoice = "system";
        }
        ThemeUtils.saveMode(this, modeChoice);

        // Save Theme Accent
        int selectedThemeId = themeRadioGroup.getCheckedRadioButtonId();
        String themeChoice = "dynamic";
        if (selectedThemeId == R.id.radioBlue) {
            themeChoice = "blue";
        } else if (selectedThemeId == R.id.radioPurple) {
            themeChoice = "purple";
        } else if (selectedThemeId == R.id.radioAmber) {
            themeChoice = "amber";
        } else if (selectedThemeId == R.id.radioEmerald) {
            themeChoice = "emerald";
        } else if (selectedThemeId == R.id.radioDynamic) {
            themeChoice = "dynamic";
        }
        ThemeUtils.saveTheme(this, themeChoice);

        ThemeUtils.setAutoplayEnabled(this, switchAutoplay.isChecked());
        ThemeUtils.setMuteDefault(this, switchMuteDefault.isChecked());

        Toast.makeText(this, "Settings saved! Applying theme...", Toast.LENGTH_SHORT).show();

        // Restart app to apply theme mode & accent cleanly
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
}