package com.reyaansh72.reyaanshsstuff;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends AppCompatActivity {

    private TextInputEditText etCitySearch;
    private ProgressBar weatherLoadingProgress;
    private TextView tvWeatherCity;
    private TextView tvWeatherTemp;
    private TextView tvWeatherCondition;
    private TextView tvHumidity;
    private TextView tvWind;
    private LinearLayout savedCitiesContainer;
    private TextView tvClearSavedCities;

    private SharedPreferences prefs;
    private final List<SavedCity> savedCities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather);

        prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);

        ImageButton backButton = findViewById(R.id.weatherBackButton);
        backButton.setOnClickListener(v -> finish());

        etCitySearch = findViewById(R.id.etCitySearch);
        weatherLoadingProgress = findViewById(R.id.weatherLoadingProgress);
        tvWeatherCity = findViewById(R.id.tvWeatherCity);
        tvWeatherTemp = findViewById(R.id.tvWeatherTemp);
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        savedCitiesContainer = findViewById(R.id.savedCitiesContainer);
        tvClearSavedCities = findViewById(R.id.tvClearSavedCities);

        MaterialButton btnSearchCity = findViewById(R.id.btnSearchCity);

        btnSearchCity.setOnClickListener(v -> {
            String query = etCitySearch.getText() != null ? etCitySearch.getText().toString().trim() : "";
            if (!TextUtils.isEmpty(query)) {
                searchAndFetchWeather(query);
            } else {
                Toast.makeText(this, "Enter a city name to search", Toast.LENGTH_SHORT).show();
            }
        });

        tvClearSavedCities.setOnClickListener(v -> {
            savedCities.clear();
            saveSavedCitiesToPrefs();
            renderSavedCityChips();
            Toast.makeText(this, "Cleared saved cities", Toast.LENGTH_SHORT).show();
        });

        loadSavedCitiesFromPrefs();

        if (savedCities.isEmpty()) {
            // Default first search for Agra
            searchAndFetchWeather("Agra");
        } else {
            renderSavedCityChips();
            SavedCity first = savedCities.get(0);
            fetchOpenMeteoWeather(first.name, first.lat, first.lon);
        }
    }

    private void searchAndFetchWeather(String cityName) {
        weatherLoadingProgress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                String searchUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + Uri.encode(cityName) + "&count=1";
                String jsonResponse = httpGet(searchUrl);
                JSONObject root = new JSONObject(jsonResponse);

                if (root.has("results")) {
                    JSONArray results = root.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject first = results.getJSONObject(0);
                        String name = first.optString("name", cityName);
                        String country = first.optString("country", "");
                        double lat = first.getDouble("latitude");
                        double lon = first.getDouble("longitude");

                        String fullName = country.isEmpty() ? name : name + ", " + country;

                        addSavedCity(fullName, lat, lon);

                        runOnUiThread(() -> {
                            renderSavedCityChips();
                            fetchOpenMeteoWeather(fullName, lat, lon);
                        });
                        return;
                    }
                }

                runOnUiThread(() -> {
                    weatherLoadingProgress.setVisibility(View.GONE);
                    Toast.makeText(WeatherActivity.this, "City '" + cityName + "' not found", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    weatherLoadingProgress.setVisibility(View.GONE);
                    Toast.makeText(WeatherActivity.this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void fetchOpenMeteoWeather(String locationName, double lat, double lon) {
        weatherLoadingProgress.setVisibility(View.VISIBLE);
        tvWeatherCity.setText(locationName);

        new Thread(() -> {
            try {
                String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                        "&longitude=" + lon + "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m";

                String response = httpGet(weatherApiUrl);
                JSONObject json = new JSONObject(response);
                JSONObject current = json.getJSONObject("current");

                double temp = current.getDouble("temperature_2m");
                int humidity = current.getInt("relative_humidity_2m");
                int weatherCode = current.getInt("weather_code");
                double wind = current.getDouble("wind_speed_10m");

                String conditionText = getWeatherConditionText(weatherCode);

                runOnUiThread(() -> {
                    weatherLoadingProgress.setVisibility(View.GONE);
                    tvWeatherTemp.setText(Math.round(temp) + "°C");
                    tvWeatherCondition.setText(conditionText);
                    tvHumidity.setText(humidity + "%");
                    tvWind.setText(Math.round(wind) + " km/h");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    weatherLoadingProgress.setVisibility(View.GONE);
                    Toast.makeText(WeatherActivity.this, "Weather fetch failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void addSavedCity(String name, double lat, double lon) {
        for (SavedCity sc : savedCities) {
            if (sc.name.equalsIgnoreCase(name)) return;
        }
        savedCities.add(0, new SavedCity(name, lat, lon));
        saveSavedCitiesToPrefs();
    }

    private void renderSavedCityChips() {
        savedCitiesContainer.removeAllViews();
        for (SavedCity sc : savedCities) {
            MaterialButton chip = new MaterialButton(this);
            chip.setText(sc.name);
            chip.setAllCaps(false);
            chip.setCornerRadius(40);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> fetchOpenMeteoWeather(sc.name, sc.lat, sc.lon));
            savedCitiesContainer.addView(chip);
        }
    }

    private void saveSavedCitiesToPrefs() {
        StringBuilder sb = new StringBuilder();
        for (SavedCity sc : savedCities) {
            sb.append(sc.name).append("|").append(sc.lat).append("|").append(sc.lon).append(";");
        }
        prefs.edit().putString("saved_cities", sb.toString()).apply();
    }

    private void loadSavedCitiesFromPrefs() {
        savedCities.clear();
        String raw = prefs.getString("saved_cities", "");
        if (!TextUtils.isEmpty(raw)) {
            String[] parts = raw.split(";");
            for (String p : parts) {
                if (p.contains("|")) {
                    String[] fields = p.split("\\|");
                    if (fields.length >= 3) {
                        try {
                            savedCities.add(new SavedCity(fields[0], Double.parseDouble(fields[1]), Double.parseDouble(fields[2])));
                        } catch (Exception e) {
                            // parse error
                        }
                    }
                }
            }
        }
    }

    private String getWeatherConditionText(int code) {
        switch (code) {
            case 0: return "☀️ Clear Sky";
            case 1:
            case 2:
            case 3: return "🌤️ Partly Cloudy / Overcast";
            case 45:
            case 48: return "🌫️ Foggy";
            case 51:
            case 53:
            case 55:
            case 61:
            case 63:
            case 65: return "🌧️ Rain & Drizzle";
            case 71:
            case 73:
            case 75: return "❄️ Snow";
            case 95:
            case 96:
            case 99: return "⚡ Thunderstorm";
            default: return "🌤️ Mild Weather";
        }
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }

    static class SavedCity {
        String name;
        double lat;
        double lon;

        SavedCity(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }
}