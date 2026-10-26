package com.reyaansh72.reyaanshsstuff;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class RpsActivity extends AppCompatActivity {

    private TextView tvScore;
    private TextView tvResult;
    private TextView tvChoices;

    private int wins = 0;
    private int losses = 0;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rps);

        ImageButton backButton = findViewById(R.id.rpsBack);
        backButton.setOnClickListener(v -> finish());

        tvScore = findViewById(R.id.tvRpsScore);
        tvResult = findViewById(R.id.tvRpsResult);
        tvChoices = findViewById(R.id.tvRpsChoices);

        MaterialButton btnRock = findViewById(R.id.btnRock);
        MaterialButton btnPaper = findViewById(R.id.btnPaper);
        MaterialButton btnScissors = findViewById(R.id.btnScissors);

        btnRock.setOnClickListener(v -> playRound("Rock", "🪨"));
        btnPaper.setOnClickListener(v -> playRound("Paper", "📄"));
        btnScissors.setOnClickListener(v -> playRound("Scissors", "✂️"));
    }

    private void playRound(String playerMove, String playerEmoji) {
        String[] moves = {"Rock", "Paper", "Scissors"};
        String[] emojis = {"🪨", "📄", "✂️"};
        int aiIdx = random.nextInt(3);
        String aiMove = moves[aiIdx];
        String aiEmoji = emojis[aiIdx];

        tvChoices.setText("You: " + playerEmoji + "  vs  AI: " + aiEmoji);

        if (playerMove.equals(aiMove)) {
            tvResult.setText("It's a Draw! 🤝");
        } else if ((playerMove.equals("Rock") && aiMove.equals("Scissors")) ||
                   (playerMove.equals("Paper") && aiMove.equals("Rock")) ||
                   (playerMove.equals("Scissors") && aiMove.equals("Paper"))) {
            wins++;
            tvResult.setText("You Win! 🎉");
        } else {
            losses++;
            tvResult.setText("AI Wins! 🤖");
        }

        tvScore.setText("Wins: " + wins + "  |  Losses: " + losses);
    }
}