package com.reyaansh72.reyaanshsstuff;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TicTacToeActivity extends AppCompatActivity {

    private final Button[][] buttons = new Button[3][3];
    private boolean player1Turn = true;
    private int roundCount = 0;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);

        findViewById(R.id.tttBack).setOnClickListener(v -> finish());
        tvStatus = findViewById(R.id.tvTttStatus);
        findViewById(R.id.btnResetTtt).setOnClickListener(v -> resetBoard());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "btn" + i + j;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                final int finalI = i;
                final int finalJ = j;
                buttons[i][j].setOnClickListener(v -> onGridClick(finalI, finalJ));
            }
        }
    }

    private void onGridClick(int row, int col) {
        if (!buttons[row][col].getText().toString().equals("")) {
            return;
        }

        if (player1Turn) {
            buttons[row][col].setText("X");
        } else {
            buttons[row][col].setText("O");
        }

        roundCount++;

        if (checkForWin()) {
            if (player1Turn) {
                tvStatus.setText("Player X Wins! 🎉");
            } else {
                tvStatus.setText("Player O Wins! 🎉");
            }
            disableBoard();
        } else if (roundCount == 9) {
            tvStatus.setText("It's a Draw! 🤝");
        } else {
            player1Turn = !player1Turn;
            tvStatus.setText(player1Turn ? "Player X's turn" : "Player O's turn");
        }
    }

    private boolean checkForWin() {
        String[][] field = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }

        // Rows
        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1]) && field[i][0].equals(field[i][2]) && !field[i][0].equals("")) {
                return true;
            }
        }

        // Columns
        for (int i = 0; i < 3; i++) {
            if (field[0][i].equals(field[1][i]) && field[0][i].equals(field[2][i]) && !field[0][i].equals("")) {
                return true;
            }
        }

        // Diagonals
        if (field[0][0].equals(field[1][1]) && field[0][0].equals(field[2][2]) && !field[0][0].equals("")) {
            return true;
        }

        if (field[0][2].equals(field[1][1]) && field[0][2].equals(field[2][0]) && !field[0][2].equals("")) {
            return true;
        }

        return false;
    }

    private void disableBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        roundCount = 0;
        player1Turn = true;
        tvStatus.setText("Player X's turn");
    }
}