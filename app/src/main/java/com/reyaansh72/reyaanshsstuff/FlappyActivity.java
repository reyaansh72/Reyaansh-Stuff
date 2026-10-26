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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlappyActivity extends AppCompatActivity {

    private FlappyView flappyView;
    private TextView tvScore;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flappy);

        findViewById(R.id.flappyBack).setOnClickListener(v -> finish());
        tvScore = findViewById(R.id.tvFlappyScore);

        LinearLayout container = findViewById(R.id.flappyContainer);
        flappyView = new FlappyView(this);
        container.addView(flappyView);
    }

    public void updateScore(int s) {
        score = s;
        runOnUiThread(() -> tvScore.setText("Score: " + score));
    }

    class FlappyView extends View {
        private float birdY = 400;
        private float birdVelocity = 0;
        private static final float GRAVITY = 1.2f;
        private static final float JUMP = -18f;

        private final List<Pipe> pipes = new ArrayList<>();
        private boolean isRunning = true;
        private final Handler handler = new Handler();
        private Runnable runnable;
        private final Paint paint = new Paint();
        private final Random random = new Random();
        private int frameCount = 0;

        public FlappyView(Context context) {
            super(context);
            initGame();
        }

        private void initGame() {
            birdY = 400;
            birdVelocity = 0;
            pipes.clear();
            score = 0;
            updateScore(0);
            frameCount = 0;

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

            birdVelocity += GRAVITY;
            birdY += birdVelocity;

            // Ground or ceiling collision
            if (birdY > height - 100 || birdY < 0) {
                isRunning = false;
                return;
            }

            frameCount++;
            if (frameCount % 90 == 0) {
                int gap = 300;
                int topHeight = random.nextInt(height - gap - 200) + 100;
                pipes.add(new Pipe(width, topHeight, gap));
            }

            List<Pipe> toRemove = new ArrayList<>();
            for (Pipe p : pipes) {
                p.x -= 7;
                if (p.x + p.width < 0) {
                    toRemove.add(p);
                    score += 5;
                    updateScore(score);
                }

                // Collision check
                if (120 >= p.x && 120 <= p.x + p.width) {
                    if (birdY <= p.topHeight || birdY >= p.topHeight + p.gap) {
                        isRunning = false;
                    }
                }
            }
            pipes.removeAll(toRemove);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();

            // Background
            paint.setColor(Color.parseColor("#121815"));
            canvas.drawRect(0, 0, width, height, paint);

            // Pipes
            paint.setColor(Color.parseColor("#52B788"));
            for (Pipe p : pipes) {
                canvas.drawRect(p.x, 0, p.x + p.width, p.topHeight, paint);
                canvas.drawRect(p.x, p.topHeight + p.gap, p.x + p.width, height, paint);
            }

            // Bird
            paint.setColor(Color.parseColor("#FF758F"));
            canvas.drawCircle(120, birdY, 25, paint);

            if (!isRunning) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(50);
                canvas.drawText("Game Over! Tap to Restart", 40, height / 2f, paint);
            }
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!isRunning) {
                    isRunning = true;
                    initGame();
                    performClick();
                    return true;
                }
                birdVelocity = JUMP;
                performClick();
            }
            return true;
        }
    }

    static class Pipe {
        float x;
        int topHeight;
        int gap;
        int width = 120;

        Pipe(float x, int topHeight, int gap) {
            this.x = x;
            this.topHeight = topHeight;
            this.gap = gap;
        }
    }
}