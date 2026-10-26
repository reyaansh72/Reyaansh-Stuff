package com.reyaansh72.reyaanshsstuff;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PongActivity extends AppCompatActivity {

    private PongView pongView;
    private TextView tvScore;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pong);

        findViewById(R.id.pongBack).setOnClickListener(v -> finish());
        tvScore = findViewById(R.id.tvPongScore);

        LinearLayout container = findViewById(R.id.pongContainer);
        pongView = new PongView(this);
        container.addView(pongView);
    }

    public void updateScore(int s) {
        score = s;
        runOnUiThread(() -> tvScore.setText("Score: " + score));
    }

    class PongView extends View {
        private float paddleX = 300;
        private float ballX = 400, ballY = 400;
        private float ballDx = 8, ballDy = -8;
        private boolean isRunning = true;
        private Handler handler = new Handler();
        private Runnable runnable;
        private Paint paint = new Paint();

        public PongView(Context context) {
            super(context);
            initGame();
        }

        private void initGame() {
            score = 0;
            updateScore(0);
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        update();
                        invalidate();
                        handler.postDelayed(this, 30);
                    }
                }
            };
            handler.postDelayed(runnable, 30);
        }

        private void update() {
            int width = getWidth();
            int height = getHeight();
            if (width == 0 || height == 0) return;

            ballX += ballDx;
            ballY += ballDy;

            // Wall bounce
            if (ballX < 20 || ballX > width - 20) ballDx = -ballDx;
            if (ballY < 20) ballDy = -ballDy;

            // Paddle collision
            float paddleY = height - 100;
            if (ballY >= paddleY - 20 && ballY <= paddleY + 20 && ballX >= paddleX && ballX <= paddleX + 200) {
                ballDy = -ballDy;
                score += 5;
                updateScore(score);
            }

            // Bottom miss (Game Over)
            if (ballY > height) {
                isRunning = false;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();

            // Background
            paint.setColor(Color.parseColor("#121815"));
            canvas.drawRect(0, 0, width, height, paint);

            // Paddle
            paint.setColor(Color.parseColor("#52B788"));
            canvas.drawRect(paddleX, height - 100, paddleX + 200, height - 80, paint);

            // Ball
            paint.setColor(Color.parseColor("#FF758F"));
            canvas.drawCircle(ballX, ballY, 20, paint);

            if (!isRunning) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(50);
                canvas.drawText("Game Over! Tap to Restart", 40, height / 2f, paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                if (!isRunning) {
                    isRunning = true;
                    ballX = 400; ballY = 400; ballDx = 8; ballDy = -8;
                    initGame();
                    return true;
                }
                paddleX = event.getX() - 100;
            }
            return true;
        }
    }
}