package com.reyaansh72.reyaanshsstuff;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeActivity extends AppCompatActivity {

    private SnakeView snakeView;
    private TextView tvScore;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);

        findViewById(R.id.snakeBack).setOnClickListener(v -> finish());
        tvScore = findViewById(R.id.tvSnakeScore);

        LinearLayout container = findViewById(R.id.snakeContainer);
        snakeView = new SnakeView(this);
        container.addView(snakeView);

        MaterialButton btnUp = findViewById(R.id.btnSnakeUp);
        MaterialButton btnDown = findViewById(R.id.btnSnakeDown);
        MaterialButton btnLeft = findViewById(R.id.btnSnakeLeft);
        MaterialButton btnRight = findViewById(R.id.btnSnakeRight);

        btnUp.setOnClickListener(v -> {
            if (snakeView.dy == 0) { snakeView.dx = 0; snakeView.dy = -1; }
        });
        btnDown.setOnClickListener(v -> {
            if (snakeView.dy == 0) { snakeView.dx = 0; snakeView.dy = 1; }
        });
        btnLeft.setOnClickListener(v -> {
            if (snakeView.dx == 0) { snakeView.dx = -1; snakeView.dy = 0; }
        });
        btnRight.setOnClickListener(v -> {
            if (snakeView.dx == 0) { snakeView.dx = 1; snakeView.dy = 0; }
        });
    }

    public void updateScore(int s) {
        score = s;
        runOnUiThread(() -> tvScore.setText("Score: " + score));
    }

    class SnakeView extends View {
        private static final int GRID_SIZE = 20;
        private final List<Point> snake = new ArrayList<>();
        private final Point food = new Point();
        public int dx = 1, dy = 0;
        private boolean isRunning = true;
        private final Handler handler = new Handler();
        private Runnable runnable;
        private final Paint paint = new Paint();
        private final Random random = new Random();

        public SnakeView(Context context) {
            super(context);
            initGame();
        }

        private void initGame() {
            snake.clear();
            snake.add(new Point(10, 10));
            snake.add(new Point(9, 10));
            snake.add(new Point(8, 10));
            spawnFood();
            score = 0;
            updateScore(0);

            runnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        update();
                        invalidate();
                        handler.postDelayed(this, 130);
                    }
                }
            };
            handler.postDelayed(runnable, 130);
        }

        private void spawnFood() {
            food.x = random.nextInt(GRID_SIZE);
            food.y = random.nextInt(GRID_SIZE);
        }

        private void update() {
            Point head = snake.get(0);
            Point newHead = new Point(head.x + dx, head.y + dy);

            // Wall collision
            if (newHead.x < 0 || newHead.x >= GRID_SIZE || newHead.y < 0 || newHead.y >= GRID_SIZE) {
                isRunning = false;
                return;
            }

            // Self collision
            for (Point p : snake) {
                if (p.x == newHead.x && p.y == newHead.y) {
                    isRunning = false;
                    return;
                }
            }

            snake.add(0, newHead);

            // Eat food
            if (newHead.x == food.x && newHead.y == food.y) {
                spawnFood();
                score += 10;
                updateScore(score);
            } else {
                snake.remove(snake.size() - 1);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            int cellSize = Math.min(width, height) / GRID_SIZE;

            // Draw background
            paint.setColor(Color.parseColor("#121815"));
            canvas.drawRect(0, 0, width, height, paint);

            // Draw food
            paint.setColor(Color.parseColor("#FF758F"));
            canvas.drawRect(food.x * cellSize, food.y * cellSize, (food.x + 1) * cellSize, (food.y + 1) * cellSize, paint);

            // Draw snake
            paint.setColor(Color.parseColor("#52B788"));
            for (Point p : snake) {
                canvas.drawRect(p.x * cellSize, p.y * cellSize, (p.x + 1) * cellSize, (p.y + 1) * cellSize, paint);
            }

            if (!isRunning) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(50);
                canvas.drawText("Game Over! Tap screen to restart", 20, height / 2f, paint);
            }
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        @Override
        public boolean onTouchEvent(android.view.MotionEvent event) {
            super.onTouchEvent(event);
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                if (!isRunning) {
                    isRunning = true;
                    initGame();
                    performClick();
                    return true;
                }
            }
            return true;
        }
    }

    static class Point {
        int x, y;
        Point() {}
        Point(int x, int y) { this.x = x; this.y = y; }
    }
}