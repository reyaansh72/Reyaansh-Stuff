package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class GamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_games);

        ImageButton backButton = findViewById(R.id.gamesBackButton);
        backButton.setOnClickListener(v -> finish());

        MaterialCardView cardSnake = findViewById(R.id.cardSnake);
        MaterialButton btnPlaySnake = findViewById(R.id.btnPlaySnake);
        View.OnClickListener snakeListener = v -> {
            startActivity(new Intent(GamesActivity.this, SnakeActivity.class));
        };
        cardSnake.setOnClickListener(snakeListener);
        btnPlaySnake.setOnClickListener(snakeListener);

        MaterialCardView cardPong = findViewById(R.id.cardPong);
        MaterialButton btnPlayPong = findViewById(R.id.btnPlayPong);
        View.OnClickListener pongListener = v -> {
            startActivity(new Intent(GamesActivity.this, PongActivity.class));
        };
        cardPong.setOnClickListener(pongListener);
        btnPlayPong.setOnClickListener(pongListener);

        MaterialCardView cardTtt = findViewById(R.id.cardTtt);
        MaterialButton btnPlayTtt = findViewById(R.id.btnPlayTtt);
        View.OnClickListener tttListener = v -> {
            startActivity(new Intent(GamesActivity.this, TicTacToeActivity.class));
        };
        cardTtt.setOnClickListener(tttListener);
        btnPlayTtt.setOnClickListener(tttListener);

        MaterialCardView cardFlappy = findViewById(R.id.cardFlappy);
        MaterialButton btnPlayFlappy = findViewById(R.id.btnPlayFlappy);
        View.OnClickListener flappyListener = v -> {
            startActivity(new Intent(GamesActivity.this, FlappyActivity.class));
        };
        cardFlappy.setOnClickListener(flappyListener);
        btnPlayFlappy.setOnClickListener(flappyListener);

        MaterialCardView cardShooter = findViewById(R.id.cardShooter);
        MaterialButton btnPlayShooter = findViewById(R.id.btnPlayShooter);
        View.OnClickListener shooterListener = v -> {
            startActivity(new Intent(GamesActivity.this, SpaceShooterActivity.class));
        };
        cardShooter.setOnClickListener(shooterListener);
        btnPlayShooter.setOnClickListener(shooterListener);

        MaterialCardView cardRps = findViewById(R.id.cardRps);
        MaterialButton btnPlayRps = findViewById(R.id.btnPlayRps);
        View.OnClickListener rpsListener = v -> {
            startActivity(new Intent(GamesActivity.this, RpsActivity.class));
        };
        cardRps.setOnClickListener(rpsListener);
        btnPlayRps.setOnClickListener(rpsListener);
    }
}